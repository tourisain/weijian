package com.tourisain.weijian.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.tourisain.weijian.BuildConfig
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

data class RemoteUpdateInfo(
    val latestVersionCode: Int,
    val latestVersionName: String,
    val downloadUrl: String,
    val message: String,
    val releaseNotes: List<String>,
    val publishedAt: String
)

sealed class RemoteUpdateResult {
    data class Available(val info: RemoteUpdateInfo) : RemoteUpdateResult()
    data object Latest : RemoteUpdateResult()
    data object Disabled : RemoteUpdateResult()
    data class Error(val message: String) : RemoteUpdateResult()
}

object RemoteUpdateChecker {
    const val UPDATE_CONFIG_URL = "https://tourisain.cn/weijian/update.json"
    private const val MAX_CONFIG_BYTES = 256 * 1024L

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .callTimeout(8, TimeUnit.SECONDS)
            .build()
    }

    suspend fun checkForUpdate(configUrl: String = UPDATE_CONFIG_URL): RemoteUpdateResult = withContext(Dispatchers.IO) {
        runCatching {
            if (!isAllowedTourisainHttpsUrl(configUrl)) {
                return@withContext RemoteUpdateResult.Error("Invalid update config URL")
            }
            val request = Request.Builder()
                .url(configUrl)
                .header("Accept", "application/json")
                .header("Cache-Control", "no-cache")
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext RemoteUpdateResult.Error("HTTP ${response.code}")
                }
                val body = response.body ?: return@withContext RemoteUpdateResult.Error("Empty response")
                val length = body.contentLength()
                if (length > MAX_CONFIG_BYTES) {
                    return@withContext RemoteUpdateResult.Error("Update config is too large")
                }
                val jsonText = body.string()
                if (jsonText.toByteArray(Charsets.UTF_8).size.toLong() > MAX_CONFIG_BYTES) {
                    return@withContext RemoteUpdateResult.Error("Update config is too large")
                }
                parseUpdateConfig(jsonText)
            }
        }.getOrElse { RemoteUpdateResult.Error(it.message ?: "Update check failed") }
    }

    fun openDownloadPage(context: Context, info: RemoteUpdateInfo): Boolean {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(info.downloadUrl))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return runCatching {
            context.startActivity(intent)
            true
        }.getOrDefault(false)
    }

    private fun parseUpdateConfig(jsonText: String): RemoteUpdateResult {
        val json = JSONObject(jsonText)
        if (!json.optBoolean("enabled", true)) return RemoteUpdateResult.Disabled

        val latestVersionCode = json.optInt("latestVersionCode", 0)
        if (latestVersionCode <= BuildConfig.VERSION_CODE) return RemoteUpdateResult.Latest

        val latestVersionName = json.optString("latestVersionName").ifBlank { latestVersionCode.toString() }
        val downloadUrl = json.optString("downloadUrl")
        if (!isAllowedTourisainHttpsUrl(downloadUrl)) {
            return RemoteUpdateResult.Error("Invalid download URL")
        }

        val notesArray = json.optJSONArray("releaseNotes")
        val releaseNotes = buildList {
            if (notesArray != null) {
                for (index in 0 until notesArray.length()) {
                    val note = notesArray.optString(index).trim()
                    if (note.isNotBlank()) add(note.take(160))
                }
            }
        }.take(8)

        return RemoteUpdateResult.Available(
            RemoteUpdateInfo(
                latestVersionCode = latestVersionCode,
                latestVersionName = latestVersionName.take(32),
                downloadUrl = downloadUrl,
                message = json.optString("message").trim().take(240),
                releaseNotes = releaseNotes,
                publishedAt = json.optString("publishedAt").trim().take(32)
            )
        )
    }

    private fun isAllowedTourisainHttpsUrl(value: String): Boolean {
        val uri = runCatching { Uri.parse(value) }.getOrNull() ?: return false
        val host = uri.host?.lowercase() ?: return false
        return uri.scheme == "https" && (host == "tourisain.cn" || host.endsWith(".tourisain.cn"))
    }
}
