package com.pillbox.laporbox.domain.models

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class LaporanModel(
    val imageUrl: String = "",
    val userId: String = "",
    @ServerTimestamp
    val timestamp: Date? = null
)
