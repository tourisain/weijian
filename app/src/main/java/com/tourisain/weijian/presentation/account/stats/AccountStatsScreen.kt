package com.tourisain.weijian.presentation.account.stats

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tourisain.weijian.R
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
fun AccountStatsScreen(navController: NavController) {
    val viewModel = hiltViewModel<AccountStatsViewModel>()
    val categoryStats by viewModel.categoryStats.collectAsStateWithLifecycle()
    val dailyStats by viewModel.dailyStats.collectAsStateWithLifecycle()
    val dateFormat = remember { DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault()) }
    val totalIncome = dailyStats.sumOf { it.income }
    val totalExpense = dailyStats.sumOf { it.expense }

    Scaffold(
        containerColor = AppleNotesStyle.Background,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStackStable(Screen.AccountList.route) }) {
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = stringResource(R.string.stats),
                        color = AppleNotesStyle.PrimaryText,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(stringResource(R.string.recent_7_days_accounting), color = AppleNotesStyle.SecondaryText)
                }
            }
            item {
                StatsSummaryCard(totalIncome = totalIncome, totalExpense = totalExpense)
            }
            item {
                SectionTitle(stringResource(R.string.expense_categories))
            }
            item {
                if (categoryStats.isEmpty()) {
                    EmptyStatsState(stringResource(R.string.account_stats_empty_expense_categories))
                } else {
                    Surface(modifier = Modifier.fillMaxWidth(), color = AppleNotesStyle.Surface, shape = AppleNotesStyle.GroupShape) {
                        Column {
                            categoryStats.forEachIndexed { index, stat ->
                                CategoryStatRow(stat)
                                if (index != categoryStats.lastIndex) {
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
            item {
                SectionTitle(stringResource(R.string.daily_income_expense))
            }
            item {
                Surface(modifier = Modifier.fillMaxWidth(), color = AppleNotesStyle.Surface, shape = AppleNotesStyle.GroupShape) {
                    Column {
                        dailyStats.forEachIndexed { index, stat ->
                            DailyStatRow(stat = stat, dateText = dateFormat.format(Date(stat.date)))
                            if (index != dailyStats.lastIndex) {
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

@Composable
private fun StatsSummaryCard(totalIncome: Double, totalExpense: Double) {
    val balance = totalIncome - totalExpense
    Surface(modifier = Modifier.fillMaxWidth(), color = AppleNotesStyle.Surface, shape = AppleNotesStyle.GroupShape) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(stringResource(R.string.seven_day_balance), color = AppleNotesStyle.SecondaryText)
            Text(
                money(balance),
                color = if (balance >= 0) AppleNotesStyle.PrimaryText else AppleNotesStyle.Destructive,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatMetric(stringResource(R.string.income), money(totalIncome), Color(0xFF248A3D), Modifier.weight(1f))
                StatMetric(stringResource(R.string.expense), money(totalExpense), AppleNotesStyle.Destructive, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatMetric(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, color = AppleNotesStyle.SearchSurface, shape = AppleNotesStyle.SearchShape) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, color = AppleNotesStyle.SecondaryText, style = MaterialTheme.typography.labelMedium)
            Text(value, color = color, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun CategoryStatRow(stat: CategoryStat) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(color = AppleNotesStyle.AccentSoft, shape = CircleShape) {
            Box(modifier = Modifier.size(38.dp), contentAlignment = Alignment.Center) {
                Icon(Lucide.Tag, contentDescription = null, tint = Color(0xFF9A6A00), modifier = Modifier.size(20.dp))
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stat.category, color = AppleNotesStyle.PrimaryText, fontWeight = FontWeight.SemiBold)
                Text(money(stat.amount), color = AppleNotesStyle.SecondaryText)
            }
            LinearProgressIndicator(
                progress = { stat.percentage.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(),
                color = AppleNotesStyle.Accent,
                trackColor = AppleNotesStyle.SearchSurface
            )
            Text("${String.format(Locale.getDefault(), "%.1f", stat.percentage * 100f)}%", color = AppleNotesStyle.TertiaryText, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun DailyStatRow(stat: DailyStat, dateText: String) {
    val incomeLabel = stringResource(R.string.income)
    val expenseLabel = stringResource(R.string.expense)
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(color = AppleNotesStyle.SearchSurface, shape = CircleShape) {
            Box(modifier = Modifier.size(38.dp), contentAlignment = Alignment.Center) {
                Icon(Lucide.Calendar, contentDescription = null, tint = AppleNotesStyle.SecondaryText, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(dateText, color = AppleNotesStyle.PrimaryText, fontWeight = FontWeight.SemiBold)
            Text("$incomeLabel ${money(stat.income)}", color = Color(0xFF248A3D), style = MaterialTheme.typography.bodySmall)
        }
        Text("$expenseLabel ${money(stat.expense)}", color = AppleNotesStyle.Destructive, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, color = AppleNotesStyle.SecondaryText, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
}

@Composable
private fun EmptyStatsState(text: String) {
    Surface(modifier = Modifier.fillMaxWidth(), color = AppleNotesStyle.Surface, shape = AppleNotesStyle.GroupShape) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Lucide.ChartBar, contentDescription = null, tint = AppleNotesStyle.Accent, modifier = Modifier.size(40.dp))
            Text(text, color = AppleNotesStyle.SecondaryText)
        }
    }
}

private fun money(value: Double): String = NumberFormat.getCurrencyInstance(Locale.getDefault()).format(value)
