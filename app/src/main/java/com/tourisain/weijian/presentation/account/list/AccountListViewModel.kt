package com.tourisain.weijian.presentation.account.list

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tourisain.weijian.R
import com.tourisain.weijian.data.database.entity.AccountRecordEntity
import com.tourisain.weijian.data.repository.AccountRepository
import com.tourisain.weijian.data.repository.UserRepository
import com.tourisain.weijian.util.ErrorReporter
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AccountListViewModel @Inject constructor(
    private val repository: AccountRepository,
    private val userRepository: UserRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _filterType = MutableStateFlow(AccountFilterType.ALL)
    val filterType = _filterType.asStateFlow()

    private val _currentMonth = MutableStateFlow(System.currentTimeMillis())
    val currentMonth = _currentMonth.asStateFlow()
    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val monthRecords = userRepository.currentUserId.flatMapLatest { userId ->
        repository.getAllRecords(userId).combine(_currentMonth) { records, monthTime ->
            records.filter { isSameMonth(it.date, monthTime) }
        }
    }.catch { error ->
        ErrorReporter.reportException(context, error)
        _error.value = error.message ?: context.getString(R.string.operation_failed)
        emit(emptyList())
    }

    val records = combine(monthRecords, _searchQuery, _filterType) { records, query, filter ->
        records
            .filter { record ->
                val matchesQuery = query.isBlank() ||
                    record.category.contains(query, ignoreCase = true) ||
                    record.note.contains(query, ignoreCase = true)
                val matchesType = when (filter) {
                    AccountFilterType.ALL -> true
                    AccountFilterType.INCOME -> record.type == "income"
                    AccountFilterType.EXPENSE -> record.type == "expense"
                }
                matchesQuery && matchesType
            }
            .sortedByDescending { it.date }
    }.catch {
        emit(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRecordCount = userRepository.currentUserId.flatMapLatest { userId ->
        repository.getAllRecords(userId).map { it.size }
    }.catch {
        emit(0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalIncome = monthRecords.map { records ->
        records.filter { it.type == "income" }.sumOf { it.amount }
    }.catch {
        emit(0.0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalExpense = monthRecords.map { records ->
        records.filter { it.type == "expense" }.sumOf { it.amount }
    }.catch {
        emit(0.0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val balance = combine(totalIncome, totalExpense) { income, expense ->
        income - expense
    }.catch {
        emit(0.0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val isUserPro = userRepository.currentUserState.map { user ->
        user?.isPro == true && (user.proExpireDate == null || user.proExpireDate > System.currentTimeMillis())
    }.catch {
        emit(false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _showProDialog = MutableStateFlow(false)
    val showProDialog = _showProDialog.asStateFlow()

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onFilterTypeChange(type: AccountFilterType) {
        _filterType.value = type
    }

    fun onPreviousMonth() {
        _currentMonth.value = shiftMonth(_currentMonth.value, -1)
    }

    fun onNextMonth() {
        _currentMonth.value = shiftMonth(_currentMonth.value, 1)
    }

    fun deleteRecord(record: AccountRecordEntity) {
        viewModelScope.launch {
            runCatching { repository.deleteRecord(record) }
                .onFailure { error ->
                    ErrorReporter.reportException(context, error)
                    _error.value = error.message ?: context.getString(R.string.operation_failed)
                }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun onStatsClick() {
        _showProDialog.value = true
    }

    fun dismissProDialog() {
        _showProDialog.value = false
    }

    private fun isSameMonth(value: Long, monthTime: Long): Boolean {
        return runCatching {
            val recordCalendar = Calendar.getInstance().apply { timeInMillis = value }
            val monthCalendar = Calendar.getInstance().apply { timeInMillis = monthTime }
            recordCalendar.get(Calendar.YEAR) == monthCalendar.get(Calendar.YEAR) &&
                recordCalendar.get(Calendar.MONTH) == monthCalendar.get(Calendar.MONTH)
        }.getOrDefault(false)
    }

    private fun shiftMonth(value: Long, months: Int): Long {
        return Calendar.getInstance().apply {
            timeInMillis = value
            add(Calendar.MONTH, months)
        }.timeInMillis
    }
}

enum class AccountFilterType {
    ALL,
    INCOME,
    EXPENSE
}
