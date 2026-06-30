package com.tourisain.weijian.presentation.note.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.tourisain.weijian.data.database.entity.NoteCategoryEntity
import com.tourisain.weijian.data.repository.NoteRepository
import com.tourisain.weijian.data.repository.NoteCategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import com.tourisain.weijian.data.database.entity.NoteEntity
import com.tourisain.weijian.presentation.note.hasChecklistMarker
import com.tourisain.weijian.presentation.note.isVisibleStandaloneNote
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class NoteListViewModel @Inject constructor(
    private val repository: NoteRepository,
    private val noteCategoryRepository: NoteCategoryRepository,
    private val userRepository: com.tourisain.weijian.data.repository.UserRepository
) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _categoryRoute = MutableStateFlow(ALL_NOTES_ROUTE)
    val categoryRoute = _categoryRoute.asStateFlow()

    private val _selectedTag = MutableStateFlow<String?>(null)
    val selectedTag = _selectedTag.asStateFlow()

    private val _selectedSmartFilter = MutableStateFlow<NoteSmartFilter?>(null)
    val selectedSmartFilter = _selectedSmartFilter.asStateFlow()

    private val gson = Gson()

    init {
        viewModelScope.launch {
            userRepository.currentUserId.flatMapLatest { userId ->
                combine(
                    noteCategoryRepository.getAllCategories(userId),
                    _categoryRoute
                ) { categories, selectedRoute ->
                    val routeExists = selectedRoute == ALL_NOTES_ROUTE ||
                        selectedRoute == UNCATEGORIZED_ROUTE ||
                        categories.any { it.id == selectedRoute }
                    selectedRoute to routeExists
                }
            }.catch {
                _categoryRoute.value = ALL_NOTES_ROUTE
            }.collect { (selectedRoute, routeExists) ->
                if (!routeExists && _categoryRoute.value == selectedRoute) {
                    _categoryRoute.value = ALL_NOTES_ROUTE
                }
            }
        }
    }

    fun setCategoryId(categoryId: String?) {
        val normalizedRoute = normalizeCategoryRoute(categoryId)
        if (_categoryRoute.value != normalizedRoute) {
            _categoryRoute.value = normalizedRoute
        }
    }

    val notes = userRepository.currentUserId.flatMapLatest { userId ->
        combine(
            _categoryRoute,
            _searchQuery,
            _selectedTag,
            _selectedSmartFilter
        ) { categoryRoute, query, selectedTag, selectedSmartFilter ->
            NoteListFilter(userId, categoryRoute, query, selectedTag, selectedSmartFilter)
        }
    }.flatMapLatest { filter ->
        val dataSource = noteListDataSourceFor(filter.categoryRoute, filter.query)
        val normalizedQuery = filter.query.trim().trimStart('#')
        val baseFlow = when (dataSource) {
            NoteListDataSource.AllNotes -> repository.getAllNotes(filter.userId)
            NoteListDataSource.UncategorizedNotes -> repository.getAllNotes(filter.userId).map { notes ->
                notes.filter { it.categoryId.isNullOrBlank() }
            }
            NoteListDataSource.CategoryNotes -> repository.getNotesByCategory(filter.userId, filter.categoryRoute)
            NoteListDataSource.SearchAllNotes -> repository.searchNotes(filter.userId, normalizedQuery)
            NoteListDataSource.SearchUncategorizedNotes -> repository.searchUncategorizedNotes(filter.userId, normalizedQuery)
            NoteListDataSource.SearchCategoryNotes -> repository.searchNotesByCategory(filter.userId, filter.categoryRoute, normalizedQuery)
        }
        baseFlow.map { notes ->
            val standaloneNotes = notes.filter { it.isVisibleStandaloneNote() }
            val isTagSearch = filter.query.trim().startsWith("#")
            val filteredList = standaloneNotes.filter { note ->
                val tags = parseTags(note.tags)
                val matchesTag = filter.selectedTag == null ||
                    tags.any { it.equals(filter.selectedTag, ignoreCase = true) }
                val matchesSmartFilter = when (filter.selectedSmartFilter) {
                    null -> true
                    NoteSmartFilter.PINNED -> note.isPinned
                    NoteSmartFilter.LOCKED -> note.isLocked
                    NoteSmartFilter.TODO -> !note.isLocked && hasChecklistMarker(note.content)
                    NoteSmartFilter.ATTACHMENTS -> !note.isLocked && parseStringList(note.attachments).isNotEmpty()
                }
                val matchesSearch = when {
                    normalizedQuery.isBlank() -> true
                    isTagSearch -> tags.any { it.contains(normalizedQuery, ignoreCase = true) }
                    dataSource.usesDatabaseSearch() -> note.title.contains(normalizedQuery, ignoreCase = true) ||
                        (!note.isLocked && note.content.contains(normalizedQuery, ignoreCase = true))
                    else -> note.title.contains(normalizedQuery, ignoreCase = true) ||
                        (!note.isLocked && note.content.contains(normalizedQuery, ignoreCase = true)) ||
                        tags.any { it.contains(normalizedQuery, ignoreCase = true) }
                }
                matchesTag && matchesSmartFilter && matchesSearch
            }
            // Sort: Pinned first, then by date descending
            filteredList.sortedWith(
                compareByDescending<NoteEntity> { it.isPinned }
                    .thenByDescending { it.createdAt }
            )
        }
    }.catch {
        emit(emptyList())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val tagFilters = userRepository.currentUserId.flatMapLatest { userId ->
        combine(
            repository.getAllNotes(userId),
            _selectedTag
        ) { notes, selectedTag ->
            val standaloneNotes = notes.filter { it.isVisibleStandaloneNote() }
            val tagCounts = standaloneNotes
                .flatMap { parseTags(it.tags) }
                .filter { it.isNotBlank() }
                .groupingBy { it }
                .eachCount()
            tagCounts
                .toSortedMap(String.CASE_INSENSITIVE_ORDER)
                .map { (tag, count) ->
                    NoteTagFilterUi(
                        name = tag,
                        count = count,
                        selected = tag.equals(selectedTag, ignoreCase = true)
                    )
                }
        }
    }.catch {
        emit(emptyList())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val smartFilters = userRepository.currentUserId.flatMapLatest { userId ->
        combine(
            repository.getAllNotes(userId),
            _selectedSmartFilter
        ) { notes, selectedSmartFilter ->
            val standaloneNotes = notes.filter { it.isVisibleStandaloneNote() }
            listOf(
                NoteSmartFilterUi(
                    type = NoteSmartFilter.PINNED,
                    count = standaloneNotes.count { it.isPinned },
                    selected = selectedSmartFilter == NoteSmartFilter.PINNED
                ),
                NoteSmartFilterUi(
                    type = NoteSmartFilter.LOCKED,
                    count = standaloneNotes.count { it.isLocked },
                    selected = selectedSmartFilter == NoteSmartFilter.LOCKED
                ),
                NoteSmartFilterUi(
                    type = NoteSmartFilter.TODO,
                    count = standaloneNotes.count { !it.isLocked && hasChecklistMarker(it.content) },
                    selected = selectedSmartFilter == NoteSmartFilter.TODO
                ),
                NoteSmartFilterUi(
                    type = NoteSmartFilter.ATTACHMENTS,
                    count = standaloneNotes.count { !it.isLocked && parseStringList(it.attachments).isNotEmpty() },
                    selected = selectedSmartFilter == NoteSmartFilter.ATTACHMENTS
                )
            ).filter { it.count > 0 || it.selected }
        }
    }.catch {
        emit(emptyList())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allNoteCount = userRepository.currentUserId.flatMapLatest { userId ->
        repository.getAllNotes(userId).map { notes ->
            notes.count { it.isVisibleStandaloneNote() }
        }
    }.catch {
        emit(0)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val isUserPro = userRepository.currentUserState.mapLatest { user ->
        user?.let { userRepository.isUserPro(it.id) } ?: false
    }.catch {
        emit(false)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val categoryFolders = userRepository.currentUserId.flatMapLatest { userId ->
        combine(
            noteCategoryRepository.getAllCategories(userId),
            repository.getAllNotes(userId),
            _categoryRoute
        ) { categories, notes, selectedRoute ->
            val standaloneNotes = notes.filter { it.isVisibleStandaloneNote() }
            val noteCountsByCategory = standaloneNotes
                .groupingBy { note -> note.categoryId?.takeIf { it.isNotBlank() } }
                .eachCount()
            val categoryItems = categories
                .filter { category ->
                    category.id.isNotBlank() &&
                        category.id.lowercase(Locale.ROOT) !in RESERVED_CATEGORY_ROUTES
                }
                .distinctBy { it.id }
                .sortedWith(compareBy<NoteCategoryEntity> { it.sortOrder }.thenBy { it.createdAt })
                .map { category ->
                    NoteCategoryFolderUi(
                        id = category.id,
                        name = category.name,
                        color = category.color,
                        count = noteCountsByCategory[category.id] ?: 0,
                        selected = selectedRoute == category.id
                    )
                }
            buildList {
                add(
                    NoteCategoryFolderUi(
                        id = ALL_NOTES_ROUTE,
                        name = "All",
                        color = "#FFB800",
                        count = standaloneNotes.size,
                        selected = selectedRoute == ALL_NOTES_ROUTE
                    )
                )
                val uncategorizedCount = noteCountsByCategory[null] ?: 0
                if (uncategorizedCount > 0 || selectedRoute == UNCATEGORIZED_ROUTE) {
                    add(
                        NoteCategoryFolderUi(
                            id = UNCATEGORIZED_ROUTE,
                            name = "Uncategorized",
                            color = "#8E8E93",
                            count = uncategorizedCount,
                            selected = selectedRoute == UNCATEGORIZED_ROUTE
                        )
                    )
                }
                addAll(categoryItems)
            }
        }
    }.catch {
        emit(
            listOf(
                NoteCategoryFolderUi(
                    id = ALL_NOTES_ROUTE,
                    name = "All",
                    color = "#FFB800",
                    count = 0,
                    selected = true
                )
            )
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedTag(tag: String?) {
        _selectedTag.value = tag?.takeIf { it.isNotBlank() }
    }

    fun setSelectedSmartFilter(filter: NoteSmartFilter?) {
        _selectedSmartFilter.value = filter
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    fun togglePin(note: NoteEntity) {
        viewModelScope.launch {
            val updatedNote = note.copy(isPinned = !note.isPinned)
            repository.updateNote(updatedNote)
        }
    }

    fun toggleArchive(note: NoteEntity) {
        viewModelScope.launch {
            val updatedNote = note.copy(isArchived = !note.isArchived)
            repository.updateNote(updatedNote)
        }
    }

    private fun parseTags(value: String?): List<String> {
        return parseStringList(value)
    }

    private fun parseStringList(value: String?): List<String> {
        if (value.isNullOrBlank() || value.equals("null", ignoreCase = true)) return emptyList()
        return runCatching {
            val parsed: Array<String>? = gson.fromJson(value, Array<String>::class.java)
            parsed.orEmpty().mapNotNull { item -> item.takeIf { it.isNotBlank() } }
        }.getOrDefault(emptyList())
    }

    private fun normalizeCategoryRoute(categoryId: String?): String {
        return when (categoryId?.takeIf { it.isNotBlank() }) {
            null, "all", "default", "null" -> ALL_NOTES_ROUTE
            UNCATEGORIZED_ROUTE -> UNCATEGORIZED_ROUTE
            else -> categoryId
        }
    }
}

private data class NoteListFilter(
    val userId: String,
    val categoryRoute: String,
    val query: String,
    val selectedTag: String?,
    val selectedSmartFilter: NoteSmartFilter?
)

data class NoteCategoryFolderUi(
    val id: String,
    val name: String,
    val color: String,
    val count: Int,
    val selected: Boolean
)

data class NoteTagFilterUi(
    val name: String,
    val count: Int,
    val selected: Boolean
)

enum class NoteSmartFilter {
    PINNED,
    LOCKED,
    TODO,
    ATTACHMENTS
}

data class NoteSmartFilterUi(
    val type: NoteSmartFilter,
    val count: Int,
    val selected: Boolean
)

const val ALL_NOTES_ROUTE = "all"
const val UNCATEGORIZED_ROUTE = "uncategorized"
private val RESERVED_CATEGORY_ROUTES = setOf(ALL_NOTES_ROUTE, "default", "null", UNCATEGORIZED_ROUTE)
