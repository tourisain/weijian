package com.tourisain.weijian.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.tourisain.weijian.data.repository.BackupRepository
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

class BackupWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            BackupWorkerEntryPoint::class.java
        )

        val result = when (inputData.getString(KEY_MODE) ?: MODE_CLOUD) {
            MODE_LOCAL -> entryPoint.backupRepository().createLocalBackup().map { Unit }
            else -> entryPoint.backupRepository().createAndUploadCloudBackup()
        }
        return if (result.isSuccess) {
            Result.success()
        } else if (runAttemptCount < 3) {
            Result.retry()
        } else {
            Result.failure()
        }
    }

    companion object {
        const val KEY_MODE = "backup_mode"
        const val MODE_CLOUD = "cloud"
        const val MODE_LOCAL = "local"
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface BackupWorkerEntryPoint {
    fun backupRepository(): BackupRepository
}
