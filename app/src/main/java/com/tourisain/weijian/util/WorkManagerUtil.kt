package com.tourisain.weijian.util

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.tourisain.weijian.service.BackupWorker
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

object WorkManagerUtil {
    private const val CLEANUP_WORK = "weijian_cleanup_work"
    private const val CLOUD_BACKUP_WORK = "weijian_cloud_backup_work"
    private const val LOCAL_BACKUP_WORK = "weijian_local_backup_work"

    fun scheduleCleanupWork(context: Context) {
        val request = PeriodicWorkRequestBuilder<CleanupWorker>(1, TimeUnit.DAYS)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            CLEANUP_WORK,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun scheduleBackupWork(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val request = PeriodicWorkRequestBuilder<BackupWorker>(1, TimeUnit.DAYS)
            .setConstraints(constraints)
            .setInputData(workDataOf(BackupWorker.KEY_MODE to BackupWorker.MODE_CLOUD))
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            CLOUD_BACKUP_WORK,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun cancelBackupWork(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(CLOUD_BACKUP_WORK)
    }

    fun scheduleLocalBackupWork(context: Context, frequency: String, time: String) {
        val repeatDays = when (frequency.lowercase()) {
            "weekly" -> 7L
            "monthly" -> 30L
            else -> 1L
        }
        val request = PeriodicWorkRequestBuilder<BackupWorker>(repeatDays, TimeUnit.DAYS)
            .setInitialDelay(initialDelayMillis(time), TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(BackupWorker.KEY_MODE to BackupWorker.MODE_LOCAL))
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            LOCAL_BACKUP_WORK,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun cancelLocalBackupWork(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(LOCAL_BACKUP_WORK)
    }

    private fun initialDelayMillis(time: String): Long {
        return runCatching {
            val localTime = LocalTime.parse(time)
            val now = LocalDateTime.now()
            var target = now
                .withHour(localTime.hour)
                .withMinute(localTime.minute)
                .withSecond(0)
                .withNano(0)
            if (!target.isAfter(now)) {
                target = target.plusDays(1)
            }
            Duration.between(now, target).toMillis()
        }.getOrDefault(0L)
    }
}
