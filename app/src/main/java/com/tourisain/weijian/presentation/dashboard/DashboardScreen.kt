package com.tourisain.weijian.presentation.dashboard

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tourisain.weijian.R
import com.tourisain.weijian.presentation.common.navigateStable
import com.tourisain.weijian.presentation.common.safeExecute
import com.tourisain.weijian.presentation.icons.Lucide
import com.tourisain.weijian.presentation.navigation.Screen
import com.tourisain.weijian.presentation.note.components.AppleNotesStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val visibleQuickEntryRoutes = remember(state.quickEntryRoutes, state.cardVisibility, state.noteCount, state.accountCount) {
        state.quickEntryRoutes.filter { quickEntryVisible(it, state) }
    }
    val recentSections = remember(state.recentActivities) {
        dashboardActivitySections(state.recentActivities)
    }
    val firstStartActions = remember(state.cardVisibility) {
        dashboardFirstStartActions(
            noteVisible = state.cardVisibility.getOrDefault("note", true),
            accountVisible = state.cardVisibility.getOrDefault("account", true)
        )
    }
    val showFirstStartActions = state.noteCount == 0 && state.accountCount == 0 && firstStartActions.isNotEmpty()

    Scaffold(
        containerColor = AppleNotesStyle.Background,
        topBar = {
            TopAppBar(
                title = {},
                actions = {
                    IconButton(onClick = { safeExecute(context) { navController.navigateStable(Screen.Settings.route) } }) {
                        Icon(Lucide.Settings, contentDescription = stringResource(R.string.settings), tint = AppleNotesStyle.Accent)
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
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = stringResource(R.string.app_name),
                        color = AppleNotesStyle.PrimaryText,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.weijian_slogan),
                        color = AppleNotesStyle.SecondaryText,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = AppleNotesStyle.SearchSurface,
                    shape = AppleNotesStyle.SearchShape
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { safeExecute(context) { navController.navigateStable(Screen.Search.route) } }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Lucide.Search, contentDescription = null, tint = AppleNotesStyle.SecondaryText, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.search_notes), color = AppleNotesStyle.SecondaryText)
                    }
                }
            }

            if (showFirstStartActions) item {
                FirstStartActionCard(
                    actions = firstStartActions,
                    onAction = { action ->
                        safeExecute(context) {
                            navigateFirstStartAction(navController, action.action)
                        }
                    }
                )
            }

            item {
                SectionTitle(stringResource(R.string.recent_items))
                Surface(color = AppleNotesStyle.Surface, shape = AppleNotesStyle.GroupShape) {
                    Column {
                        if (state.recentActivities.isEmpty()) {
                            Text(
                                text = stringResource(R.string.no_recent_activities),
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 18.dp),
                                color = AppleNotesStyle.SecondaryText,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        } else {
                            recentSections.forEachIndexed { sectionIndex, section ->
                                ActivitySectionHeader(
                                    title = dashboardActivitySectionTitle(section.key)
                                )
                                section.activities.forEachIndexed { index, activity ->
                                    ActivityRow(
                                        activity = activity,
                                        onClick = {
                                            when (activity.type) {
                                                DashboardActivityType.Note -> safeExecute(context) { navController.navigateStable(Screen.NoteDetail.createRoute(activity.id, "all")) }
                                                DashboardActivityType.Account -> safeExecute(context) { navController.navigateStable(Screen.AccountEdit.createRoute(activity.id)) }
                                            }
                                        }
                                    )
                                    if (index != section.activities.lastIndex || sectionIndex != recentSections.lastIndex) RowDivider()
                                }
                            }
                        }
                    }
                }
            }

            if (state.cardVisibility.getOrDefault("note", true)) item {
                SectionTitle(stringResource(R.string.notes_title))
                Surface(color = AppleNotesStyle.Surface, shape = AppleNotesStyle.GroupShape) {
                    Column {
                        FolderRow(
                            icon = Lucide.Folder,
                            title = stringResource(R.string.all),
                            count = state.noteCount,
                            onClick = { safeExecute(context) { navController.navigateStable(Screen.NotesAll.route) } }
                        )
                        if (state.uncategorizedNoteCount > 0) {
                            RowDivider()
                            FolderRow(
                                icon = Lucide.Folder,
                                title = stringResource(R.string.uncategorized),
                                count = state.uncategorizedNoteCount,
                                tint = AppleNotesStyle.SecondaryText,
                                onClick = { safeExecute(context) { navController.navigateStable(Screen.NoteList.createRoute("uncategorized")) } }
                            )
                        }
                        state.noteFolders.forEach { folder ->
                            RowDivider()
                            FolderRow(
                                icon = Lucide.Folder,
                                title = folder.name,
                                count = folder.count,
                                tint = parseFolderColor(folder.color),
                                onClick = { safeExecute(context) { navController.navigateStable(Screen.NoteList.createRoute(folder.id)) } }
                            )
                        }
                        RowDivider()
                        FolderRow(
                            icon = Lucide.Star,
                            title = stringResource(R.string.pinned),
                            count = state.pinnedNoteCount,
                            onClick = { safeExecute(context) { navController.navigateStable(Screen.NotesPinned.route) } }
                        )
                        if (state.todoNoteCount > 0) {
                            RowDivider()
                            FolderRow(
                                icon = Lucide.Check,
                                title = stringResource(R.string.filter_todos),
                                count = state.todoNoteCount,
                                onClick = { safeExecute(context) { navController.navigateStable(Screen.NotesTodo.route) } }
                            )
                        }
                        RowDivider()
                        FolderRow(
                            icon = Lucide.Trash,
                            title = stringResource(R.string.recycle_bin),
                            count = state.recycleBinCount,
                            onClick = { safeExecute(context) { navController.navigateStable(Screen.RecycleBin.route) } }
                        )
                    }
                }
            }

            if (visibleQuickEntryRoutes.isNotEmpty()) item {
                SectionTitle(stringResource(R.string.home_common_entries))
                Surface(color = AppleNotesStyle.Surface, shape = AppleNotesStyle.GroupShape) {
                    Column {
                        visibleQuickEntryRoutes.forEachIndexed { index, route ->
                            FolderRow(
                                icon = quickEntryIcon(route),
                                title = quickEntryTitle(route),
                                count = quickEntryCount(route, state),
                                onClick = { safeExecute(context) { navigateQuickEntry(navController, route) } }
                            )
                            if (index != visibleQuickEntryRoutes.lastIndex) RowDivider()
                        }
                    }
                }
            }
        }
    }
}

