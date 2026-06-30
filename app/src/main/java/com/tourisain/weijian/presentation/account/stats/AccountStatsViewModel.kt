package com.tourisain.weijian.presentation.account.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tourisain.weijian.data.repository.AccountRepository
import com.tourisain.weijian.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import javax.inject.Inject

data class CategoryStat(
    val category: String,
    val amount: Double,
    val percentage: Float
)

data class DailyStat(
    val date: Long,
    val income: Double,
    val expense: Double
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AccountStatsViewModel @Inject constructor(
    private val repository: AccountRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    val categoryStats = userRepository.currentUserId.flatMapLatest { userId ->
        repository.getAllRecords(userId).map { records ->
            val firstDay = recentWindowStart()
            val expenseRecords = records.filter { it.type == "expense" && it.date >= firstDay }
            val totalExpense = expenseRecords.sumOf { it.amount }
            if (totalExpense <= 0.0) {
                emptyList()
            } else {
                expenseRecords
                    .groupBy { it.category }
                    .map { (category, items) ->
                        val amount = items.sumOf { it.amount }
                        CategoryStat(category, amount, (amount / totalExpense).toFloat())
                    }
                    .sortedByDescending { it.amount }
            }
        }
    }.catch {
        emit(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dailyStats = userRepository.currentUserId.flatMapLatest { userId ->
        repository.getAllRecords(userId).map { records ->
            val firstDay = recentWindowStart()
            val totalsByDay = records
                .asSequence()
                .filter { it.date >= firstDay && it.date < firstDay + 7L * DAY_MILLIS }
                .groupBy { ((it.date - firstDay) / DAY_MILLIS).toInt() }
                .mapValues { (_, dayRecords) ->
                    dayRecords.fold(0.0 to 0.0) { (income, expense), record ->
                        when (record.type) {
                            "income" -> income + record.amount to expense
                            "expense" -> income to expense + record.amount
                            else -> income to expense
                        }
                    }
                }

            (0..6).map { index ->
                val start = firstDay + index * DAY_MILLIS
                val (income, expense) = totalsByDay[index] ?: (0.0 to 0.0)
                DailyStat(
                    date = start,
                    income = income,
                    expense = expense
                )
            }
        }
    }.catch {
        emit(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun recentWindowStart(): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis - 6L * DAY_MILLIS
    }

    private companion object {
        const val DAY_MILLIS = 24L * 60L * 60L * 1000L
    }
}
