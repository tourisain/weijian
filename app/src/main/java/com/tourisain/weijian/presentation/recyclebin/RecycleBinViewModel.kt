package com.tourisain.weijian.presentation.recyclebin

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tourisain.weijian.R
import com.tourisain.weijian.data.database.entity.AccountRecordEntity
import com.tourisain.weijian.data.database.entity.NoteEntity
import com.tourisain.weijian.data.repository.AccountRepository
import com.tourisain.weijian.data.repository.NoteRepository
import com.tourisain.weijian.data.repository.UserRepository
import com.tourisain.weijian.util.ErrorReporter
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class RecycleBinViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val noteRepository: NoteRepository,
    private val userRepository: UserRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    val deletedNotes = userRepository.currentUserId.flatMapLatest { userId ->
        noteRepository.getDeletedNotes(userId)
    }.catch { error ->
        reportDataError(error)
        emit(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val deletedAccountRecords = userRepository.currentUserId.flatMapLatest { userId ->
        accountRepository.getDeletedRecords(userId)
    }.catch { error ->
        reportDataError(error)
        emit(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun clearError() {
        _error.value = null
    }

    fun restoreNote(note: NoteEntity) {
        launchRecycleAction { noteRepository.restoreNote(note).getOrThrow() }
    }

    fun permanentDeleteNote(note: NoteEntity) {
        launchRecycleAction { noteRepository.permanentDeleteNote(note).getOrThrow() }
    }

    fun restoreAccountRecord(record: AccountRecordEntity) {
        launchRecycleAction { accountRepository.restoreRecord(record) }
    }

    fun permanentDeleteAccountRecord(record: AccountRecordEntity) {
        launchRecycleAction { accountRepository.permanentDeleteRecord(record) }
    }

    fun clearAll() {
        launchRecycleAction {
            val userId = userRepository.currentUserId.first()
            noteRepository.clearRecycleBin(userId).getOrThrow()
            accountRepository.clearRecycleBin(userId)
        }
    }

    private fun launchRecycleAction(block: suspend () -> Unit) {
        viewModelScope.launch {
            runCatching { block() }
                .onFailure { error ->
                    reportDataError(error)
                }
        }
    }

    private fun reportDataError(error: Throwable) {
        ErrorReporter.reportException(context, error)
        _error.value = error.message ?: context.getString(R.string.operation_failed)
    }
}
