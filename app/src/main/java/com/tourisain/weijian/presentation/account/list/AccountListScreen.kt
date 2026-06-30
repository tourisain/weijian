package com.tourisain.weijian.presentation.account.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.HorizontalDivider
import com.tourisain.weijian.presentation.common.AppleAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tourisain.weijian.R
import com.tourisain.weijian.data.database.entity.AccountRecordEntity
import com.tourisain.weijian.presentation.common.navigateStable
import com.tourisain.weijian.presentation.common.popBackStackStable
import com.tourisain.weijian.presentation.icons.Lucide
import com.tourisain.weijian.presentation.navigation.Screen
import com.tourisain.weijian.presentation.note.components.AppleNotesStyle
import java.text.DateFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountListScreen(
    navController: NavController,
    viewModel: AccountListViewModel = hiltViewModel()
) {
    val records by viewModel.records.collectAsStateWithLifecycle()
    val totalIncome by viewModel.totalIncome.collectAsStateWithLifecycle()
    val totalExpense by viewModel.totalExpense.collectAsStateWithLifecycle()
    val balance by viewModel.balance.collectAsStateWithLifecycle()
    val showProDialog by viewModel.showProDialog.collectAsStateWithLifecycle()
    val isUserPro by viewModel.isUserPro.collectAsStateWithLifecycle()
    val allRecordCount by viewModel.allRecordCount.collectAsStateWithLifecycle()
    val currentMonth by viewModel.currentMonth.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val filterType by viewModel.filterType.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showLimitDialog by remember { mutableStateOf(false) }
    val openNewRecord = {
        if (isUserPro || allRecordCount < FREE_ACCOUNT_RECORD_LIMIT) {
            navController.navigateStable(Screen.AccountEdit.createRoute("new"))
        } else {
            showLimitDialog = true
        }
    }

    LaunchedEffect(error) {
        val currentError = error
        if (!currentError.isNullOrBlank()) {
            snackbarHostState.showSnackbar(currentError)
            viewModel.clearError()
        }
    }

    if (showProDialog) {
        AppleAlertDialog(
            onDismissRequest = { viewModel.dismissProDialog() },
            title = { Text(stringResource(R.string.membership_feature_title)) },
            text = { Text(stringResource(R.string.account_stats_premium_message)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.dismissProDialog()
                    navController.navigateStable(Screen.Profile.route)
                }) { Text(stringResource(R.string.upgrade_now_button), color = AppleNotesStyle.Accent) }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissProDialog() }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    if (showLimitDialog) {
        AppleAlertDialog(
            onDismissRequest = { showLimitDialog = false },
            title = { Text(stringResource(R.string.account_member_limit_title)) },
            text = { Text(stringResource(R.string.account_member_limit_message)) },
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

    Scaffold(
        containerColor = AppleNotesStyle.Background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStackStable(Screen.Dashboard.route) }) {
                        Icon(Lucide.ChevronLeft, contentDescription = stringResource(R.string.back), tint = AppleNotesStyle.Accent)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (isUserPro) navController.navigateStable(Screen.AccountStats.route) else viewModel.onStatsClick()
                    }) {
                        Icon(Lucide.ChartBar, contentDescription = stringResource(R.string.stats), tint = AppleNotesStyle.Accent)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppleNotesStyle.Background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = openNewRecord,
                shape = CircleShape,
                containerColor = AppleNotesStyle.Accent,
                contentColor = Color.Black
            ) {
                Icon(Lucide.Plus, contentDescription = stringResource(R.string.add_account_record))
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(AppleNotesStyle.Background)
                .padding(paddingValues),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = stringResource(R.string.accounting),
                        color = AppleNotesStyle.PrimaryText,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(stringResource(R.string.account_records_count, records.size), color = AppleNotesStyle.SecondaryText, style = MaterialTheme.typography.bodyMedium)
                }
            }
            item {
                MonthHeader(
                    currentMonth = currentMonth,
                    onPrevious = viewModel::onPreviousMonth,
                    onNext = viewModel::onNextMonth
                )
            }
            item {
                SummaryCard(totalIncome = totalIncome, totalExpense = totalExpense, balance = balance)
            }
            item {
                AccountSearchField(value = searchQuery, onValueChange = viewModel::onSearchQueryChange)
            }
            item {
                AccountFilterChips(selected = filterType, onSelected = viewModel::onFilterTypeChange)
            }
            item {
                if (records.isEmpty()) {
                    EmptyAccountList(onCreate = openNewRecord)
                } else {
                    Surface(modifier = Modifier.fillMaxWidth(), color = AppleNotesStyle.Surface, shape = AppleNotesStyle.GroupShape) {
                        Column {
                            records.forEachIndexed { index, record ->
                                AccountRecordItem(
                                    record = record,
                                    onClick = { navController.navigateStable(Screen.AccountEdit.createRoute(record.id)) },
                                    onDelete = { viewModel.deleteRecord(record) }
                                )
                                if (index != records.lastIndex) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(start = 64.dp),
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
}

private const val FREE_ACCOUNT_RECORD_LIMIT = 3

@Composable
private fun MonthHeader(currentMonth: Long, onPrevious: () -> Unit, onNext: () -> Unit) {
    val formatter = remember { SimpleDateFormat("yyyy MMM", Locale.getDefault()) }
    Surface(modifier = Modifier.fillMaxWidth(), color = AppleNotesStyle.Surface, shape = AppleNotesStyle.GroupShape) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrevious) {
                Icon(Lucide.ChevronLeft, contentDescription = stringResource(R.string.previous_month), tint = AppleNotesStyle.Accent)
            }
            Text(
                formatter.format(Date(currentMonth)),
                color = AppleNotesStyle.PrimaryText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            IconButton(onClick = onNext) {
                Icon(Lucide.ChevronRight, contentDescription = stringResource(R.string.next_month), tint = AppleNotesStyle.Accent)
            }
        }
    }
}

@Composable
private fun SummaryCard(totalIncome: Double, totalExpense: Double, balance: Double) {
    val formatter = remember { NumberFormat.getCurrencyInstance(Locale.getDefault()) }
    Surface(modifier = Modifier.fillMaxWidth(), color = AppleNotesStyle.Surface, shape = AppleNotesStyle.GroupShape) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(stringResource(R.string.balance), color = AppleNotesStyle.SecondaryText, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = money(balance, formatter),
                color = if (balance >= 0) AppleNotesStyle.PrimaryText else AppleNotesStyle.Destructive,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SummaryMetric(
                    title = stringResource(R.string.income),
                    value = money(totalIncome, formatter),
                    color = Color(0xFF248A3D),
                    modifier = Modifier.weight(1f)
                )
                SummaryMetric(
                    title = stringResource(R.string.expense),
                    value = money(totalExpense, formatter),
                    color = AppleNotesStyle.Destructive,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SummaryMetric(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, color = AppleNotesStyle.SearchSurface, shape = AppleNotesStyle.SearchShape) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, color = AppleNotesStyle.SecondaryText, style = MaterialTheme.typography.labelMedium)
            Text(value, color = color, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun AccountSearchField(value: String, onValueChange: (String) -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), color = AppleNotesStyle.SearchSurface, shape = AppleNotesStyle.SearchShape) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
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
                    if (value.isBlank()) {
                        Text(stringResource(R.string.search_accounts_hint), color = AppleNotesStyle.TertiaryText, fontSize = 16.sp)
                    }
                    innerTextField()
                }
            )
        }
    }
}

