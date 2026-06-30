package com.tourisain.weijian.presentation.category

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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.tourisain.weijian.R
import com.tourisain.weijian.data.database.entity.NoteCategoryEntity
import com.tourisain.weijian.presentation.common.AppleAlertDialog
import com.tourisain.weijian.presentation.common.navigateStable
import com.tourisain.weijian.presentation.common.popBackStackStable
import com.tourisain.weijian.presentation.icons.Lucide
import com.tourisain.weijian.presentation.navigation.Screen
import com.tourisain.weijian.presentation.note.components.AppleNotesStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManagementScreen(
    navController: NavController,
    viewModel: CategoryViewModel = hiltViewModel()
) {
    val summaries by viewModel.categorySummaries.collectAsStateWithLifecycle()
    val uncategorizedCount by viewModel.uncategorizedCount.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showEditDialog by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<NoteCategoryEntity?>(null) }
    var editingCategory by remember { mutableStateOf<NoteCategoryEntity?>(null) }
    var name by remember { mutableStateOf("") }
    var color by remember { mutableStateOf(DefaultCategoryColors.first()) }

    fun openCreateDialog() {
        editingCategory = null
        name = ""
        color = DefaultCategoryColors.first()
        showEditDialog = true
    }

    fun openEditDialog(category: NoteCategoryEntity) {
        editingCategory = category
        name = category.name
        color = category.color
        showEditDialog = true
    }

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
                    IconButton(onClick = { openCreateDialog() }) {
                        Icon(Lucide.Plus, contentDescription = stringResource(R.string.create), tint = AppleNotesStyle.Accent)
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
                        text = stringResource(R.string.note_folder_management),
                        color = AppleNotesStyle.PrimaryText,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.note_category_summary, summaries.size, uncategorizedCount),
                        color = AppleNotesStyle.SecondaryText,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            item {
                Surface(color = AppleNotesStyle.Surface, shape = AppleNotesStyle.GroupShape) {
                    Column {
                        NoteFolderRow(
                            title = stringResource(R.string.uncategorized),
                            subtitle = stringResource(R.string.category_notes_count, uncategorizedCount),
                            color = AppleNotesStyle.SecondaryText,
                            onOpen = { navController.navigateStable(Screen.NoteList.createRoute(UNCATEGORIZED_ROUTE)) }
                        )
                    }
                }
            }

            if (summaries.isEmpty()) {
                item { EmptyCategoryState(onCreate = { openCreateDialog() }) }
            } else {
                item { SectionHeader(stringResource(R.string.note_folders_title)) }
                item {
                    Surface(color = AppleNotesStyle.Surface, shape = AppleNotesStyle.GroupShape) {
                        Column {
                            summaries.forEachIndexed { index, summary ->
                                NoteCategoryRow(
                                    summary = summary,
                                    canMoveUp = index > 0,
                                    canMoveDown = index < summaries.lastIndex,
                                    onOpen = { navController.navigateStable(Screen.NoteList.createRoute(summary.category.id)) },
                                    onEdit = { openEditDialog(summary.category) },
                                    onDelete = { pendingDelete = summary.category },
                                    onMoveUp = { viewModel.moveCategoryUp(summary.category) },
                                    onMoveDown = { viewModel.moveCategoryDown(summary.category) }
                                )
                                if (index != summaries.lastIndex) RowDivider()
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEditDialog) {
        CategoryEditDialog(
            name = name,
            color = color,
            isEditing = editingCategory != null,
            onNameChange = { name = it },
            onColorChange = { color = it },
            onDismiss = {
                showEditDialog = false
                editingCategory = null
            },
            onConfirm = {
                val currentCategory = editingCategory
                if (currentCategory == null) {
                    viewModel.addCategory(name, color)
                } else {
                    viewModel.updateCategory(currentCategory, name, color)
                }
                name = ""
                color = DefaultCategoryColors.first()
                editingCategory = null
                showEditDialog = false
            }
        )
    }

    pendingDelete?.let { category ->
        AppleAlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(stringResource(R.string.delete)) },
            text = { Text(stringResource(R.string.delete_note_category_message, category.name)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteCategory(category)
                    pendingDelete = null
                }) {
                    Text(stringResource(R.string.delete), color = AppleNotesStyle.Destructive)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun CategoryEditDialog(
    name: String,
    color: String,
    isEditing: Boolean,
    onNameChange: (String) -> Unit,
    onColorChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AppleAlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (isEditing) stringResource(R.string.edit_note_folder) else stringResource(R.string.add_note_folder))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text(stringResource(R.string.note_folder_name)) },
                    singleLine = true,
                    shape = AppleNotesStyle.SearchShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppleNotesStyle.Accent,
                        unfocusedBorderColor = AppleNotesStyle.Separator,
                        focusedLabelColor = AppleNotesStyle.Accent,
                        cursorColor = AppleNotesStyle.Accent
                    )
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.note_folder_color), color = AppleNotesStyle.SecondaryText, style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        DefaultCategoryColors.forEach { option ->
                            val swatchColor = parseColor(option)
                            Surface(
                                modifier = Modifier
                                    .size(if (option == color) 34.dp else 28.dp)
                                    .clickable { onColorChange(option) },
                                color = swatchColor,
                                shape = CircleShape,
                                tonalElevation = if (option == color) 3.dp else 0.dp
                            ) {}
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel))
                }
                TextButton(onClick = onConfirm, enabled = name.isNotBlank()) {
                    Text(
                        if (isEditing) stringResource(R.string.save) else stringResource(R.string.create),
                        color = AppleNotesStyle.Accent
                    )
                }
            }
        }
    )
}

