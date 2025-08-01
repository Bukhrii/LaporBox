package com.pillbox.laporbox.presentation.ui.screens.lapor

import android.content.Context
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
import com.pillbox.laporbox.data.local.database.dao.LaporanDao
import com.pillbox.laporbox.data.local.database.entity.LaporanEntity
import com.pillbox.laporbox.data.worker.LaporanUploadWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.Executor

data class LaporanState(
    val uploadStatus: UploadStatus = UploadStatus.IDLE,
    val errorMessage: String? = null
)
enum class UploadStatus { IDLE, SAVING, SUCCESS, ERROR }

class LaporViewModel(
    // Hanya butuh DAO dan context untuk WorkManager
    private val LaporanDao: LaporanDao,
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(LaporanState())
    val uiState = _uiState.asStateFlow()

    // Ganti nama fungsi menjadi lebih sesuai
    fun takePhotoAndQueueForUpload(
        context: Context,
        imageCapture: ImageCapture,
        executor: Executor,
        resepId: String
    ) {
        _uiState.update { it.copy(uploadStatus = UploadStatus.SAVING) }
        val photoFile = File(context.externalCacheDir, "laporan_${System.currentTimeMillis()}.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions, executor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e("LaporViewModel", "Gagal mengambil foto.", exc)
                    _uiState.update { it.copy(uploadStatus = UploadStatus.ERROR, errorMessage = "Gagal mengambil foto.") }
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    viewModelScope.launch {
                        // 1. Buat entitas untuk disimpan di database offline
                        val offlineLaporan = LaporanEntity(
                            resepId = resepId,
                            imagePath = photoFile.absolutePath
                        )
                        // 2. Simpan ke database lokal
                        LaporanDao.insert(offlineLaporan)

                        // 3. Jadwalkan Worker untuk berjalan
                        scheduleLaporanUploadWorker()

                        // 4. Update UI bahwa tugas selesai (dari sisi pengguna)
                        _uiState.update { it.copy(uploadStatus = UploadStatus.SUCCESS) }
                    }
                }
            }
        )
    }

    private fun scheduleLaporanUploadWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED) // Hanya berjalan jika ada internet
            .build()

        val uploadWorkRequest = OneTimeWorkRequestBuilder<LaporanUploadWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueue(uploadWorkRequest)
        Log.d("LaporViewModel", "LaporanUploadWorker telah dijadwalkan.")
    }

    fun resetStatus() {
        _uiState.update { it.copy(uploadStatus = UploadStatus.IDLE, errorMessage = null) }
    }
}