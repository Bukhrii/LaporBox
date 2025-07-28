package com.pillbox.laporbox.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.pillbox.laporbox.domain.repository.ResepRepository

class SyncResepWorker(
    appContext: Context,
    workerParams: WorkerParameters,
    private val resepRepository: ResepRepository // Terima repository via DI
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // Panggil fungsi sinkronisasi dari repository
            val isSuccess = resepRepository.syncPendingReseps()
            if (isSuccess) {
                Result.success()
            } else {
                // Jika ada beberapa yang gagal, coba lagi nanti
                Result.retry()
            }
        } catch (e: Exception) {
            // Jika ada error (misal: tidak ada internet), coba lagi nanti
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "SyncResepWorker"
    }
}