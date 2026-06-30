package com.tourisain.weijian.presentation.dashboard

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tourisain.weijian.R
import com.tourisain.weijian.data.database.entity.AccountRecordEntity
import com.tourisain.weijian.data.database.entity.NoteCategoryEntity
import com.tourisain.weijian.data.database.entity.NoteEntity
import com.tourisain.weijian.data.preferences.UserPreferences
import com.tourisain.weijian.data.repository.AccountRepository
import com.tourisain.weijian.data.repository.NoteCategoryRepository
import com.tourisain.weijian.data.repository.NoteRepository
import com.tourisain.weijian.data.repository.UserRepository
import com.tourisain.weijian.presentation.note.hasChecklistMarker
import com.tourisain.weijian.presentation.note.isVisibleStandaloneNote
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val noteCategoryRepository: NoteCategoryRepository,
    private val accountRepository: AccountRepository,
    private val userRepository: UserRepository,
    private val userPreferences: UserPreferences,
    @ApplicationContext private val context: Context
) : ViewModel() {
    val state: StateFlow<DashboardState> = userRepository.currentUserId
        .flatMapLatest { userId ->
            val contentFlow = combine(
                noteRepository.getAllNotes(userId),
                noteCategoryRepository.getAllCategories(userId),
                accountRepository.getAllRecords(userId)
            ) { notes, noteCategories, records ->
                DashboardContent(notes, noteCategories, records)
            }

            val recycleBinCountFlow = combine(
                noteRepository.getDeletedNotes(userId),
                accountRepository.getDeletedRecords(userId)
            ) { deletedNotes, deletedRecords ->
                deletedNotes.size + deletedRecords.size
            }

            combine(
                contentFlow,
                recycleBinCountFlow,
                userPreferences.getCardVisibilityFlow(),
                userPreferences.getSidebarItemsFlow()
            ) { content, recycleBinCount, cardVisibility, sidebarItems ->
                buildDashboardState(
                    notes = content.notes,
                    noteCategories = content.noteCategories,
                    records = content.records,
                    recycleBinCount = recycleBinCount,
                    cardVisibility = defaultCardVisibility + cardVisibility,
                    quickEntryRoutes = sidebarItems
                        .ifEmpty { defaultQuickEntryRoutes.map { route -> com.tourisain.weijian.presentation.settings.SidebarItem(route, route, true) } }
                        .filter { it.visible && it.key in validQuickEntryRoutes }
                        .map { it.key }
                )
            }
        }
        .catch {
            emit(DashboardState(isLoading = false))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DashboardState(isLoading = true)
        )

    private fun buildDashboardState(
        notes: List<NoteEntity>,
        noteCategories: List<NoteCategoryEntity>,
        records: List<AccountRecordEntity>,
        recycleBinCount: Int,
        cardVisibility: Map<String, Boolean>,
        quickEntryRoutes: List<String>
    ): DashboardState {
        val visibleNotes = notes.filter { it.isVisibleStandaloneNote() }
        val noteCountsByCategory = visibleNotes
            .groupingBy { note -> note.categoryId?.takeIf { it.isNotBlank() } }
            .eachCount()
        val noteFolders = noteCategories
            .sortedWith(compareBy<NoteCategoryEntity> { it.sortOrder }.thenBy { it.createdAt })
            .map { category ->
                DashboardNoteFolder(
                    id = category.id,
                    name = category.name,
                    color = category.color,
                    count = noteCountsByCategory[category.id] ?: 0
                )
            }
        val uncategorizedNoteCount = noteCountsByCategory[null] ?: 0
        val formatter = NumberFormat.getCurrencyInstance(Locale.CHINA)
        val activities = buildList {
            visibleNotes.take(4).forEach { note ->
                add(
                    DashboardActivity(
                        id = note.id,
                        title = note.title.ifBlank { context.getString(R.string.no_title) },
                        subtitle = note.content.ifBlank { context.getString(R.string.no_content) }.take(80),
                        type = DashboardActivityType.Note,
                        timestamp = note.createdAt
                    )
                )
            }
            records.take(3).forEach { record ->
                val isIncome = record.type == "income"
                val amount = "${if (isIncome) "+" else "-"}${formatter.format(record.amount)}"
                add(
                    DashboardActivity(
                        id = record.id,
                        title = record.note.ifBlank { record.category.ifBlank { context.getString(R.string.accounting) } },
                        subtitle = context.getString(
                            R.string.account_activity_subtitle,
                            record.category.ifBlank { context.getString(R.string.unknown) },
                            amount
                        ),
                        type = DashboardActivityType.Account,
                        timestamp = maxOf(record.createdAt, record.date)
                    )
                )
            }
        }
            .sortedByDescending { it.timestamp }
            .take(6)

        return DashboardState(
            noteCount = visibleNotes.size,
            pinnedNoteCount = visibleNotes.count { it.isPinned },
            todoNoteCount = visibleNotes.count { !it.isLocked && hasChecklistMarker(it.content) },
            uncategorizedNoteCount = uncategorizedNoteCount,
            noteFolders = noteFolders,
            accountCount = records.size,
            recycleBinCount = recycleBinCount,
            recentActivities = activities,
            cardVisibility = cardVisibility,
            quickEntryRoutes = quickEntryRoutes,
            isLoading = false
        )
    }
}

