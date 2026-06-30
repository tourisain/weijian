package com.tourisain.weijian.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Credentials
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.net.URLDecoder
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Element

data class WebDavRemoteFile(
    val name: String,
    val size: Long?,
    val lastModified: Long?
)

class WebDavClient(
    private val url: String,
    private val username: String,
    private val password: String
) {
    suspend fun upload(fileName: String, bytes: ByteArray): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            require(fileName.isNotBlank()) { "Backup file name is blank" }
            require(bytes.isNotEmpty()) { "Backup file is empty" }

            val baseUrl = validatedBaseUrl()
            ensureCollectionExists(baseUrl)

            val fileUrl = baseUrl.newBuilder()
                .addPathSegment(fileName.trim())
                .build()
            val body = bytes.toRequestBody(BINARY_MEDIA_TYPE)
            execute(requestBuilder(fileUrl).put(body).build()).use { response ->
                if (!response.isWebDavSuccess()) {
                    throw IOException("WebDAV upload failed: HTTP ${response.code} ${response.safeMessage()}")
                }
            }
        }
    }

    suspend fun checkConnection(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val baseUrl = validatedBaseUrl()
            ensureCollectionExists(baseUrl)
        }
    }

    suspend fun listFiles(): Result<List<WebDavRemoteFile>> = withContext(Dispatchers.IO) {
        runCatching {
            val baseUrl = validatedBaseUrl()
            ensureCollectionExists(baseUrl)

            execute(
                requestBuilder(baseUrl)
                    .method("PROPFIND", PROPFIND_BODY.toRequestBody(XML_MEDIA_TYPE))
                    .header("Depth", "1")
                    .build()
            ).use { response ->
                if (!response.isWebDavSuccess()) {
                    throw IOException("WebDAV file list failed: HTTP ${response.code} ${response.safeMessage()}")
                }
                parseFileList(response.body?.string().orEmpty())
            }
        }
    }

    suspend fun download(fileName: String): Result<ByteArray> = withContext(Dispatchers.IO) {
        runCatching {
            require(fileName.isNotBlank()) { "Backup file name is blank" }
            val baseUrl = validatedBaseUrl()
            val fileUrl = baseUrl.newBuilder()
                .addPathSegment(fileName.trim())
                .build()
            execute(requestBuilder(fileUrl).get().build()).use { response ->
                if (!response.isWebDavSuccess()) {
                    throw IOException("WebDAV download failed: HTTP ${response.code} ${response.safeMessage()}")
                }
                response.body?.bytes() ?: throw IOException("WebDAV download returned empty response")
            }
        }
    }

    private fun validatedBaseUrl(): HttpUrl {
        val trimmed = url.trim()
        require(trimmed.isNotBlank()) { "WebDAV url is blank" }
        require(trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            "WebDAV url must start with http:// or https://"
        }
        val normalized = if (trimmed.endsWith("/")) trimmed else "$trimmed/"
        return normalized.toHttpUrl()
    }

    private fun ensureCollectionExists(baseUrl: HttpUrl) {
        execute(
            requestBuilder(baseUrl)
                .method("PROPFIND", EMPTY_XML_BODY)
                .header("Depth", "0")
                .build()
        ).use { response ->
            when {
                response.isWebDavSuccess() -> return
                response.code == 404 -> createCollection(baseUrl)
                response.code == 401 || response.code == 403 -> {
                    throw IOException("WebDAV authentication failed: HTTP ${response.code}")
                }
                response.code == 405 -> verifyWithHead(baseUrl)
                else -> throw IOException("WebDAV connection failed: HTTP ${response.code} ${response.safeMessage()}")
            }
        }
    }

    private fun createCollection(baseUrl: HttpUrl) {
        execute(
            requestBuilder(baseUrl)
                .method("MKCOL", EMPTY_XML_BODY)
                .build()
        ).use { response ->
            if (response.isWebDavSuccess() || response.code == 405) {
                return
            }
            throw IOException("WebDAV folder creation failed: HTTP ${response.code} ${response.safeMessage()}")
        }
    }

    private fun verifyWithHead(baseUrl: HttpUrl) {
        execute(requestBuilder(baseUrl).head().build()).use { response ->
            if (response.isWebDavSuccess()) {
                return
            }
            throw IOException("WebDAV connection failed: HTTP ${response.code} ${response.safeMessage()}")
        }
    }

    private fun requestBuilder(requestUrl: HttpUrl): Request.Builder {
        val builder = Request.Builder()
            .url(requestUrl)
            .header("User-Agent", "Weijian-Android-WebDAV")
        if (username.isNotBlank() || password.isNotBlank()) {
            builder.header("Authorization", Credentials.basic(username, password))
        }
        return builder
    }

    private fun execute(request: Request): Response {
        return client.newCall(request).execute()
    }

    private fun parseFileList(xml: String): List<WebDavRemoteFile> {
        if (xml.isBlank()) return emptyList()

        val factory = DocumentBuilderFactory.newInstance().apply {
            isNamespaceAware = true
            runCatching { setFeature("http://apache.org/xml/features/disallow-doctype-decl", true) }
            runCatching { setFeature("http://xml.org/sax/features/external-general-entities", false) }
            runCatching { setFeature("http://xml.org/sax/features/external-parameter-entities", false) }
        }
        val document = factory.newDocumentBuilder().parse(xml.byteInputStream())
        val responses = document.getElementsByTagNameNS("*", "response")
        val files = mutableListOf<WebDavRemoteFile>()

        for (index in 0 until responses.length) {
            val response = responses.item(index) as? Element ?: continue
            if (response.getElementsByTagNameNS("*", "collection").length > 0) continue

            val name = hrefToFileName(response.firstText("href")) ?: continue
            files += WebDavRemoteFile(
                name = name,
                size = response.firstText("getcontentlength")?.toLongOrNull(),
                lastModified = parseLastModified(response.firstText("getlastmodified"))
            )
        }

        return files
    }

    private fun Element.firstText(localName: String): String? {
        val nodes = getElementsByTagNameNS("*", localName)
        if (nodes.length == 0) return null
        return nodes.item(0)?.textContent?.trim()?.takeIf { it.isNotBlank() }
    }

    private fun hrefToFileName(href: String?): String? {
        val cleaned = href
            ?.substringBefore("?")
            ?.trimEnd('/')
            ?.substringAfterLast('/')
            ?.takeIf { it.isNotBlank() }
            ?: return null
        return URLDecoder.decode(cleaned, Charsets.UTF_8.name()).takeIf { it.isNotBlank() }
    }

    private fun parseLastModified(value: String?): Long? {
        return value?.let {
            runCatching {
                ZonedDateTime.parse(it, DateTimeFormatter.RFC_1123_DATE_TIME)
                    .toInstant()
                    .toEpochMilli()
            }.getOrNull()
        }
    }

    private fun Response.isWebDavSuccess(): Boolean {
        return code in 200..299 || code == 207
    }

    private fun Response.safeMessage(): String {
        val bodyText = runCatching { peekBody(MAX_ERROR_BODY_BYTES).string().trim() }.getOrDefault("")
        return bodyText.ifBlank { message }
    }

    private companion object {
        private val BINARY_MEDIA_TYPE = "application/octet-stream".toMediaType()
        private val XML_MEDIA_TYPE = "application/xml; charset=utf-8".toMediaType()
        private val EMPTY_XML_BODY = "".toRequestBody(XML_MEDIA_TYPE)
        private const val PROPFIND_BODY =
            """<?xml version="1.0" encoding="utf-8"?><propfind xmlns="DAV:"><prop><resourcetype/><getcontentlength/><getlastmodified/></prop></propfind>"""
        private const val MAX_ERROR_BODY_BYTES = 512L

        private val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }
}
