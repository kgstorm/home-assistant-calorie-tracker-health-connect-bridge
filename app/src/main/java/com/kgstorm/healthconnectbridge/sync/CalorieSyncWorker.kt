package com.kgstorm.healthconnectbridge.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

/**
 * WorkManager worker for periodic calorie syncing
 */
class CalorieSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "CalorieSyncWorker"
        const val WORK_NAME = "CalorieSyncWork"
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting background sync work")
        
        val syncService = CalorieSyncService(applicationContext)
        
        return syncService.performSync().fold(
            onSuccess = { message ->
                Log.d(TAG, "Background sync completed: $message")
                Result.success()
            },
            onFailure = { e ->
                Log.e(TAG, "Background sync failed: ${e.message}", e)
                // Retry on failure
                Result.retry()
            }
        )
    }
}
