package com.tourisain.weijian.util

import android.content.Context
import android.util.Log
import java.io.File

object ErrorLogger {
    fun logError(context: Context, tag: String, message: String, throwable: Throwable? = null) {
        Log.e(tag, message, throwable)
        runCatching {
            val dir = File(context.filesDir, "error_logs").apply { mkdirs() }
            File(dir, "latest.log").appendText("[$tag] $message\n${throwable?.stackTraceToString().orEmpty()}\n")
        }
    }
    fun getLogFiles(context: Context): List<File> = File(context.filesDir, "error_logs").listFiles()?.filter { it.isFile } ?: emptyList()
    fun readLogFile(file: File): String = runCatching { file.readText() }.getOrDefault("")
    fun clearAllLogs(context: Context) { getLogFiles(context).forEach { it.delete() } }
}
