package com.pillbox.laporbox.presentation.ui.screens.lapor

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import com.pillbox.laporbox.BuildConfig
import com.pillbox.laporbox.domain.models.LaporanModel
import com.pillbox.laporbox.domain.models.ResepModel
import com.pillbox.laporbox.domain.models.UserModel
import com.pillbox.laporbox.domain.repository.EmailRepository
import com.pillbox.laporbox.domain.repository.UserRepository
import com.pillbox.laporbox.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Calendar
import java.util.Date
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

// State untuk mengontrol UI, seperti loading indicator
data class LaporanState(
    val uploadStatus: UploadStatus = UploadStatus.IDLE,
    val errorMessage: String? = null
)

// Enum untuk status upload
enum class UploadStatus { IDLE, UPLOADING, SUCCESS, ERROR }

class LaporViewModel(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val emailRepository: EmailRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val generativeModel = Firebase.ai(backend = GenerativeBackend.googleAI())
        .generativeModel("gemini-2.5-flash")

    private val _uiState = MutableStateFlow(LaporanState())
    val uiState = _uiState.asStateFlow()

    fun takePhotoAndValidate(
        context: Context,
        imageCapture: ImageCapture,
        executor: Executor,
        resepId: String
    ) {
        _uiState.update { it.copy(uploadStatus = UploadStatus.UPLOADING) }
        val photoFile = File(context.cacheDir, "laporan_${System.currentTimeMillis()}.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions, executor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e("LaporViewModel", "Gagal mengambil foto.", exc)
                    _uiState.update { it.copy(uploadStatus = UploadStatus.ERROR, errorMessage = "Gagal mengambil foto: ${exc.message}") }
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = output.savedUri ?: Uri.fromFile(photoFile)
                    validateImageWithGemini(context, savedUri, resepId)
                }
            }
        )
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

    private fun validateImageWithGemini(context: Context, imageUri: Uri, resepId: String) {
        viewModelScope.launch {
            try {
                val bitmap = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(imageUri)?.use { BitmapFactory.decodeStream(it) }
                }
                if (bitmap == null) {
                    _uiState.update { it.copy(uploadStatus = UploadStatus.ERROR, errorMessage = "Gagal membaca gambar.") }
                    return@launch
                }

                val userId = auth.currentUser!!.uid
                val resepDoc = firestore.collection("users").document(userId).collection("resep").document(resepId).get().await()
                val resepData = resepDoc.toObject(ResepModel::class.java) ?: throw IllegalStateException("Resep tidak ditemukan")

                val laporanHariIni = getLaporanCountForToday(userId, resepId)
                val dosisHarian = parseFrekuensi(resepData.frekuensiObat)

                if (laporanHariIni >= dosisHarian) {
                    _uiState.update { it.copy(uploadStatus = UploadStatus.ERROR, errorMessage = "Anda sudah melaporkan sesuai dosis resep (${dosisHarian}x) hari ini.") }
                    return@launch
                }

                val textPrompt = buildDynamicPrompt(resepData)
                val prompt = content { image(bitmap); text(textPrompt) }
                val response = generativeModel.generateContent(prompt)

                val responseText = response.text?.trim()
                Log.d("GeminiValidate", "Prompt: $textPrompt")
                Log.d("GeminiValidate", "Respon AI: $responseText")

                when {
                    "TRUE".equals(responseText, ignoreCase = true) -> {
                        uploadImageToCloudinaryAndSave(imageUri, resepId)
                    }
                    "FALSE_TIDAK_PATUH".equals(responseText, ignoreCase = true) -> {
                        _uiState.update { it.copy(uploadStatus = UploadStatus.ERROR, errorMessage = "Pasien tidak patuh, jumlah obat tidak sesuai.") }
                    }
                    "FALSE_KUALITAS".equals(responseText, ignoreCase = true) -> {
                        _uiState.update { it.copy(uploadStatus = UploadStatus.ERROR, errorMessage = "Kualitas gambar kurang jelas, silakan foto ulang.") }
                    }
                    else -> {
                        _uiState.update { it.copy(uploadStatus = UploadStatus.ERROR, errorMessage = "Gagal memvalidasi gambar, coba lagi.") }
                    }
                }

            } catch (e: Exception) {
                _uiState.update { it.copy(uploadStatus = UploadStatus.ERROR, errorMessage = e.message ?: "Terjadi kesalahan saat validasi AI.") }
                Log.e("GeminiValidate", "Error:", e)
            }
        }
    }

    private fun buildDynamicPrompt(resep: ResepModel): String {
        // Logika perhitungan ini sudah 100% benar dan tidak perlu diubah.
        val totalDosesTakenPreviously = resep.totalLaporan.toInt()
        val activeCompartment = (totalDosesTakenPreviously / 3) % 10 + 1
        val pillsTakenFromActiveCompartment = totalDosesTakenPreviously % 3
        val expectedPills = 3 - (pillsTakenFromActiveCompartment + 1)

        // --- PROMPT BARU YANG LEBIH RINGKAS DAN TO-THE-POINT UNTUK AI ---
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

    private suspend fun uploadImageToCloudinaryAndSave(uri: Uri, resepId: String) {
        val imageUrl = suspendCoroutine<String?> { continuation ->
            MediaManager.get().upload(uri).callback(object : UploadCallback {
                override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
                    continuation.resume(resultData?.get("secure_url") as? String)
                }
                override fun onError(requestId: String?, error: ErrorInfo?) {
                    _uiState.update { it.copy(uploadStatus = UploadStatus.ERROR, errorMessage = error?.description ?: "Gagal upload ke Cloudinary.") }
                    continuation.resume(null)
                }
                override fun onStart(requestId: String?) {}
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
            }).dispatch()
        }

        if (imageUrl != null) {
            saveLaporanToFirestore(imageUrl, resepId)
        }
    }

    private suspend fun saveLaporanToFirestore(imageUrl: String, resepId: String) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _uiState.update { it.copy(uploadStatus = UploadStatus.ERROR, errorMessage = "User tidak login.") }
            return
        }

        val resepRef = firestore.collection("users").document(userId).collection("resep").document(resepId)
        val resepData = resepRef.get().await().toObject(ResepModel::class.java)
        val userDataResource = userRepository.getUser(userId)

        if (resepData == null || userDataResource !is Resource.Success || userDataResource.data == null) {
            _uiState.update { it.copy(uploadStatus = UploadStatus.ERROR, errorMessage = "Gagal mengambil data resep atau pengguna.") }
            return
        }
        val userData = userDataResource.data

        try {
            val laporan = LaporanModel(imageUrl = imageUrl, userId = userId)
            firestore.runBatch { batch ->
                val laporanRef = resepRef.collection("laporan").document()
                batch.set(laporanRef, laporan)
                val resepUpdateData = mapOf("totalLaporan" to FieldValue.increment(1), "terakhirLapor" to FieldValue.serverTimestamp())
                batch.update(resepRef, resepUpdateData)
            }.await()

            val emailResult = sendEmailNotification(resepData, imageUrl, userData)
            if (emailResult is Resource.Error) {
                Log.e("LaporViewModel", "Notifikasi email gagal dikirim: ${emailResult.message}")
            }

            _uiState.update { it.copy(uploadStatus = UploadStatus.SUCCESS) }

        } catch (e: Exception) {
            _uiState.update { it.copy(uploadStatus = UploadStatus.ERROR, errorMessage = e.message) }
        }
    }

    private suspend fun sendEmailNotification(resep: ResepModel, imageUrl: String, user: UserModel): Resource<Unit> {
        val penerima = listOfNotNull(
            resep.emailFaskes.takeIf { it.isNotBlank() },
            resep.emailKeluarga.takeIf { it.isNotBlank() }
        )

        if (penerima.isEmpty()) {
            Log.w("LaporViewModel", "Tidak ada penerima email, notifikasi tidak dikirim.")
            return Resource.Success(Unit)
        }

        val subjek = "Laporan Kepatuhan Obat Baru dari Pasien"
        val kontenHtml = """
            <h1>Laporan Baru Telah Diterima</h1>
            <p>Halo,</p>
            <p>Pasien atas nama ${user.username} telah mengirimkan laporan foto kepatuhan minum obat dengan detail sebagai berikut:</p>
            <ul>
                <li><strong>Nama Obat:</strong> ${resep.namaObat}</li>
                <li><strong>Dosis:</strong> ${resep.frekuensiObat}</li>
                <li><strong>Waktu Laporan:</strong> ${Date()}</li>
            </ul>
            <p>Berikut adalah foto yang dilampirkan:</p>
            <img src="$imageUrl" alt="Foto Laporan" style="max-width: 400px;"/>
            <p>Terima kasih atas perhatian Anda.</p>
            <p><strong>LaporBox</strong></p>
        """.trimIndent()

        return withContext(Dispatchers.IO) {
            val result = emailRepository.sendEmailNotification(penerima, subjek, kontenHtml)
            if (result is Resource.Error) {
                Log.e("LaporViewModel", "Gagal mengirim email notifikasi: ${result.message}")
            } else {
                Log.d("LaporViewModel", "Panggilan API untuk mengirim email notifikasi berhasil.")
            }
            result
        }
    }

    fun resetStatus() {
        _uiState.update { it.copy(uploadStatus = UploadStatus.IDLE, errorMessage = null) }
    }
}