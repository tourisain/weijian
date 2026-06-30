package com.tourisain.weijian.util

import android.os.SystemClock
import android.util.Log
import java.util.concurrent.ConcurrentHashMap

object PerformanceMonitor {
    private const val TAG = "PerformanceMonitor"
    private const val SLOW_OPERATION_MS = 500L
    private val operations = ConcurrentHashMap<String, Long>()

    fun startOperation(name: String) {
        operations[name] = SystemClock.elapsedRealtime()
    }

    fun endOperation(name: String) {
        val start = operations.remove(name) ?: return
        val duration = SystemClock.elapsedRealtime() - start
        if (duration >= SLOW_OPERATION_MS) {
            Log.d(TAG, "$name took ${duration}ms")
        }
    }

    fun recordError(name: String, throwable: Throwable) {
        operations.remove(name)
        Log.e(TAG, name, throwable)
    }

    fun clearAll() {
        operations.clear()
    }
}
