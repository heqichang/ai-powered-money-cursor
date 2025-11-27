package com.heqichang.dailymoney2.presentation.ledger.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heqichang.dailymoney2.domain.model.Ledger
import com.heqichang.dailymoney2.domain.model.MoneyTransaction
import com.heqichang.dailymoney2.domain.usecase.ledger.ObserveLedgerUseCase
import com.heqichang.dailymoney2.domain.usecase.ledger.DeleteLedgerUseCase
import com.heqichang.dailymoney2.domain.usecase.ledger.ExportLedgerDataUseCase
import com.heqichang.dailymoney2.domain.usecase.category.ObserveCategoriesUseCase
import com.heqichang.dailymoney2.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class LedgerDetailViewModel @Inject constructor(
    private val observeLedgerUseCase: ObserveLedgerUseCase,
    private val transactionRepository: TransactionRepository,
    private val observeCategoriesUseCase: ObserveCategoriesUseCase,
    private val deleteLedgerUseCase: DeleteLedgerUseCase,
    private val exportLedgerDataUseCase: ExportLedgerDataUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LedgerDetailUiState())
    val uiState: StateFlow<LedgerDetailUiState> = _uiState.asStateFlow()

    private var observeJob: Job? = null
    private var currentLedgerId: Long? = null
    private val pageSize = 100
    private var currentOffset = 0

    fun setLedger(ledgerId: Long) {
        currentLedgerId = ledgerId
        observeJob?.cancel()
        currentOffset = 0
        observeJob = viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    isLoading = true, 
                    errorMessage = null,
                    transactions = emptyList(),
                    hasMore = true
                ) 
            }
            
            combine(
                observeLedgerUseCase(ledgerId),
                observeCategoriesUseCase(ledgerId)
            ) { ledger, categories ->
                Pair(ledger, categories)
            }.collect { (ledger, categories) ->
                // 加载第一页数据和统计数据
                loadMoreTransactions(ledgerId)
                loadExpenseStatistics(ledgerId)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        ledgerName = ledger?.name ?: "",
                        categories = categories.associateBy { it.id }
                    )
                }
            }
        }
    }

    fun loadMoreTransactions() {
        val ledgerId = currentLedgerId ?: return
        viewModelScope.launch {
            loadMoreTransactions(ledgerId)
        }
    }

    private suspend fun loadMoreTransactions(ledgerId: Long) {
        if (!_uiState.value.hasMore || _uiState.value.isLoadingMore) return
        
        _uiState.update { it.copy(isLoadingMore = true) }
        
        try {
            val newTransactions = transactionRepository.getTransactions(
                ledgerId = ledgerId,
                limit = pageSize,
                offset = currentOffset
            )
            
            if (newTransactions.isEmpty()) {
                _uiState.update { 
                    it.copy(
                        isLoadingMore = false,
                        hasMore = false
                    ) 
                }
                return
            }
            
            _uiState.update { state ->
                state.copy(
                    transactions = state.transactions + newTransactions,
                    isLoadingMore = false,
                    hasMore = newTransactions.size == pageSize
                )
            }
            
            currentOffset += newTransactions.size
        } catch (e: Exception) {
            _uiState.update { 
                it.copy(
                    isLoadingMore = false,
                    errorMessage = e.message ?: "加载失败"
                ) 
            }
        }
    }

    private suspend fun loadExpenseStatistics(ledgerId: Long) {
        try {
            val now = LocalDate.now()
            val yearExpense = transactionRepository.getYearExpense(ledgerId, now.year)
            val monthExpense = transactionRepository.getMonthExpense(ledgerId, now.year, now.monthValue)
            
            _uiState.update {
                it.copy(
                    thisYearExpense = yearExpense,
                    thisMonthExpense = monthExpense
                )
            }
        } catch (e: Exception) {
            // 忽略统计加载错误，不影响主功能
        }
    }

    fun deleteLedger() {
        val ledgerId = currentLedgerId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                deleteLedgerUseCase(ledgerId)
            }.onSuccess {
                _uiState.update { it.copy(isLoading = false, ledgerDeleted = true) }
            }.onFailure { error ->
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "删除账本失败"
                    )
                }
            }
        }
    }

    suspend fun exportLedgerData(): Result<String> {
        val ledgerId = currentLedgerId ?: return Result.failure(IllegalStateException("账本ID不存在"))
        return runCatching {
            exportLedgerDataUseCase(ledgerId)
        }
    }
}

