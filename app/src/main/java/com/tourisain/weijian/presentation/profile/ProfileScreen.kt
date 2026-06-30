package com.tourisain.weijian.presentation.profile

import android.content.Intent
import android.widget.Toast
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
import com.tourisain.weijian.presentation.common.AppleAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.tourisain.weijian.R
import com.tourisain.weijian.presentation.common.PermissionDisclosureScenario
import com.tourisain.weijian.presentation.common.navigateStable
import com.tourisain.weijian.presentation.common.permissionDisclosureFor
import com.tourisain.weijian.presentation.common.popBackStackStable
import com.tourisain.weijian.presentation.icons.Lucide
import com.tourisain.weijian.presentation.navigation.Screen
import com.tourisain.weijian.presentation.note.components.AppleNotesStyle
import com.tourisain.weijian.presentation.settings.SettingItem
import com.tourisain.weijian.presentation.settings.SettingsDivider
import com.tourisain.weijian.presentation.settings.SettingsGroup

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()
    val isUserPro by viewModel.isUserPro.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val localFingerprint = remember { viewModel.getDeviceActivationFingerprint() }
    var showEditNameDialog by remember { mutableStateOf(false) }
    var showAvatarFileAccessDialog by remember { mutableStateOf(false) }
    var newUsername by remember { mutableStateOf("") }
    val avatarDisclosure = permissionDisclosureFor(PermissionDisclosureScenario.Avatar)
    val avatarPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            viewModel.updateAvatar(uri)
        }
    }

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    Scaffold(
        containerColor = AppleNotesStyle.Background,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStackStable(Screen.Dashboard.route) }) {
                        Icon(Lucide.ChevronLeft, contentDescription = stringResource(R.string.back), tint = AppleNotesStyle.Accent)
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = stringResource(R.string.profile),
                    color = AppleNotesStyle.PrimaryText,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold
                )
            }
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = AppleNotesStyle.Surface,
                    shape = AppleNotesStyle.GroupShape
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(color = AppleNotesStyle.AccentSoft, shape = CircleShape) {
                            Box(modifier = Modifier.size(74.dp), contentAlignment = Alignment.Center) {
                                val avatarUri = currentUser?.avatarUri?.takeIf { it.isNotBlank() }
                                if (avatarUri != null) {
                                    AsyncImage(
                                        model = avatarUri,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(74.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(Lucide.User, contentDescription = null, tint = AppleNotesStyle.Accent, modifier = Modifier.size(40.dp))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = currentUser?.username ?: "User",
                                color = AppleNotesStyle.PrimaryText,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Surface(
                                color = if (isUserPro) AppleNotesStyle.AccentSoft else AppleNotesStyle.SearchSurface,
                                shape = CircleShape
                            ) {
                                Text(
                                    text = if (isUserPro) stringResource(R.string.pro_member) else stringResource(R.string.standard_user),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    color = if (isUserPro) AppleNotesStyle.Accent else AppleNotesStyle.SecondaryText,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Text(
                                text = stringResource(R.string.user_id, localFingerprint),
                                color = AppleNotesStyle.TertiaryText,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
            item {
                SettingsGroup {
                    SettingItem(
                        icon = Lucide.Pencil,
                        title = stringResource(R.string.profile_edit_username),
                        subtitle = stringResource(R.string.account_settings),
                        onClick = {
                            newUsername = currentUser?.username.orEmpty()
                            showEditNameDialog = true
                        }
                    )
                    SettingsDivider()
                    SettingItem(
                        icon = Lucide.User,
                        title = stringResource(R.string.profile_change_avatar),
                        subtitle = stringResource(R.string.profile_change_avatar_desc),
                        onClick = { showAvatarFileAccessDialog = true }
                    )
                    SettingsDivider()
                    SettingItem(
                        icon = Lucide.Star,
                        title = stringResource(R.string.membership_center),
                        subtitle = stringResource(R.string.member_features),
                        onClick = { navController.navigateStable(Screen.Membership.route) }
                    )
                    SettingsDivider()
                    SettingItem(
                        icon = Lucide.Cloud,
                        title = stringResource(R.string.backup_restore),
                        subtitle = stringResource(R.string.restore_from_backup),
                        onClick = { navController.navigateStable(Screen.Backup.route) }
                    )
                }
            }
        }
    }

    if (showEditNameDialog) {
        AppleAlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = { Text(stringResource(R.string.edit)) },
            text = {
                OutlinedTextField(
                    value = newUsername,
                    onValueChange = { newUsername = it.take(64) },
                    label = { Text(stringResource(R.string.profile)) },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.updateUsername(newUsername)
                        showEditNameDialog = false
                    },
                    enabled = newUsername.trim().isNotEmpty()
                ) {
                    Text(stringResource(R.string.save), color = AppleNotesStyle.Accent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showAvatarFileAccessDialog) {
        AppleAlertDialog(
            onDismissRequest = { showAvatarFileAccessDialog = false },
            title = { Text(avatarDisclosure.title) },
            text = { Text(avatarDisclosure.message, color = AppleNotesStyle.SecondaryText) },
            confirmButton = {
                TextButton(onClick = {
                    showAvatarFileAccessDialog = false
                    avatarPickerLauncher.launch(arrayOf("image/*"))
                }) {
                    Text(avatarDisclosure.confirmLabel, color = AppleNotesStyle.Accent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAvatarFileAccessDialog = false }) {
                    Text(avatarDisclosure.dismissLabel)
                }
            }
        )
    }
}
