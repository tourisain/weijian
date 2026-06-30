package com.tourisain.weijian.presentation.note.detail

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.rememberScrollState
import com.tourisain.weijian.presentation.common.AppleAlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.tourisain.weijian.R
import com.tourisain.weijian.data.database.entity.NoteRevisionEntity
import com.tourisain.weijian.presentation.common.PermissionDisclosureScenario
import com.tourisain.weijian.presentation.common.navigateStable
import com.tourisain.weijian.presentation.common.permissionDisclosureFor
import com.tourisain.weijian.presentation.common.popBackStackStable
import com.tourisain.weijian.presentation.icons.Lucide
import com.tourisain.weijian.presentation.navigation.Screen
import com.tourisain.weijian.presentation.note.components.AppleNotesStyle
import com.tourisain.weijian.presentation.privacy.PrivacyLockScreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.ceil
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    navController: NavController,
    noteId: String? = null,
    viewModel: NoteDetailViewModel = hiltViewModel()
) {
    val title by viewModel.title.collectAsStateWithLifecycle()
    val content by viewModel.content.collectAsStateWithLifecycle()
    val isPinned by viewModel.isPinned.collectAsStateWithLifecycle()
    val isLocked by viewModel.isLocked.collectAsStateWithLifecycle()
    val isAutoSaving by viewModel.isAutoSaving.collectAsStateWithLifecycle()
    val lastSaveTime by viewModel.lastSaveTime.collectAsStateWithLifecycle()
    val canUndo by viewModel.canUndo.collectAsStateWithLifecycle()
    val canRedo by viewModel.canRedo.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val categoryId by viewModel.categoryId.collectAsStateWithLifecycle()
    val folderOptions by viewModel.folderOptions.collectAsStateWithLifecycle()
    val tags by viewModel.tags.collectAsStateWithLifecycle()
    val attachments by viewModel.attachments.collectAsStateWithLifecycle()
    val revisions by viewModel.revisions.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val density = LocalDensity.current
    val imeBottomPx = WindowInsets.ime.getBottom(density)
    var isFormatToolbarExpanded by rememberSaveable { mutableStateOf(false) }
    val editorContentBottomPaddingDp = editorContentBottomPaddingDp(
        imeBottomPx = imeBottomPx,
        density = density.density,
        isToolbarExpanded = isFormatToolbarExpanded
    )
    var contentValue by remember { mutableStateOf(TextFieldValue(content)) }
    var contentFontSize by rememberSaveable { mutableStateOf(DEFAULT_CONTENT_FONT_SIZE) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showFolderDialog by remember { mutableStateOf(false) }
    var showTagDialog by remember { mutableStateOf(false) }
    var showAttachmentAccessDialog by remember { mutableStateOf(false) }
    var showExportAccessDialog by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var showTopActionMenu by remember { mutableStateOf(false) }
    var showMetadataDialog by remember { mutableStateOf(false) }
    var pendingContentSync by remember { mutableStateOf<String?>(null) }
    var lastEditorInputAtMs by remember { mutableStateOf(0L) }
    var richRenderClockMs by remember { mutableStateOf(0L) }
    var tagInput by rememberSaveable { mutableStateOf("") }
    var isNoteUnlocked by rememberSaveable(noteId) { mutableStateOf(false) }
    val attachmentDisclosure = permissionDisclosureFor(PermissionDisclosureScenario.Attachment)
    val exportDisclosure = permissionDisclosureFor(PermissionDisclosureScenario.FileExport)
    val selectedFolderName = remember(categoryId, folderOptions) {
        folderOptions.firstOrNull { it.id == categoryId }?.name
    }
    val richVisualTransformation = remember { RichNoteVisualTransformation() }
    val titleTextStyle = MaterialTheme.typography.displaySmall.copy(
        color = AppleNotesStyle.PrimaryText,
        fontSize = 30.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 36.sp
    )
    val contentTextStyle = MaterialTheme.typography.bodyLarge.copy(
        color = AppleNotesStyle.PrimaryText,
        fontSize = contentFontSize.sp,
        lineHeight = (contentFontSize * 1.5f).sp
    )
    val attachmentLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            runCatching {
                context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            viewModel.addAttachment(it.toString())
        }
    }
    val imageInsertLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            runCatching {
                context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            viewModel.addAttachment(it.toString())
            val imageMarkdown = "![](${it.toString()})"
            val nextValue = normalizeEditorValue(insertAtSelection(contentValue, imageMarkdown))
            lastEditorInputAtMs = 0L
            contentValue = nextValue
            viewModel.onEditorActionContentChange(nextValue.text)
        }
    }
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/markdown")) { uri ->
        uri?.let { writeNoteExport(context, it, title, contentValue.text, tags, attachments) }
    }
    val contentHasRichMarker = remember(contentValue.text) {
        hasRichTextMarker(contentValue.text)
    }
    val contentVisualTransformation = remember(
        contentValue.text.length,
        contentHasRichMarker,
        contentValue.selection,
        contentValue.composition,
        lastEditorInputAtMs,
        richRenderClockMs
    ) {
        if (
            shouldUseRichRendering(
                value = contentValue,
                hasRichMarker = contentHasRichMarker,
                nowMs = richRenderClockMs,
                lastInputAtMs = lastEditorInputAtMs
            )
        ) {
            richVisualTransformation
        } else {
            VisualTransformation.None
        }
    }

    LaunchedEffect(content) {
        if (pendingContentSync == null && contentValue.text != content) {
            contentValue = TextFieldValue(
                text = content,
                selection = stableSelectionAfterContentSync(contentValue, content)
            )
        }
    }

    LaunchedEffect(contentValue.text, lastEditorInputAtMs) {
        richRenderClockMs = SystemClock.elapsedRealtime()
        if (lastEditorInputAtMs > 0L) {
            delay(RICH_RENDER_SETTLE_MS)
            richRenderClockMs = SystemClock.elapsedRealtime()
        }
    }

    LaunchedEffect(pendingContentSync) {
        val pending = pendingContentSync ?: return@LaunchedEffect
        delay(editorSyncDelayMs(pending.length))
        if (pendingContentSync == pending) {
            viewModel.onContentChange(pending)
            pendingContentSync = null
        }
    }

    LaunchedEffect(isLocked) {
        if (!isLocked) isNoteUnlocked = true
    }

    if (isLocked && !isNoteUnlocked && noteId != null && noteId != "new") {
        PrivacyLockScreen(onUnlock = { isNoteUnlocked = true }, requirePassword = true)
        return
    }

    fun flushPendingContentSync() {
        pendingContentSync?.let {
            viewModel.onContentChange(it)
            pendingContentSync = null
        }
    }

    BackHandler {
        flushPendingContentSync()
        viewModel.saveNote { navController.popBackStackStable(Screen.Dashboard.route) }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                flushPendingContentSync()
                viewModel.saveNote {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    fun applyEditorAction(action: EditorAction) {
        pendingContentSync = null
        val nextValue = normalizeEditorValue(applyEditorAction(contentValue, action))
        if (!shouldCommitEditorTransaction(contentValue, nextValue)) return
        lastEditorInputAtMs = 0L
        contentValue = nextValue
        viewModel.onEditorActionContentChange(nextValue.text)
    }

    fun adjustContentFontSize(delta: Int) {
        contentFontSize = (contentFontSize + delta).coerceIn(MIN_CONTENT_FONT_SIZE, MAX_CONTENT_FONT_SIZE)
    }

    fun updateContentValue(nextValue: TextFieldValue) {
        val nowMs = SystemClock.elapsedRealtime()
        val interactionState = editorInteractionState(
            value = nextValue,
            nowMs = nowMs,
            lastInputAtMs = lastEditorInputAtMs
        )
        val assisted = if (shouldApplyTypingAssist(interactionState)) {
            applyEditorTypingAssist(contentValue, nextValue)
        } else {
            nextValue
        }
        val normalized = normalizeEditorValue(assisted)
        val previousValue = contentValue
        lastEditorInputAtMs = nowMs
        contentValue = normalized
        if (!shouldSyncEditorText(previousValue.text, normalized.text)) return
        if (
            shouldDeferEditorSync(
                previousTextLength = previousValue.text.length,
                nextTextLength = normalized.text.length,
                selectionLength = kotlin.math.abs(normalized.selection.end - normalized.selection.start),
                hasComposition = normalized.composition != null
            )
        ) {
            pendingContentSync = normalized.text
        } else {
            pendingContentSync = null
            viewModel.onContentChange(normalized.text)
        }
    }

    fun updateTitleValue(nextValue: String) {
        viewModel.onTitleChange(normalizeTitleInput(nextValue))
    }

    Scaffold(
        containerColor = AppleNotesStyle.Background,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = {
                        flushPendingContentSync()
                        viewModel.saveNote { navController.popBackStackStable(Screen.Dashboard.route) }
                    }) {
                        Icon(Lucide.ChevronLeft, contentDescription = stringResource(R.string.back), tint = AppleNotesStyle.Accent)
                    }
                },
                actions = {
                    Box {
                        TextButton(onClick = { showTopActionMenu = true }) {
                            Text(stringResource(R.string.more), color = AppleNotesStyle.Accent, fontWeight = FontWeight.SemiBold)
                        }
                        DropdownMenu(
                            expanded = showTopActionMenu,
                            onDismissRequest = { showTopActionMenu = false },
                            modifier = Modifier
                                .background(AppleNotesStyle.Surface, AppleNotesStyle.GroupShape)
                                .padding(vertical = 4.dp)
                        ) {
                            noteDetailTopActions(isPinned = isPinned, isLocked = isLocked).forEach { item ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = item.label,
                                            color = AppleNotesStyle.PrimaryText,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    },
                                    onClick = {
                                        showTopActionMenu = false
                                        when (item.action) {
                                            NoteDetailTopAction.Pin -> viewModel.togglePin()
                                            NoteDetailTopAction.Lock -> viewModel.toggleLock()
                                            NoteDetailTopAction.History -> showHistoryDialog = true
                                            NoteDetailTopAction.Share -> shareNoteMarkdown(context, title, contentValue.text, tags, attachments)
                                            NoteDetailTopAction.Export -> showExportAccessDialog = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                    TextButton(onClick = {
                        flushPendingContentSync()
                        viewModel.saveNote { navController.popBackStackStable(Screen.Dashboard.route) }
                    }) {
                        Text(stringResource(R.string.save), color = AppleNotesStyle.Accent, fontWeight = FontWeight.SemiBold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppleNotesStyle.Background)
            )
        },
        bottomBar = {
            EditorToolbar(
                canUndo = canUndo,
                canRedo = canRedo,
                onUndo = viewModel::undo,
                onRedo = viewModel::redo,
                contentFontSize = contentFontSize,
                onDecreaseFontSize = { adjustContentFontSize(-1) },
                onIncreaseFontSize = { adjustContentFontSize(1) },
                onHeading = { applyEditorAction(EditorAction.Heading) },
                onChecklist = { applyEditorAction(EditorAction.Checklist) },
                onList = { applyEditorAction(EditorAction.List) },
                onBold = { applyEditorAction(EditorAction.Bold) },
                onItalic = { applyEditorAction(EditorAction.Italic) },
                onStrikethrough = { applyEditorAction(EditorAction.Strikethrough) },
                onQuote = { applyEditorAction(EditorAction.Quote) },
                onCode = { applyEditorAction(EditorAction.Code) },
                onDivider = { applyEditorAction(EditorAction.Divider) },
                onImage = { imageInsertLauncher.launch(arrayOf("image/*")) },
                onLink = { applyEditorAction(EditorAction.Link) },
                onTable = { applyEditorAction(EditorAction.Table) },
                onAttachment = { showAttachmentAccessDialog = true },
                onDelete = { showDeleteConfirm = true },
                onHideKeyboard = {
                    isFormatToolbarExpanded = false
                    keyboardController?.hide()
                },
                formatToolsExpanded = isFormatToolbarExpanded,
                onFormatToolsExpandedChange = { isFormatToolbarExpanded = it }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppleNotesStyle.Background)
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            BasicTextField(
                value = title,
                onValueChange = ::updateTitleValue,
                singleLine = true,
                textStyle = titleTextStyle,
                cursorBrush = SolidColor(AppleNotesStyle.Accent),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    Box {
                        if (title.isBlank()) {
                            Text(
                                text = if (noteId == null || noteId == "new") stringResource(R.string.new_note) else stringResource(R.string.note_title),
                                color = AppleNotesStyle.TertiaryText,
                                style = titleTextStyle
                            )
                        }
                        innerTextField()
                    }
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (isAutoSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = AppleNotesStyle.Accent)
                    Text(stringResource(R.string.auto_save), color = AppleNotesStyle.SecondaryText, style = MaterialTheme.typography.labelSmall)
                } else if (lastSaveTime > 0L) {
                    Text(
                        stringResource(R.string.saved_at, formatSaveTime(lastSaveTime)),
                        color = AppleNotesStyle.SecondaryText,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            NoteMetadataSummaryRow(
                summary = noteMetadataSummary(
                    folderName = selectedFolderName ?: stringResource(R.string.uncategorized),
                    tagCount = tags.size,
                    attachmentCount = attachments.size
                ),
                onClick = { showMetadataDialog = true }
            )
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = EDITOR_PAPER_HORIZONTAL_PADDING_DP.dp)
            ) {
                BasicTextField(
                    value = contentValue,
                    onValueChange = ::updateContentValue,
                    textStyle = contentTextStyle,
                    visualTransformation = contentVisualTransformation,
                    cursorBrush = SolidColor(AppleNotesStyle.Accent),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 8.dp, bottom = editorContentBottomPaddingDp.dp),
                    decorationBox = { innerTextField ->
                        Box {
                            if (contentValue.text.isBlank()) {
                                Text(
                                    stringResource(R.string.input_note_content),
                                    color = AppleNotesStyle.TertiaryText,
                                    style = contentTextStyle
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }
        }
    }

    if (showDeleteConfirm) {
        AppleAlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.delete_note)) },
            text = { Text(stringResource(R.string.delete_note_message)) },
            confirmButton = {
                TextButton(onClick = { showDeleteConfirm = false; viewModel.deleteNote { navController.popBackStackStable(Screen.Dashboard.route) } }) {
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
    if (!error.isNullOrBlank()) {
        AppleAlertDialog(
            onDismissRequest = viewModel::clearError,
            title = { Text(stringResource(R.string.note_member_limit_title)) },
            text = { Text(error.orEmpty()) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearError()
                    navController.navigateStable(Screen.Membership.route)
                }) {
                    Text(stringResource(R.string.membership_purchase_button), color = AppleNotesStyle.Accent)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::clearError) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
    if (showHistoryDialog) {
        NoteHistoryDialog(
            revisions = revisions,
            onRestore = { revision ->
                viewModel.restoreRevision(revision)
                showHistoryDialog = false
            },
            onDismiss = { showHistoryDialog = false }
        )
    }
    if (showFolderDialog) {
        NoteFolderPickerDialog(
            folders = folderOptions,
            selectedFolderId = categoryId,
            onSelect = { newFolderId ->
                viewModel.onCategoryChange(newFolderId)
                showFolderDialog = false
            },
            onDismiss = { showFolderDialog = false }
        )
    }
    if (showTagDialog) {
        AddTagDialog(
            value = tagInput,
            onValueChange = { tagInput = it },
            onConfirm = {
                viewModel.addTag(tagInput)
                tagInput = ""
                showTagDialog = false
            },
            onDismiss = {
                tagInput = ""
                showTagDialog = false
            }
        )
    }
    if (showMetadataDialog) {
        NoteMetadataDialog(
            folderName = selectedFolderName ?: stringResource(R.string.uncategorized),
            tags = tags,
            attachments = attachments,
            onFolderClick = {
                showMetadataDialog = false
                showFolderDialog = true
            },
            onAddTag = {
                tagInput = ""
                showMetadataDialog = false
                showTagDialog = true
            },
            onRemoveTag = viewModel::removeTag,
            onOpenAttachment = { uri -> openAttachment(context, uri) },
            onRemoveAttachment = viewModel::removeAttachment,
            onAddAttachment = {
                showMetadataDialog = false
                showAttachmentAccessDialog = true
            },
            onDismiss = { showMetadataDialog = false }
        )
    }
    if (showAttachmentAccessDialog) {
        AppleAlertDialog(
            onDismissRequest = { showAttachmentAccessDialog = false },
            title = { Text(attachmentDisclosure.title) },
            text = { Text(attachmentDisclosure.message, color = AppleNotesStyle.SecondaryText) },
            confirmButton = {
                TextButton(onClick = {
                    showAttachmentAccessDialog = false
                    attachmentLauncher.launch(arrayOf("*/*"))
                }) {
                    Text(attachmentDisclosure.confirmLabel, color = AppleNotesStyle.Accent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAttachmentAccessDialog = false }) {
                    Text(attachmentDisclosure.dismissLabel)
                }
            }
        )
    }
    if (showExportAccessDialog) {
        AppleAlertDialog(
            onDismissRequest = { showExportAccessDialog = false },
            title = { Text(exportDisclosure.title) },
            text = { Text(exportDisclosure.message, color = AppleNotesStyle.SecondaryText) },
            confirmButton = {
                TextButton(onClick = {
                    showExportAccessDialog = false
                    exportLauncher.launch(noteExportFileName(title))
                }) {
                    Text(exportDisclosure.confirmLabel, color = AppleNotesStyle.Accent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportAccessDialog = false }) {
                    Text(exportDisclosure.dismissLabel)
                }
            }
        )
    }
}

@Composable
private fun NoteMetadataSummaryRow(
    summary: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = Color.Transparent,
        shape = AppleNotesStyle.SearchShape
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 2.dp, vertical = NOTE_METADATA_ROW_VERTICAL_PADDING_DP.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Lucide.Folder, contentDescription = null, tint = AppleNotesStyle.Accent, modifier = Modifier.size(18.dp))
            Text(
                text = summary,
                modifier = Modifier.weight(1f),
                color = AppleNotesStyle.SecondaryText,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(R.string.note_info),
                color = AppleNotesStyle.Accent,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            Icon(Lucide.ChevronRight, contentDescription = null, tint = AppleNotesStyle.TertiaryText, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun NoteMetadataDialog(
    folderName: String,
    tags: List<String>,
    attachments: List<String>,
    onFolderClick: () -> Unit,
    onAddTag: () -> Unit,
    onRemoveTag: (String) -> Unit,
    onOpenAttachment: (String) -> Unit,
    onRemoveAttachment: (String) -> Unit,
    onAddAttachment: () -> Unit,
    onDismiss: () -> Unit
) {
    AppleAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.note_info)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                NoteFolderChip(
                    folderName = folderName,
                    onClick = onFolderClick
                )
                NoteTagsRow(
                    tags = tags,
                    onAdd = onAddTag,
                    onRemove = onRemoveTag
                )
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onAddAttachment),
                    color = AppleNotesStyle.SearchSurface,
                    shape = AppleNotesStyle.SearchShape
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Lucide.FilePlus, contentDescription = null, tint = AppleNotesStyle.Accent, modifier = Modifier.size(18.dp))
                        Text(
                            text = stringResource(R.string.add_attachment),
                            color = AppleNotesStyle.PrimaryText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                if (attachments.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .height(220.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        AttachmentList(
                            attachments = attachments,
                            onOpen = onOpenAttachment,
                            onRemove = onRemoveAttachment
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun NoteFolderChip(
    folderName: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        color = AppleNotesStyle.SearchSurface,
        shape = CircleShape
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Lucide.Folder, contentDescription = null, tint = AppleNotesStyle.Accent, modifier = Modifier.size(18.dp))
            Text(
                text = stringResource(R.string.note_folder_label, folderName),
                color = AppleNotesStyle.SecondaryText,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            Icon(Lucide.ChevronDown, contentDescription = null, tint = AppleNotesStyle.TertiaryText, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun NoteHistoryDialog(
    revisions: List<NoteRevisionEntity>,
    onRestore: (NoteRevisionEntity) -> Unit,
    onDismiss: () -> Unit
) {
    AppleAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.note_history_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = stringResource(R.string.note_history_description),
                    color = AppleNotesStyle.SecondaryText,
                    style = MaterialTheme.typography.bodySmall
                )
                if (revisions.isEmpty()) {
                    Text(
                        text = stringResource(R.string.note_history_empty),
                        color = AppleNotesStyle.SecondaryText,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .height(360.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        revisions.forEach { revision ->
                            NoteRevisionRow(
                                revision = revision,
                                onRestore = { onRestore(revision) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun NoteRevisionRow(
    revision: NoteRevisionEntity,
    onRestore: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AppleNotesStyle.SearchSurface,
        shape = AppleNotesStyle.SearchShape
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Lucide.Clock, contentDescription = null, tint = AppleNotesStyle.Accent, modifier = Modifier.size(16.dp))
                Text(
                    text = formatRevisionTime(revision.savedAt),
                    color = AppleNotesStyle.SecondaryText,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
            }
            Text(
                text = revision.title.ifBlank { stringResource(R.string.untitled_note) },
                color = AppleNotesStyle.PrimaryText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = revision.content.lineSequence().firstOrNull { it.isNotBlank() }?.take(120)
                    ?: stringResource(R.string.no_content_note),
                color = AppleNotesStyle.SecondaryText,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            TextButton(onClick = onRestore, modifier = Modifier.align(Alignment.End)) {
                Text(stringResource(R.string.note_history_restore), color = AppleNotesStyle.Accent)
            }
        }
    }
}

@Composable
private fun AttachmentList(
    attachments: List<String>,
    onOpen: (String) -> Unit,
    onRemove: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.attachments),
            color = AppleNotesStyle.SecondaryText,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
        attachments.forEach { attachment ->
            AttachmentRow(
                attachment = attachment,
                onOpen = { onOpen(attachment) },
                onRemove = { onRemove(attachment) }
            )
        }
    }
}

@Composable
private fun AttachmentRow(
    attachment: String,
    onOpen: () -> Unit,
    onRemove: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AppleNotesStyle.SearchSurface,
        shape = AppleNotesStyle.SearchShape
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpen)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Lucide.FileText, contentDescription = null, tint = AppleNotesStyle.Accent, modifier = Modifier.size(18.dp))
            Text(
                text = attachmentDisplayName(attachment),
                modifier = Modifier.weight(1f),
                color = AppleNotesStyle.PrimaryText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
            Icon(
                Lucide.X,
                contentDescription = stringResource(R.string.remove_attachment),
                tint = AppleNotesStyle.SecondaryText,
                modifier = Modifier
                    .size(18.dp)
                    .clickable(onClick = onRemove)
            )
        }
    }
}

@Composable
private fun NoteTagsRow(
    tags: List<String>,
    onAdd: () -> Unit,
    onRemove: (String) -> Unit
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        tags.forEach { tag ->
            NoteTagChip(tag = tag, onRemove = { onRemove(tag) })
        }
        Surface(
            modifier = Modifier.clickable(onClick = onAdd),
            color = AppleNotesStyle.SearchSurface,
            shape = CircleShape
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Lucide.Plus, contentDescription = null, tint = AppleNotesStyle.Accent, modifier = Modifier.size(16.dp))
                Text(
                    text = stringResource(R.string.add_tag),
                    color = AppleNotesStyle.Accent,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun NoteTagChip(
    tag: String,
    onRemove: () -> Unit
) {
    Surface(
        color = AppleNotesStyle.AccentSoft,
        shape = CircleShape
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "#$tag",
                color = AppleNotesStyle.PrimaryText,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            Icon(
                Lucide.X,
                contentDescription = stringResource(R.string.remove_tag),
                tint = AppleNotesStyle.SecondaryText,
                modifier = Modifier
                    .size(16.dp)
                    .clickable(onClick = onRemove)
            )
        }
    }
}

