package com.heqichang.dailymoney2.presentation.ledger.list

import com.heqichang.dailymoney2.domain.model.Ledger

data class LedgerListUiState(
    val ledgers: List<Ledger> = emptyList(),
    val selectedLedgerId: Long? = null,
    val isLoading: Boolean = false,
    val isEditorVisible: Boolean = false,
    val editingLedger: Ledger? = null,
    val errorMessage: String? = null,
    val importSuccess: Boolean = false
)

sealed interface LedgerListAction {
    data class SelectLedger(val ledgerId: Long?) : LedgerListAction
    data class EditLedger(val ledger: Ledger?) : LedgerListAction
    data class SaveLedger(val ledger: Ledger) : LedgerListAction
    data class DeleteLedger(val ledgerId: Long) : LedgerListAction
    data object DismissEditor : LedgerListAction
    data class SetImportSuccess(val ledgerId: Long) : LedgerListAction
    data object ResetImportSuccess : LedgerListAction
}

