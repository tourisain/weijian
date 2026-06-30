package com.tourisain.weijian.util

import android.app.LocaleManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale

object AppLocaleManager {
    private const val PREFS_NAME = "weijian_runtime_preferences"
    private const val LANGUAGE_KEY = "app_language"
    private const val LANGUAGE_SYSTEM = "system"
    private const val LANGUAGE_CHINESE = "zh-CN"
    private const val LANGUAGE_ENGLISH = "en"
    private const val LANGUAGE_FRENCH = "fr"

    fun persistLanguage(context: Context, language: String?) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(LANGUAGE_KEY, normalizeLanguage(language))
            .apply()
    }

    fun storedLanguage(context: Context): String {
        return normalizeLanguage(
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(LANGUAGE_KEY, LANGUAGE_SYSTEM)
        )
    }

    fun wrapContext(base: Context, language: String = storedLanguage(base)): Context {
        val locale = localeFor(language)
        Locale.setDefault(locale)
        val configuration = Configuration(base.resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocales(LocaleList(locale))
        } else {
            @Suppress("DEPRECATION")
            configuration.setLocale(locale)
        }
        return base.createConfigurationContext(configuration)
    }

    fun applyToResources(context: Context, language: String?) {
        val normalized = normalizeLanguage(language)
        persistLanguage(context, normalized)
        updatePlatformLocale(context, normalized)
        val locale = localeFor(normalized)
        Locale.setDefault(locale)
        val configuration = Configuration(context.resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocales(LocaleList(locale))
        } else {
            @Suppress("DEPRECATION")
            configuration.setLocale(locale)
        }
        @Suppress("DEPRECATION")
        context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
    }

    private fun updatePlatformLocale(context: Context, language: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val localeManager = context.getSystemService(LocaleManager::class.java)
            localeManager?.applicationLocales = if (language == LANGUAGE_SYSTEM) {
                LocaleList.getEmptyLocaleList()
            } else {
                LocaleList.forLanguageTags(language)
            }
        }
    }

    private fun localeFor(language: String): Locale {
        return when (normalizeLanguage(language)) {
            LANGUAGE_CHINESE -> Locale.SIMPLIFIED_CHINESE
            LANGUAGE_ENGLISH -> Locale.ENGLISH
            LANGUAGE_FRENCH -> Locale.FRENCH
            else -> systemLocale()
        }
    }

    private fun normalizeLanguage(language: String?): String {
        return when (language) {
            LANGUAGE_CHINESE, LANGUAGE_ENGLISH, LANGUAGE_FRENCH -> language
            else -> LANGUAGE_SYSTEM
        }
    }

    private fun systemLocale(): Locale {
        val configuration = android.content.res.Resources.getSystem().configuration
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.locales[0] ?: Locale.getDefault()
        } else {
            @Suppress("DEPRECATION")
            configuration.locale ?: Locale.getDefault()
        }
    }
}
