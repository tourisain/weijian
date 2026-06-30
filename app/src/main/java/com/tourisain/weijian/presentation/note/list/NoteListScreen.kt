package com.tourisain.weijian.presentation.note.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.google.gson.Gson
import com.tourisain.weijian.R
import com.tourisain.weijian.data.database.entity.NoteEntity
import com.tourisain.weijian.presentation.common.AppleAlertDialog
import com.tourisain.weijian.presentation.common.navigateStable
import com.tourisain.weijian.presentation.common.popBackStackStable
import com.tourisain.weijian.presentation.icons.Lucide
import com.tourisain.weijian.presentation.navigation.Screen
import com.tourisain.weijian.presentation.note.components.AppleNotesStyle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListScreen(
    navController: NavController,
    categoryId: String? = null,
    smartFilter: String? = null,
    viewModel: NoteListViewModel = hiltViewModel()
) {
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val allNoteCount by viewModel.allNoteCount.collectAsStateWithLifecycle()
    val isUserPro by viewModel.isUserPro.collectAsStateWithLifecycle()
    val categoryFolders by viewModel.categoryFolders.collectAsStateWithLifecycle()
    val selectedCategoryRoute by viewModel.categoryRoute.collectAsStateWithLifecycle()
    val tagFilters by viewModel.tagFilters.collectAsStateWithLifecycle()
    val selectedTag by viewModel.selectedTag.collectAsStateWithLifecycle()
    val smartFilters by viewModel.smartFilters.collectAsStateWithLifecycle()
    val selectedSmartFilter by viewModel.selectedSmartFilter.collectAsStateWithLifecycle()
    val selectedFolder = remember(categoryFolders, selectedCategoryRoute) {
        categoryFolders.firstOrNull { it.id == selectedCategoryRoute }
    }
    val folderNamesById = remember(categoryFolders) {
        categoryFolders.associate { it.id to it.name }
    }
    val uncategorizedLabel = stringResource(R.string.uncategorized)
    val showFolderInRows = selectedCategoryRoute == ALL_NOTES_ROUTE
    val pinnedNotes = remember(notes) { notes.filter { it.isPinned } }
    val normalNotes = remember(notes) { notes.filterNot { it.isPinned } }
    var showLimitDialog by remember { mutableStateOf(false) }

    LaunchedEffect(categoryId, smartFilter) {
        viewModel.setCategoryId(categoryId)
        viewModel.setSelectedSmartFilter(noteSmartFilterFromRoute(smartFilter))
    }

    fun noteFolderName(note: NoteEntity): String? {
        if (!showFolderInRows) return null
        val noteCategoryId = note.categoryId?.takeIf { it.isNotBlank() } ?: return uncategorizedLabel
        return folderNamesById[noteCategoryId]
    }

    Scaffold(
        containerColor = AppleNotesStyle.Background,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStackStable(Screen.Dashboard.route)
                    }) {
                        Icon(Lucide.ChevronLeft, contentDescription = stringResource(R.string.back), tint = AppleNotesStyle.Accent)
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigateStable(Screen.Search.route) }) {
                        Icon(Lucide.Search, contentDescription = stringResource(R.string.search), tint = AppleNotesStyle.Accent)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppleNotesStyle.Background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (isUserPro || allNoteCount < FREE_NOTE_LIMIT) {
                        navController.navigateStable(Screen.NoteDetail.createRoute("new", selectedCategoryRoute))
                    } else {
                        showLimitDialog = true
                    }
                },
                shape = CircleShape,
                containerColor = AppleNotesStyle.Accent,
                contentColor = Color.Black
            ) {
                Icon(Lucide.PenLine, contentDescription = stringResource(R.string.create))
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(AppleNotesStyle.Background)
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = selectedTag?.let { "#$it" }
                            ?: selectedSmartFilter?.displayTitle()
                            ?: selectedFolder?.displayName()
                            ?: stringResource(R.string.notes_title),
                        color = AppleNotesStyle.PrimaryText,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.category_notes_count, notes.size),
                        color = AppleNotesStyle.SecondaryText,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            item {
                AppleSearchField(
                    value = searchQuery,
                    onValueChange = viewModel::onSearchQueryChange,
                    placeholder = stringResource(R.string.search_notes)
                )
            }

            if (categoryFolders.isNotEmpty()) {
                item {
                    NoteCategorySelector(
                        folders = categoryFolders,
                        onOpen = viewModel::setCategoryId
                    )
                }
            }

            if (smartFilters.isNotEmpty()) {
                item {
                    NoteSmartFilterSelector(
                        filters = smartFilters,
                        selectedFilter = selectedSmartFilter,
                        onSelect = viewModel::setSelectedSmartFilter
                    )
                }
            }

            if (tagFilters.isNotEmpty()) {
                item {
                    NoteTagFilterSelector(
                        tags = tagFilters,
                        selectedTag = selectedTag,
                        onSelect = viewModel::setSelectedTag
                    )
                }
            }

            if (notes.isEmpty()) {
                item {
                    EmptyNotesState(
                        message = if (searchQuery.isBlank()) stringResource(R.string.no_notes) else stringResource(R.string.no_matching_notes)
                    )
                }
            } else {
                if (pinnedNotes.isNotEmpty()) {
                    noteRowsSection(
                        sectionKey = "pinned",
                        title = { stringResource(R.string.pinned) },
                        notes = pinnedNotes,
                        searchQuery = searchQuery,
                        folderName = ::noteFolderName,
                        onOpen = { note -> navController.navigateStable(Screen.NoteDetail.createRoute(note.id, selectedCategoryRoute)) },
                        onDelete = viewModel::deleteNote,
                        onTogglePin = viewModel::togglePin
                    )
                }

                if (normalNotes.isNotEmpty()) {
                    noteRowsSection(
                        sectionKey = "normal",
                        title = {
                            if (selectedCategoryRoute == ALL_NOTES_ROUTE) {
                                stringResource(R.string.all)
                            } else {
                                stringResource(R.string.notes_title)
                            }
                        },
                        notes = normalNotes,
                        searchQuery = searchQuery,
                        folderName = ::noteFolderName,
                        onOpen = { note -> navController.navigateStable(Screen.NoteDetail.createRoute(note.id, selectedCategoryRoute)) },
                        onDelete = viewModel::deleteNote,
                        onTogglePin = viewModel::togglePin
                    )
                }
            }
        }
    }

    if (showLimitDialog) {
        AppleAlertDialog(
            onDismissRequest = { showLimitDialog = false },
            title = { Text(stringResource(R.string.note_member_limit_title)) },
            text = { Text(stringResource(R.string.note_member_limit_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showLimitDialog = false
                    navController.navigateStable(Screen.Membership.route)
                }) {
                    Text(stringResource(R.string.membership_purchase_button), color = AppleNotesStyle.Accent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLimitDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

private const val FREE_NOTE_LIMIT = 3

@Composable
private fun NoteSmartFilterSelector(
    filters: List<NoteSmartFilterUi>,
    selectedFilter: NoteSmartFilter?,
    onSelect: (NoteSmartFilter?) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Surface(
                modifier = Modifier.clickable { onSelect(null) },
                color = if (selectedFilter == null) AppleNotesStyle.AccentSoft else AppleNotesStyle.Surface,
                shape = CircleShape
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Lucide.ListFilter, contentDescription = null, tint = AppleNotesStyle.Accent, modifier = Modifier.size(14.dp))
                    Text(
                        text = stringResource(R.string.all_smart_filters),
                        color = AppleNotesStyle.PrimaryText,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (selectedFilter == null) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
        items(filters, key = { it.type.name }) { filter ->
            Surface(
                modifier = Modifier.clickable { onSelect(filter.type) },
                color = if (filter.selected) AppleNotesStyle.AccentSoft else AppleNotesStyle.Surface,
                shape = CircleShape
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(filter.type.icon(), contentDescription = null, tint = AppleNotesStyle.Accent, modifier = Modifier.size(14.dp))
                    Text(
                        text = filter.type.displayTitle(),
                        color = AppleNotesStyle.PrimaryText,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (filter.selected) FontWeight.SemiBold else FontWeight.Normal
                    )
                    Text(
                        text = filter.count.toString(),
                        color = AppleNotesStyle.SecondaryText,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Composable
private fun NoteTagFilterSelector(
    tags: List<NoteTagFilterUi>,
    selectedTag: String?,
    onSelect: (String?) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Surface(
                modifier = Modifier.clickable { onSelect(null) },
                color = if (selectedTag == null) AppleNotesStyle.AccentSoft else AppleNotesStyle.Surface,
                shape = CircleShape
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Lucide.Tag, contentDescription = null, tint = AppleNotesStyle.Accent, modifier = Modifier.size(14.dp))
                    Text(
                        text = stringResource(R.string.all_tags),
                        color = AppleNotesStyle.PrimaryText,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (selectedTag == null) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
        itemsIndexed(tags, key = { _, tag -> "tag:${tag.name}" }) { _, tag ->
            Surface(
                modifier = Modifier.clickable { onSelect(tag.name) },
                color = if (tag.selected) AppleNotesStyle.AccentSoft else AppleNotesStyle.Surface,
                shape = CircleShape
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "#${tag.name}",
                        color = AppleNotesStyle.PrimaryText,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (tag.selected) FontWeight.SemiBold else FontWeight.Normal
                    )
                    Text(
                        text = tag.count.toString(),
                        color = AppleNotesStyle.SecondaryText,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Composable
private fun NoteCategorySelector(
    folders: List<NoteCategoryFolderUi>,
    onOpen: (String) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        itemsIndexed(folders, key = { _, folder -> "folder:${folder.id}" }) { _, folder ->
            val tint = parseFolderColor(folder.color)
            Surface(
                modifier = Modifier.clickable { onOpen(folder.id) },
                color = if (folder.selected) AppleNotesStyle.AccentSoft else AppleNotesStyle.Surface,
                shape = CircleShape
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(color = tint.copy(alpha = 0.16f), shape = CircleShape) {
                        Box(modifier = Modifier.size(18.dp), contentAlignment = Alignment.Center) {
                            Icon(
                                if (folder.id == ALL_NOTES_ROUTE) Lucide.StickyNote else Lucide.Folder,
                                contentDescription = null,
                                tint = tint,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                    Text(
                        text = folder.displayName(),
                        color = AppleNotesStyle.PrimaryText,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (folder.selected) FontWeight.SemiBold else FontWeight.Normal
                    )
                    Text(
                        text = folder.count.toString(),
                        color = AppleNotesStyle.SecondaryText,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Composable
private fun NoteCategoryFolderUi.displayName(): String = when (id) {
    ALL_NOTES_ROUTE -> stringResource(R.string.all)
    UNCATEGORIZED_ROUTE -> stringResource(R.string.uncategorized)
    else -> name
}

@Composable
private fun NoteSmartFilter.displayTitle(): String = when (this) {
    NoteSmartFilter.PINNED -> stringResource(R.string.filter_pinned)
    NoteSmartFilter.LOCKED -> stringResource(R.string.filter_locked)
    NoteSmartFilter.TODO -> stringResource(R.string.filter_todos)
    NoteSmartFilter.ATTACHMENTS -> stringResource(R.string.filter_attachments)
}

private fun NoteSmartFilter.icon() = when (this) {
    NoteSmartFilter.PINNED -> Lucide.Star
    NoteSmartFilter.LOCKED -> Lucide.Lock
    NoteSmartFilter.TODO -> Lucide.Check
    NoteSmartFilter.ATTACHMENTS -> Lucide.FileText
}

private fun LazyListScope.noteRowsSection(
    sectionKey: String,
    title: @Composable () -> String,
    notes: List<NoteEntity>,
    searchQuery: String,
    folderName: (NoteEntity) -> String?,
    onOpen: (NoteEntity) -> Unit,
    onDelete: (NoteEntity) -> Unit,
    onTogglePin: (NoteEntity) -> Unit
) {
    item(key = "$sectionKey-title") {
        SectionTitle(title())
    }
    itemsIndexed(
        items = notes,
        key = { _, note -> "$sectionKey:${note.id}" }
    ) { index, note ->
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = AppleNotesStyle.Surface,
            shape = groupedRowShape(index, notes.size)
        ) {
            Column {
                AppleNoteRow(
                    note = note,
                    searchQuery = searchQuery,
                    folderName = folderName(note),
                    onClick = { onOpen(note) },
                    onDelete = { onDelete(note) },
                    onTogglePin = { onTogglePin(note) }
                )
                if (index != notes.lastIndex) RowDivider()
            }
        }
    }
}

private fun groupedRowShape(index: Int, total: Int): RoundedCornerShape {
    val radius = AppleNotesStyle.GroupRadiusDp.dp
    return when {
        total <= 1 -> AppleNotesStyle.GroupShape
        index == 0 -> RoundedCornerShape(topStart = radius, topEnd = radius)
        index == total - 1 -> RoundedCornerShape(bottomStart = radius, bottomEnd = radius)
        else -> RoundedCornerShape(0.dp)
    }
}

fun NoteSmartFilter.routeValue(): String = name.lowercase(Locale.ROOT)

fun noteSmartFilterFromRoute(value: String?): NoteSmartFilter? {
    return when (value?.trim()?.lowercase(Locale.ROOT)) {
        NoteSmartFilter.PINNED.routeValue(), "pin" -> NoteSmartFilter.PINNED
        NoteSmartFilter.LOCKED.routeValue(), "lock" -> NoteSmartFilter.LOCKED
        NoteSmartFilter.TODO.routeValue(), "todo", "checklist" -> NoteSmartFilter.TODO
        NoteSmartFilter.ATTACHMENTS.routeValue(), "attachment" -> NoteSmartFilter.ATTACHMENTS
        else -> null
    }
}

@Composable
private fun AppleSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AppleNotesStyle.SearchSurface,
        shape = AppleNotesStyle.SearchShape
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Lucide.Search, contentDescription = null, tint = AppleNotesStyle.SecondaryText, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(color = AppleNotesStyle.PrimaryText, fontSize = 16.sp),
                modifier = Modifier.weight(1f),
                decorationBox = { innerTextField ->
                    if (value.isBlank()) Text(placeholder, color = AppleNotesStyle.TertiaryText, fontSize = 16.sp)
                    innerTextField()
                }
            )
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title.uppercase(Locale.getDefault()),
        modifier = Modifier.padding(start = 4.dp, top = 4.dp),
        color = AppleNotesStyle.SecondaryText,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun NotesGroup(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AppleNotesStyle.Surface.copy(alpha = 0.98f),
        shape = AppleNotesStyle.GroupShape
    ) {
        Column { content() }
    }
}

@Composable
private fun RowDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 56.dp),
        color = AppleNotesStyle.Separator,
        thickness = 1.dp
    )
}

@Composable
private fun EmptyNotesState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Surface(color = AppleNotesStyle.AccentSoft, shape = CircleShape) {
                Icon(
                    Lucide.StickyNote,
                    contentDescription = null,
                    tint = AppleNotesStyle.Accent,
                    modifier = Modifier
                        .padding(18.dp)
                        .size(34.dp)
                )
            }
            Text(message, color = AppleNotesStyle.SecondaryText, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppleNoteRow(
    note: NoteEntity,
    searchQuery: String,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onTogglePin: () -> Unit,
    folderName: String? = null
) {
    var showActions by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val fallbackTitle = stringResource(R.string.no_title)
    val fallbackContent = stringResource(R.string.no_content_note)
    val lockedContent = stringResource(R.string.locked_note_preview)
    val rowTitle = note.title.ifBlank { fallbackTitle }
    val rowContent = if (note.isLocked) lockedContent else note.content.ifBlank { fallbackContent }
    val tags = remember(note.tags) { parseNoteTags(note.tags) }
    val attachments = remember(note.attachments) { parseAttachments(note.attachments) }
    val highlightedTitle = remember(rowTitle, searchQuery) {
        highlightText(
            text = rowTitle,
            query = searchQuery,
            highlightColor = AppleNotesStyle.AccentSoft
        )
    }
    val highlightedContent = remember(rowContent, searchQuery) {
        highlightText(
            text = rowContent,
            query = searchQuery,
            highlightColor = AppleNotesStyle.AccentSoft
        )
    }
    val createdTime = remember(note.createdAt) { formatTimeAppleStyle(note.createdAt) }
    val metaText = remember(createdTime, folderName) { noteRowMetaText(createdTime, folderName) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = { showActions = true })
            .padding(horizontal = 14.dp, vertical = AppleNotesStyle.NoteRowVerticalPaddingDp.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(AppleNotesStyle.ListIconSizeDp.dp)
                .clip(CircleShape)
                .background(if (note.isPinned) AppleNotesStyle.AccentSoft.copy(alpha = 0.74f) else AppleNotesStyle.SearchSurface),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                when {
                    note.isLocked -> Lucide.Lock
                    note.isPinned -> Lucide.Star
                    else -> Lucide.FileText
                },
                contentDescription = null,
                tint = if (note.isPinned || note.isLocked) AppleNotesStyle.Accent else AppleNotesStyle.SecondaryText,
                modifier = Modifier.size(AppleNotesStyle.ListIconGlyphSizeDp.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = highlightedTitle,
                color = AppleNotesStyle.PrimaryText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = highlightedContent,
                color = AppleNotesStyle.SecondaryText,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (!note.isLocked && (tags.isNotEmpty() || attachments.isNotEmpty())) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    tags.take(3).forEach { tag ->
                        NoteRowTag(tag)
                    }
                    if (attachments.isNotEmpty()) {
                        NoteRowAttachmentCount(attachments.size)
                    }
                }
            }
            Text(
                text = metaText,
                color = AppleNotesStyle.TertiaryText,
                style = MaterialTheme.typography.labelSmall
            )
        }
        Icon(Lucide.ChevronRight, contentDescription = null, tint = AppleNotesStyle.TertiaryText, modifier = Modifier.size(20.dp))
    }

    if (showActions) {
        AppleAlertDialog(
            onDismissRequest = { showActions = false },
            title = { Text(stringResource(R.string.note_actions)) },
            text = { Text(stringResource(R.string.choose_action)) },
            confirmButton = {
                TextButton(onClick = { showActions = false; onTogglePin() }) {
                    Text(if (note.isPinned) stringResource(R.string.unpin) else stringResource(R.string.pin))
                }
            },
            dismissButton = {
                TextButton(onClick = { showActions = false; showDeleteConfirm = true }) {
                    Text(stringResource(R.string.delete), color = AppleNotesStyle.Destructive)
                }
            }
        )
    }
    if (showDeleteConfirm) {
        AppleAlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.delete_note)) },
            text = { Text(stringResource(R.string.delete_note_message)) },
            confirmButton = {
                TextButton(onClick = { showDeleteConfirm = false; onDelete() }) {
                    Text(stringResource(R.string.delete), color = AppleNotesStyle.Destructive)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun NoteRowAttachmentCount(count: Int) {
    Surface(
        color = AppleNotesStyle.SearchSurface,
        shape = CircleShape
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(Lucide.FileText, contentDescription = null, tint = AppleNotesStyle.SecondaryText, modifier = Modifier.size(12.dp))
            Text(
                text = stringResource(R.string.attachment_count, count),
                color = AppleNotesStyle.SecondaryText,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun NoteRowTag(tag: String) {
    Surface(
        color = AppleNotesStyle.SearchSurface,
        shape = CircleShape
    ) {
        Text(
            text = "#$tag",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = AppleNotesStyle.SecondaryText,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun NoteItem(
    note: NoteEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onTogglePin: () -> Unit,
    searchQuery: String
) = AppleNoteRow(note, searchQuery, onClick, onDelete, onTogglePin)

@Composable
fun NoteListItem(
    note: NoteEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onTogglePin: () -> Unit,
    searchQuery: String
) = AppleNoteRow(note, searchQuery, onClick, onDelete, onTogglePin)

private fun highlightText(text: String, query: String, highlightColor: Color): AnnotatedString {
    if (query.isBlank()) return AnnotatedString(text)
    val locale = Locale.getDefault()
    val lowerText = text.lowercase(locale)
    val lowerQuery = query.lowercase(locale)
    if (lowerQuery.isBlank()) return AnnotatedString(text)

    return buildAnnotatedString {
        var cursor = 0
        while (cursor < text.length) {
            val index = lowerText.indexOf(lowerQuery, cursor)
            if (index == -1) {
                append(text.substring(cursor))
                break
            }
            append(text.substring(cursor, index))
            pushStyle(SpanStyle(color = AppleNotesStyle.PrimaryText, background = highlightColor, fontWeight = FontWeight.SemiBold))
            append(text.substring(index, index + lowerQuery.length))
            pop()
            cursor = index + lowerQuery.length
        }
    }
}

private fun parseFolderColor(color: String): Color {
    return runCatching { Color(android.graphics.Color.parseColor(color)) }
        .getOrDefault(AppleNotesStyle.Accent)
}

private fun parseNoteColor(color: String): Color = try {
    Color(android.graphics.Color.parseColor(color))
} catch (_: Exception) {
    Color.White
}

fun isLightColor(colorString: String): Boolean = try {
    val color = android.graphics.Color.parseColor(colorString)
    val darkness = 1 - (0.299 * android.graphics.Color.red(color) + 0.587 * android.graphics.Color.green(color) + 0.114 * android.graphics.Color.blue(color)) / 255
    darkness < 0.5
} catch (_: Exception) {
    true
}

fun parseAttachments(attachments: String?): List<String> {
    return parseStoredStringList(attachments)
}

fun parseNoteTags(tags: String?): List<String> {
    return parseStoredStringList(tags)
}

private fun parseStoredStringList(value: String?): List<String> {
    if (value.isNullOrBlank() || value.equals("null", ignoreCase = true)) return emptyList()
    return runCatching {
        val parsed: Array<String>? = noteAttachmentsGson.fromJson(value, Array<String>::class.java)
        parsed.orEmpty().mapNotNull { item -> item.takeIf { it.isNotBlank() } }
    }.getOrDefault(emptyList())
}

fun getImageAttachments(attachments: List<String>): List<String> {
    val imageExtensions = setOf(".jpg", ".jpeg", ".png", ".gif", ".webp")
    return attachments.filter { uri -> imageExtensions.any { uri.lowercase().endsWith(it) } }
}

fun formatDate(dateString: String): String = dateString
fun formatTime(timestamp: Long): String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
fun formatTimeAppleStyle(timestamp: Long): String = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestamp))

private val noteAttachmentsGson = Gson()
