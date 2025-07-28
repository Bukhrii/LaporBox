package com.pillbox.laporbox.domain.repository

import kotlinx.coroutines.flow.Flow

interface DataStoreRepository {
    suspend fun saveOnboardingState(isCompleted: Boolean)
    fun readOnboardingState(): Flow<Boolean>
}