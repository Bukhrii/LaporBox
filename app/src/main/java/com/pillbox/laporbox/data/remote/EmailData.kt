package com.pillbox.laporbox.data.remote

import com.google.gson.annotations.SerializedName

// Data yang akan dikirim ke Resend API
data class EmailRequest(
    @SerializedName("from") val from: String,
    @SerializedName("to") val to: List<String>,
    @SerializedName("subject") val subject: String,
    @SerializedName("html") val html: String
)

data class BatchEMailSuccessResponse (
    @SerializedName("id") val id: String? = null
)

// Respons yang diterima dari Resend API
data class BatchEmailResponse(
    @SerializedName("data") val data: List<BatchEMailSuccessResponse>? = null,
    @SerializedName("error") val error: String? = null
)

