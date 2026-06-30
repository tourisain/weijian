package com.tourisain.weijian.util

import android.content.Context
import android.os.Build
import android.provider.Settings
import java.security.MessageDigest

object DeviceUtil {
    fun getDeviceId(context: Context): String {
        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            ?.takeIf { it.isNotBlank() && it != "9774d56d682e549c" }
        if (androidId != null) return androidId

        val fallback = listOf(
            context.packageName,
            Build.BRAND,
            Build.DEVICE,
            Build.MODEL,
            Build.MANUFACTURER
        ).joinToString("|")
        return sha256(fallback).take(32)
    }

    private fun sha256(value: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }
}
