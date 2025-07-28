package com.pillbox.laporbox.data.repository

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.pillbox.laporbox.data.local.database.dao.ResepDao
import com.pillbox.laporbox.data.local.database.entity.ResepEntity
import com.pillbox.laporbox.data.worker.SyncResepWorker
import com.pillbox.laporbox.domain.models.ResepModel
import com.pillbox.laporbox.domain.repository.ResepRepository
import com.pillbox.laporbox.util.Resource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.UUID
import java.util.concurrent.TimeUnit

class ResepRepositoryImpl(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val resepDao: ResepDao,
    private val context: Context
) : ResepRepository {

    private val tag = "ResepRepositoryImpl"
    private fun getCurrentUserId(): String? = auth.currentUser?.uid

    override suspend fun fetchAndSyncReseps(): Resource<Unit> {
        val userId = getCurrentUserId() ?: return Resource.Error("User tidak login")
        return try {
            Log.d(tag, "Memulai fetchAndSyncReseps untuk user: $userId")
            // 1. Ambil semua data resep dari Firestore untuk user ini
            val snapshot = firestore.collection("users").document(userId)
                .collection("resep").get().await()

            // 2. Ubah setiap dokumen Firestore menjadi ResepEntity
            val resepEntities = snapshot.documents.mapNotNull { document ->
                // Menggunakan toObject untuk konversi otomatis, lebih aman
                document.toObject(ResepEntity::class.java)?.copy(id = document.id, userId = userId)
            }

            Log.d(tag, "Ditemukan ${resepEntities.size} resep dari Firestore.")

            // 3. Simpan semua entitas ke dalam database Room
            // insertOrUpdate akan menambahkan data baru dan memperbarui yang sudah ada
            resepDao.insertOrUpdateAll(resepEntities)

            Log.d(tag, "Sinkronisasi ke database lokal berhasil.")
            Resource.Success(Unit)
        } catch (e: Exception) {
            Log.e(tag, "Gagal sinkronisasi dari Firestore", e)
            Resource.Error("Gagal sinkronisasi data: ${e.message}")
        }
    }


    override fun getReseps(): Flow<List<ResepModel>> {
        // Flow ini akan memancarkan (emit) UID pengguna setiap kali status login berubah.
        return callbackFlow {
            val authListener = FirebaseAuth.AuthStateListener { auth ->
                trySend(auth.currentUser?.uid)
            }
            auth.addAuthStateListener(authListener)
            // Hapus listener saat flow ditutup untuk menghindari memory leak.
            awaitClose { auth.removeAuthStateListener(authListener) }
        }.flatMapLatest { userId ->
            if (userId == null) {
                // Jika pengguna logout, kembalikan daftar kosong.
                flowOf(emptyList())
            } else {
                // Jika pengguna login, buat flow baru yang mendengarkan pembaruan real-time dari Firestore.
                val query = firestore.collection("users").document(userId)
                    .collection("resep")
                    .orderBy("createdAt", Query.Direction.DESCENDING)

                callbackFlow {
                    val snapshotListener = query.addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Log.w(tag, "Firestore listen error", error)
                            close(error) // Tutup flow jika terjadi error.
                            return@addSnapshotListener
                        }
                        if (snapshot != null) {
                            // Ubah dokumen Firestore menjadi List<ResepModel>.
                            val resepModels = snapshot.toObjects(ResepModel::class.java)

                            // PENTING: Perbarui cache lokal (Room) dengan data baru dari Firestore.
                            // Ini memastikan akses offline ke data terbaru.
                            val resepEntities = snapshot.documents.mapNotNull { document ->
                                document.toObject(ResepEntity::class.java)?.copy(
                                    id = document.id,
                                    userId = userId,
                                    isSynced = true // Data dari Firestore selalu dianggap tersinkronisasi.
                                )
                            }

                            // Jalankan proses penulisan ke database di background thread.
                            CoroutineScope(Dispatchers.IO).launch {
                                resepDao.insertOrUpdateAll(resepEntities)
                                Log.d(tag, "Cache lokal diperbarui dengan ${resepEntities.size} item dari Firestore.")
                            }

                            // Kirim data terbaru ke UI.
                            trySend(resepModels).isSuccess
                        }
                    }
                    // Hapus listener saat flow tidak lagi diamati untuk mencegah memory leak.
                    awaitClose {
                        Log.d(tag, "Menutup listener Firestore untuk user $userId")
                        snapshotListener.remove()
                    }
                }
            }
        }
    }

    override fun getResepById(id: String): Flow<ResepModel?> {
        return resepDao.getResepById(id).map { it?.toResepModel() }
    }

    override suspend fun addResep(resep: ResepModel): Resource<Unit> {
        val userId = getCurrentUserId() ?: return Resource.Error("User tidak login")
        val entity = resep.toResepEntity(userId).copy(isSynced = false) // Tandai belum sinkron
        resepDao.insertOrUpdateResep(entity) // Simpan ke lokal
        syncSingleResepToFirestore(entity) // Coba sinkronisasi
        return Resource.Success(Unit) // Langsung kembalikan success karena sudah aman di lokal
    }

    override suspend fun updateResep(resep: ResepModel): Resource<Unit> {
        val userId = getCurrentUserId() ?: return Resource.Error("User tidak login")
        val entity = resep.toResepEntity(userId).copy(isSynced = false) // Tandai butuh sinkronisasi
        resepDao.insertOrUpdateResep(entity) // Update di lokal
        syncSingleResepToFirestore(entity) // Coba sinkronisasi
        return Resource.Success(Unit)
    }

    override suspend fun deleteResep(resep: ResepModel): Resource<Unit> {
        val userId = getCurrentUserId() ?: return Resource.Error("User tidak login")
        val entity = resep.toResepEntity(userId)

        resepDao.deleteResep(entity) // Hapus dari lokal

        // Coba hapus dari Firestore
        try {
            firestore.collection("users").document(userId)
                .collection("resep").document(resep.id)
                .delete().await()
            Log.d(tag, "Resep ${resep.id} berhasil dihapus dari Firestore.")
        } catch (e: Exception) {
            Log.e(tag, "Gagal hapus di Firestore, data sudah dihapus lokal.", e)
            // Di masa depan, bisa ditambahkan logika untuk menyimpan ID yang gagal dihapus
            // dan mencoba lagi nanti.
        }
        return Resource.Success(Unit)
    }

    // Fungsi private untuk mencoba sinkronisasi satu resep
    private suspend fun syncSingleResepToFirestore(entity: ResepEntity) {
        try {
            firestore.collection("users").document(entity.userId)
                .collection("resep").document(entity.id)
                .set(entity).await()
            // Jika berhasil, update status di Room
            resepDao.insertOrUpdateResep(entity.copy(isSynced = true))
            Log.d(tag, "Resep ${entity.id} berhasil disinkronkan.")
        } catch (e: Exception) {
            Log.e(tag, "Gagal sinkronisasi resep ${entity.id}, data tetap tersimpan lokal.", e)
        }
    }

    // Mapper Helpers (bisa diletakkan di file terpisah)
    private fun ResepEntity.toResepModel(): ResepModel {
        return ResepModel(
            id = this.id,
            userId = this.userId,
            namaDokter = this.namaDokter,
            emailFaskes = this.emailFaskes,
            tanggalKontrolTerakhir = this.tanggalKontrolTerakhir,
            tanggalKontrolBerikutnya = this.tanggalKontrolBerikutnya,
            penyakit = this.penyakit,
            penyakitLainnya = this.penyakitLainnya,
            namaKeluarga = this.namaKeluarga,
            emailKeluarga = this.emailKeluarga,
            namaObat = this.namaObat,
            frekuensiObat = this.frekuensiObat,
            createdAt = this.createdAt,
            isSynced = this.isSynced,
            totalLaporan = this.totalLaporan,
            terakhirLapor = this.terakhirLapor
        )
    }

    private fun ResepModel.toResepEntity(userId: String): ResepEntity {
        return ResepEntity(
            id = this.id.ifBlank { UUID.randomUUID().toString() },
            userId = userId,
            namaDokter = this.namaDokter,
            emailFaskes = this.emailFaskes,
            tanggalKontrolTerakhir = this.tanggalKontrolTerakhir,
            tanggalKontrolBerikutnya = this.tanggalKontrolBerikutnya,
            penyakit = this.penyakit,
            penyakitLainnya = this.penyakitLainnya,
            namaKeluarga = this.namaKeluarga,
            emailKeluarga = this.emailKeluarga,
            namaObat = this.namaObat,
            frekuensiObat = this.frekuensiObat,
            createdAt = this.createdAt ?: Date(),
            isSynced = this.isSynced,
            totalLaporan = this.totalLaporan,
            terakhirLapor = this.terakhirLapor
        )
    }

    override suspend fun syncPendingReseps(): Boolean {
        val userId = getCurrentUserId() ?: return false
        val unsyncedReseps = resepDao.getUnsyncedReseps(userId)
        if (unsyncedReseps.isEmpty()) {
            Log.d(tag, "Tidak ada resep untuk disinkronkan.")
            return true // Dianggap sukses karena tidak ada yang perlu dilakukan
        }

        var allSucceeded = true
        unsyncedReseps.forEach { entity ->
            try {
                firestore.collection("users").document(userId)
                    .collection("resep").document(entity.id)
                    .set(entity).await()
                // Jika berhasil, update status di Room
                resepDao.insertOrUpdateResep(entity.copy(isSynced = true))
                Log.d(tag, "Sinkronisasi berhasil untuk resep: ${entity.id}")
            } catch (e: Exception) {
                Log.e(tag, "Sinkronisasi gagal untuk resep: ${entity.id}", e)
                allSucceeded = false // Tandai jika ada satu saja yang gagal
            }
        }
        return allSucceeded
    }

    /**
     * Memulai pekerjaan sinkronisasi periodik menggunakan WorkManager.
     * Akan berjalan setiap 6 jam jika perangkat terhubung ke internet.
     */
    override fun startSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val periodicSyncRequest = PeriodicWorkRequestBuilder<SyncResepWorker>(6, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            SyncResepWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP, // Tetap jalankan job yang ada
            periodicSyncRequest
        )
        Log.d(tag, "Periodic sync worker dimulai.")
    }

    /**
     * Menghentikan pekerjaan sinkronisasi.
     */
    override fun stopSync() {
        WorkManager.getInstance(context).cancelUniqueWork(SyncResepWorker.WORK_NAME)
        Log.d(tag, "Periodic sync worker dihentikan.")
    }
}
