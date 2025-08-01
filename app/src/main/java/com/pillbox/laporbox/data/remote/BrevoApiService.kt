package com.pillbox.laporbox.data.remote

import com.pillbox.laporbox.BuildConfig
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

data class BrevoEmailRequest(
    val sender: Sender,
    val to: List<Recipient>,
    val subject: String,
    val htmlContent: String
)

data class Sender(val email: String, val name: String)

data class Recipient(val email: String)

interface BrevoApiService {
    @POST("smtp/email")
    suspend fun sendTransactionalEmail(
        @Header("api-key") apiKey: String = BuildConfig.BREVO_API_KEY,
        @Body emailRequest: BrevoEmailRequest
    ): Response<Unit>
}