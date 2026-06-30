package com.tourisain.weijian

import android.app.Application
import android.os.Build
import android.os.Looper
import android.os.StrictMode
import android.util.Log
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
// import com.tourisain.weijian.BuildConfig
import com.tourisain.weijian.util.AppLocaleManager
import com.tourisain.weijian.util.CrashHandler
import com.tourisain.weijian.util.ErrorReporter
import com.tourisain.weijian.util.PerformanceMonitor
import com.tourisain.weijian.util.SecurityUtil
import com.tourisain.weijian.util.PremiumManager
import com.tourisain.weijian.util.TimeManager
import com.tourisain.weijian.util.WorkManagerUtil
import com.tourisain.weijian.data.database.AppDatabase
import com.tourisain.weijian.data.repository.UserRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@HiltAndroidApp
class MemoApplication : Application(), ImageLoaderFactory {
    @Inject
    lateinit var premiumManager: PremiumManager
    
    @Inject
    lateinit var userPreferences: com.tourisain.weijian.data.preferences.UserPreferences

    @Inject
    lateinit var userRepository: UserRepository

    private var originalUncaughtExceptionHandler: Thread.UncaughtExceptionHandler? = null
    
    // Application-level scope for app lifetime background work.
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val postConsentInitializationStarted = AtomicBoolean(false)

    override fun onCreate() {
        super.onCreate()
        
        // Track startup without blocking the first UI frame.
        PerformanceMonitor.startOperation("AppStart")
        
        initLanguageSettings()
        
        originalUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        
        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            handleGlobalException(throwable)
        }
        
        PerformanceMonitor.endOperation("AppStart")
        Log.d("MemoApplication", "App UI startup completed; post-consent work is gated.")
        
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()
                    .penaltyLog()
                    .build()
            )
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .build()
            )
        }

        applicationScope.launch(Dispatchers.Default) {
            if (hasAcceptedRequiredPolicies()) {
                runPostConsentInitialization()
            }
        }

        android.util.Log.d("MemoApplication", "App started successfully.")
    }

    fun runPostConsentInitialization() {
        if (!postConsentInitializationStarted.compareAndSet(false, true)) return
        applicationScope.launch(Dispatchers.Default) {
            if (!hasAcceptedRequiredPolicies()) {
                postConsentInitializationStarted.set(false)
                return@launch
            }
            try {
                TimeManager.initialize()
                CrashHandler.init()
                SecurityUtil.init(this@MemoApplication)
                userRepository.ensureLocalUserId()
                WorkManagerUtil.scheduleCleanupWork(this@MemoApplication)
            } catch (e: Exception) {
                Log.e("MemoApplication", "核心功能初始化失败", e)
                ErrorReporter.reportException(this@MemoApplication, e)
            }

            kotlinx.coroutines.delay(1000)
            try {
                PerformanceMonitor.startOperation("initializePremiumStatus")
                premiumManager.initializePremiumStatus()
                PerformanceMonitor.endOperation("initializePremiumStatus")
            } catch (e: Exception) {
                Log.e("MemoApplication", "会员状态初始化失败", e)
                PerformanceMonitor.recordError("initializePremiumStatus", e)
            }
        }
    }

    private suspend fun hasAcceptedRequiredPolicies(): Boolean {
        return userPreferences.isPrivacyPolicyAccepted.first() &&
            userPreferences.isUserAgreementAccepted.first()
    }

    private fun handleGlobalException(throwable: Throwable) {
        val currentThread = Thread.currentThread()
        when (throwable) {
            is IOException -> {
                when (throwable) {
                    is SocketTimeoutException -> {
                        ErrorReporter.reportError(this, "NetworkTimeout", throwable)
                        PerformanceMonitor.recordError("NetworkTimeout", throwable)
                    }
                    is UnknownHostException -> {
                        ErrorReporter.reportError(this, "UnknownHost", throwable)
                        PerformanceMonitor.recordError("UnknownHost", throwable)
                    }
                    else -> {
                        ErrorReporter.reportError(this, "IOException", throwable)
                        PerformanceMonitor.recordError("IOException", throwable)
                    }
                }
            }
            is OutOfMemoryError -> {
                ErrorReporter.reportError(this, "OutOfMemory", throwable)
                PerformanceMonitor.recordError("OutOfMemory", throwable)
                PerformanceMonitor.clearAll()
            }
            else -> {
                ErrorReporter.reportError(this, "GeneralException", throwable)
                PerformanceMonitor.recordError("GeneralException", throwable)
            }
        }
        if (currentThread == Looper.getMainLooper().thread) {
            originalUncaughtExceptionHandler?.uncaughtException(currentThread, throwable)
                ?: android.os.Process.killProcess(android.os.Process.myPid())
        }
    }

    private fun initLanguageSettings() {
        applicationScope.launch {
            try {
                val language = userPreferences.appLanguage.first()
                setAppLanguage(language)
            } catch (e: Exception) {
                Log.e("MemoApplication", "Language initialization failed", e)
            }
        }
    }

    private fun setAppLanguage(language: String?) {
        AppLocaleManager.applyToResources(this, language)
    }
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.15)
                    .strongReferencesEnabled(false)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.02)
                    .build()
            }
            .crossfade(false)
            .build()
    }

    @Suppress("DEPRECATION")
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level >= android.content.ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW) {
            PerformanceMonitor.clearAll()
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        PerformanceMonitor.clearAll()
    }
}
