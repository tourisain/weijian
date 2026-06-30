package com.tourisain.weijian.presentation.search

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tourisain.weijian.R
import com.tourisain.weijian.presentation.common.navigateStable
import com.tourisain.weijian.presentation.common.popBackStackStable
import com.tourisain.weijian.presentation.icons.Lucide
import com.tourisain.weijian.presentation.navigation.Screen
import com.tourisain.weijian.presentation.note.components.AppleNotesStyle
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val query by viewModel.searchQuery.collectAsStateWithLifecycle()
    val results by viewModel.searchResults.collectAsStateWithLifecycle()
    val smartSuggestionsEnabled by viewModel.smartSuggestionsEnabled.collectAsStateWithLifecycle()
    val totalResults = results.notes.size + results.records.size
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale.CHINA) }

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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = stringResource(R.string.search),
                    color = AppleNotesStyle.PrimaryText,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold
                )
            }
            item {
                SearchField(value = query, onValueChange = viewModel::onQueryChange)
            }

            if (query.isBlank()) {
                if (smartSuggestionsEnabled) {
                    item {
                        SearchSuggestions(
                            suggestions = listOf(
                                stringResource(R.string.notes_title),
                                stringResource(R.string.accounting)
                            ),
                            onSuggestionClick = viewModel::onQueryChange
                        )
                    }
                }
                item { SearchEmptyState(stringResource(R.string.search_notes)) }
            } else if (totalResults == 0) {
                item { SearchEmptyState(stringResource(R.string.no_matching_notes)) }
            } else {
                if (results.notes.isNotEmpty()) {
                    item { SectionHeader("${stringResource(R.string.notes_title)} (${results.notes.size})") }
                    item {
                        ResultGroup {
                            results.notes.forEachIndexed { index, note ->
                                SearchResultRow(
                                    icon = Lucide.StickyNote,
                                    title = note.title.ifBlank { stringResource(R.string.no_title) },
                                    subtitle = note.content,
                                    onClick = { navController.navigateStable(Screen.NoteDetail.createRoute(note.id, "all")) }
                                )
                                if (index != results.notes.lastIndex) ResultDivider()
                            }
                        }
                    }
                }

                if (results.records.isNotEmpty()) {
                    item { SectionHeader("${stringResource(R.string.accounting)} (${results.records.size})") }
                    item {
                        ResultGroup {
                            results.records.forEachIndexed { index, record ->
                                val isIncome = record.type == "income"
                                val amount = "${if (isIncome) "+" else "-"}${currencyFormatter.format(record.amount)}"
                                val category = record.category.ifBlank { stringResource(R.string.unknown) }
                                SearchResultRow(
                                    icon = Lucide.Wallet,
                                    title = record.note.ifBlank { category },
                                    subtitle = "$category · $amount",
                                    onClick = { navController.navigateStable(Screen.AccountEdit.createRoute(record.id)) }
                                )
                                if (index != results.records.lastIndex) ResultDivider()
                            }
                        }
                    }
                }

            }
        }
    }
}

@Composable
private fun SearchSuggestions(
    suggestions: List<String>,
    onSuggestionClick: (String) -> Unit
) {
    Surface(modifier = Modifier.fillMaxWidth(), color = AppleNotesStyle.Surface, shape = AppleNotesStyle.GroupShape) {
        Column(modifier = Modifier.padding(vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Lucide.Sparkles, contentDescription = null, tint = AppleNotesStyle.Accent, modifier = Modifier.size(18.dp))
                Text(
                    text = stringResource(R.string.behavior_smart_suggestions),
                    modifier = Modifier.padding(start = 8.dp),
                    color = AppleNotesStyle.PrimaryText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(suggestions) { suggestion ->
                    FilterChip(
                        selected = false,
                        onClick = { onSuggestionClick(suggestion) },
                        label = { Text(suggestion) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = AppleNotesStyle.SearchSurface,
                            labelColor = AppleNotesStyle.PrimaryText
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchField(value: String, onValueChange: (String) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AppleNotesStyle.SearchSurface,
        shape = AppleNotesStyle.SearchShape
    ) {
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
                cursorBrush = SolidColor(AppleNotesStyle.Accent),
                modifier = Modifier.weight(1f),
                decorationBox = { innerTextField ->
                    if (value.isBlank()) {
                        Text(stringResource(R.string.search_notes), color = AppleNotesStyle.TertiaryText, fontSize = 16.sp)
                    }
                    innerTextField()
                }
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(start = 4.dp, top = 4.dp),
        color = AppleNotesStyle.SecondaryText,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun ResultGroup(content: @Composable () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), color = AppleNotesStyle.Surface, shape = AppleNotesStyle.GroupShape) {
        Column(content = { content() })
    }
}

@Composable
private fun ResultDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 60.dp),
        color = AppleNotesStyle.Separator,
        thickness = 1.dp
    )
}

@Composable
private fun SearchResultRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
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
                Text(subtitle.take(120), color = AppleNotesStyle.SecondaryText, style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
        if (onClick != null) {
            Icon(Lucide.ChevronRight, contentDescription = null, tint = AppleNotesStyle.TertiaryText, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun SearchEmptyState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 72.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(message, color = AppleNotesStyle.SecondaryText, style = MaterialTheme.typography.bodyLarge)
    }
}
