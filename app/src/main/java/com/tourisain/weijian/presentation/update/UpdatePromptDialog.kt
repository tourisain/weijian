package com.tourisain.weijian.presentation.update

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tourisain.weijian.BuildConfig
import com.tourisain.weijian.R
import com.tourisain.weijian.presentation.common.AppleAlertDialog
import com.tourisain.weijian.presentation.note.components.AppleNotesStyle
import com.tourisain.weijian.util.RemoteUpdateInfo

@Composable
fun UpdatePromptDialog(
    info: RemoteUpdateInfo,
    onUpdate: () -> Unit,
    onLater: () -> Unit
) {
    AppleAlertDialog(
        onDismissRequest = onLater,
        title = { Text(stringResource(R.string.update_available_title)) },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(
                            R.string.update_version_summary,
                            BuildConfig.VERSION_NAME,
                            info.latestVersionName
                        ),
                        color = AppleNotesStyle.PrimaryText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (info.message.isNotBlank()) {
                        Text(info.message, color = AppleNotesStyle.SecondaryText)
                    }
                    if (info.releaseNotes.isNotEmpty()) {
                        Text(
                            stringResource(R.string.update_release_notes),
                            color = AppleNotesStyle.PrimaryText,
                            fontWeight = FontWeight.SemiBold
                        )
                        info.releaseNotes.forEach { note ->
                            Text("- $note", color = AppleNotesStyle.SecondaryText)
                        }
                    }
                    if (info.publishedAt.isNotBlank()) {
                        Text(
                            stringResource(R.string.update_published_at, info.publishedAt),
                            color = AppleNotesStyle.TertiaryText,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onUpdate) {
                Text(stringResource(R.string.update_now), color = AppleNotesStyle.Accent)
            }
        },
        dismissButton = {
            TextButton(onClick = onLater) {
                Text(stringResource(R.string.update_later))
            }
        }
    )
}
