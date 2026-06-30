package com.tourisain.weijian.presentation.recyclebin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import com.tourisain.weijian.presentation.common.AppleAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tourisain.weijian.R
import com.tourisain.weijian.data.database.entity.AccountRecordEntity
import com.tourisain.weijian.presentation.common.popBackStackStable
import com.tourisain.weijian.presentation.icons.Lucide
import com.tourisain.weijian.presentation.navigation.Screen
import com.tourisain.weijian.presentation.note.components.AppleNotesStyle
import java.text.DateFormat
import java.text.NumberFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecycleBinScreen(
    navController: NavController,
    viewModel: RecycleBinViewModel = hiltViewModel()
) {
    val deletedNotes by viewModel.deletedNotes.collectAsStateWithLifecycle()
    val deletedAccountRecords by viewModel.deletedAccountRecords.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val totalItems = deletedNotes.size + deletedAccountRecords.size
    var showClearConfirm by remember { mutableStateOf(false) }
    val dateFormatter = remember { DateFormat.getDateInstance(DateFormat.MEDIUM) }

    LaunchedEffect(error) {
        val currentError = error
        if (!currentError.isNullOrBlank()) {
            snackbarHostState.showSnackbar(currentError)
            viewModel.clearError()
        }
    }

    Scaffold(
        containerColor = AppleNotesStyle.Background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStackStable(Screen.Settings.route) }) {
                        Icon(Lucide.ChevronLeft, contentDescription = stringResource(R.string.back), tint = AppleNotesStyle.Accent)
                    }
                },
                actions = {
                    if (totalItems > 0) {
                        TextButton(onClick = { showClearConfirm = true }) {
                            Text(stringResource(R.string.clear_recycle_bin), color = AppleNotesStyle.Destructive)
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
                        text = stringResource(R.string.recycle_bin),
                        color = AppleNotesStyle.PrimaryText,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.recycle_bin_count, totalItems),
                        color = AppleNotesStyle.SecondaryText,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            if (totalItems == 0) {
                item { EmptyRecycleBin() }
            } else {
                if (deletedNotes.isNotEmpty()) {
                    item { SectionHeader("${stringResource(R.string.notes_title)} (${deletedNotes.size})") }
                    item {
                        DeletedGroup {
                            deletedNotes.forEachIndexed { index, note ->
                                val deletedAt = note.deletedAt ?: note.createdAt
                                DeletedItemRow(
                                    icon = Lucide.StickyNote,
                                    title = note.title.ifBlank { stringResource(R.string.no_title) },
                                    subtitle = stringResource(R.string.deleted_at, dateFormatter.format(Date(deletedAt))),
                                    onRestore = { viewModel.restoreNote(note) },
                                    onDelete = { viewModel.permanentDeleteNote(note) }
                                )
                                if (index != deletedNotes.lastIndex) DeletedDivider()
                            }
                        }
                    }
                }

                if (deletedAccountRecords.isNotEmpty()) {
                    item { SectionHeader("${stringResource(R.string.accounting)} (${deletedAccountRecords.size})") }
                    item {
                        DeletedGroup {
                            deletedAccountRecords.forEachIndexed { index, record ->
                                DeletedItemRow(
                                    icon = Lucide.Wallet,
                                    title = deletedAccountTitle(record, stringResource(R.string.uncategorized)),
                                    subtitle = stringResource(
                                        R.string.deleted_at,
                                        dateFormatter.format(Date(record.deletedAt ?: record.createdAt))
                                    ),
                                    onRestore = { viewModel.restoreAccountRecord(record) },
                                    onDelete = { viewModel.permanentDeleteAccountRecord(record) }
                                )
                                if (index != deletedAccountRecords.lastIndex) DeletedDivider()
                            }
                        }
                    }
                }
            }
        }
    }

    if (showClearConfirm) {
        AppleAlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text(stringResource(R.string.recycle_bin)) },
            text = { Text(stringResource(R.string.clear_recycle_bin_message)) },
            confirmButton = {
                TextButton(onClick = { showClearConfirm = false; viewModel.clearAll() }) {
                    Text(stringResource(R.string.delete), color = AppleNotesStyle.Destructive)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(start = 4.dp),
        color = AppleNotesStyle.SecondaryText,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun DeletedGroup(content: @Composable () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), color = AppleNotesStyle.Surface, shape = AppleNotesStyle.GroupShape) {
        Column(content = { content() })
    }
}

@Composable
private fun DeletedDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 60.dp),
        color = AppleNotesStyle.Separator,
        thickness = 1.dp
    )
}

@Composable
private fun DeletedItemRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onRestore: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(color = AppleNotesStyle.AccentSoft, shape = CircleShape) {
            Box(modifier = Modifier.size(34.dp), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = AppleNotesStyle.Accent, modifier = Modifier.size(19.dp))
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, color = AppleNotesStyle.PrimaryText, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (subtitle.isNotBlank()) {
                Text(subtitle.take(96), color = AppleNotesStyle.SecondaryText, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        TextButton(onClick = onRestore) {
            Text(stringResource(R.string.restore), color = AppleNotesStyle.Accent)
        }
        TextButton(onClick = onDelete) {
            Text(stringResource(R.string.delete), color = AppleNotesStyle.Destructive)
        }
    }
}

@Composable
private fun EmptyRecycleBin() {
    Surface(color = AppleNotesStyle.Surface, shape = AppleNotesStyle.GroupShape) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Lucide.Trash, contentDescription = null, tint = AppleNotesStyle.Accent, modifier = Modifier.size(36.dp))
            Text(stringResource(R.string.recycle_bin_empty), color = AppleNotesStyle.SecondaryText)
        }
    }
}

private fun deletedAccountTitle(record: AccountRecordEntity, uncategorized: String): String {
    val amount = NumberFormat.getCurrencyInstance(Locale.getDefault()).format(record.amount)
    val prefix = if (record.type == "income") "+" else "-"
    val category = record.category.ifBlank { uncategorized }
    return "$category  $prefix$amount"
}
