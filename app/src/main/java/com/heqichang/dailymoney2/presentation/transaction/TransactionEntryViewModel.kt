package com.heqichang.dailymoney2.presentation.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heqichang.dailymoney2.data.local.entity.CategoryType
import com.heqichang.dailymoney2.domain.model.Category
import com.heqichang.dailymoney2.domain.model.MoneyTransaction
import com.heqichang.dailymoney2.domain.usecase.category.ObserveCategoriesUseCase
import com.heqichang.dailymoney2.domain.usecase.transaction.DeleteTransactionUseCase
import com.heqichang.dailymoney2.domain.usecase.transaction.ObserveTransactionsForDateUseCase
import com.heqichang.dailymoney2.domain.usecase.transaction.UpsertTransactionUseCase
import kotlin.math.abs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class TransactionEntryViewModel @Inject constructor(
    private val observeCategoriesUseCase: ObserveCategoriesUseCase,
    private val observeTransactionsForDateUseCase: ObserveTransactionsForDateUseCase,
    private val upsertTransactionUseCase: UpsertTransactionUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionEntryUiState())
    val uiState: StateFlow<TransactionEntryUiState> = _uiState.asStateFlow()

    private var categoryJob: Job? = null
    private var transactionJob: Job? = null

    fun setLedgerAndDate(ledgerId: Long, date: LocalDate) {
        val shouldUpdateDate = uiState.value.selectedDate != date
        val shouldUpdateLedger = uiState.value.ledgerId != ledgerId
        if (!shouldUpdateDate && !shouldUpdateLedger) return
        _uiState.update { it.copy(ledgerId = ledgerId, selectedDate = date) }
        observeCategories(ledgerId)
        observeTransactions(ledgerId, date)
        resetDraftForDate(date)
    }

    fun onAction(action: TransactionEntryAction) {
        when (action) {
            is TransactionEntryAction.SelectCategory -> {
                val currentDraft = uiState.value.draft
                val currentAmount = abs(currentDraft.amountInCents)
                val newAmount = when (action.categoryType) {
                    CategoryType.EXPENSE -> -currentAmount
                    CategoryType.INCOME -> currentAmount
                }
                updateDraft { 
                    it.copy(
                        categoryId = action.categoryId,
                        amountInCents = newAmount
                    )
                }
            }
            is TransactionEntryAction.UpdateAmount -> {
                val currentDraft = uiState.value.draft
                val category = uiState.value.categories.firstOrNull { it.id == currentDraft.categoryId }
                val categoryType = category?.type ?: CategoryType.EXPENSE
                val absoluteAmount = abs(action.amountInCents)
                val adjustedAmount = when (categoryType) {
                    CategoryType.EXPENSE -> -absoluteAmount
                    CategoryType.INCOME -> absoluteAmount
                }
                updateDraft { it.copy(amountInCents = adjustedAmount) }
            }
            is TransactionEntryAction.UpdateNote -> updateDraft { it.copy(note = action.note) }
            TransactionEntryAction.SaveDraft -> saveDraft()
            TransactionEntryAction.ClearDraft -> resetDraftForDate(uiState.value.selectedDate ?: LocalDate.now())
            is TransactionEntryAction.EditTransaction -> loadExistingTransaction(action.transaction)
            is TransactionEntryAction.DeleteTransaction -> deleteTransaction(action.transactionId)
            is TransactionEntryAction.ChangeDate -> uiState.value.ledgerId?.let { ledgerId ->
                setLedgerAndDate(ledgerId, action.date)
            }
            TransactionEntryAction.ResetSaveSuccess -> {
                _uiState.update { it.copy(saveSuccess = false) }
            }
        }
    }

    private fun observeCategories(ledgerId: Long) {
        categoryJob?.cancel()
        categoryJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoadingCategories = true, errorMessage = null) }
            observeCategoriesUseCase(ledgerId).collect { categories ->
                _uiState.update {
                    it.copy(
                        isLoadingCategories = false,
                        categories = categories
                    )
                }
            }
        }
    }

    private fun observeTransactions(ledgerId: Long, date: LocalDate) {
        transactionJob?.cancel()
        transactionJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoadingTransactions = true, errorMessage = null) }
            observeTransactionsForDateUseCase(ledgerId, date).collect { transactions ->
                _uiState.update {
                    it.copy(
                        isLoadingTransactions = false,
                        transactions = transactions
                    )
                }
            }
        }
    }

    private fun updateDraft(transform: (TransactionDraft) -> TransactionDraft) {
        _uiState.update { state ->
            state.copy(draft = transform(state.draft))
        }
    }

    private fun resetDraftForDate(date: LocalDate) {
        _uiState.update {
            it.copy(
                draft = TransactionDraft(
                    ledgerId = it.ledgerId ?: 0,
                    occurredOn = date
                )
            )
        }
    }

    private fun loadExistingTransaction(transaction: MoneyTransaction) {
        val currentState = _uiState.value
        val category = currentState.categories.firstOrNull { cat -> cat.id == transaction.categoryId }
        val currentAmount = abs(transaction.amountInCents)
        val adjustedAmount = category?.let { cat ->
            when (cat.type) {
                CategoryType.EXPENSE -> -currentAmount
                CategoryType.INCOME -> currentAmount
            }
        } ?: transaction.amountInCents
        
        _uiState.update {
            it.copy(
                draft = TransactionDraft(
                    id = transaction.id,
                    ledgerId = transaction.ledgerId,
                    categoryId = transaction.categoryId,
                    amountInCents = adjustedAmount,
                    occurredOn = transaction.occurredOn,
                    note = transaction.note
                )
            )
        }
    }

    private fun saveDraft() {
        val current = uiState.value
        val draft = current.draft
        if (current.ledgerId == null) {
            _uiState.update { it.copy(errorMessage = "请先选择账本") }
            return
        }
        viewModelScope.launch {
            runCatching {
                upsertTransactionUseCase(
                    MoneyTransaction(
                        id = draft.id ?: 0,
                        ledgerId = current.ledgerId,
                        categoryId = draft.categoryId,
                        amountInCents = draft.amountInCents,
                        occurredOn = draft.occurredOn,
                        note = draft.note
                    )
                )
            }.onSuccess {
                resetDraftForDate(current.selectedDate ?: LocalDate.now())
                _uiState.update { it.copy(saveSuccess = true) }
            }.onFailure { error ->
                _uiState.update { it.copy(errorMessage = error.message) }
            }
        }
    }

    private fun deleteTransaction(transactionId: Long) {
        viewModelScope.launch {
            runCatching { deleteTransactionUseCase(transactionId) }
                .onSuccess {
                    // 删除成功后，重置草稿并设置删除成功标志
                    resetDraftForDate(uiState.value.selectedDate ?: LocalDate.now())
                    _uiState.update { it.copy(saveSuccess = true) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(errorMessage = error.message) }
                }
        }
    }
}

