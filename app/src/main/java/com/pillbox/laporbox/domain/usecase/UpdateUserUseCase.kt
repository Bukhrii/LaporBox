package com.pillbox.laporbox.domain.usecase


import com.pillbox.laporbox.domain.models.UserModel
import com.pillbox.laporbox.domain.repository.UserRepository

class UpdateUserUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(user: UserModel) = repository.updateUser(user)
}