internal enum class DashboardFirstStartAction {
    NewNote,
    NewAccount,
    CreateFolder,
    Backup
}

internal data class DashboardFirstStartActionItem(
    val action: DashboardFirstStartAction
)

internal fun dashboardFirstStartActions(
    noteVisible: Boolean,
    accountVisible: Boolean
): List<DashboardFirstStartActionItem> {
    return buildList {
        if (noteVisible) {
            add(DashboardFirstStartActionItem(DashboardFirstStartAction.NewNote))
        }
        if (accountVisible) {
            add(DashboardFirstStartActionItem(DashboardFirstStartAction.NewAccount))
        }
        if (noteVisible) {
            add(DashboardFirstStartActionItem(DashboardFirstStartAction.CreateFolder))
        }
        add(DashboardFirstStartActionItem(DashboardFirstStartAction.Backup))
    }
}

private fun navigateFirstStartAction(navController: NavController, action: DashboardFirstStartAction) {
    when (action) {
        DashboardFirstStartAction.NewNote -> navController.navigateStable(Screen.NoteDetail.createRoute("new", "all"))
        DashboardFirstStartAction.NewAccount -> navController.navigateStable(Screen.AccountEdit.createRoute("new"))
        DashboardFirstStartAction.CreateFolder -> navController.navigateStable(Screen.CategoryManagement.route)
        DashboardFirstStartAction.Backup -> navController.navigateStable(Screen.Backup.route)
    }
}

@Composable
private fun FirstStartActionCard(
    actions: List<DashboardFirstStartActionItem>,
    onAction: (DashboardFirstStartActionItem) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionTitle(stringResource(R.string.dashboard_first_start_title))
        Surface(color = AppleNotesStyle.Surface, shape = AppleNotesStyle.GroupShape) {
            Column {
                Text(
                    text = stringResource(R.string.dashboard_first_start_subtitle),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    color = AppleNotesStyle.SecondaryText,
                    style = MaterialTheme.typography.bodyMedium
                )
                actions.forEach { action ->
                    RowDivider()
                    FirstStartActionRow(action, onClick = { onAction(action) })
                }
            }
        }
    }
}

