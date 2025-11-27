package com.heqichang.dailymoney2.presentation.ledger.detail

import com.heqichang.dailymoney2.domain.model.Category
import com.heqichang.dailymoney2.domain.model.MoneyTransaction
import java.time.LocalDate

data class LedgerDetailUiState(
    val ledgerName: String = "",
    val transactions: List<MoneyTransaction> = emptyList(),
    val categories: Map<Long, Category> = emptyMap(),
    val thisYearExpense: Long = 0,
    val thisMonthExpense: Long = 0,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = true,
    val errorMessage: String? = null,
    val ledgerDeleted: Boolean = false,
    val exportData: String? = null
) {
    fun getCategoryName(categoryId: Long?): String? {
        return categoryId?.let { categories[it]?.name }
    }
}

