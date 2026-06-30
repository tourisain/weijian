package com.tourisain.weijian.util

import com.tourisain.weijian.data.database.entity.AccountRecordEntity
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FinancialAnalyzer @Inject constructor() {
    data class CategorySummary(
        val category: String,
        val amount: Double,
        val percentage: Double
    )

    data class DailySummary(
        val date: Long,
        val income: Double,
        val expense: Double,
        val balance: Double
    )

    data class MonthlySummary(
        val month: String,
        val income: Double,
        val expense: Double,
        val balance: Double
    )

    fun analyzeByCategory(records: List<AccountRecordEntity>, type: String): List<CategorySummary> {
        val categoryTotals = records
            .filter { it.type == type }
            .groupBy { it.category }
            .mapValues { (_, items) -> items.sumOf { it.amount } }

        val total = categoryTotals.values.sum()
        return categoryTotals.map { (category, amount) ->
            CategorySummary(
                category = category,
                amount = amount,
                percentage = if (total > 0.0) amount / total * 100.0 else 0.0
            )
        }.sortedByDescending { it.amount }
    }

    fun analyzeDaily(records: List<AccountRecordEntity>, days: Int = 30): List<DailySummary> {
        val endDate = System.currentTimeMillis()
        val startDate = endDate - days.coerceAtLeast(1) * DAY_MILLIS

        return records
            .filter { it.date in startDate..endDate }
            .groupBy { dateKey(it.date) }
            .map { (date, items) ->
                val income = items.filter { it.type == "income" }.sumOf { it.amount }
                val expense = items.filter { it.type == "expense" }.sumOf { it.amount }
                DailySummary(date, income, expense, income - expense)
            }
            .sortedBy { it.date }
    }

    fun analyzeMonthly(records: List<AccountRecordEntity>, months: Int = 6): List<MonthlySummary> {
        val endDate = System.currentTimeMillis()
        val startDate = endDate - months.coerceAtLeast(1) * 30L * DAY_MILLIS

        return records
            .filter { it.date in startDate..endDate }
            .groupBy { monthKey(it.date) }
            .map { (month, items) ->
                val income = items.filter { it.type == "income" }.sumOf { it.amount }
                val expense = items.filter { it.type == "expense" }.sumOf { it.amount }
                MonthlySummary(month, income, expense, income - expense)
            }
            .sortedBy { it.month }
    }

    private fun dateKey(timestamp: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun monthKey(timestamp: Long): String {
        val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        return "$year-${month.toString().padStart(2, '0')}"
    }

    private companion object {
        const val DAY_MILLIS = 24L * 60L * 60L * 1000L
    }
}
