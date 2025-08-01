package com.pillbox.laporbox.data.repository

import android.util.Log
import com.google.gson.Gson
import com.pillbox.laporbox.data.remote.BrevoApiService
import com.pillbox.laporbox.data.remote.BrevoEmailRequest
import com.pillbox.laporbox.data.remote.Recipient
import com.pillbox.laporbox.data.remote.Sender
import com.pillbox.laporbox.domain.repository.EmailRepository
import com.pillbox.laporbox.util.Resource

class EmailRepositoryImpl(private val apiService: BrevoApiService) : EmailRepository {

    override suspend fun sendEmailNotification(
        penerima: List<String>,
        subjek: String,
        kontenHtml: String
    ): Resource<Unit> {
        // Log saat fungsi dimulai
        Log.d("EmailRepositoryImpl", "--- MEMULAI PROSES PENGIRIMAN EMAIL ---")
        Log.d("EmailRepositoryImpl", "Penerima: $penerima")

        if (penerima.isEmpty()) {
            Log.w("EmailRepositoryImpl", "Tidak ada penerima, proses dibatalkan.")
            return Resource.Success(Unit)
        }

        try {
            val recipients = penerima.map { email -> Recipient(email = email) }

            val emailRequest = BrevoEmailRequest(
                sender = Sender(email = "laporbox.app@gmail.com", name = "LaporBox"),
                to = recipients,
                subject = subjek,
                htmlContent = kontenHtml
            )

            // Log request body dalam format JSON sebelum dikirim
            val jsonRequest = Gson().toJson(emailRequest)
            Log.d("EmailRepositoryImpl", "Request Body JSON yang akan dikirim: $jsonRequest")

            Log.i("EmailRepositoryImpl", "Mengirim permintaan ke API Brevo...")
            val response = apiService.sendTransactionalEmail(emailRequest = emailRequest)

            return if (response.isSuccessful) {
                Log.i("EmailRepositoryImpl", "SUKSES: Email berhasil dikirim. Status Code: ${response.code()}")
                Resource.Success(Unit)
            } else {
                val errorCode = response.code()
                val errorBody = response.errorBody()?.string()
                Log.e("EmailRepositoryImpl", "GAGAL: Gagal mengirim email. Status Code: $errorCode")
                Log.e("EmailRepositoryImpl", "Error Body: $errorBody")
                Resource.Error("Gagal mengirim email: $errorBody")
            }
        } catch (e: Exception) {
            Log.e("EmailRepositoryImpl", "EXCEPTION: Terjadi kesalahan kritis saat mengirim email.", e)
            return Resource.Error("Terjadi kesalahan: ${e.message}")
        }
    }
}