package com.tourisain.weijian.util

import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.ApplicationInfo
import android.os.Build
import android.os.Debug
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.net.InetSocketAddress
import java.net.Socket
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

@Singleton
class SecurityEnvironmentMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _snapshot = MutableStateFlow(SecurityEnvironmentSnapshot())
    val snapshot: StateFlow<SecurityEnvironmentSnapshot> = _snapshot.asStateFlow()

    suspend fun scanNow(): SecurityEnvironmentSnapshot = withContext(Dispatchers.IO) {
        val memory = readMemoryState()
        val debugBuild = isDebuggableBuild()
        val result = SecurityEnvironmentSnapshot(
            lowMemory = memory.lowMemory,
            trimMemoryLevel = _snapshot.value.trimMemoryLevel,
            availableMemoryMb = memory.availableMemoryMb,
            totalMemoryMb = memory.totalMemoryMb,
            xposedDetected = isXposedDetected(),
            fridaDetected = isFridaDetected(),
            debuggerDetected = isDebuggerDetected(),
            tracerDetected = isTracerDetected(),
            tamperedSignature = isReleaseSignatureTampered(debugBuild),
            debugBuild = debugBuild,
            lastCheckedAt = TimeManager.getCurrentTime()
        )
        _snapshot.value = result
        if (result.requiresAttention) {
            Log.w(TAG, "Security environment attention: ${result.riskSummary()}")
        }
        result
    }

    suspend fun refreshMemoryState(): SecurityEnvironmentSnapshot = withContext(Dispatchers.IO) {
        val memory = readMemoryState()
        val result = _snapshot.value.copy(
            lowMemory = memory.lowMemory,
            trimMemoryLevel = if (memory.lowMemory) _snapshot.value.trimMemoryLevel else 0,
            availableMemoryMb = memory.availableMemoryMb,
            totalMemoryMb = memory.totalMemoryMb,
            lastCheckedAt = TimeManager.getCurrentTime()
        )
        _snapshot.value = result
        if (result.lowMemory) {
            PerformanceMonitor.clearAll()
        }
        result
    }

    fun onTrimMemory(level: Int) {
        val current = _snapshot.value
        _snapshot.value = current.copy(
            lowMemory = current.lowMemory || level >= LOW_MEMORY_TRIM_LEVEL,
            trimMemoryLevel = level,
            lastCheckedAt = TimeManager.getCurrentTime()
        )
        if (level >= LOW_MEMORY_TRIM_LEVEL) {
            PerformanceMonitor.clearAll()
        }
    }

    fun onLowMemory() {
        val current = _snapshot.value
        _snapshot.value = current.copy(
            lowMemory = true,
            trimMemoryLevel = LOW_MEMORY_TRIM_LEVEL,
            lastCheckedAt = TimeManager.getCurrentTime()
        )
        PerformanceMonitor.clearAll()
    }

    private fun readMemoryState(): MemoryState {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            ?: return MemoryState()
        val info = ActivityManager.MemoryInfo()
        manager.getMemoryInfo(info)
        return MemoryState(
            lowMemory = info.lowMemory,
            availableMemoryMb = info.availMem / BYTES_PER_MB,
            totalMemoryMb = info.totalMem / BYTES_PER_MB
        )
    }

    private fun isXposedDetected(): Boolean {
        val classDetected = XPOSED_CLASS_NAMES.any { className ->
            runCatching { Class.forName(className) }.isSuccess
        }
        if (classDetected) return true

        val stackDetected = Thread.currentThread().stackTrace.any { trace ->
            val value = "${trace.className}.${trace.methodName}".lowercase()
            HOOK_KEYWORDS.any { value.contains(it) }
        }
        if (stackDetected) return true

        return XPOSED_FILES.any { File(it).exists() }
    }

    private fun isFridaDetected(): Boolean {
        val mapsDetected = runCatching {
            File("/proc/self/maps").useLines { lines ->
                lines.any { line ->
                    val value = line.lowercase()
                    NATIVE_ANALYSIS_KEYWORDS.any { value.contains(it) }
                }
            }
        }.getOrDefault(false)
        if (mapsDetected) return true

        return FRIDA_PORTS.any { port ->
            runCatching {
                Socket().use { socket ->
                    socket.connect(InetSocketAddress("127.0.0.1", port), PORT_CHECK_TIMEOUT_MS)
                    true
                }
            }.getOrDefault(false)
        }
    }

    private fun isDebuggableBuild(): Boolean {
        return (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }

    private fun isDebuggerDetected(): Boolean {
        return Debug.isDebuggerConnected() || Debug.waitingForDebugger()
    }

    private fun isTracerDetected(): Boolean {
        return runCatching {
            File("/proc/self/status").useLines { lines ->
                lines.firstOrNull { it.startsWith("TracerPid:") }
                    ?.substringAfter(':')
                    ?.trim()
                    ?.toIntOrNull()
                    ?.let { it > 0 }
                    ?: false
            }
        }.getOrDefault(false)
    }

    @Suppress("DEPRECATION")
    private fun isReleaseSignatureTampered(debugBuild: Boolean): Boolean {
        if (debugBuild) return false
        val current = runCatching { currentSigningCertificateDigests() }.getOrDefault(emptyList())
        if (current.isEmpty()) return true
        return current.none { it.equals(RELEASE_CERT_SHA256, ignoreCase = true) }
    }

    @Suppress("DEPRECATION")
    private fun currentSigningCertificateDigests(): List<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val packageInfo = context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_SIGNING_CERTIFICATES
            )
            val signingInfo = packageInfo.signingInfo ?: return emptyList()
            val signatures = if (signingInfo.hasMultipleSigners()) {
                signingInfo.apkContentsSigners
            } else {
                signingInfo.signingCertificateHistory
            }
            signatures?.map { sha256(it.toByteArray()) }.orEmpty()
        } else {
            val packageInfo = context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_SIGNATURES
            )
            packageInfo.signatures?.map { sha256(it.toByteArray()) }.orEmpty()
        }
    }

    private fun sha256(value: ByteArray): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256").digest(value)
        return digest.joinToString("") { "%02x".format(it) }
    }

    private data class MemoryState(
        val lowMemory: Boolean = false,
        val availableMemoryMb: Long = 0,
        val totalMemoryMb: Long = 0
    )

    private companion object {
        const val TAG = "SecurityEnvironment"
        const val BYTES_PER_MB = 1024L * 1024L
        const val LOW_MEMORY_TRIM_LEVEL = android.content.ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW
        const val PORT_CHECK_TIMEOUT_MS = 45
        const val RELEASE_CERT_SHA256 = "69bc69822ab8fc70eb5f474f6c86ecc38a5bc18425993bed629413b1eaad9e71"
        val FRIDA_PORTS = listOf(27042, 27043)
        val HOOK_KEYWORDS = listOf("xposed", "lsposed", "edxposed", "substrate", "zygisk", "riru")
        val NATIVE_ANALYSIS_KEYWORDS = listOf(
            "frida",
            "gum-js-loop",
            "gadget",
            "xposed",
            "lsposed",
            "edxposed",
            "substrate",
            "zygisk",
            "riru"
        )
        val XPOSED_CLASS_NAMES = listOf(
            "de.robv.android.xposed.XposedBridge",
            "de.robv.android.xposed.XC_MethodHook",
            "org.lsposed.lspd.nativebridge.HookBridge",
            "com.saurik.substrate.MS\$2"
        )
        val XPOSED_FILES = listOf(
            "/system/framework/XposedBridge.jar",
            "/system/bin/app_process32_xposed",
            "/system/bin/app_process64_xposed",
            "/sbin/.magisk/modules/riru_lsposed",
            "/data/adb/modules/zygisk_lsposed"
        )
    }
}

