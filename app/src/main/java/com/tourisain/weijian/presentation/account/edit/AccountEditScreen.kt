package com.tourisain.weijian.presentation.account.edit

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import com.tourisain.weijian.presentation.common.AppleAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tourisain.weijian.R
import com.tourisain.weijian.presentation.common.popBackStackStable
import com.tourisain.weijian.presentation.icons.Lucide
import com.tourisain.weijian.presentation.navigation.Screen
import com.tourisain.weijian.presentation.note.components.AppleNotesStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountEditScreen(
    navController: NavController,
    viewModel: AccountEditViewModel = hiltViewModel()
) {
    val amount by viewModel.amount.collectAsStateWithLifecycle()
    val type by viewModel.type.collectAsStateWithLifecycle()
    val category by viewModel.category.collectAsStateWithLifecycle()
    val note by viewModel.note.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val autoEnabled by viewModel.autoEnabled.collectAsStateWithLifecycle()
    val autoFrequency by viewModel.autoFrequency.collectAsStateWithLifecycle()
    val imageUri by viewModel.imageUri.collectAsStateWithLifecycle()
    val smartSuggestionsEnabled by viewModel.smartSuggestionsEnabled.collectAsStateWithLifecycle()
    val showAddCategoryDialog by viewModel.showAddCategoryDialog.collectAsStateWithLifecycle()
    val showProDialog by viewModel.showProDialog.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(error) {
        val currentError = error
        if (!currentError.isNullOrBlank()) {
            snackbarHostState.showSnackbar(currentError)
            viewModel.clearError()
        }
    }

    if (showAddCategoryDialog) {
        AddCategoryDialog(
            onDismiss = viewModel::dismissAddCategoryDialog,
            onConfirm = viewModel::addCategory
        )
    }

    if (showProDialog) {
        AppleAlertDialog(
            onDismissRequest = viewModel::dismissProDialog,
            title = { Text(stringResource(R.string.membership_feature_title)) },
            text = { Text(stringResource(R.string.custom_category_premium_message)) },
            confirmButton = {
                TextButton(onClick = viewModel::dismissProDialog) {
                    Text(stringResource(R.string.ok), color = AppleNotesStyle.Accent)
                }
            }
        )
    }

    Scaffold(
        containerColor = AppleNotesStyle.Background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStackStable(Screen.AccountList.route) }) {
                        Icon(Lucide.ChevronLeft, contentDescription = stringResource(R.string.back), tint = AppleNotesStyle.Accent)
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.saveRecord { navController.popBackStackStable(Screen.AccountList.route) } }, enabled = !isLoading) {
                        Text(stringResource(R.string.done), color = AppleNotesStyle.Accent, fontWeight = FontWeight.SemiBold)
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
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = if (amount.isBlank() && category.isBlank()) stringResource(R.string.new_account_record) else stringResource(R.string.account_record),
                        color = AppleNotesStyle.PrimaryText,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(if (type == "income") stringResource(R.string.income_record) else stringResource(R.string.expense_record), color = AppleNotesStyle.SecondaryText)
                }
            }
            item {
                TypePicker(type = type, onTypeChange = viewModel::onTypeChange)
            }
            item {
                AccountEditForm(
                    amount = amount,
                    category = category,
                    note = note,
                    onAmountChange = viewModel::onAmountChange,
                    onCategoryChange = viewModel::onCategoryChange,
                    onNoteChange = viewModel::onNoteChange
                )
            }
            item {
                CategoryPicker(
                    selected = category,
                    categories = categories.map { it.name },
                    onSelect = viewModel::onCategoryChange,
                    onAdd = viewModel::onAddCategoryClick
                )
            }
            if (smartSuggestionsEnabled) item {
                SmartTools(
                    autoEnabled = autoEnabled,
                    autoFrequency = autoFrequency,
                    hasImage = !imageUri.isNullOrBlank(),
                    onAutoEnabledChange = viewModel::onAutoEnabledChange,
                    onAutoFrequencyChange = viewModel::onAutoFrequencyChange,
                    onApplyAi = viewModel::applyAiFromNote,
                    onClearImage = viewModel::clearImage
                )
            }
            item {
                Button(
                    onClick = { viewModel.saveRecord { navController.popBackStackStable(Screen.AccountList.route) } },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = AppleNotesStyle.ButtonShape,
                    colors = ButtonDefaults.buttonColors(containerColor = AppleNotesStyle.Accent, contentColor = Color.Black)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.Black)
                    } else {
                        Icon(Lucide.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(stringResource(R.string.save_account_record), fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun TypePicker(type: String, onTypeChange: (String) -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), color = AppleNotesStyle.Surface, shape = AppleNotesStyle.GroupShape) {
        Row(modifier = Modifier.padding(10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TypeButton(
                selected = type == "expense",
                title = stringResource(R.string.expense),
                iconTint = AppleNotesStyle.Destructive,
                icon = Lucide.Wallet,
                modifier = Modifier.weight(1f),
                onClick = { onTypeChange("expense") }
            )
            TypeButton(
                selected = type == "income",
                title = stringResource(R.string.income),
                iconTint = Color(0xFF248A3D),
                icon = Lucide.ArrowUpDown,
                modifier = Modifier.weight(1f),
                onClick = { onTypeChange("income") }
            )
        }
    }
}

@Composable
private fun TypeButton(
    selected: Boolean,
    title: String,
    iconTint: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(title) },
        leadingIcon = {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
        },
        modifier = modifier,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = AppleNotesStyle.AccentSoft,
            selectedLabelColor = AppleNotesStyle.PrimaryText
        )
    )
}

