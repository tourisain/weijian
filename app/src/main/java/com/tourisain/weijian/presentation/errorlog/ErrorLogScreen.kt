package com.tourisain.weijian.presentation.errorlog

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import com.tourisain.weijian.presentation.common.AppleAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.tourisain.weijian.R
import com.tourisain.weijian.presentation.common.popBackStackStable
import com.tourisain.weijian.presentation.icons.Lucide
import com.tourisain.weijian.presentation.navigation.Screen
import com.tourisain.weijian.presentation.note.components.AppleNotesStyle
import com.tourisain.weijian.util.ErrorLogger
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ErrorLogScreen(navController: NavController) {
    val context = LocalContext.current
    var logFiles by remember { mutableStateOf(ErrorLogger.getLogFiles(context).sortedByDescending { it.lastModified() }) }
    var selectedLogFile by remember { mutableStateOf<File?>(null) }
    var logContent by remember { mutableStateOf("") }
    var showClearConfirm by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = AppleNotesStyle.Background,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStackStable(Screen.Settings.route) }) {
                        Icon(Lucide.ChevronLeft, contentDescription = stringResource(R.string.back), tint = AppleNotesStyle.Accent)
                    }
                },
                actions = {
                    if (logFiles.isNotEmpty()) {
                        IconButton(onClick = { showClearConfirm = true }) {
                            Icon(Lucide.Trash2, contentDescription = stringResource(R.string.delete), tint = AppleNotesStyle.Destructive)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppleNotesStyle.Background)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(AppleNotesStyle.Background)
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = stringResource(R.string.error_log_list),
                        color = AppleNotesStyle.PrimaryText,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(stringResource(R.string.error_log_count, logFiles.size), color = AppleNotesStyle.SecondaryText)
                }
            }

            if (logFiles.isEmpty()) {
                item { EmptyLogState() }
            } else {
                item {
                    Surface(modifier = Modifier.fillMaxWidth(), color = AppleNotesStyle.Surface, shape = AppleNotesStyle.GroupShape) {
                        Column {
                            logFiles.forEachIndexed { index, file ->
                                LogFileRow(
                                    file = file,
                                    onClick = {
                                        selectedLogFile = file
                                        logContent = ErrorLogger.readLogFile(file)
                                    }
                                )
                                if (index != logFiles.lastIndex) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(start = 60.dp),
                                        color = AppleNotesStyle.Separator,
                                        thickness = 1.dp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    selectedLogFile?.let { file ->
        LogDetailDialog(
            file = file,
            content = logContent,
            onDismiss = {
                selectedLogFile = null
                logContent = ""
            },
            onShare = { shareLogFile(context, file) }
        )
    }

    if (showClearConfirm) {
        AppleAlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text(stringResource(R.string.error_log_collection)) },
            text = { Text(stringResource(R.string.clear_error_logs_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    ErrorLogger.clearAllLogs(context)
                    logFiles = emptyList()
                    selectedLogFile = null
                    logContent = ""
                    showClearConfirm = false
                }) { Text(stringResource(R.string.delete), color = AppleNotesStyle.Destructive) }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
}

@Composable
private fun LogFileRow(file: File, onClick: () -> Unit) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }
    val lastModified = dateFormat.format(Date(file.lastModified()))
    val fileSize = file.length() / 1024
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(color = AppleNotesStyle.AccentSoft, shape = CircleShape) {
            Box(modifier = Modifier.size(34.dp), contentAlignment = Alignment.Center) {
                Icon(Lucide.FileText, contentDescription = null, tint = AppleNotesStyle.Accent, modifier = Modifier.size(19.dp))
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(file.name, color = AppleNotesStyle.PrimaryText, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("$lastModified · ${fileSize}KB", color = AppleNotesStyle.SecondaryText, style = MaterialTheme.typography.bodySmall)
        }
        Icon(Lucide.ChevronRight, contentDescription = null, tint = AppleNotesStyle.TertiaryText, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun LogDetailDialog(file: File, content: String, onDismiss: () -> Unit, onShare: () -> Unit) {
    AppleAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(file.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        text = {
            Surface(color = AppleNotesStyle.SearchSurface, shape = AppleNotesStyle.SearchShape) {
                Text(
                    text = content.ifBlank { stringResource(R.string.no_error_logs) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(12.dp),
                    color = AppleNotesStyle.PrimaryText,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onShare) {
                Text(stringResource(R.string.share_log_file), color = AppleNotesStyle.Accent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) }
        }
    )
}

@Composable
private fun EmptyLogState() {
    Surface(modifier = Modifier.fillMaxWidth(), color = AppleNotesStyle.Surface, shape = AppleNotesStyle.GroupShape) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Lucide.FileText, contentDescription = null, tint = AppleNotesStyle.Accent, modifier = Modifier.size(36.dp))
            Text(stringResource(R.string.no_error_logs), color = AppleNotesStyle.SecondaryText)
        }
    }
}

private fun shareLogFile(context: Context, file: File) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.error_log_share_subject, file.name))
        putExtra(Intent.EXTRA_TEXT, ErrorLogger.readLogFile(file))
    }
    context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_log_file)))
}