data class SecurityEnvironmentSnapshot(
    val lowMemory: Boolean = false,
    val trimMemoryLevel: Int = 0,
    val availableMemoryMb: Long = 0,
    val totalMemoryMb: Long = 0,
    val xposedDetected: Boolean = false,
    val fridaDetected: Boolean = false,
    val debuggerDetected: Boolean = false,
    val tracerDetected: Boolean = false,
    val tamperedSignature: Boolean = false,
    val debugBuild: Boolean = false,
    val lastCheckedAt: Long = 0
) {
    val riskCount: Int
        get() = listOf(
            lowMemory,
            xposedDetected,
            fridaDetected,
            debuggerDetected,
            tracerDetected,
            tamperedSignature,
            debugBuild
        ).count { it }

    val criticalRiskCount: Int
        get() = listOf(
            xposedDetected,
            fridaDetected,
            debuggerDetected,
            tracerDetected,
            tamperedSignature
        ).count { it }

    val shouldTerminate: Boolean
        get() = criticalRiskCount > 0

    val requiresAttention: Boolean
        get() = riskCount > 0

    fun riskSummary(): String = buildList {
        if (lowMemory) add("low_memory")
        if (xposedDetected) add("xposed")
        if (fridaDetected) add("frida")
        if (debuggerDetected) add("debugger")
        if (tracerDetected) add("tracer")
        if (tamperedSignature) add("signature")
        if (debugBuild) add("debuggable")
    }.joinToString(",")
}
