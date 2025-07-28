package com.pillbox.laporbox.domain.usecase

import com.pillbox.laporbox.domain.repository.DataStoreRepository
import kotlinx.coroutines.flow.Flow

class ReadOnboardingUseCase(private val dataStoreRepository: DataStoreRepository) {
    operator fun invoke(): Flow<Boolean> {
        return dataStoreRepository.readOnboardingState()
    }
}