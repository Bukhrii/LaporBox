package com.pillbox.laporbox.domain.repository

import com.pillbox.laporbox.util.Resource

interface EmailRepository {
    suspend fun sendEmailNotification(
        penerima: List<String>,
        subjek: String,
        kontenHtml: String
    ): Resource<Unit>
}