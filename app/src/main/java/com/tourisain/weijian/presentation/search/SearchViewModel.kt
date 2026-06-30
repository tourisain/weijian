package com.tourisain.weijian.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tourisain.weijian.data.database.entity.NoteEntity
import com.tourisain.weijian.data.repository.NoteRepository
import com.tourisain.weijian.data.repository.UserRepository
import com.tourisain.weijian.data.repository.AccountRepository
import com.tourisain.weijian.data.database.entity.AccountRecordEntity
import com.tourisain.weijian.data.preferences.UserPreferences
import com.tourisain.weijian.presentation.note.isVisibleStandaloneNote
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
data class SearchResults(
    val notes: List<NoteEntity> = emptyList(),
    val records: List<AccountRecordEntity> = emptyList()
)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val userRepository: UserRepository,
    private val accountRepository: AccountRepository,
    userPreferences: UserPreferences
) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()
    val smartSuggestionsEnabled = userPreferences.enableSmartSuggestions
        .catch { emit(true) }
        .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )
    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val searchResults = _searchQuery
        .map { normalizeSearchQuery(it) }
        .distinctUntilChanged()
        .debounce(300L)
        .flatMapLatest { query ->
            if (query.isBlank()) {
                flowOf(SearchResults())
            } else {
                userRepository.currentUserId.flatMapLatest { userId ->
                    combine(
                        noteRepository.searchNotes(userId, query),
                        accountRepository.searchRecords(userId, query)
                    ) { notes, records ->
                        SearchResults(
                            notes = notes.filter { it.isVisibleStandaloneNote() },
                            records = records
                        )
                    }
                }
            }
        }
        .catch { emit(SearchResults()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SearchResults()
        )
    fun onQueryChange(newQuery: String) {
        _searchQuery.value = normalizeSearchQuery(newQuery)
    }
}

private fun normalizeSearchQuery(value: String): String {
    return value
        .replace('\u0000', ' ')
        .replace('\r', ' ')
        .trimStart()
        .take(MAX_SEARCH_QUERY_LENGTH)
}

private const val MAX_SEARCH_QUERY_LENGTH = 120
