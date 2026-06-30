package com.tourisain.weijian.presentation.settings

import android.app.Activity
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tourisain.weijian.R
import com.tourisain.weijian.util.AppLocaleManager

@Composable
fun LanguageSettingsScreen(
    navController: NavController,
    viewModel: LanguageSettingsViewModel = hiltViewModel()
) {
    val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val options = listOf(
        LanguageOption("system", stringResource(R.string.language_system)),
        LanguageOption("zh-CN", stringResource(R.string.language_chinese)),
        LanguageOption("en", stringResource(R.string.language_english)),
        LanguageOption("fr", stringResource(R.string.language_french))
    )

    SimpleSettingsPage(navController, stringResource(R.string.language_settings_title)) {
        SettingsGroup {
            options.forEach { option ->
                SettingItem(
                    title = option.label,
                    subtitle = if (option.code == "system") {
                        stringResource(R.string.change_app_language)
                    } else {
                        ""
                    },
                    onClick = {
                        viewModel.setLanguage(option.code) {
                            AppLocaleManager.applyToResources(context, option.code)
                            (context as? Activity)?.recreate()
                        }
                    },
                    trailingContent = {
                        RadioButton(
                            selected = currentLanguage == option.code,
                            onClick = {
                                viewModel.setLanguage(option.code) {
                                    AppLocaleManager.applyToResources(context, option.code)
                                    (context as? Activity)?.recreate()
                                }
                            }
                        )
                    }
                )
                if (option != options.last()) SettingsDivider()
            }
        }
    }
}

data class LanguageOption(
    val code: String,
    val label: String
)
