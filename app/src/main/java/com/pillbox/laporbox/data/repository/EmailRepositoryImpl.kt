package com.pillbox.laporbox.data.repository

import android.util.Log
import com.pillbox.laporbox.data.remote.EmailRequest
import com.pillbox.laporbox.data.remote.ResendApiService
import com.pillbox.laporbox.domain.repository.EmailRepository
import com.pillbox.laporbox.util.Resource

class EmailRepositoryImpl(private val apiService: ResendApiService) : EmailRepository {

    override suspend fun sendEmailNotification(
        penerima: List<String>,
        subjek: String,
        kontenHtml: String
    ): Resource<Unit> {
        if (penerima.isEmpty()) {
            Log.w("EmailRepositoryImpl", "Tidak ada penerima email, notifikasi tidak dikirim.")
            return Resource.Success(Unit)
        }

        try {
            // Membuat satu permintaan untuk setiap penerima.
            val emailRequests = penerima.map { email ->
                EmailRequest(
                    from = "LaporBox <onboarding@resend.dev>",
                    to = listOf(email), // Setiap request hanya berisi satu alamat email tujuan
                    subject = subjek,
                    html = kontenHtml
                )
            }

            // Mengirim seluruh list dalam satu panggilan API ke endpoint batch
            val response = apiService.sendBatchEmails(emailRequests = emailRequests)

            return if (response.isSuccessful && response.body()?.data != null) {
                Log.d("EmailRepositoryImpl", "Batch email berhasil dikirim.")
                Resource.Success(Unit)
            } else {
                val errorBody = response.errorBody()?.string() ?: response.body()?.error
                Log.e("EmailRepositoryImpl", "Gagal mengirim batch email: $errorBody")
                Resource.Error("Gagal mengirim email: $errorBody")
            }
        } catch (e: Exception) {
            Log.e("EmailRepositoryImpl", "Terjadi kesalahan saat mengirim batch email", e)
            return Resource.Error("Terjadi kesalahan: ${e.message}")
        }
    }
}