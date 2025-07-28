package com.pillbox.laporbox.domain.repository

import com.pillbox.laporbox.domain.models.ResepModel
import com.pillbox.laporbox.util.Resource
import kotlinx.coroutines.flow.Flow

interface ResepRepository {
    suspend fun addResep(transaction: ResepModel): Resource<Unit>
    fun getReseps(): Flow<List<ResepModel>>
    suspend fun updateResep(transaction: ResepModel): Resource<Unit>
    suspend fun deleteResep(transaction: ResepModel): Resource<Unit>
    fun getResepById(id: String): Flow<ResepModel?>

    suspend fun syncPendingReseps(): Boolean
    suspend fun fetchAndSyncReseps(): Resource<Unit>
    fun startSync()
    fun stopSync()
}