package com.tourisain.weijian.presentation.category

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tourisain.weijian.R
import com.tourisain.weijian.data.database.entity.NoteCategoryEntity
import com.tourisain.weijian.data.repository.NoteCategoryRepository
import com.tourisain.weijian.data.repository.NoteRepository
import com.tourisain.weijian.data.repository.UserRepository
import com.tourisain.weijian.presentation.note.isVisibleStandaloneNote
import com.tourisain.weijian.util.ErrorReporter
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class NoteCategorySummary(
    val category: NoteCategoryEntity,
    val noteCount: Int
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val noteCategoryRepository: NoteCategoryRepository,
    private val noteRepository: NoteRepository,
    private val userRepository: UserRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    val categorySummaries = userRepository.currentUserId.flatMapLatest { userId ->
        combine(
            noteCategoryRepository.getAllCategories(userId),
            noteRepository.getAllNotes(userId)
        ) { categories, notes ->
            val visibleNotes = notes.filter { it.isVisibleStandaloneNote() }
            categories
                .sortedWith(compareBy<NoteCategoryEntity> { it.sortOrder }.thenBy { it.createdAt })
                .map { category ->
                    NoteCategorySummary(
                        category = category,
                        noteCount = visibleNotes.count { it.categoryId == category.id }
                    )
                }
        }
    }.catch { error ->
        reportDataError(error)
        emit(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val uncategorizedCount = userRepository.currentUserId.flatMapLatest { userId ->
        noteRepository.getAllNotes(userId)
    }.combine(categorySummaries) { notes, _ ->
        notes.count { note ->
            note.isVisibleStandaloneNote() && note.categoryId.isNullOrBlank()
        }
    }.catch { error ->
        reportDataError(error)
        emit(0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun clearError() {
        _error.value = null
    }

    fun addCategory(name: String, color: String = DEFAULT_CATEGORY_COLOR) {
        saveCategory(existing = null, name = name, color = color)
    }

    fun updateCategory(category: NoteCategoryEntity, name: String, color: String) {
        saveCategory(existing = category, name = name, color = color)
    }

    fun deleteCategory(category: NoteCategoryEntity) {
        viewModelScope.launch {
            runCatching { noteCategoryRepository.deleteNoteCategory(category) }
                .onFailure { error ->
                    reportDataError(error, context.getString(R.string.category_delete_failed))
                }
        }
    }

    fun moveCategoryUp(category: NoteCategoryEntity) {
        moveCategory(category, moveUp = true)
    }

    fun moveCategoryDown(category: NoteCategoryEntity) {
        moveCategory(category, moveUp = false)
    }

    private fun moveCategory(category: NoteCategoryEntity, moveUp: Boolean) {
        viewModelScope.launch {
            runCatching {
                val userId = userRepository.currentUserId.first()
                if (moveUp) {
                    noteCategoryRepository.moveCategoryUp(userId, category.id)
                } else {
                    noteCategoryRepository.moveCategoryDown(userId, category.id)
                }
            }.onFailure { error ->
                reportDataError(error)
            }
        }
    }

    private fun saveCategory(existing: NoteCategoryEntity?, name: String, color: String) {
        viewModelScope.launch {
            val normalizedName = name.trim()
            if (normalizedName.isBlank()) {
                _error.value = context.getString(R.string.category_name_required)
                return@launch
            }

            runCatching {
                val userId = userRepository.currentUserId.first()
                val duplicate = categorySummaries.value
                    .map { it.category }
                    .firstOrNull {
                        it.userId == userId &&
                            it.name.equals(normalizedName, ignoreCase = true) &&
                            it.id != existing?.id
                    }
                if (duplicate != null) {
                    _error.value = context.getString(R.string.category_already_exists)
                    return@launch
                }

                val category = existing?.copy(
                    name = normalizedName,
                    color = color
                ) ?: NoteCategoryEntity(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    name = normalizedName,
                    color = color
                )

                if (existing == null) {
                    noteCategoryRepository.createNoteCategory(category)
                } else {
                    noteCategoryRepository.updateNoteCategory(category)
                }
            }.onFailure { error ->
                reportDataError(error, context.getString(R.string.category_save_failed))
            }
        }
    }

    private fun reportDataError(error: Throwable, fallback: String = context.getString(R.string.operation_failed)) {
        ErrorReporter.reportException(context, error)
        _error.value = error.message ?: fallback
    }

    private companion object {
        const val DEFAULT_CATEGORY_COLOR = "#FFB800"
    }
}