@Composable
private fun NoteCategoryRow(
    summary: NoteCategorySummary,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CategoryIcon(color = parseColor(summary.category.color))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = summary.category.name,
                color = AppleNotesStyle.PrimaryText,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                stringResource(R.string.category_notes_count, summary.noteCount),
                color = AppleNotesStyle.SecondaryText,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onMoveUp, enabled = canMoveUp, modifier = Modifier.size(36.dp)) {
                Icon(Lucide.ChevronUp, contentDescription = null, tint = if (canMoveUp) AppleNotesStyle.SecondaryText else AppleNotesStyle.TertiaryText, modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = onMoveDown, enabled = canMoveDown, modifier = Modifier.size(36.dp)) {
                Icon(Lucide.ChevronDown, contentDescription = null, tint = if (canMoveDown) AppleNotesStyle.SecondaryText else AppleNotesStyle.TertiaryText, modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                Icon(Lucide.Pencil, contentDescription = stringResource(R.string.edit), tint = AppleNotesStyle.Accent, modifier = Modifier.size(19.dp))
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(Lucide.Trash2, contentDescription = stringResource(R.string.delete), tint = AppleNotesStyle.Destructive, modifier = Modifier.size(19.dp))
            }
        }
    }
}

@Composable
private fun NoteFolderRow(
    title: String,
    subtitle: String,
    color: Color,
    onOpen: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CategoryIcon(color = color)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, color = AppleNotesStyle.PrimaryText, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = AppleNotesStyle.SecondaryText, style = MaterialTheme.typography.bodySmall)
        }
        Icon(Lucide.ChevronRight, contentDescription = null, tint = AppleNotesStyle.TertiaryText, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun CategoryIcon(color: Color) {
    Surface(color = color.copy(alpha = 0.14f), shape = CircleShape) {
        Box(modifier = Modifier.size(34.dp), contentAlignment = Alignment.Center) {
            Icon(Lucide.Folder, contentDescription = null, tint = color, modifier = Modifier.size(19.dp))
        }
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
private fun EmptyCategoryState(onCreate: () -> Unit) {
    Surface(color = AppleNotesStyle.Surface, shape = AppleNotesStyle.GroupShape) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Lucide.Folder, contentDescription = null, tint = AppleNotesStyle.Accent, modifier = Modifier.size(36.dp))
            Text(stringResource(R.string.category_empty_hint), color = AppleNotesStyle.SecondaryText)
            TextButton(onClick = onCreate) {
                Text(stringResource(R.string.create), color = AppleNotesStyle.Accent)
            }
        }
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

private fun parseColor(value: String): Color {
    return runCatching { Color(android.graphics.Color.parseColor(value)) }
        .getOrDefault(AppleNotesStyle.Accent)
}

private val DefaultCategoryColors = listOf(
    "#FFB800",
    "#34C759",
    "#32ADE6",
    "#AF52DE",
    "#FF3B30"
)

private const val UNCATEGORIZED_ROUTE = "uncategorized"
