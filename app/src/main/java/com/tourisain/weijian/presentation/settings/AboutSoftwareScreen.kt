package com.tourisain.weijian.presentation.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.tourisain.weijian.BuildConfig
import com.tourisain.weijian.R
import com.tourisain.weijian.presentation.common.AppleAlertDialog
import com.tourisain.weijian.presentation.common.navigateStable
import com.tourisain.weijian.presentation.icons.Lucide
import com.tourisain.weijian.presentation.navigation.Screen
import com.tourisain.weijian.presentation.note.components.AppleNotesStyle
import com.tourisain.weijian.presentation.update.UpdatePromptDialog
import com.tourisain.weijian.util.RemoteUpdateChecker
import com.tourisain.weijian.util.RemoteUpdateInfo
import com.tourisain.weijian.util.RemoteUpdateResult
import kotlinx.coroutines.launch

@Composable
fun AboutSoftwareScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isCheckingUpdate by remember { mutableStateOf(false) }
    var availableUpdate by remember { mutableStateOf<RemoteUpdateInfo?>(null) }
    var updateMessageRes by remember { mutableIntStateOf(0) }

    SimpleSettingsPage(navController, stringResource(R.string.about_weijian)) {
        SettingsGroup {
            SettingItem(
                icon = Lucide.Info,
                title = stringResource(R.string.app_name),
                subtitle = stringResource(R.string.weijian_slogan),
                trailingContent = {}
            )
            SettingsDivider()
            SettingItem(
                icon = Lucide.Package,
                title = stringResource(R.string.app_version),
                subtitle = BuildConfig.VERSION_NAME,
                trailingContent = {}
            )
            SettingsDivider()
            SettingItem(
                icon = Lucide.User,
                title = stringResource(R.string.author_info),
                subtitle = stringResource(R.string.record_filing_info),
                trailingContent = {}
            )
        }

        SettingsGroup {
            SettingItem(
                icon = Lucide.RefreshCw,
                title = stringResource(R.string.check_update),
                subtitle = if (isCheckingUpdate) {
                    stringResource(R.string.checking_update)
                } else {
                    stringResource(R.string.update_check_subtitle, BuildConfig.VERSION_NAME)
                },
                onClick = {
                    if (!isCheckingUpdate) {
                        isCheckingUpdate = true
                        scope.launch {
                            when (val result = RemoteUpdateChecker.checkForUpdate()) {
                                is RemoteUpdateResult.Available -> availableUpdate = result.info
                                RemoteUpdateResult.Disabled -> updateMessageRes = R.string.update_check_disabled
                                is RemoteUpdateResult.Error -> updateMessageRes = R.string.update_check_failed
                                RemoteUpdateResult.Latest -> updateMessageRes = R.string.already_latest_version
                            }
                            isCheckingUpdate = false
                        }
                    }
                }
            )
            SettingsDivider()
            SettingItem(
                icon = Lucide.Globe,
                title = stringResource(R.string.official_websites),
                subtitle = stringResource(R.string.official_websites_subtitle),
                onClick = { navController.navigateStable(Screen.OfficialWebsites.route) }
            )
            SettingsDivider()
            SettingItem(
                icon = Lucide.Mail,
                title = stringResource(R.string.user_feedback),
                subtitle = stringResource(R.string.user_feedback_subtitle),
                onClick = { openFeedbackEmail(context) }
            )
            SettingsDivider()
            SettingItem(
                icon = Lucide.Shield,
                title = stringResource(R.string.about_privacy),
                subtitle = stringResource(R.string.about_privacy_subtitle),
                onClick = { navController.navigateStable(Screen.ComplianceCenter.route) }
            )
        }

        SettingsGroup {
            SettingItem(
                icon = Lucide.Smartphone,
                title = stringResource(R.string.about_phone),
                subtitle = "${Build.MANUFACTURER} ${Build.MODEL} · Android ${Build.VERSION.RELEASE}",
                trailingContent = {}
            )
        }
    }

    availableUpdate?.let { info ->
        UpdatePromptDialog(
            info = info,
            onUpdate = {
                if (!RemoteUpdateChecker.openDownloadPage(context, info)) {
                    Toast.makeText(context, context.getString(R.string.open_website_failed), Toast.LENGTH_SHORT).show()
                }
                availableUpdate = null
            },
            onLater = { availableUpdate = null }
        )
    }

    if (updateMessageRes != 0) {
        AppleAlertDialog(
            onDismissRequest = { updateMessageRes = 0 },
            title = { Text(stringResource(R.string.check_update)) },
            text = { Text(stringResource(updateMessageRes)) },
            confirmButton = {
                TextButton(onClick = { updateMessageRes = 0 }) {
                    Text(stringResource(R.string.close), color = AppleNotesStyle.Accent)
                }
            }
        )
    }
}

internal data class FeedbackEmailChannel(
    val recipients: List<String>
) {
    val mailtoUri: String = recipients.joinToString(separator = ",", prefix = "mailto:")
}

internal fun feedbackEmailChannel(): FeedbackEmailChannel {
    return FeedbackEmailChannel(
        recipients = listOf("tourisain@163.com", "grllq458@gmail.com")
    )
}

internal fun feedbackEmailBody(
    baseBody: String,
    versionName: String,
    versionCode: Int,
    manufacturer: String,
    model: String,
    androidVersion: String
): String {
    return buildString {
        append(baseBody.trimEnd())
        append("\n\n---\n")
        append("Version: ")
        append(versionName)
        append(" (")
        append(versionCode)
        append(")\nDevice: ")
        append(manufacturer)
        append(' ')
        append(model)
        append("\nAndroid: ")
        append(androidVersion)
    }
}

private fun openFeedbackEmail(context: Context) {
    val channel = feedbackEmailChannel()
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse(channel.mailtoUri)
        putExtra(Intent.EXTRA_EMAIL, channel.recipients.toTypedArray())
        putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.user_feedback_email_subject))
        putExtra(
            Intent.EXTRA_TEXT,
            feedbackEmailBody(
                baseBody = context.getString(R.string.user_feedback_email_body),
                versionName = BuildConfig.VERSION_NAME,
                versionCode = BuildConfig.VERSION_CODE,
                manufacturer = Build.MANUFACTURER,
                model = Build.MODEL,
                androidVersion = Build.VERSION.RELEASE
            )
        )
    }
    runCatching {
        context.startActivity(intent)
    }.onFailure {
        Toast.makeText(context, context.getString(R.string.user_feedback_no_email_app), Toast.LENGTH_LONG).show()
    }
}