@Composable
private fun AccountEditForm(
    amount: String,
    category: String,
    note: String,
    onAmountChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onNoteChange: (String) -> Unit
) {
    Surface(modifier = Modifier.fillMaxWidth(), color = AppleNotesStyle.Surface, shape = AppleNotesStyle.GroupShape) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AccountTextField(
                value = amount,
                onValueChange = onAmountChange,
                label = stringResource(R.string.amount_label),
                keyboardType = KeyboardType.Decimal,
                singleLine = true
            )
            AccountTextField(
                value = category,
                onValueChange = onCategoryChange,
                label = stringResource(R.string.category_label),
                singleLine = true
            )
            AccountTextField(
                value = note,
                onValueChange = onNoteChange,
                label = stringResource(R.string.note_label),
                minLines = 5
            )
        }
    }
}

@Composable
private fun AccountTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = false,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = singleLine,
        minLines = minLines,
        shape = AppleNotesStyle.SearchShape,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AppleNotesStyle.Accent,
            unfocusedBorderColor = AppleNotesStyle.Separator,
            focusedLabelColor = AppleNotesStyle.Accent,
            cursorColor = AppleNotesStyle.Accent
        )
    )
}

@Composable
private fun CategoryPicker(
    selected: String,
    categories: List<String>,
    onSelect: (String) -> Unit,
    onAdd: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxWidth(), color = AppleNotesStyle.Surface, shape = AppleNotesStyle.GroupShape) {
        Column(modifier = Modifier.padding(vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.common_categories), color = AppleNotesStyle.PrimaryText, fontWeight = FontWeight.SemiBold)
                TextButton(onClick = onAdd) { Text(stringResource(R.string.add), color = AppleNotesStyle.Accent) }
            }
            if (categories.isEmpty()) {
                Text(stringResource(R.string.category_auto_save_hint), color = AppleNotesStyle.SecondaryText, modifier = Modifier.padding(horizontal = 16.dp))
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories.distinct()) { name ->
                        FilterChip(
                            selected = selected == name,
                            onClick = { onSelect(name) },
                            label = { Text(name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AppleNotesStyle.AccentSoft,
                                selectedLabelColor = AppleNotesStyle.PrimaryText
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SmartTools(
    autoEnabled: Boolean,
    autoFrequency: String,
    hasImage: Boolean,
    onAutoEnabledChange: (Boolean) -> Unit,
    onAutoFrequencyChange: (String) -> Unit,
    onApplyAi: () -> Unit,
    onClearImage: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxWidth(), color = AppleNotesStyle.Surface, shape = AppleNotesStyle.GroupShape) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(34.dp).background(AppleNotesStyle.AccentSoft, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Lucide.Sparkles, contentDescription = null, tint = Color(0xFF9A6A00), modifier = Modifier.size(18.dp))
                }
                Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                    Text(stringResource(R.string.smart_organize), color = AppleNotesStyle.PrimaryText, fontWeight = FontWeight.SemiBold)
                    Text(stringResource(R.string.smart_organize_subtitle), color = AppleNotesStyle.SecondaryText, style = MaterialTheme.typography.bodySmall)
                }
                TextButton(onClick = onApplyAi) { Text(stringResource(R.string.extract), color = AppleNotesStyle.Accent) }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.auto_save_title), color = AppleNotesStyle.PrimaryText, fontWeight = FontWeight.SemiBold)
                    Text(stringResource(R.string.auto_save_account_subtitle), color = AppleNotesStyle.SecondaryText, style = MaterialTheme.typography.bodySmall)
                }
                Switch(checked = autoEnabled, onCheckedChange = onAutoEnabledChange)
            }
            if (autoEnabled) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        "none" to stringResource(R.string.manual),
                        "daily" to stringResource(R.string.daily),
                        "weekly" to stringResource(R.string.weekly),
                        "monthly" to stringResource(R.string.monthly)
                    ).forEach { (value, label) ->
                        FilterChip(
                            selected = autoFrequency == value,
                            onClick = { onAutoFrequencyChange(value) },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = AppleNotesStyle.AccentSoft)
                        )
                    }
                }
            }
            if (hasImage) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Lucide.Image, contentDescription = null, tint = AppleNotesStyle.Accent)
                    Text(stringResource(R.string.receipt_image_attached), color = AppleNotesStyle.SecondaryText, modifier = Modifier.weight(1f).padding(start = 10.dp))
                    TextButton(onClick = onClearImage) { Text(stringResource(R.string.remove), color = AppleNotesStyle.Destructive) }
                }
            }
        }
    }
}

@Composable
private fun AddCategoryDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    AppleAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_category)) },
        text = {
            AccountTextField(
                value = name,
                onValueChange = { name = it },
                label = stringResource(R.string.category_name),
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name) }, enabled = name.isNotBlank()) {
                Text(stringResource(R.string.add), color = AppleNotesStyle.Accent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}
