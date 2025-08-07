package com.pillbox.laporbox.domain.repository

import com.pillbox.laporbox.domain.models.UserModel
import com.pillbox.laporbox.util.Resource


interface UserRepository {

    suspend fun createUser(user: UserModel): Resource<Unit>
    suspend fun getUser(uid: String): Resource<UserModel>
    suspend fun updateUser(user: UserModel): Resource<Unit>
    suspend fun updateUserReminders(uid: String, reminders: Map<String, String>): Resource<Unit>
}