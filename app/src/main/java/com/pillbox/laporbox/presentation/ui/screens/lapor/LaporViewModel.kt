package com.pillbox.laporbox.presentation.ui.screens.lapor

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pillbox.laporbox.data.local.database.dao.LaporanDao
import com.pillbox.laporbox.data.local.database.entity.LaporanEntity
import com.pillbox.laporbox.data.worker.LaporanUploadWorker
import com.pillbox.laporbox.domain.models.ResepModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.Executor

enum class LaporStatus {
    IDLE,
    PHOTO_TAKEN,
    VALIDATING,
    QUEUED,
    VALIDATION_FAILED,
    ERROR
}

data class LaporanState(
    val status: LaporStatus = LaporStatus.IDLE,
    val imagePath: String? = null,
    val errorMessage: String? = null
)

class LaporViewModel(
    private val laporanDao: LaporanDao,
    private val context: Context,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(LaporanState())
    val uiState = _uiState.asStateFlow()

    private val generativeModel = Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel("gemini-2.5-pro")

    fun takePhoto(
        imageCapture: ImageCapture,
        executor: Executor
    ) {
        val photoFile = File(context.externalCacheDir, "laporan_preview_${System.currentTimeMillis()}.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions, executor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    _uiState.update { it.copy(status = LaporStatus.ERROR, errorMessage = "Gagal mengambil foto.") }
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    _uiState.update {
                        it.copy(
                            status = LaporStatus.PHOTO_TAKEN,
                            imagePath = photoFile.absolutePath
                        )
                    }
                }
            }
        )
    }

    fun validateAndQueueUpload(resepId: String) {
        val currentImagePath = _uiState.value.imagePath ?: return
        val userId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(status = LaporStatus.VALIDATING) }

            val validationResult = validateWithAI(Uri.fromFile(File(currentImagePath)), resepId, userId)

            if (validationResult == "TRUE") {
                val offlineLaporan = LaporanEntity(
                    resepId = resepId,
                    imagePath = currentImagePath
                )
                laporanDao.insert(offlineLaporan)
                scheduleLaporanUploadWorker()
                _uiState.update { it.copy(status = LaporStatus.QUEUED) }
            } else {
                val userFriendlyMessage = mapValidationResultToUserMessage(validationResult)
                _uiState.update {
                    it.copy(
                        status = LaporStatus.VALIDATION_FAILED,
                        errorMessage = userFriendlyMessage
                    )
                }
            }
        }
    }

    fun retakePhoto() {
        _uiState.value.imagePath?.let {
            File(it).delete()
        }
        _uiState.update { LaporanState() }
    }

    fun resetStatus() {
        _uiState.update { LaporanState() }
    }

    private fun scheduleLaporanUploadWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val uploadWorkRequest = OneTimeWorkRequestBuilder<LaporanUploadWorker>()
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(context).enqueue(uploadWorkRequest)
        Log.d("LaporViewModel", "LaporanUploadWorker telah dijadwalkan.")
    }

    private suspend fun validateWithAI(imageUri: Uri, resepId: String, userId: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val bitmap = context.contentResolver.openInputStream(imageUri)?.use { BitmapFactory.decodeStream(it) }
                    ?: return@withContext "FALSE_KUALITAS"

                val resepDoc = firestore.collection("users").document(userId).collection("resep").document(resepId).get().await()
                val resepData = resepDoc.toObject(ResepModel::class.java)
                    ?: return@withContext "ERROR_RESEP_TIDAK_DITEMUKAN"

                val textPrompt = buildDynamicPrompt(resepData)
                val prompt = content { image(bitmap); text(textPrompt) }
                val response = generativeModel.generateContent(prompt)
                response.text?.trim() ?: "ERROR_RESPONS_KOSONG"
            } catch (e: Exception) {
                Log.e("LaporViewModel", "Error saat validasi AI:", e)
                "ERROR_EXCEPTION"
            }
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
        1. Temukan kompartemen nomor $activeCompartment pada gambar. Urutan: ujung baris bawah (kiri ke kanan), lalu ujung baris atas (kanan ke kiri).
        2. Hitung jumlah pil HANYA di dalam kompartemen nomor $activeCompartment.
        3. Jumlah pil yang benar seharusnya adalah $expectedPills.
        ATURAN RESPON:
        - Jika kualitas gambar buruk (buram/gelap) sehingga Anda tidak bisa menghitung, JAWAB: "FALSE_KUALITAS".
        - Jika gambar jelas tetapi jumlah pil di kompartemen $activeCompartment TIDAK SAMA DENGAN $expectedPills, JAWAB: "FALSE_TIDAK_PATUH".
        - Jika gambar jelas DAN jumlah pil di kompartemen $activeCompartment TEPAT SAMA DENGAN $expectedPills, JAWAB: "TRUE".
        """.trimIndent()
    }

    private fun mapValidationResultToUserMessage(result: String): String {
        return when (result) {
            "FALSE_KUALITAS" -> "Kualitas foto kurang baik. Pastikan gambar jelas, terang, dan tidak buram."
            "FALSE_TIDAK_PATUH" -> "Jumlah pil pada foto tidak sesuai dengan dosis yang diharapkan. Mohon periksa kembali dan foto ulang."
            "ERROR_RESEP_TIDAK_DITEMUKAN" -> "Gagal memuat data resep Anda. Periksa koneksi dan coba lagi."
            "ERROR_RESPONS_KOSONG" -> "Sistem validasi tidak memberi respons. Silakan coba lagi."
            "ERROR_EXCEPTION" -> "Terjadi kesalahan pada sistem validasi. Coba lagi beberapa saat."
            else -> "Validasi foto gagal. Alasan tidak diketahui. Silakan coba lagi."
        }
    }
}