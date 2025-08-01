package com.pillbox.laporbox.presentation.ui.screens.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.pillbox.laporbox.domain.models.LaporanModel
import com.pillbox.laporbox.domain.models.ResepModel
import com.pillbox.laporbox.domain.models.UserModel
import com.pillbox.laporbox.domain.repository.EmailRepository
import com.pillbox.laporbox.domain.repository.ResepRepository
import com.pillbox.laporbox.domain.repository.UserRepository
import com.pillbox.laporbox.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class EmailSendStatus { IDLE, SENDING, SUCCESS, ERROR }

class HomeViewModel(
    private val resepRepository: ResepRepository,
    private val auth: FirebaseAuth,
    private val userRepository: UserRepository,
    private val emailRepository: EmailRepository
) : ViewModel() {

    val reseps: StateFlow<List<ResepModel>> = resepRepository.getReseps()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _laporanList = MutableStateFlow<List<LaporanModel>>(emptyList())
    val laporanList = _laporanList.asStateFlow()

    private val _isLoadingLaporan = MutableStateFlow(true)
    val isLoadingLaporan = _isLoadingLaporan.asStateFlow()


    private val _rekapEmailStatus = MutableStateFlow(EmailSendStatus.IDLE)
    val rekapEmailStatus = _rekapEmailStatus.asStateFlow()

    init {
        viewModelScope.launch {
            reseps.collect { resepList ->
                val firstResepId = resepList.firstOrNull()?.id
                if (firstResepId != null) {
                    fetchLaporanForResep(firstResepId)
                } else {
                    _isLoadingLaporan.value = false
                    _laporanList.value = emptyList()
                }
            }
        }
    }

    // Fungsi sinkronisasi manual tidak lagi diperlukan untuk alur utama,
    // namun bisa tetap ada jika Anda ingin menambahkan fitur "refresh" manual.
    fun syncResepsFromRemote() {
        viewModelScope.launch {
            Log.d("HomeViewModel", "Memulai sinkronisasi manual dari remote...")
            val result = resepRepository.fetchAndSyncReseps()
            if (result is Resource.Error) {
                Log.e("HomeViewModel", "Sinkronisasi Gagal: ${result.message}")
            } else {
                Log.d("HomeViewModel", "Sinkronisasi Selesai.")
            }
        }
    }

    fun sendLaporanRekapEmail() {
        viewModelScope.launch {
            _rekapEmailStatus.value = EmailSendStatus.SENDING
            val userId = auth.currentUser?.uid ?: run {
                Log.e("HomeViewModel", "User tidak login.")
                _rekapEmailStatus.value = EmailSendStatus.ERROR
                return@launch
            }

            val resep = reseps.value.firstOrNull()
            val laporan = laporanList.value

            if (resep == null || laporan.isEmpty()) {
                Log.w("HomeViewModel", "Tidak ada resep atau laporan untuk dikirim.")
                _rekapEmailStatus.value = EmailSendStatus.ERROR
                return@launch
            }

            val userResource = userRepository.getUser(userId)
            val user = if (userResource is Resource.Success) userResource.data else null

            if (user == null) {
                Log.e("HomeViewModel", "Gagal mendapatkan data user.")
                _rekapEmailStatus.value = EmailSendStatus.ERROR
                return@launch
            }

            // Gabungkan email faskes dan keluarga, dan hapus duplikat jika ada
            val penerima = listOfNotNull(
                resep.emailFaskes.takeIf { it.isNotBlank() },
                resep.emailKeluarga.takeIf { it.isNotBlank() }
            ).distinct()

            if (penerima.isEmpty()) {
                Log.w("HomeViewModel", "Tidak ada alamat email penerima yang valid.")
                _rekapEmailStatus.value = EmailSendStatus.ERROR
                return@launch
            }

            val subject = "Rekap Laporan Kepatuhan Obat - Pasien: ${user.username}"
            val contentHtml = buildHtmlRecap(resep, laporan, user)

            val result = emailRepository.sendEmailNotification(penerima, subject, contentHtml)

            _rekapEmailStatus.value = if (result is Resource.Success) {
                EmailSendStatus.SUCCESS
            } else {
                EmailSendStatus.ERROR
            }
        }
    }

    fun resetEmailStatus() {
        _rekapEmailStatus.value = EmailSendStatus.IDLE
    }

    private fun buildHtmlRecap(resep: ResepModel, laporan: List<LaporanModel>, user: UserModel): String {
        // Format tanggal ke Bahasa Indonesia
        val formatter = SimpleDateFormat("EEEE, dd MMMM yyyy, HH:mm", Locale("id", "ID"))

        // Buat daftar laporan dalam format HTML
        val laporanHtml = laporan.joinToString("") { lap ->
            """
            <div style="border: 1px solid #ddd; padding: 10px; margin-bottom: 10px; border-radius: 8px;">
                <p><strong>Waktu Laporan:</strong> ${lap.timestamp?.let { formatter.format(it) } ?: "Tidak diketahui"}</p>
                <img src="${lap.imageUrl}" alt="Foto Laporan" style="max-width: 300px; border-radius: 4px;"/>
            </div>
            """.trimIndent()
        }

        return """
        <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6;">
                <h1>Rekap Laporan Kepatuhan Obat Pasien</h1>
                <p>Berikut adalah rekap laporan kepatuhan minum obat untuk:</p>
                <ul>
                    <li><strong>Nama Pasien:</strong> ${user.username}</li>
                    <li><strong>Penyakit:</strong> ${resep.penyakit}</li>
                    <li><strong>Nama Obat:</strong> ${resep.namaObat}</li>
                    <li><strong>Frekuensi:</strong> ${resep.frekuensiObat}</li>
                </ul>
                <hr>
                <h2>Detail Laporan:</h2>
                $laporanHtml
                <p>Rekap ini dibuat secara otomatis oleh sistem LaporBox.</p>
                <p>Terima kasih.</p>
            </body>
        </html>
        """.trimIndent()
    }

    private fun fetchLaporanForResep(resepId: String) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _isLoadingLaporan.value = false
            return
        }

        val firestore = FirebaseFirestore.getInstance()
        _isLoadingLaporan.value = true
        firestore.collection("users").document(userId)
            .collection("resep").document(resepId)
            .collection("laporan")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("HomeViewModel", "Gagal listen ke riwayat laporan", error)
                    _isLoadingLaporan.value = false
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    _laporanList.value = snapshot.toObjects(LaporanModel::class.java)
                }
                _isLoadingLaporan.value = false
            }
    }

    fun deleteResep(resep: ResepModel) {
        viewModelScope.launch {
            resepRepository.deleteResep(resep)
        }
    }
}