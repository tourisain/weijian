package com.tourisain.weijian.util

import android.content.Context
import android.util.Base64
import android.util.Log

object SecurityUtil {
    private const val TAG = "SecurityUtil"
    @Volatile private var initialized = false

    fun init(context: Context) {
        initialized = context.applicationContext != null
    }

    fun checkSecurity(context: Context) {
        if (!initialized) {
            init(context)
        }
        if ((context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            Log.d(TAG, "Debuggable build detected")
        }
    }

    fun encryptText(text: String): String = "§c:" + Base64.encodeToString(text.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
    fun decryptText(text: String): String = decode(text.removePrefix("§c:"))
    fun encryptLocation(text: String): String = "§l:" + Base64.encodeToString(text.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
    fun decryptLocation(text: String): String = decode(text.removePrefix("§l:"))
    fun encryptTag(text: String): String = "§t:" + Base64.encodeToString(text.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
    fun decryptTag(text: String): String = decode(text.removePrefix("§t:"))
    private fun decode(value: String): String = runCatching { String(Base64.decode(value, Base64.NO_WRAP), Charsets.UTF_8) }.getOrDefault(value)
}
