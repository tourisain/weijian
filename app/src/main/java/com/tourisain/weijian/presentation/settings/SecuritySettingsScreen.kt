package com.tourisain.weijian.presentation.settings

import androidx.compose.foundation.text.KeyboardOptions
import com.tourisain.weijian.presentation.common.AppleAlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tourisain.weijian.R
import com.tourisain.weijian.presentation.icons.Lucide
import com.tourisain.weijian.presentation.note.components.AppleNotesStyle

@Composable
fun SecuritySettingsScreen(
    navController: NavController,
    viewModel: SettingsPreferencesViewModel = hiltViewModel()
) {
    val privacyModeEnabled by viewModel.privacyModeEnabled.collectAsStateWithLifecycle()
    val privacyPassword by viewModel.privacyPassword.collectAsStateWithLifecycle()
    var showPasswordDialog by remember { mutableStateOf(false) }

    SimpleSettingsPage(navController, stringResource(R.string.security)) {
        SettingsGroup {
            SettingItem(
                icon = Lucide.Lock,
                title = stringResource(R.string.security_privacy_lock),
                subtitle = if (privacyModeEnabled) {
                    stringResource(R.string.privacy_lock_active)
                } else {
                    stringResource(R.string.privacy_lock_inactive)
                },
                trailingContent = {
                    Switch(
                        checked = privacyModeEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled) {
                                showPasswordDialog = true
                            } else {
                                viewModel.disablePrivacyMode()
                            }
                        }
                    )
                }
            )
            SettingsDivider()
            SettingItem(
                icon = Lucide.Key,
                title = if (privacyPassword.isNullOrBlank()) {
                    stringResource(R.string.security_set_password)
                } else {
                    stringResource(R.string.security_change_password)
                },
                subtitle = stringResource(R.string.security_pin_digits),
                onClick = { showPasswordDialog = true }
            )
        }

        SettingsGroup {
            SettingItem(
                icon = Lucide.Shield,
                title = stringResource(R.string.security_local_first),
                subtitle = stringResource(R.string.security_local_first_desc),
                trailingContent = { Text(stringResource(R.string.enabled)) }
            )
            SettingsDivider()
            SettingItem(
                icon = Lucide.Key,
                title = stringResource(R.string.security_membership_encrypted),
                subtitle = stringResource(R.string.security_membership_encrypted_desc),
                trailingContent = { Text(stringResource(R.string.enabled)) }
            )
            SettingsDivider()
            SettingItem(
                icon = Lucide.EyeOff,
                title = stringResource(R.string.security_privacy_state),
                subtitle = if (privacyModeEnabled) {
                    stringResource(R.string.security_requires_verification)
                } else {
                    stringResource(R.string.security_lock_off)
                },
                trailingContent = {
                    Text(
                        if (privacyModeEnabled) {
                            stringResource(R.string.protected_state)
                        } else {
                            stringResource(R.string.off)
                        }
                    )
                }
            )
        }
    }

    if (showPasswordDialog) {
        PasswordDialog(
            onDismiss = { showPasswordDialog = false },
            onConfirm = {
                viewModel.setPrivacyPassword(it)
                showPasswordDialog = false
            }
        )
    }
}

@Composable
private fun PasswordDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var password by remember { mutableStateOf("") }
    val valid = password.length in 4..6
    AppleAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.set_privacy_password)) },
        text = {
            OutlinedTextField(
                value = password,
                onValueChange = { value ->
                    if (value.length <= 6 && value.all { it.isDigit() }) password = value
                },
                singleLine = true,
                label = { Text(stringResource(R.string.security_pin_digits)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                visualTransformation = PasswordVisualTransformation(),
                shape = AppleNotesStyle.SearchShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppleNotesStyle.Accent,
                    unfocusedBorderColor = AppleNotesStyle.Separator,
                    cursorColor = AppleNotesStyle.Accent,
                    focusedLabelColor = AppleNotesStyle.Accent
                )
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(password) }, enabled = valid) {
                Text(stringResource(R.string.save), color = AppleNotesStyle.Accent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