@Composable
private fun FirstStartActionRow(
    item: DashboardFirstStartActionItem,
    onClick: () -> Unit
) {
    val (icon, title, subtitle) = when (item.action) {
        DashboardFirstStartAction.NewNote -> Triple(
            Lucide.StickyNote,
            stringResource(R.string.dashboard_action_new_note),
            stringResource(R.string.dashboard_action_new_note_subtitle)
        )
        DashboardFirstStartAction.NewAccount -> Triple(
            Lucide.Wallet,
            stringResource(R.string.dashboard_action_new_account),
            stringResource(R.string.dashboard_action_new_account_subtitle)
        )
        DashboardFirstStartAction.CreateFolder -> Triple(
            Lucide.Folder,
            stringResource(R.string.dashboard_action_create_folder),
            stringResource(R.string.dashboard_action_create_folder_subtitle)
        )
        DashboardFirstStartAction.Backup -> Triple(
            Lucide.Shield,
            stringResource(R.string.dashboard_action_backup),
            stringResource(R.string.dashboard_action_backup_subtitle)
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(color = AppleNotesStyle.AccentSoft, shape = CircleShape) {
            Box(modifier = Modifier.size(34.dp), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = AppleNotesStyle.Accent, modifier = Modifier.size(18.dp))
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, color = AppleNotesStyle.PrimaryText, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = AppleNotesStyle.SecondaryText, style = MaterialTheme.typography.bodySmall)
        }
        Icon(Lucide.ChevronRight, contentDescription = null, tint = AppleNotesStyle.TertiaryText, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun quickEntryTitle(route: String): String = when (route) {
    "note_list/default" -> stringResource(R.string.notes_title)
    "accounts" -> stringResource(R.string.accounting)
    "search" -> stringResource(R.string.search)
    "settings" -> stringResource(R.string.settings)
    else -> route
}

private fun quickEntryIcon(route: String) = when (route) {
    "note_list/default" -> Lucide.StickyNote
    "accounts" -> Lucide.Wallet
    "search" -> Lucide.Search
    "settings" -> Lucide.Settings
    else -> Lucide.Menu
}

private fun quickEntryCount(route: String, state: DashboardState): Int? = when (route) {
    "note_list/default" -> state.noteCount
    "accounts" -> state.accountCount
    else -> null
}

private fun quickEntryVisible(route: String, state: DashboardState): Boolean = when (route) {
    "note_list/default" -> state.cardVisibility.getOrDefault("note", true)
    "accounts" -> state.cardVisibility.getOrDefault("account", true)
    else -> true
}

private fun navigateQuickEntry(navController: NavController, route: String) {
    when (route) {
        "note_list/default" -> navController.navigateStable(Screen.NotesAll.route)
        "accounts" -> navController.navigateStable(Screen.AccountList.route)
        "search" -> navController.navigateStable(Screen.Search.route)
        "settings" -> navController.navigateStable(Screen.Settings.route)
        else -> navController.navigateStable(Screen.Settings.route)
    }
}

@Composable
private fun dashboardActivitySectionTitle(key: DashboardActivitySectionKey): String = when (key) {
    DashboardActivitySectionKey.Today -> stringResource(R.string.dashboard_recent_today)
    DashboardActivitySectionKey.Yesterday -> stringResource(R.string.dashboard_recent_yesterday)
    DashboardActivitySectionKey.Earlier -> stringResource(R.string.dashboard_recent_earlier)
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
        color = AppleNotesStyle.SecondaryText,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun ActivitySectionHeader(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(start = 14.dp, top = 12.dp, bottom = 2.dp),
        color = AppleNotesStyle.TertiaryText,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun FolderRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    count: Int?,
    tint: Color = AppleNotesStyle.Accent,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(color = tint.copy(alpha = 0.14f), shape = CircleShape) {
            Box(modifier = Modifier.size(34.dp), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(19.dp))
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(title, modifier = Modifier.weight(1f), color = AppleNotesStyle.PrimaryText, style = MaterialTheme.typography.bodyLarge)
        if (count != null) {
            Text(count.toString(), color = AppleNotesStyle.SecondaryText, style = MaterialTheme.typography.bodyMedium)
        }
        Icon(Lucide.ChevronRight, contentDescription = null, tint = AppleNotesStyle.TertiaryText, modifier = Modifier.size(20.dp))
    }
}

private fun parseFolderColor(value: String): Color {
    return runCatching { Color(android.graphics.Color.parseColor(value)) }
        .getOrDefault(AppleNotesStyle.Accent)
}

@Composable
private fun ActivityRow(
    activity: DashboardActivity,
    onClick: () -> Unit
) {
    val icon = when (activity.type) {
        DashboardActivityType.Note -> Lucide.StickyNote
        DashboardActivityType.Account -> Lucide.Wallet
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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
            Text(
                text = activity.title,
                color = AppleNotesStyle.PrimaryText,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (activity.subtitle.isNotBlank()) {
                Text(
                    text = activity.subtitle,
                    color = AppleNotesStyle.SecondaryText,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Icon(Lucide.ChevronRight, contentDescription = null, tint = AppleNotesStyle.TertiaryText, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun RowDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 60.dp),
        color = AppleNotesStyle.Separator,
        thickness = 1.dp
    )
}
