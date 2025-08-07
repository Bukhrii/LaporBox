package com.pillbox.laporbox.presentation.ui.screens.resep

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.pillbox.laporbox.domain.models.LaporanModel
import com.pillbox.laporbox.domain.models.ResepModel
import com.pillbox.laporbox.domain.models.UserModel
import com.pillbox.laporbox.domain.repository.EmailRepository
import com.pillbox.laporbox.domain.repository.UserRepository
import com.pillbox.laporbox.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class EmailSendStatus { IDLE, SENDING, SUCCESS, ERROR }

data class ResepDetailState(
    val resep: ResepModel? = null,
    val laporanList: List<LaporanModel> = emptyList(),
    val isLoading: Boolean = true,
    val emailStatus: EmailSendStatus = EmailSendStatus.IDLE
)

class ResepDetailViewModel(
    private val userRepository: UserRepository,
    private val emailRepository: EmailRepository,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResepDetailState())
    val uiState = _uiState.asStateFlow()

    private val resepId: String = checkNotNull(savedStateHandle["resepId"])

    init {
        fetchResepAndLaporanDetail()
    }

    private fun fetchResepAndLaporanDetail() {
        val userId = auth.currentUser?.uid ?: return
        _uiState.value = _uiState.value.copy(isLoading = true)

        firestore.collection("users").document(userId)
            .collection("resep").document(resepId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    _uiState.value = _uiState.value.copy(resep = snapshot.toObject(ResepModel::class.java))
                }
            }

        firestore.collection("users").document(userId)
            .collection("resep").document(resepId)
            .collection("laporan")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    _uiState.value = _uiState.value.copy(
                        laporanList = snapshot.toObjects(LaporanModel::class.java),
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            }
    }

    fun sendLaporanRekapEmail() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(emailStatus = EmailSendStatus.SENDING)
            val userId = auth.currentUser?.uid ?: run {
                _uiState.value = _uiState.value.copy(emailStatus = EmailSendStatus.ERROR)
                return@launch
            }

            val resep = _uiState.value.resep
            val laporan = _uiState.value.laporanList

            if (resep == null || laporan.isEmpty()) {
                _uiState.value = _uiState.value.copy(emailStatus = EmailSendStatus.ERROR)
                return@launch
            }

            val user = when(val result = userRepository.getUser(userId)) {
                is Resource.Success -> result.data
                else -> null
            }

            if (user == null) {
                _uiState.value = _uiState.value.copy(emailStatus = EmailSendStatus.ERROR)
                return@launch
            }

            val penerima = listOfNotNull(
                resep.emailFaskes.takeIf { it.isNotBlank() },
                resep.emailKeluarga.takeIf { it.isNotBlank() }
            ).distinct()

            if (penerima.isEmpty()) {
                _uiState.value = _uiState.value.copy(emailStatus = EmailSendStatus.ERROR)
                return@launch
            }

            val subject = "Rekap Laporan Kepatuhan Obat - Pasien: ${user.username}"
            val contentHtml = buildHtmlRecap(resep, laporan, user)
            val result = emailRepository.sendEmailNotification(penerima, subject, contentHtml)

            _uiState.value = _uiState.value.copy(
                emailStatus = if (result is Resource.Success) EmailSendStatus.SUCCESS else EmailSendStatus.ERROR
            )
        }
    }

    fun resetEmailStatus() {
        _uiState.value = _uiState.value.copy(emailStatus = EmailSendStatus.IDLE)
    }

    private fun buildHtmlRecap(resep: ResepModel, laporan: List<LaporanModel>, user: UserModel): String {
        val formatter = SimpleDateFormat("EEEE, dd MMMM yyyy, HH:mm", Locale("id", "ID"))
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
}