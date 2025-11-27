package com.heqichang.dailymoney2.presentation.statistics

import com.heqichang.dailymoney2.domain.model.Category
import com.heqichang.dailymoney2.domain.model.MoneyTransaction
import java.time.YearMonth
import java.time.Year

data class YearlyStats(
    val year: Year,
    val incomeInCents: Long,
    val expenseInCents: Long
) {
    val netInCents: Long = incomeInCents + expenseInCents
}

data class StatisticsUiState(
    val ledgerId: Long? = null,
    val transactions: List<MoneyTransaction> = emptyList(),
    val categories: Map<Long, Category> = emptyMap(),
    val yearlyStats: List<YearlyStats> = emptyList(),
    val selectedYear: Year? = null,
    val selectedCategoryId: Long? = null,
    val expandedMonths: Set<YearMonth> = emptySet(),
    val showCategoryFilterDialog: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    fun getCategoryName(categoryId: Long?): String? {
        return categoryId?.let { categories[it]?.name }
    }
}

sealed interface StatisticsAction {
    data class SelectYear(val year: Year) : StatisticsAction
    data class ToggleMonthExpanded(val yearMonth: YearMonth) : StatisticsAction
    data class SelectCategory(val categoryId: Long?) : StatisticsAction
    object ShowCategoryFilterDialog : StatisticsAction
    object DismissCategoryFilterDialog : StatisticsAction
}

