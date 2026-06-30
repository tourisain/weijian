package com.tourisain.weijian.presentation.common

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tourisain.weijian.R
import com.tourisain.weijian.presentation.note.components.AppleNotesStyle
import com.tourisain.weijian.util.ErrorReporter

@Composable
fun ErrorBoundary(
    modifier: Modifier = Modifier,
    fallback: @Composable (Throwable, () -> Unit) -> Unit = { throwable, retry ->
        val context = LocalContext.current
        LaunchedEffect(throwable) {
            ErrorReporter.reportException(context, throwable)
        }
        val fallbackMessage = throwable.message ?: stringResource(R.string.page_error_message)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.page_error_title),
                    color = AppleNotesStyle.PrimaryText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = fallbackMessage,
                    color = AppleNotesStyle.SecondaryText,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(18.dp))
                Button(onClick = retry) {
                    Text(stringResource(R.string.page_error_retry))
                }
            }
        }
    },
    content: @Composable () -> Unit
) {
    val errorState = remember { mutableStateOf<Throwable?>(null) }
    val error = errorState.value
    if (error == null) {
        Box(modifier = modifier) { content() }
    } else {
        fallback(error) { errorState.value = null }
    }
}

inline fun safeExecute(
    context: Context,
    onError: (Throwable) -> Unit = {
        ErrorReporter.reportException(context, it)
        Toast.makeText(context, it.message ?: "Error", Toast.LENGTH_SHORT).show()
    },
    block: () -> Unit
) {
    try {
        block()
    } catch (throwable: Throwable) {
        onError(throwable)
    }
}
