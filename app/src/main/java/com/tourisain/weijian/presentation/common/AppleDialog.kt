package com.tourisain.weijian.presentation.common

import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.tourisain.weijian.presentation.note.components.AppleNotesStyle

@Composable
fun AppleAlertDialog(
    onDismissRequest: () -> Unit,
    title: @Composable (() -> Unit)? = null,
    text: @Composable (() -> Unit)? = null,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: @Composable (() -> Unit)? = null,
    properties: DialogProperties = DialogProperties()
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = confirmButton,
        modifier = modifier.heightIn(max = AppleNotesStyle.DialogMaxHeightDp.dp),
        dismissButton = dismissButton,
        title = title,
        text = text,
        shape = AppleNotesStyle.GroupShape,
        containerColor = AppleNotesStyle.Surface,
        titleContentColor = AppleNotesStyle.PrimaryText,
        textContentColor = AppleNotesStyle.SecondaryText,
        tonalElevation = 0.dp,
        properties = properties
    )
}
