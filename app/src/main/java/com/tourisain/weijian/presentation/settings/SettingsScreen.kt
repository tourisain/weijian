package com.tourisain.weijian.presentation.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.tourisain.weijian.R
import com.tourisain.weijian.presentation.common.navigateStable
import com.tourisain.weijian.presentation.icons.Lucide
import com.tourisain.weijian.presentation.navigation.Screen

@Composable
fun SettingsScreen(navController: NavController) {
    SimpleSettingsPage(navController, stringResource(R.string.settings_title)) {
        SettingsGroup {
            SettingItem(
                icon = Lucide.User,
                title = stringResource(R.string.profile),
                subtitle = stringResource(R.string.account_settings),
                onClick = { navController.navigateStable(Screen.Profile.route) }
            )
            SettingsDivider()
            SettingItem(
                icon = Lucide.Star,
                title = stringResource(R.string.membership_center),
                subtitle = stringResource(R.string.member_features),
                onClick = { navController.navigateStable(Screen.Membership.route) }
            )
        }

        SettingsGroup {
            SettingItem(
                icon = Lucide.Folder,
                title = stringResource(R.string.note_folder_management),
                subtitle = stringResource(R.string.manage_note_folders),
                onClick = { navController.navigateStable(Screen.CategoryManagement.route) }
            )
            SettingsDivider()
            SettingItem(
                icon = Lucide.Cloud,
                title = stringResource(R.string.backup_restore),
                subtitle = stringResource(R.string.restore_from_backup),
                onClick = { navController.navigateStable(Screen.Backup.route) }
            )
            SettingsDivider()
            SettingItem(
                icon = Lucide.Trash,
                title = stringResource(R.string.recycle_bin),
                subtitle = stringResource(R.string.restore_data),
                onClick = { navController.navigateStable(Screen.RecycleBin.route) }
            )
        }

        SettingsGroup {
            SettingItem(
                icon = Lucide.Palette,
                title = stringResource(R.string.appearance),
                subtitle = stringResource(R.string.appearance_settings_subtitle),
                onClick = { navController.navigateStable(Screen.AppearanceSettings.route) }
            )
            SettingsDivider()
            SettingItem(
                icon = Lucide.Sparkles,
                title = stringResource(R.string.behavior),
                subtitle = stringResource(R.string.behavior_settings_subtitle),
                onClick = { navController.navigateStable(Screen.BehaviorSettings.route) }
            )
            SettingsDivider()
            SettingItem(
                icon = Lucide.Lock,
                title = stringResource(R.string.security),
                subtitle = stringResource(R.string.security_settings_subtitle),
                onClick = { navController.navigateStable(Screen.SecuritySettings.route) }
            )
            SettingsDivider()
            SettingItem(
                icon = Lucide.LayoutGrid,
                title = stringResource(R.string.app_layout_settings),
                subtitle = stringResource(R.string.app_settings),
                onClick = { navController.navigateStable(Screen.AppLayoutSettings.route) }
            )
            SettingsDivider()
            SettingItem(
                icon = Lucide.Globe,
                title = stringResource(R.string.language_settings),
                subtitle = stringResource(R.string.change_app_language),
                onClick = { navController.navigateStable(Screen.LanguageSettings.route) }
            )
        }

        SettingsGroup {
            SettingItem(
                icon = Lucide.Smartphone,
                title = stringResource(R.string.about_phone),
                subtitle = stringResource(R.string.about_phone_subtitle),
                onClick = { navController.navigateStable(Screen.AboutSoftware.route) }
            )
        }
    }
}

@Composable
fun SettingsItem(
    title: String,
    subtitle: String = "",
    onClick: () -> Unit = {}
) {
    SettingItem(title = title, subtitle = subtitle, onClick = onClick)
}

@Composable
fun SettingCard(content: @Composable () -> Unit) {
    SettingsGroup { content() }
}

@Composable
fun CreateMenuItem(title: String, onClick: () -> Unit = {}) {
    SettingItem(title = title, onClick = onClick)
}
