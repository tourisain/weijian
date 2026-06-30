package com.tourisain.weijian.util

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class CleanupWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {
    override fun doWork(): Result = Result.success()
}
