package com.pillbox.laporbox.domain.usecase

import com.pillbox.laporbox.domain.repository.DataStoreRepository

class SaveOnboardingUseCase(private val dataStoreRepository: DataStoreRepository) {
    suspend operator fun invoke(isCompleted: Boolean) {
        dataStoreRepository.saveOnboardingState(isCompleted)
    }
}