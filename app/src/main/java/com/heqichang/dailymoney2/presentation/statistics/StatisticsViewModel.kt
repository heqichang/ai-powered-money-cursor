package com.heqichang.dailymoney2.presentation.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heqichang.dailymoney2.domain.usecase.category.ObserveCategoriesUseCase
import com.heqichang.dailymoney2.domain.usecase.transaction.ObserveTransactionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.Year
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val observeTransactionsUseCase: ObserveTransactionsUseCase,
    private val observeCategoriesUseCase: ObserveCategoriesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    private var observeJob: Job? = null

    fun setLedger(ledgerId: Long) {
        if (uiState.value.ledgerId == ledgerId) return
        _uiState.update { it.copy(ledgerId = ledgerId) }
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            combine(
                observeTransactionsUseCase(ledgerId),
                observeCategoriesUseCase(ledgerId)
            ) { transactions, categories ->
                Pair(transactions, categories)
            }.collect { (transactions, categories) ->
                val yearlyStats = calculateYearlyStats(transactions)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        transactions = transactions,
                        categories = categories.associateBy { it.id },
                        yearlyStats = yearlyStats,
                        selectedYear = it.selectedYear ?: yearlyStats.firstOrNull()?.year
                    )
                }
            }
        }
    }

    fun onAction(action: StatisticsAction) {
        when (action) {
            is StatisticsAction.SelectYear -> selectYear(action.year)
            is StatisticsAction.ToggleMonthExpanded -> toggleMonthExpanded(action.yearMonth)
            is StatisticsAction.SelectCategory -> selectCategory(action.categoryId)
            is StatisticsAction.ShowCategoryFilterDialog -> showCategoryFilterDialog()
            is StatisticsAction.DismissCategoryFilterDialog -> dismissCategoryFilterDialog()
        }
    }

    private fun selectYear(year: Year) {
        _uiState.update { it.copy(selectedYear = year) }
    }

    private fun toggleMonthExpanded(yearMonth: YearMonth) {
        _uiState.update { state ->
            val expandedMonths = state.expandedMonths.toMutableSet()
            if (expandedMonths.contains(yearMonth)) {
                expandedMonths.remove(yearMonth)
            } else {
                expandedMonths.add(yearMonth)
            }
            state.copy(expandedMonths = expandedMonths)
        }
    }

    private fun selectCategory(categoryId: Long?) {
        _uiState.update { it.copy(selectedCategoryId = categoryId, showCategoryFilterDialog = false) }
    }

    private fun showCategoryFilterDialog() {
        _uiState.update { it.copy(showCategoryFilterDialog = true) }
    }

    private fun dismissCategoryFilterDialog() {
        _uiState.update { it.copy(showCategoryFilterDialog = false) }
    }

    private fun calculateYearlyStats(transactions: List<com.heqichang.dailymoney2.domain.model.MoneyTransaction>): List<YearlyStats> {
        val statsMap = mutableMapOf<Year, Pair<Long, Long>>() // year -> (income, expense)
        
        transactions.forEach { transaction ->
            val year = Year.from(transaction.occurredOn)
            val current = statsMap.getOrDefault(year, 0L to 0L)
            if (transaction.amountInCents >= 0) {
                statsMap[year] = current.first + transaction.amountInCents to current.second
            } else {
                statsMap[year] = current.first to current.second + transaction.amountInCents
            }
        }
        
        return statsMap.map { (year, pair) ->
            YearlyStats(
                year = year,
                incomeInCents = pair.first,
                expenseInCents = pair.second
            )
        }.sortedByDescending { it.year.value }
    }
}