private data class DashboardContent(
    val notes: List<NoteEntity>,
    val noteCategories: List<NoteCategoryEntity>,
    val records: List<AccountRecordEntity>
)

enum class DashboardActivityType {
    Note,
    Account
}

data class DashboardActivity(
    val id: String = "",
    val title: String = "",
    val subtitle: String = "",
    val type: DashboardActivityType = DashboardActivityType.Note,
    val timestamp: Long = System.currentTimeMillis()
)

enum class DashboardActivitySectionKey {
    Today,
    Yesterday,
    Earlier
}

data class DashboardActivitySection(
    val key: DashboardActivitySectionKey,
    val activities: List<DashboardActivity>
)

fun dashboardActivitySections(
    activities: List<DashboardActivity>,
    now: Long = System.currentTimeMillis(),
    timeZone: TimeZone = TimeZone.getDefault()
): List<DashboardActivitySection> {
    if (activities.isEmpty()) return emptyList()
    val todayStart = startOfDay(now, timeZone)
    val yesterdayStart = todayStart - DAY_IN_MILLIS
    val grouped = activities
        .sortedByDescending { it.timestamp }
        .groupBy { activity ->
            when {
                activity.timestamp >= todayStart -> DashboardActivitySectionKey.Today
                activity.timestamp >= yesterdayStart -> DashboardActivitySectionKey.Yesterday
                else -> DashboardActivitySectionKey.Earlier
            }
        }
    return listOf(
        DashboardActivitySectionKey.Today,
        DashboardActivitySectionKey.Yesterday,
        DashboardActivitySectionKey.Earlier
    ).mapNotNull { key ->
        grouped[key]?.takeIf { it.isNotEmpty() }?.let { DashboardActivitySection(key, it) }
    }
}

private fun startOfDay(timestamp: Long, timeZone: TimeZone): Long {
    return Calendar.getInstance(timeZone).apply {
        timeInMillis = timestamp
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

data class DashboardState(
    val noteCount: Int = 0,
    val pinnedNoteCount: Int = 0,
    val todoNoteCount: Int = 0,
    val uncategorizedNoteCount: Int = 0,
    val noteFolders: List<DashboardNoteFolder> = emptyList(),
    val accountCount: Int = 0,
    val recycleBinCount: Int = 0,
    val recentActivities: List<DashboardActivity> = emptyList(),
    val cardVisibility: Map<String, Boolean> = emptyMap(),
    val quickEntryRoutes: List<String> = emptyList(),
    val isLoading: Boolean = false
)

private const val DAY_IN_MILLIS = 24L * 60L * 60L * 1000L

data class DashboardNoteFolder(
    val id: String,
    val name: String,
    val color: String,
    val count: Int
)

private val defaultCardVisibility = mapOf(
    "note" to true,
    "account" to true
)

private val defaultQuickEntryRoutes = listOf(
    "note_list/default",
    "accounts",
    "search",
    "settings"
)

private val validQuickEntryRoutes = defaultQuickEntryRoutes.toSet()
