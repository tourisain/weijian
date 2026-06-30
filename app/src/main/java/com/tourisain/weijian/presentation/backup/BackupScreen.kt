package com.tourisain.weijian.presentation.backup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.text.KeyboardOptions
import com.tourisain.weijian.presentation.common.AppleAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tourisain.weijian.R
import com.tourisain.weijian.data.repository.LocalBackupFile
import com.tourisain.weijian.presentation.common.PermissionDisclosureScenario
import com.tourisain.weijian.presentation.common.navigateStable
import com.tourisain.weijian.presentation.common.permissionDisclosureFor
import com.tourisain.weijian.presentation.common.popBackStackStable
import com.tourisain.weijian.presentation.icons.Lucide
import com.tourisain.weijian.presentation.navigation.Screen
import com.tourisain.weijian.presentation.note.components.AppleNotesStyle
import com.tourisain.weijian.presentation.settings.SettingsDivider
import com.tourisain.weijian.presentation.settings.SettingsGroup
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    navController: NavController,
    viewModel: BackupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val webDavUrl by viewModel.webDavUrl.collectAsStateWithLifecycle()
    val webDavUser by viewModel.webDavUser.collectAsStateWithLifecycle()
    val webDavPass by viewModel.webDavPass.collectAsStateWithLifecycle()
    val isAutoBackupEnabled by viewModel.isAutoBackupEnabled.collectAsStateWithLifecycle()
    val isPro by viewModel.isPro.collectAsStateWithLifecycle()
    val backupProgress by viewModel.backupProgress.collectAsStateWithLifecycle()
    val webDavStatus by viewModel.webDavStatus.collectAsStateWithLifecycle()
    val localBackups by viewModel.localBackups.collectAsStateWithLifecycle()
    val restorePreview by viewModel.restorePreview.collectAsStateWithLifecycle()
    val backupHealth = remember(localBackups, webDavStatus, isAutoBackupEnabled) {
        backupHealthState(
            localBackupCount = localBackups.size,
            latestLocalBackupAt = localBackups.maxOfOrNull { it.lastModified } ?: 0L,
            webDavStatus = webDavStatus,
            isAutoBackupEnabled = isAutoBackupEnabled
        )
    }
    val fileImportDisclosure = permissionDisclosureFor(PermissionDisclosureScenario.FileImport)
    val fileExportDisclosure = permissionDisclosureFor(PermissionDisclosureScenario.FileExport)
    val webDavDisclosure = permissionDisclosureFor(PermissionDisclosureScenario.WebDavNetwork)

    var showPasswordDialog by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var showWebDavDialog by remember { mutableStateOf(false) }
    var showWebDavRestoreDialog by remember { mutableStateOf(false) }
    var showSaveFileAccessDialog by remember { mutableStateOf(false) }
    var localBackupToRestore by remember { mutableStateOf<LocalBackupFile?>(null) }
    var localBackupToDelete by remember { mutableStateOf<LocalBackupFile?>(null) }
    var password by remember { mutableStateOf("") }
    var restorePassword by remember { mutableStateOf("") }
    var webDavRestorePassword by remember { mutableStateOf("") }
    var localRestorePassword by remember { mutableStateOf("") }

    val restoreLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.restoreBackup(it, restorePassword.ifBlank { null }) }
    }
    val saveBackupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        uri?.let { viewModel.saveCurrentBackup(it) }
    }

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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppleNotesStyle.Background)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(AppleNotesStyle.Background)
                .padding(paddingValues),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = stringResource(R.string.backup_restore),
                    color = AppleNotesStyle.PrimaryText,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold
                )
            }
            item {
                BackupTrustCard()
            }
            item {
                BackupHealthCard(backupHealth)
            }
            if (uiState != BackupUiState.Idle) {
                item {
                    BackupStatusCard(
                        state = uiState,
                        progress = backupProgress
                    )
                }
            }
            if (!isPro) {
                item {
                    BackupMemberRequiredCard(
                        onOpenMembership = { navController.navigateStable(Screen.Membership.route) }
                    )
                }
            } else {
                item {
                    SettingsGroup {
                        BackupActionRow(
                            icon = Lucide.Download,
                            title = stringResource(R.string.export_data),
                            subtitle = stringResource(R.string.export_all_data),
                            action = stringResource(R.string.backup_action_export),
                            onClick = { showPasswordDialog = true }
                        )
                        SettingsDivider()
                        BackupActionRow(
                            icon = Lucide.Upload,
                            title = stringResource(R.string.restore_data),
                            subtitle = stringResource(R.string.restore_from_backup),
                            action = stringResource(R.string.backup_action_restore),
                            onClick = { showRestoreDialog = true }
                        )
                        if (uiState is BackupUiState.BackupReady) {
                            SettingsDivider()
                            BackupActionRow(
                                icon = Lucide.Save,
                                title = stringResource(R.string.save),
                                subtitle = stringResource(R.string.backup_save_generated_subtitle),
                                action = stringResource(R.string.backup_action_save),
                                onClick = { showSaveFileAccessDialog = true }
                            )
                            SettingsDivider()
                            BackupActionRow(
                                icon = Lucide.Check,
                                title = stringResource(R.string.confirm),
                                subtitle = stringResource(R.string.backup_verify_generated_subtitle),
                                action = stringResource(R.string.backup_action_verify),
                                onClick = { viewModel.verifyBackupFile() }
                            )
                        }
                    }
                }
                item {
                    SettingsGroup {
                        BackupActionRow(
                            icon = Lucide.Save,
                            title = stringResource(R.string.backup_create_local),
                            subtitle = stringResource(R.string.backup_local_history_subtitle),
                            action = stringResource(R.string.backup_action_create),
                            onClick = { viewModel.createLocalBackup() }
                        )
                        SettingsDivider()
                        BackupActionRow(
                            icon = Lucide.Clock,
                            title = stringResource(R.string.backup_local_history),
                            subtitle = if (localBackups.isEmpty()) {
                                stringResource(R.string.backup_no_history)
                            } else {
                                stringResource(R.string.category_notes_count, localBackups.size)
                            },
                            action = stringResource(R.string.refresh),
                            onClick = { viewModel.refreshLocalBackups() }
                        )
                        localBackups.forEach { backup ->
                            SettingsDivider()
                            LocalBackupHistoryRow(
                                backup = backup,
                                onRestore = {
                                    viewModel.clearRestorePreview()
                                    localBackupToRestore = backup
                                },
                                onDelete = { localBackupToDelete = backup }
                            )
                        }
                    }
                }
                item {
                    SettingsGroup {
                        BackupActionRow(
                            icon = Lucide.Cloud,
                            title = "WebDAV",
                            subtitle = webDavStatusSubtitle(
                                status = webDavStatus,
                                url = webDavUrl,
                                isAutoBackupEnabled = isAutoBackupEnabled,
                                labels = WebDavStatusLabels(
                                    notConfigured = stringResource(R.string.backup_webdav_not_configured),
                                    checking = stringResource(R.string.backup_checking_webdav),
                                    configured = stringResource(R.string.backup_webdav_configured),
                                    autoEnabled = stringResource(R.string.backup_auto_enabled),
                                    autoDisabled = stringResource(R.string.backup_auto_disabled)
                                )
                            ),
                            action = stringResource(R.string.backup_action_configure),
                            onClick = { showWebDavDialog = true }
                        )
                        SettingsDivider()
                        BackupActionRow(
                            icon = when (webDavStatus.lastSuccess) {
                                true -> Lucide.Check
                                false -> Lucide.Frown
                                null -> Lucide.RefreshCw
                            },
                            title = stringResource(R.string.backup_webdav_status),
                            subtitle = if (webDavStatus.lastCheckedAt > 0L) {
                                stringResource(R.string.backup_webdav_last_checked)
                            } else {
                                stringResource(R.string.backup_webdav_status_subtitle)
                            },
                            action = if (webDavStatus.isChecking) {
                                stringResource(R.string.backup_checking)
                            } else {
                                stringResource(R.string.backup_webdav_check_now)
                            },
                            onClick = { viewModel.checkWebDavConnectionNow() }
                        )
                        SettingsDivider()
                        BackupActionRow(
                            icon = Lucide.Upload,
                            title = stringResource(R.string.backup_webdav_upload_now),
                            subtitle = stringResource(R.string.backup_webdav_upload_now_subtitle),
                            action = stringResource(R.string.backup_action_upload),
                            onClick = { viewModel.uploadBackupToWebDav() }
                        )
                        SettingsDivider()
                        BackupActionRow(
                            icon = Lucide.Download,
                            title = stringResource(R.string.backup_webdav_restore_latest),
                            subtitle = stringResource(R.string.backup_webdav_restore_latest_subtitle),
                            action = stringResource(R.string.backup_action_restore),
                            onClick = {
                                viewModel.clearRestorePreview()
                                showWebDavRestoreDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    if (showPasswordDialog) {
        AppleAlertDialog(
            onDismissRequest = { showPasswordDialog = false },
            title = { Text(stringResource(R.string.backup_restore)) },
            text = {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(R.string.backup_optional_password)) },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.createBackup(password.ifBlank { null })
                    showPasswordDialog = false
                }) { Text(stringResource(R.string.ok), color = AppleNotesStyle.Accent) }
            },
            dismissButton = {
                TextButton(onClick = { showPasswordDialog = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    if (showRestoreDialog) {
        AppleAlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = { Text(fileImportDisclosure.title) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(fileImportDisclosure.message, color = AppleNotesStyle.SecondaryText)
                    Text(stringResource(R.string.backup_restore_password_hint), color = AppleNotesStyle.SecondaryText)
                    OutlinedTextField(
                        value = restorePassword,
                        onValueChange = { restorePassword = it },
                        label = { Text(stringResource(R.string.backup_password)) },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showRestoreDialog = false
                    restoreLauncher.launch("*/*")
                }) { Text(fileImportDisclosure.confirmLabel, color = AppleNotesStyle.Accent) }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) { Text(fileImportDisclosure.dismissLabel) }
            }
        )
    }

    if (showSaveFileAccessDialog) {
        AppleAlertDialog(
            onDismissRequest = { showSaveFileAccessDialog = false },
            title = { Text(fileExportDisclosure.title) },
            text = { Text(fileExportDisclosure.message, color = AppleNotesStyle.SecondaryText) },
            confirmButton = {
                TextButton(onClick = {
                    showSaveFileAccessDialog = false
                    saveBackupLauncher.launch("weijian-backup-${System.currentTimeMillis()}.bin")
                }) {
                    Text(fileExportDisclosure.confirmLabel, color = AppleNotesStyle.Accent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveFileAccessDialog = false }) {
                    Text(fileExportDisclosure.dismissLabel)
                }
            }
        )
    }

    if (showWebDavRestoreDialog) {
        AppleAlertDialog(
            onDismissRequest = {
                showWebDavRestoreDialog = false
                viewModel.clearRestorePreview()
            },
            title = { Text(stringResource(R.string.backup_webdav_restore_latest)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(webDavDisclosure.message, color = AppleNotesStyle.SecondaryText)
                    Text(stringResource(R.string.backup_restore_password_hint), color = AppleNotesStyle.SecondaryText)
                    OutlinedTextField(
                        value = webDavRestorePassword,
                        onValueChange = { webDavRestorePassword = it },
                        label = { Text(stringResource(R.string.backup_password)) },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )
                    restorePreview?.let { preview ->
                        Surface(color = AppleNotesStyle.AccentSoft, shape = AppleNotesStyle.GroupShape) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("\u6062\u590d\u524d\u9884\u89c8", color = AppleNotesStyle.PrimaryText, fontWeight = FontWeight.SemiBold)
                                backupRestorePreviewSummaryLines(preview).forEach { line ->
                                    Text(line, color = AppleNotesStyle.SecondaryText, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (restorePreview == null) {
                        viewModel.previewLatestWebDavBackup(webDavRestorePassword.ifBlank { null })
                    } else {
                        viewModel.restoreLatestWebDavBackup(webDavRestorePassword.ifBlank { null })
                        showWebDavRestoreDialog = false
                        viewModel.clearRestorePreview()
                    }
                }) {
                    Text(
                        if (restorePreview == null) "\u9884\u89c8" else stringResource(R.string.restore),
                        color = AppleNotesStyle.Accent
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showWebDavRestoreDialog = false
                    viewModel.clearRestorePreview()
                }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    if (showWebDavDialog) {
        var url by remember(showWebDavDialog) { mutableStateOf(webDavUrl) }
        var user by remember(showWebDavDialog) { mutableStateOf(webDavUser) }
        var pass by remember(showWebDavDialog) { mutableStateOf(webDavPass) }
        var enabled by remember(showWebDavDialog) { mutableStateOf(isAutoBackupEnabled) }

        AppleAlertDialog(
            onDismissRequest = { showWebDavDialog = false },
            title = { Text("WebDAV") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(webDavDisclosure.message, color = AppleNotesStyle.SecondaryText)
                    OutlinedTextField(value = url, onValueChange = { url = it }, label = { Text(stringResource(R.string.backup_server_url)) })
                    OutlinedTextField(value = user, onValueChange = { user = it }, label = { Text(stringResource(R.string.backup_username)) })
                    OutlinedTextField(
                        value = pass,
                        onValueChange = { pass = it },
                        label = { Text(stringResource(R.string.backup_password)) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.backup_auto_backup), modifier = Modifier.weight(1f))
                        Switch(checked = enabled, onCheckedChange = { enabled = it })
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.saveWebDavConfig(url, user, pass, enabled)
                    showWebDavDialog = false
                }) { Text(webDavDisclosure.confirmLabel, color = AppleNotesStyle.Accent) }
            },
            dismissButton = {
                TextButton(onClick = { showWebDavDialog = false }) { Text(webDavDisclosure.dismissLabel) }
            }
        )
    }

    localBackupToRestore?.let { backup ->
        AppleAlertDialog(
            onDismissRequest = {
                localBackupToRestore = null
                localRestorePassword = ""
                viewModel.clearRestorePreview()
            },
            title = { Text(stringResource(R.string.restore_data)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(formatLocalBackupMeta(backup), color = AppleNotesStyle.SecondaryText)
                    Text(stringResource(R.string.backup_restore_password_hint), color = AppleNotesStyle.SecondaryText)
                    OutlinedTextField(
                        value = localRestorePassword,
                        onValueChange = { localRestorePassword = it },
                        label = { Text(stringResource(R.string.backup_password)) },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )
                    restorePreview?.let { preview ->
                        Surface(color = AppleNotesStyle.AccentSoft, shape = AppleNotesStyle.GroupShape) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("\u6062\u590d\u524d\u9884\u89c8", color = AppleNotesStyle.PrimaryText, fontWeight = FontWeight.SemiBold)
                                backupRestorePreviewSummaryLines(preview).forEach { line ->
                                    Text(line, color = AppleNotesStyle.SecondaryText, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (restorePreview == null) {
                        viewModel.previewLocalBackup(backup.fileName, localRestorePassword.ifBlank { null })
                    } else {
                        viewModel.restoreLocalBackup(backup.fileName, localRestorePassword.ifBlank { null })
                        localBackupToRestore = null
                        localRestorePassword = ""
                        viewModel.clearRestorePreview()
                    }
                }) {
                    Text(
                        if (restorePreview == null) "\u9884\u89c8" else stringResource(R.string.restore),
                        color = AppleNotesStyle.Accent
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    localBackupToRestore = null
                    localRestorePassword = ""
                    viewModel.clearRestorePreview()
                }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    localBackupToDelete?.let { backup ->
        AppleAlertDialog(
            onDismissRequest = { localBackupToDelete = null },
            title = { Text(stringResource(R.string.delete)) },
            text = { Text(formatLocalBackupMeta(backup), color = AppleNotesStyle.SecondaryText) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteLocalBackup(backup.fileName)
                    localBackupToDelete = null
                }) {
                    Text(stringResource(R.string.delete), color = AppleNotesStyle.Destructive)
                }
            },
            dismissButton = {
                TextButton(onClick = { localBackupToDelete = null }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
}

@Composable
private fun BackupHealthCard(health: BackupHealthUiState) {
    val titleRes = when (health.level) {
        BackupHealthLevel.Attention -> R.string.backup_health_attention_title
        BackupHealthLevel.LocalReady -> R.string.backup_health_local_title
        BackupHealthLevel.WebDavReady -> R.string.backup_health_webdav_title
    }
    val subtitle = when (health.level) {
        BackupHealthLevel.Attention -> stringResource(R.string.backup_health_attention_subtitle)
        BackupHealthLevel.LocalReady -> stringResource(
            R.string.backup_health_local_subtitle,
            formatBackupTime(health.referenceTime)
        )
        BackupHealthLevel.WebDavReady -> stringResource(
            R.string.backup_health_webdav_subtitle,
            formatBackupTime(health.referenceTime)
        )
    }
    val icon = when (health.level) {
        BackupHealthLevel.Attention -> Lucide.Info
        BackupHealthLevel.LocalReady -> Lucide.Save
        BackupHealthLevel.WebDavReady -> Lucide.Cloud
    }

    Surface(modifier = Modifier.fillMaxWidth(), color = AppleNotesStyle.Surface, shape = AppleNotesStyle.GroupShape) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(color = AppleNotesStyle.AccentSoft, shape = CircleShape) {
                Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = AppleNotesStyle.Accent, modifier = Modifier.size(22.dp))
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(titleRes),
                    color = AppleNotesStyle.PrimaryText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    color = AppleNotesStyle.SecondaryText,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun BackupTrustCard() {
    Surface(modifier = Modifier.fillMaxWidth(), color = AppleNotesStyle.Surface, shape = AppleNotesStyle.GroupShape) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = stringResource(R.string.backup_trust_title),
                color = AppleNotesStyle.PrimaryText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            BackupTrustRow(icon = Lucide.Shield, text = stringResource(R.string.backup_trust_local))
            BackupTrustRow(icon = Lucide.Check, text = stringResource(R.string.backup_trust_preview))
            BackupTrustRow(icon = Lucide.Cloud, text = stringResource(R.string.backup_trust_webdav))
            BackupTrustRow(icon = Lucide.Clock, text = stringResource(R.string.backup_trust_history))
        }
    }
}

@Composable
private fun BackupTrustRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Surface(color = AppleNotesStyle.AccentSoft, shape = CircleShape) {
            Box(modifier = Modifier.size(28.dp), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = AppleNotesStyle.Accent, modifier = Modifier.size(15.dp))
            }
        }
        Text(
            text = text,
            modifier = Modifier.weight(1f),
            color = AppleNotesStyle.SecondaryText,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun BackupStatusCard(
    state: BackupUiState,
    progress: Int
) {
    val icon = when (state) {
        is BackupUiState.Error -> Lucide.Frown
        is BackupUiState.Success,
        is BackupUiState.BackupReady -> Lucide.Check
        is BackupUiState.Loading -> Lucide.RefreshCw
        BackupUiState.Idle -> Lucide.Info
    }
    val message = when (state) {
        is BackupUiState.BackupReady -> stringResource(R.string.backup_created_size, state.data.size)
        is BackupUiState.Error -> state.message
        is BackupUiState.Loading -> state.message
        is BackupUiState.Success -> state.message
        BackupUiState.Idle -> stringResource(R.string.backup_state_ready)
    }

    Surface(modifier = Modifier.fillMaxWidth(), color = AppleNotesStyle.Surface, shape = AppleNotesStyle.GroupShape) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = AppleNotesStyle.AccentSoft, shape = CircleShape) {
                    Box(modifier = Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = AppleNotesStyle.Accent, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(stringResource(R.string.backup_state_status), color = AppleNotesStyle.PrimaryText, fontWeight = FontWeight.SemiBold)
                    Text(message, color = AppleNotesStyle.SecondaryText, style = MaterialTheme.typography.bodySmall)
                }
            }
            if (state is BackupUiState.Loading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = AppleNotesStyle.Accent,
                    trackColor = AppleNotesStyle.AccentSoft
                )
            } else if (progress == 100) {
                LinearProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxWidth(),
                    color = AppleNotesStyle.Accent,
                    trackColor = AppleNotesStyle.AccentSoft
                )
            }
        }
    }
}

@Composable
private fun BackupMemberRequiredCard(onOpenMembership: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), color = AppleNotesStyle.Surface, shape = AppleNotesStyle.GroupShape) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = AppleNotesStyle.AccentSoft, shape = CircleShape) {
                    Box(modifier = Modifier.size(42.dp), contentAlignment = Alignment.Center) {
                        Icon(Lucide.Lock, contentDescription = null, tint = AppleNotesStyle.Accent, modifier = Modifier.size(22.dp))
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.backup_member_required_title), color = AppleNotesStyle.PrimaryText, fontWeight = FontWeight.SemiBold)
                    Text(stringResource(R.string.backup_member_required_desc), color = AppleNotesStyle.SecondaryText, style = MaterialTheme.typography.bodySmall)
                }
            }
            TextButton(onClick = onOpenMembership) {
                Text(stringResource(R.string.backup_go_membership), color = AppleNotesStyle.Accent)
            }
        }
    }
}

@Composable
private fun BackupActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    action: String,
    onClick: () -> Unit
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
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, color = AppleNotesStyle.PrimaryText, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, color = AppleNotesStyle.SecondaryText, style = MaterialTheme.typography.bodySmall)
        }
        TextButton(onClick = onClick) {
            Text(action, color = AppleNotesStyle.Accent)
        }
    }
}

@Composable
private fun LocalBackupHistoryRow(
    backup: LocalBackupFile,
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
                Icon(Lucide.Clock, contentDescription = null, tint = AppleNotesStyle.Accent, modifier = Modifier.size(19.dp))
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(backup.fileName, color = AppleNotesStyle.PrimaryText, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
            Text(formatLocalBackupMeta(backup), color = AppleNotesStyle.SecondaryText, style = MaterialTheme.typography.bodySmall)
        }
        TextButton(onClick = onRestore) {
            Text(stringResource(R.string.restore), color = AppleNotesStyle.Accent)
        }
        TextButton(onClick = onDelete) {
            Text(stringResource(R.string.delete), color = AppleNotesStyle.Destructive)
        }
    }
}

private fun formatBackupTime(timestamp: Long): String {
    return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(timestamp))
}

private fun formatLocalBackupMeta(backup: LocalBackupFile): String {
    val date = formatBackupTime(backup.lastModified)
    val sizeKb = (backup.sizeBytes / 1024L).coerceAtLeast(1L)
    return "$date · ${sizeKb}KB"
}