@Composable
private fun AccountFilterChips(selected: AccountFilterType, onSelected: (AccountFilterType) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AccountFilterType.values().forEach { type ->
            FilterChip(
                selected = selected == type,
                onClick = { onSelected(type) },
                label = {
                    Text(
                        when (type) {
                            AccountFilterType.ALL -> stringResource(R.string.all)
                            AccountFilterType.INCOME -> stringResource(R.string.income)
                            AccountFilterType.EXPENSE -> stringResource(R.string.expense)
                        }
                    )
                },
                leadingIcon = if (selected == type) {
                    { Icon(Lucide.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                } else {
                    null
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AppleNotesStyle.AccentSoft,
                    selectedLabelColor = AppleNotesStyle.PrimaryText
                )
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AccountRecordItem(
    record: AccountRecordEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val formatter = remember { DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault()) }
    val moneyFormatter = remember { NumberFormat.getCurrencyInstance(Locale.getDefault()) }
    val isIncome = record.type == "income"
    val amountColor = if (isIncome) Color(0xFF248A3D) else AppleNotesStyle.Destructive

    if (showDeleteDialog) {
        AppleAlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_account_record)) },
            text = { Text(stringResource(R.string.delete_account_record_message)) },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteDialog = false
                }) { Text(stringResource(R.string.delete), color = AppleNotesStyle.Destructive) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = { showDeleteDialog = true })
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(if (isIncome) Color(0xFFE8F5EA) else AppleNotesStyle.AccentSoft, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isIncome) Lucide.ArrowUpDown else Lucide.Wallet,
                contentDescription = null,
                tint = if (isIncome) Color(0xFF248A3D) else Color(0xFF9A6A00),
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(record.category.ifBlank { stringResource(R.string.uncategorized) }, color = AppleNotesStyle.PrimaryText, fontWeight = FontWeight.SemiBold)
            Text(
                text = record.note.ifBlank { formatter.format(Date(record.date)) },
                color = AppleNotesStyle.SecondaryText,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1
            )
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = "${if (isIncome) "+" else "-"}${money(record.amount, moneyFormatter)}",
                color = amountColor,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(formatter.format(Date(record.date)), color = AppleNotesStyle.TertiaryText, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun EmptyAccountList(onCreate: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), color = AppleNotesStyle.Surface, shape = AppleNotesStyle.GroupShape) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Lucide.Wallet, contentDescription = null, tint = AppleNotesStyle.Accent, modifier = Modifier.size(40.dp))
            Text(stringResource(R.string.no_account_records_this_month), color = AppleNotesStyle.SecondaryText)
            TextButton(onClick = onCreate) {
                Text(stringResource(R.string.add_one_account_record), color = AppleNotesStyle.Accent)
            }
        }
    }
}

private fun money(value: Double, formatter: NumberFormat): String = formatter.format(value)
