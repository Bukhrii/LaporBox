package com.pillbox.laporbox.data.remote

import com.pillbox.laporbox.BuildConfig
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ResendApiService {
    @POST("emails/batch")
    suspend fun sendBatchEmails(
        @Header("Authorization") apiKey: String = "Bearer ${BuildConfig.RESEND_API_KEY}",
        @Body emailRequests: List<EmailRequest>
    ): Response<BatchEmailResponse>
}