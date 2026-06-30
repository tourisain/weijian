package com.tourisain.weijian.presentation.update

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import com.tourisain.weijian.BuildConfig
import com.tourisain.weijian.R
import com.tourisain.weijian.presentation.common.AppleAlertDialog
import com.tourisain.weijian.presentation.note.components.AppleNotesStyle

@Composable
fun LocalReleaseNotesDialog(
    onDismiss: () -> Unit
) {
    val releaseNotes = localReleaseNotesFor(
        versionCode = BuildConfig.VERSION_CODE,
        language = Locale.current.language
    )
    AppleAlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.whats_new_title, releaseNotes.versionName))
        },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.whats_new_intro, releaseNotes.versionName),
                        color = AppleNotesStyle.PrimaryText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    releaseNotes.notes.forEach { note ->
                        Text(
                            text = "- $note",
                            color = AppleNotesStyle.SecondaryText,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.whats_new_done), color = AppleNotesStyle.Accent)
            }
        }
    )
}
