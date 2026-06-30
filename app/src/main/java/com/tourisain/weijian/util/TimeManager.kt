package com.tourisain.weijian.util

import android.os.SystemClock

object TimeManager {
    @Volatile private var initializedAtElapsedRealtime: Long = 0L

    fun initialize() {
        initializedAtElapsedRealtime = SystemClock.elapsedRealtime()
    }

    fun getCurrentTime(): Long = System.currentTimeMillis()

    fun getUptimeSinceInitialize(): Long {
        val initializedAt = initializedAtElapsedRealtime
        return if (initializedAt == 0L) 0L else SystemClock.elapsedRealtime() - initializedAt
    }
}
