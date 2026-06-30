package com.tourisain.weijian.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tourisain.weijian.R
import com.tourisain.weijian.presentation.common.AppleAlertDialog
import com.tourisain.weijian.presentation.common.PermissionDisclosureScenario
import com.tourisain.weijian.presentation.common.permissionDisclosureFor
import com.tourisain.weijian.presentation.icons.Lucide
import com.tourisain.weijian.presentation.note.components.AppleNotesStyle

@Composable
fun BehaviorSettingsScreen(
    navController: NavController,
    viewModel: SettingsPreferencesViewModel = hiltViewModel()
) {
    val confirmBeforeExit by viewModel.confirmBeforeExit.collectAsStateWithLifecycle()
    val smartReminders by viewModel.smartReminders.collectAsStateWithLifecycle()
    val smartCategorization by viewModel.smartCategorization.collectAsStateWithLifecycle()
    val smartSuggestions by viewModel.smartSuggestions.collectAsStateWithLifecycle()
    var showReminderDisclosure by remember { mutableStateOf(false) }
    val notificationDisclosure = permissionDisclosureFor(PermissionDisclosureScenario.Notification)
    val exactAlarmDisclosure = permissionDisclosureFor(PermissionDisclosureScenario.ExactAlarm)

    SimpleSettingsPage(navController, stringResource(R.string.behavior)) {
        SettingsGroup {
            SettingItem(
                icon = Lucide.Check,
                title = stringResource(R.string.behavior_confirm_exit),
                subtitle = stringResource(R.string.behavior_confirm_exit_desc),
                trailingContent = {
                    Switch(checked = confirmBeforeExit == true, onCheckedChange = viewModel::setConfirmBeforeExit)
                }
            )
        }

        SettingsGroup {
            SettingItem(
                icon = Lucide.Bell,
                title = stringResource(R.string.behavior_smart_reminders),
                subtitle = stringResource(R.string.behavior_smart_reminders_desc),
                trailingContent = {
                    Switch(
                        checked = smartReminders,
                        onCheckedChange = { checked ->
                            if (checked) {
                                showReminderDisclosure = true
                            } else {
                                viewModel.setSmartReminders(false)
                            }
                        }
                    )
                }
            )
            SettingsDivider()
            SettingItem(
                icon = Lucide.Tag,
                title = stringResource(R.string.behavior_smart_categorization),
                subtitle = stringResource(R.string.behavior_smart_categorization_desc),
                trailingContent = {
                    Switch(checked = smartCategorization, onCheckedChange = viewModel::setSmartCategorization)
                }
            )
            SettingsDivider()
            SettingItem(
                icon = Lucide.Sparkles,
                title = stringResource(R.string.behavior_smart_suggestions),
                subtitle = stringResource(R.string.behavior_smart_suggestions_desc),
                trailingContent = {
                    Switch(checked = smartSuggestions, onCheckedChange = viewModel::setSmartSuggestions)
                }
            )
        }
    }

    if (showReminderDisclosure) {
        AppleAlertDialog(
            onDismissRequest = { showReminderDisclosure = false },
            title = { Text(notificationDisclosure.title) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(notificationDisclosure.message, color = AppleNotesStyle.SecondaryText)
                    Text(exactAlarmDisclosure.message, color = AppleNotesStyle.SecondaryText)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showReminderDisclosure = false
                        viewModel.setSmartReminders(true)
                    }
                ) {
                    Text(notificationDisclosure.confirmLabel, color = AppleNotesStyle.Accent)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showReminderDisclosure = false
                        viewModel.setSmartReminders(false)
                    }
                ) {
                    Text(notificationDisclosure.dismissLabel)
                }
            }
        )
    }
}
