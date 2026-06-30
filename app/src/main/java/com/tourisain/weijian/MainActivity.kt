package com.tourisain.weijian

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.SideEffect
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.Modifier
import com.tourisain.weijian.presentation.navigation.AppNavigation
import com.tourisain.weijian.presentation.privacy.PrivacyLockScreen
import com.tourisain.weijian.presentation.privacy.PrivacyViewModel
import com.tourisain.weijian.presentation.note.components.AppleNotesStyle
import com.tourisain.weijian.presentation.theme.MemoAppTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.hilt.navigation.compose.hiltViewModel
import com.tourisain.weijian.presentation.common.AppleAlertDialog
import com.tourisain.weijian.presentation.splash.SplashScreen
import com.tourisain.weijian.presentation.update.LocalReleaseNotesDialog
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import com.tourisain.weijian.util.ErrorReporter
import androidx.lifecycle.lifecycleScope
import com.tourisain.weijian.util.ActivityLifecycleMonitor
import com.tourisain.weijian.util.AppLocaleManager

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var userPreferences: com.tourisain.weijian.data.preferences.UserPreferences
    @Inject lateinit var premiumManager: com.tourisain.weijian.util.PremiumManager
    @Inject lateinit var activationCodeManager: com.tourisain.weijian.util.ActivationCodeManager
    
    private val showExitDialog = mutableStateOf(false)
    // Guard against accidental repeated clicks while navigation settles.
    private var lastClickTime = 0L
    private val CLICK_INTERVAL = 500L

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(AppLocaleManager.wrapContext(newBase))
    }

    private fun applySavedLanguageBeforeContent() {
        try {
            AppLocaleManager.applyToResources(this, AppLocaleManager.storedLanguage(this))
        } catch (e: Exception) {
            ErrorReporter.reportException(this, e)
        }
    }

    private fun requestExit(confirmBeforeExit: Boolean) {
        if (confirmBeforeExit) {
            showExitDialog.value = true
        } else {
            performExit()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applySavedLanguageBeforeContent()
        ActivityLifecycleMonitor.init(application)
        enableEdgeToEdge()
        setContent {
            val appTheme by userPreferences.appTheme.collectAsStateWithLifecycle(initialValue = "system")
            val dynamicTheme by userPreferences.dynamicTheme.collectAsStateWithLifecycle(initialValue = true)
            val colorScheme by userPreferences.colorScheme.collectAsStateWithLifecycle(initialValue = "classic")
            val appFont by userPreferences.appFont.collectAsStateWithLifecycle(initialValue = "system")
            val textScale by userPreferences.textScale.collectAsStateWithLifecycle(initialValue = "normal")
            val confirmBeforeExit by userPreferences.confirmBeforeExit.collectAsStateWithLifecycle(initialValue = true)
            val systemDark = isSystemInDarkTheme()
            val darkTheme = when (appTheme) {
                "dark" -> true
                "light" -> false
                else -> systemDark
            }
            val styleMode = when (colorScheme) {
                "ios", "paper", "sage", "graphite" -> colorScheme.orEmpty()
                else -> "classic"
            }
            MemoAppTheme(
                darkTheme = darkTheme,
                dynamicColor = dynamicTheme ?: true,
                themeMode = styleMode,
                appFont = appFont ?: "system",
                textScale = textScale ?: "normal"
            ) {
                val materialColors = MaterialTheme.colorScheme
                val useDynamicStyle = shouldApplyDynamicAppearance(
                    dynamicTheme = dynamicTheme,
                    sdkInt = Build.VERSION.SDK_INT,
                    styleMode = styleMode
                )
                SideEffect {
                    AppleNotesStyle.applyAppearance(
                        darkTheme = darkTheme,
                        style = styleMode,
                        dynamicBackground = materialColors.background.takeIf { useDynamicStyle },
                        dynamicSurface = materialColors.surface.takeIf { useDynamicStyle },
                        dynamicSearchSurface = materialColors.surfaceVariant.takeIf { useDynamicStyle },
                        dynamicAccent = materialColors.primary.takeIf { useDynamicStyle }
                    )
                }
                @OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val privacyViewModel: PrivacyViewModel = hiltViewModel()
                    val isPrivacyEnabled by privacyViewModel.isPrivacyEnabled.collectAsStateWithLifecycle()
                    val isUnlocked by privacyViewModel.isUnlocked.collectAsStateWithLifecycle()
                    
                    val showSplash = remember { mutableStateOf(true) }
                    val hasAgreedToPrivacyPolicy by userPreferences.isPrivacyPolicyAccepted.collectAsStateWithLifecycle(initialValue = false)
                    val hasAgreedToUserAgreement by userPreferences.isUserAgreementAccepted.collectAsStateWithLifecycle(initialValue = false)
                    val routeTo = intent.getStringExtra("route_to")
                    val pendingLocalReleaseNotes = remember { mutableStateOf(false) }
                    val localReleaseNotesCheckStarted = remember { mutableStateOf(false) }

                    LaunchedEffect(
                        showSplash.value,
                        hasAgreedToPrivacyPolicy,
                        hasAgreedToUserAgreement,
                        isPrivacyEnabled,
                        isUnlocked
                    ) {
                        val canShowReleaseNotes =
                            !showSplash.value &&
                                hasAgreedToPrivacyPolicy &&
                                hasAgreedToUserAgreement &&
                                (!isPrivacyEnabled || isUnlocked)
                        if (canShowReleaseNotes && !localReleaseNotesCheckStarted.value) {
                            localReleaseNotesCheckStarted.value = true
                            val lastShownVersionCode = userPreferences.getLastShownReleaseNotesVersionCode()
                            if (lastShownVersionCode <= 0) {
                                userPreferences.setLastShownReleaseNotesVersionCode(BuildConfig.VERSION_CODE)
                            } else if (lastShownVersionCode < BuildConfig.VERSION_CODE) {
                                pendingLocalReleaseNotes.value = true
                            }
                        }
                    }

                    Box {
                        if (showSplash.value) {
                            SplashScreen(onSplashComplete = {
                                showSplash.value = false
                            })
                        } else if (!hasAgreedToPrivacyPolicy || !hasAgreedToUserAgreement) {
                            com.tourisain.weijian.presentation.privacy.PrivacyConsentScreen(
                                onAccept = {
                                    lifecycleScope.launch {
                                        userPreferences.setPrivacyPolicyAccepted(true)
                                        userPreferences.setUserAgreementAccepted(true)
                                        (application as? MemoApplication)?.runPostConsentInitialization()
                                    }
                                },
                                onExit = { finish() }
                            )
                        } else if (isPrivacyEnabled && !isUnlocked) {
                            PrivacyLockScreen(onUnlock = { })
                        } else {
                            @OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
                            AppNavigation(
                                premiumManager = premiumManager, 
                                activationCodeManager = activationCodeManager,
                                routeTo = routeTo,
                                onExitRequested = { requestExit(confirmBeforeExit) }
                            )
                        }
                        if (showExitDialog.value) {
                            AppleAlertDialog(
                                onDismissRequest = { showExitDialog.value = false },
                                title = { Text(getString(R.string.confirm_exit_title)) },
                                text = { Text(getString(R.string.confirm_exit_message)) },
                                confirmButton = {
                                    TextButton(onClick = {
                                        showExitDialog.value = false
                                        performExit()
                                    }) {
                                        Text(getString(R.string.ok))
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showExitDialog.value = false }) {
                                        Text(getString(R.string.cancel))
                                    }
                                }
                            )
                        }
                        if (pendingLocalReleaseNotes.value) {
                            LocalReleaseNotesDialog(
                                onDismiss = {
                                    lifecycleScope.launch {
                                        userPreferences.setLastShownReleaseNotesVersionCode(BuildConfig.VERSION_CODE)
                                    }
                                    pendingLocalReleaseNotes.value = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime < CLICK_INTERVAL) {
            return
        }
        lastClickTime = currentTime
        try {
            @Suppress("DEPRECATION")
            super.onBackPressed()
        } catch (e: Exception) {
            ErrorReporter.reportException(this, e)
            runOnUiThread {
                android.widget.Toast.makeText(this, getString(R.string.operation_failed_retry), android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun isClickAllowed(): Boolean {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime < CLICK_INTERVAL) {
            return false
        }
        lastClickTime = currentTime
        return true
    }

    override fun finish() {
        performExit()
    }

    private fun performExit() {
        try {
            super.finish()
        } catch (e: Exception) {
            ErrorReporter.reportException(this, e)
            android.os.Process.killProcess(android.os.Process.myPid())
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        lifecycleScope.launch {
            try {
                val currentLanguage = userPreferences.appLanguage.first()
                AppLocaleManager.applyToResources(this@MainActivity, currentLanguage)
            } catch (e: Exception) {
                ErrorReporter.reportException(this@MainActivity, e)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}

internal fun shouldApplyDynamicAppearance(dynamicTheme: Boolean?, sdkInt: Int, styleMode: String): Boolean {
    return dynamicTheme == true &&
        sdkInt >= Build.VERSION_CODES.S &&
        styleMode == "classic"
}

