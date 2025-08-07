package com.pillbox.laporbox.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.pillbox.laporbox.domain.repository.ResepRepository

class SyncResepWorker(
    appContext: Context,
    workerParams: WorkerParameters,
    private val resepRepository: ResepRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val isSuccess = resepRepository.syncPendingReseps()
            if (isSuccess) {
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "SyncResepWorker"
    }
}