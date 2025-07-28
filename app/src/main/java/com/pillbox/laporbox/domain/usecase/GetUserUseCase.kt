package com.pillbox.laporbox.domain.usecase

import com.pillbox.laporbox.domain.repository.UserRepository

class GetUserUseCase (private val repository: UserRepository){
    suspend operator fun invoke(uid: String) = repository.getUser(uid)
}