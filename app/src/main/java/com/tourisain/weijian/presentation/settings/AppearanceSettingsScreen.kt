package com.tourisain.weijian.presentation.settings

import android.os.Build
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tourisain.weijian.R
import com.tourisain.weijian.presentation.icons.Lucide

@Composable
fun AppearanceSettingsScreen(
    navController: NavController,
    viewModel: SettingsPreferencesViewModel = hiltViewModel()
) {
    val appTheme by viewModel.appTheme.collectAsStateWithLifecycle()
    val dynamicTheme by viewModel.dynamicTheme.collectAsStateWithLifecycle()
    val appFont by viewModel.appFont.collectAsStateWithLifecycle()
    val textScale by viewModel.textScale.collectAsStateWithLifecycle()
    val colorScheme by viewModel.colorScheme.collectAsStateWithLifecycle()
    val styleIsClassic = colorScheme !in setOf("ios", "paper", "sage", "graphite")
    val dynamicColorSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val dynamicColorSubtitle = when {
        !dynamicColorSupported -> stringResource(R.string.appearance_dynamic_color_unavailable_desc)
        !styleIsClassic -> stringResource(R.string.appearance_dynamic_color_classic_only_desc)
        else -> stringResource(R.string.appearance_dynamic_color_desc)
    }

    SimpleSettingsPage(navController, stringResource(R.string.appearance)) {
        SettingsGroup {
            SettingItemWithRadio(
                title = stringResource(R.string.appearance_mode_system),
                subtitle = stringResource(R.string.appearance_mode_system_desc),
                selected = appTheme == "system",
                onClick = { viewModel.setAppTheme("system") }
            )
            SettingsDivider()
            SettingItemWithRadio(
                title = stringResource(R.string.appearance_mode_light),
                subtitle = stringResource(R.string.appearance_mode_light_desc),
                selected = appTheme == "light",
                onClick = { viewModel.setAppTheme("light") }
            )
            SettingsDivider()
            SettingItemWithRadio(
                title = stringResource(R.string.appearance_mode_dark),
                subtitle = stringResource(R.string.appearance_mode_dark_desc),
                selected = appTheme == "dark",
                onClick = { viewModel.setAppTheme("dark") }
            )
        }

        SettingsGroup {
            SettingItem(
                icon = Lucide.Palette,
                title = stringResource(R.string.appearance_dynamic_color),
                subtitle = dynamicColorSubtitle,
                trailingContent = {
                    Switch(
                        checked = dynamicTheme == true,
                        enabled = dynamicColorSupported,
                        onCheckedChange = viewModel::setDynamicTheme
                    )
                }
            )
            SettingsDivider()
            SettingItemWithRadio(
                title = stringResource(R.string.appearance_style_classic),
                subtitle = stringResource(R.string.appearance_style_classic_desc),
                selected = styleIsClassic,
                onClick = { viewModel.setColorScheme("classic") }
            )
            SettingsDivider()
            SettingItemWithRadio(
                title = stringResource(R.string.appearance_style_apple),
                subtitle = stringResource(R.string.appearance_style_apple_desc),
                selected = colorScheme == "ios",
                onClick = { viewModel.setColorScheme("ios") }
            )
            SettingsDivider()
            SettingItemWithRadio(
                title = stringResource(R.string.appearance_style_paper),
                subtitle = stringResource(R.string.appearance_style_paper_desc),
                selected = colorScheme == "paper",
                onClick = { viewModel.setColorScheme("paper") }
            )
            SettingsDivider()
            SettingItemWithRadio(
                title = stringResource(R.string.appearance_style_sage),
                subtitle = stringResource(R.string.appearance_style_sage_desc),
                selected = colorScheme == "sage",
                onClick = { viewModel.setColorScheme("sage") }
            )
            SettingsDivider()
            SettingItemWithRadio(
                title = stringResource(R.string.appearance_style_graphite),
                subtitle = stringResource(R.string.appearance_style_graphite_desc),
                selected = colorScheme == "graphite",
                onClick = { viewModel.setColorScheme("graphite") }
            )
        }

        SettingsGroup {
            SettingChoiceRow(
                title = stringResource(R.string.appearance_font),
                subtitle = fontLabel(appFont),
                options = listOf(
                    "system" to stringResource(R.string.appearance_font_system),
                    "serif" to stringResource(R.string.appearance_font_serif),
                    "mono" to stringResource(R.string.appearance_font_mono)
                ),
                selected = appFont ?: "system",
                onSelected = viewModel::setAppFont
            )
            SettingsDivider()
            SettingChoiceRow(
                title = stringResource(R.string.appearance_text_size),
                subtitle = textScaleLabel(textScale),
                options = listOf(
                    "compact" to stringResource(R.string.appearance_text_compact),
                    "normal" to stringResource(R.string.appearance_text_normal),
                    "large" to stringResource(R.string.appearance_text_large)
                ),
                selected = textScale ?: "normal",
                onSelected = viewModel::setTextScale
            )
        }
    }
}

@Composable
private fun SettingItemWithRadio(title: String, subtitle: String, selected: Boolean, onClick: () -> Unit) {
    SettingItem(
        title = title,
        subtitle = subtitle,
        onClick = onClick,
        trailingContent = {
            RadioButton(selected = selected, onClick = onClick)
        }
    )
}

@Composable
private fun SettingChoiceRow(
    title: String,
    subtitle: String,
    options: List<Pair<String, String>>,
    selected: String,
    onSelected: (String) -> Unit
) {
    SettingItem(
        title = title,
        subtitle = subtitle,
        trailingContent = {
            Text(options.firstOrNull { it.first == selected }?.second ?: selected)
        },
        onClick = {
            val index = options.indexOfFirst { it.first == selected }.coerceAtLeast(0)
            onSelected(options[(index + 1) % options.size].first)
        }
    )
}

@Composable
private fun fontLabel(value: String?): String = when (value) {
    "serif" -> stringResource(R.string.appearance_font_serif)
    "mono" -> stringResource(R.string.appearance_font_mono)
    else -> stringResource(R.string.appearance_font_system)
}

@Composable
private fun textScaleLabel(value: String?): String = when (value) {
    "compact" -> stringResource(R.string.appearance_text_compact)
    "large" -> stringResource(R.string.appearance_text_large)
    else -> stringResource(R.string.appearance_text_normal)
}
