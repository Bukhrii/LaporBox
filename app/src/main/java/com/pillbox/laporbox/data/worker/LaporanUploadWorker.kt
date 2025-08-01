package com.pillbox.laporbox.data.worker

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.pillbox.laporbox.data.local.database.dao.LaporanDao
import com.pillbox.laporbox.data.local.database.entity.LaporanEntity
import com.pillbox.laporbox.domain.models.LaporanModel
import com.pillbox.laporbox.domain.models.ResepModel
import com.pillbox.laporbox.domain.models.UserModel
import com.pillbox.laporbox.domain.repository.EmailRepository
import com.pillbox.laporbox.domain.repository.UserRepository
import com.pillbox.laporbox.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Calendar
import java.util.Date
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class LaporanUploadWorker(
    private val context: Context,
    workerParams: WorkerParameters,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val laporanDao: LaporanDao,
    private val userRepository: UserRepository,
    private val emailRepository: EmailRepository
) : CoroutineWorker(context, workerParams) {

    private val generativeModel = Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel("gemini-1.5-flash")
    private val tag = "LaporanUploadWorker"

    override suspend fun doWork(): Result {
        val pendingLaporan = laporanDao.getNextPendingLaporan() ?: run {
            Log.d(tag, "Tidak ada laporan offline untuk diunggah. Pekerjaan selesai.")
            return Result.success()
        }

        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.e(tag, "User tidak login, tidak bisa memproses laporan.")
            return Result.failure()
        }

        return try {
            val imageFile = File(pendingLaporan.imagePath)
            if (!imageFile.exists()) {
                Log.e(tag, "File gambar tidak ditemukan di path: ${pendingLaporan.imagePath}")
                laporanDao.delete(pendingLaporan)
                return Result.failure()
            }

            Log.d(tag, "Memulai validasi AI untuk laporan ID: ${pendingLaporan.id}")
            val validationResult = validateWithAI(Uri.fromFile(imageFile), pendingLaporan.resepId, userId)

            when (validationResult) {
                "TRUE" -> {
                    Log.d(tag, "Validasi AI berhasil. Mengunggah gambar ke Cloudinary...")
                    val imageUrl = uploadToCloudinary(Uri.fromFile(imageFile))
                    if (imageUrl != null) {
                        Log.d(tag, "Unggah Cloudinary berhasil. Menyimpan ke Firestore...")
                        saveToFirestoreAndNotify(imageUrl, pendingLaporan.resepId, userId)
                        laporanDao.delete(pendingLaporan)
                        Log.d(tag, "Laporan ID: ${pendingLaporan.id} berhasil diproses dan dihapus dari antrean.")
                        Result.success()
                    } else {
                        Log.e(tag, "Gagal mengunggah ke Cloudinary.")
                        handleRetry(pendingLaporan)
                    }
                }
                else -> {
                    Log.w(tag, "Validasi AI gagal untuk laporan ID: ${pendingLaporan.id} dengan alasan: $validationResult. Laporan dihapus.")
                    laporanDao.delete(pendingLaporan)
                    Result.failure()
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Terjadi error saat memproses laporan ID: ${pendingLaporan.id}", e)
            handleRetry(pendingLaporan)
        }
    }

    private suspend fun handleRetry(laporan: LaporanEntity): Result {
        val newAttemptCount = laporan.attemptCount + 1
        return if (newAttemptCount >= 3) {
            Log.e(tag, "Laporan ID: ${laporan.id} sudah gagal 3 kali, tidak akan diulang lagi.")
            laporanDao.delete(laporan)
            Result.failure()
        } else {
            Log.w(tag, "Pekerjaan gagal, akan dicoba lagi nanti. Percobaan ke-$newAttemptCount")
            laporanDao.update(laporan.copy(attemptCount = newAttemptCount))
            Result.retry()
        }
    }

    private suspend fun validateWithAI(imageUri: Uri, resepId: String, userId: String): String {
        try {
            val bitmap = withContext(Dispatchers.IO) {
                context.contentResolver.openInputStream(imageUri)?.use { BitmapFactory.decodeStream(it) }
            } ?: return "FALSE_KUALITAS"

            val resepDoc = firestore.collection("users").document(userId).collection("resep").document(resepId).get().await()
            val resepData = resepDoc.toObject(ResepModel::class.java) ?: return "ERROR_RESEP_TIDAK_DITEMUKAN"

            val laporanHariIni = getLaporanCountForToday(userId, resepId)
            val dosisHarian = parseFrekuensi(resepData.frekuensiObat)

            if (laporanHariIni >= dosisHarian) {
                return "ERROR_DOSIS_TERPENUHI"
            }

            val textPrompt = buildDynamicPrompt(resepData)
            val prompt = content { image(bitmap); text(textPrompt) }
            val response = generativeModel.generateContent(prompt)
            val responseText = response.text?.trim() ?: "ERROR_RESPONS_KOSONG"

            Log.d(tag, "Respon AI: $responseText")
            return responseText
        } catch (e: Exception) {
            Log.e(tag, "Error saat validasi AI:", e)
            return "ERROR_EXCEPTION"
        }
    }

    private suspend fun uploadToCloudinary(uri: Uri): String? {
        return suspendCoroutine { continuation ->
            MediaManager.get().upload(uri).callback(object : UploadCallback {
                override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
                    continuation.resume(resultData?.get("secure_url") as? String)
                }
                override fun onError(requestId: String?, error: ErrorInfo?) {
                    Log.e(tag, "Cloudinary upload error: ${error?.description}")
                    continuation.resume(null)
                }
                override fun onStart(requestId: String?) {}
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
            }).dispatch()
        }
    }

    private suspend fun saveToFirestoreAndNotify(imageUrl: String, resepId: String, userId: String) {
        val resepRef = firestore.collection("users").document(userId).collection("resep").document(resepId)
        val userDataResource = userRepository.getUser(userId)

        // Mengambil data resep dan pengguna secara bersamaan
        val resepData = resepRef.get().await().toObject(ResepModel::class.java)
        val user = if (userDataResource is Resource.Success) userDataResource.data else null

        if (resepData == null || user == null) {
            Log.e(tag, "Gagal mengambil data resep atau pengguna untuk notifikasi.")
            return
        }

        try {
            val laporan = LaporanModel(imageUrl = imageUrl, userId = userId)
            firestore.runBatch { batch ->
                val laporanRef = resepRef.collection("laporan").document()
                batch.set(laporanRef, laporan)
                val resepUpdateData = mapOf(
                    "totalLaporan" to FieldValue.increment(1),
                    "terakhirLapor" to FieldValue.serverTimestamp()
                )
                batch.update(resepRef, resepUpdateData)
            }.await()

            sendEmailNotification(resepData, imageUrl, user)
        } catch (e: Exception) {
            Log.e(tag, "Gagal menyimpan laporan ke Firestore atau mengirim email.", e)
        }
    }

    private suspend fun sendEmailNotification(resep: ResepModel, imageUrl: String, user: UserModel) {
        val penerima = listOfNotNull(
            resep.emailFaskes.takeIf { it.isNotBlank() },
            resep.emailKeluarga.takeIf { it.isNotBlank() }
        ).distinct()

        if (penerima.isEmpty()) {
            Log.w(tag, "Tidak ada penerima email, notifikasi tidak dikirim.")
            return
        }

        val subjek = "Laporan Kepatuhan Obat Baru dari Pasien: ${user.username}"
        val kontenHtml = """
            <html><body>
            <h1>Laporan Baru Telah Diterima</h1>
            <p>Halo,</p>
            <p>Pasien atas nama <strong>${user.username}</strong> telah mengirimkan laporan foto kepatuhan minum obat dengan detail sebagai berikut:</p>
            <ul>
                <li><strong>Nama Obat:</strong> ${resep.namaObat}</li>
                <li><strong>Dosis:</strong> ${resep.frekuensiObat}</li>
                <li><strong>Waktu Laporan:</strong> ${Date()}</li>
            </ul>
            <p>Berikut adalah foto yang dilampirkan:</p>
            <img src="$imageUrl" alt="Foto Laporan" style="max-width: 400px; border-radius: 8px;"/>
            <br>
            <p>Terima kasih atas perhatian Anda.</p>
            <p><strong>LaporBox Automated System</strong></p>
            </body></html>
        """.trimIndent()

        val result = emailRepository.sendEmailNotification(penerima, subjek, kontenHtml)
        if (result is Resource.Error) {
            Log.e(tag, "Notifikasi email gagal dikirim di dalam worker: ${result.message}")
        } else {
            Log.d(tag, "Panggilan API email notifikasi berhasil diinisiasi dari worker.")
        }
    }

    private fun buildDynamicPrompt(resep: ResepModel): String {
        val totalDosesTakenPreviously = resep.totalLaporan.toInt()
        val activeCompartment = (totalDosesTakenPreviously / 3) % 10 + 1
        val pillsTakenFromActiveCompartment = totalDosesTakenPreviously % 3
        val expectedPills = 3 - (pillsTakenFromActiveCompartment + 1)

        return """
        PERAN: Anda adalah AI validator gambar pillbox. Jawab HANYA dengan "TRUE", "FALSE_TIDAK_PATUH", atau "FALSE_KUALITAS".
    
        TUGAS ANDA:
        1. Temukan kompartemen nomor $activeCompartment pada gambar. Urutan: baris bawah (kiri ke kanan), lalu baris atas (kanan ke kiri).
        2. Hitung jumlah pil HANYA di dalam kompartemen nomor $activeCompartment.
        3. Jumlah pil yang benar seharusnya adalah $expectedPills.
    
        ATURAN RESPON:
        - Jika kualitas gambar buruk (buram/gelap) sehingga Anda tidak bisa menghitung, JAWAB: "FALSE_KUALITAS".
        - Jika gambar jelas tetapi jumlah pil di kompartemen $activeCompartment TIDAK SAMA DENGAN $expectedPills, JAWAB: "FALSE_TIDAK_PATUH".
        - Jika gambar jelas DAN jumlah pil di kompartemen $activeCompartment TEPAT SAMA DENGAN $expectedPills, JAWAB: "TRUE".
        """.trimIndent()
    }

    private suspend fun getLaporanCountForToday(userId: String, resepId: String): Int {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0); calendar.set(Calendar.MINUTE, 0); calendar.set(Calendar.SECOND, 0)
        val startOfDay = calendar.time
        calendar.set(Calendar.HOUR_OF_DAY, 23); calendar.set(Calendar.MINUTE, 59); calendar.set(Calendar.SECOND, 59)
        val endOfDay = calendar.time

        val query = firestore.collection("users").document(userId)
            .collection("resep").document(resepId)
            .collection("laporan")
            .whereGreaterThanOrEqualTo("timestamp", startOfDay)
            .whereLessThanOrEqualTo("timestamp", endOfDay)
            .get().await()
        return query.size()
    }

    private fun parseFrekuensi(frekuensi: String): Int {
        return frekuensi.firstOrNull()?.toString()?.toIntOrNull() ?: 1
    }
}