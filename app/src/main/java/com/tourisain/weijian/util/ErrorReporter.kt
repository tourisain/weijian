package com.tourisain.weijian.util

import android.content.Context
import android.os.Build
import android.util.Log
import com.tourisain.weijian.BuildConfig
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ErrorReporter {
    private const val TAG = "ErrorReporter"
    private const val LOG_DIR = "error_logs"

    fun reportError(context: Context, errorType: String, throwable: Throwable) {
        try {
            val errorMessage = buildErrorMessage(errorType, throwable)
            Log.e(TAG, errorMessage, throwable)
            saveErrorLog(context, errorMessage)
        } catch (e: Exception) {
            Log.e(TAG, "Error reporting failed", e)
        }
    }

    fun reportException(context: Context, throwable: Throwable) {
        reportError(context, "Exception", throwable)
    }

    fun reportCrash(context: Context, throwable: Throwable) {
        reportError(context, "Crash", throwable)
    }

    private fun buildErrorMessage(errorType: String, throwable: Throwable): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val timestamp = formatter.format(Date())
        
        return buildString {
            append("[$timestamp] $errorType\n")
            append("Device: ${Build.MANUFACTURER} ${Build.MODEL}\n")
            append("Android: ${Build.VERSION.RELEASE}\n")
            append("App Version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})\n")
            append("Error: ${throwable.message}\n")
            append("Stack Trace:\n")
            append(throwable.stackTrace.joinToString("\n"))
            append("\n\n")
        }
    }

    private fun saveErrorLog(context: Context, errorMessage: String) {
        try {
            val logDir = File(context.getExternalFilesDir(null), LOG_DIR)
            if (!logDir.exists()) {
                logDir.mkdirs()
            }
            
            val formatter = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
            val fileName = "error_${formatter.format(Date())}.txt"
            val logFile = File(logDir, fileName)
            FileWriter(logFile, true).use {
                it.write(errorMessage)
            }
            Log.d(TAG, "Error log saved to ${logFile.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save error log", e)
        }
    }

    fun getErrorLogFiles(context: Context): List<File> {
        val logDir = File(context.getExternalFilesDir(null), LOG_DIR)
        if (!logDir.exists()) {
            return emptyList()
        }
        return logDir.listFiles { file ->
            file.isFile && file.name.startsWith("error_") && file.name.endsWith(".txt")
        }?.toList() ?: emptyList()
    }

    fun clearErrorLogs(context: Context) {
        val logDir = File(context.getExternalFilesDir(null), LOG_DIR)
        if (logDir.exists()) {
            logDir.listFiles()?.forEach {
                it.delete()
            }
        }
    }
}
