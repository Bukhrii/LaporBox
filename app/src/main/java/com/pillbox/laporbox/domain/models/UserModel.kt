package com.pillbox.laporbox.domain.models

data class UserModel(
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    val phoneNumber: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
