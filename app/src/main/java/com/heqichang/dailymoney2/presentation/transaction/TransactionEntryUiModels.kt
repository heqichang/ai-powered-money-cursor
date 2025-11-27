package com.heqichang.dailymoney2.presentation.transaction

import com.heqichang.dailymoney2.data.local.entity.CategoryType
import com.heqichang.dailymoney2.domain.model.Category
import com.heqichang.dailymoney2.domain.model.MoneyTransaction
import java.time.LocalDate

data class TransactionEntryUiState(
    val ledgerId: Long? = null,
    val selectedDate: LocalDate? = null,
    val categories: List<Category> = emptyList(),
    val transactions: List<MoneyTransaction> = emptyList(),
    val draft: TransactionDraft = TransactionDraft(),
    val isLoadingCategories: Boolean = false,
    val isLoadingTransactions: Boolean = false,
    val errorMessage: String? = null,
    val saveSuccess: Boolean = false
)

data class TransactionDraft(
    val id: Long? = null,
    val ledgerId: Long = 0,
    val categoryId: Long? = null,
    val amountInCents: Long = 0,
    val occurredOn: LocalDate = LocalDate.now(),
    val note: String? = null
)

sealed interface TransactionEntryAction {
    data class SelectCategory(val categoryId: Long?, val categoryType: CategoryType) : TransactionEntryAction
    data class UpdateAmount(val amountInCents: Long) : TransactionEntryAction
    data class UpdateNote(val note: String?) : TransactionEntryAction
    data class EditTransaction(val transaction: MoneyTransaction) : TransactionEntryAction
    data class DeleteTransaction(val transactionId: Long) : TransactionEntryAction
    data class ChangeDate(val date: LocalDate) : TransactionEntryAction
    data object SaveDraft : TransactionEntryAction
    data object ClearDraft : TransactionEntryAction
    data object ResetSaveSuccess : TransactionEntryAction
}

