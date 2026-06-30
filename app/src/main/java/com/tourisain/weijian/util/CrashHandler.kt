package com.tourisain.weijian.util

import android.util.Log

object CrashHandler : Thread.UncaughtExceptionHandler {
    private var previous: Thread.UncaughtExceptionHandler? = null
    fun init() {
        previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(this)
    }
    override fun uncaughtException(t: Thread, e: Throwable) {
        Log.e("CrashHandler", "Uncaught exception", e)
        previous?.uncaughtException(t, e)
    }
}
