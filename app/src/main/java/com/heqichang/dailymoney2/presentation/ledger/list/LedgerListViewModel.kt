package com.heqichang.dailymoney2.presentation.ledger.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heqichang.dailymoney2.domain.model.Ledger
import com.heqichang.dailymoney2.domain.usecase.ledger.DeleteLedgerUseCase
import com.heqichang.dailymoney2.domain.usecase.ledger.ImportLedgerDataUseCase
import com.heqichang.dailymoney2.domain.usecase.ledger.ObserveLedgersUseCase
import com.heqichang.dailymoney2.domain.usecase.ledger.UpsertLedgerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LedgerListViewModel @Inject constructor(
    private val observeLedgersUseCase: ObserveLedgersUseCase,
    private val upsertLedgerUseCase: UpsertLedgerUseCase,
    private val deleteLedgerUseCase: DeleteLedgerUseCase,
    private val importLedgerDataUseCase: ImportLedgerDataUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LedgerListUiState())
    val uiState: StateFlow<LedgerListUiState> = _uiState.asStateFlow()

    private var observeJob: Job? = null

    init {
        observeLedgers()
    }

    fun onAction(action: LedgerListAction) {
        when (action) {
            is LedgerListAction.SelectLedger -> selectLedger(action.ledgerId)
            is LedgerListAction.EditLedger -> openEditor(action.ledger ?: defaultLedgerTemplate())
            is LedgerListAction.DismissEditor -> closeEditor()
            is LedgerListAction.SaveLedger -> saveLedger(action.ledger)
            is LedgerListAction.DeleteLedger -> deleteLedger(action.ledgerId)
            is LedgerListAction.SetImportSuccess -> {
                _uiState.update { 
                    it.copy(
                        importSuccess = true,
                        selectedLedgerId = action.ledgerId
                    )
                }
            }
            is LedgerListAction.ResetImportSuccess -> {
                _uiState.update { it.copy(importSuccess = false) }
            }
        }
    }

    private fun observeLedgers() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            observeLedgersUseCase()
                .collect { ledgers ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            ledgers = ledgers,
                            selectedLedgerId = it.selectedLedgerId ?: ledgers.firstOrNull()?.id
                        )
                    }
                }
        }
    }

    private fun selectLedger(ledgerId: Long?) {
        _uiState.update { it.copy(selectedLedgerId = ledgerId) }
    }

    private fun openEditor(ledger: Ledger) {
        _uiState.update {
            it.copy(
                isEditorVisible = true,
                editingLedger = ledger
            )
        }
    }

    private fun closeEditor() {
        _uiState.update {
            it.copy(
                isEditorVisible = false,
                editingLedger = null
            )
        }
    }

    private fun saveLedger(ledger: Ledger) {
        viewModelScope.launch {
            runCatching { upsertLedgerUseCase(ledger) }
                .onFailure { error ->
                    _uiState.update { it.copy(errorMessage = error.message) }
                }
                .onSuccess { closeEditor() }
        }
    }

    private fun deleteLedger(ledgerId: Long) {
        viewModelScope.launch {
            runCatching { deleteLedgerUseCase(ledgerId) }
                .onFailure { error ->
                    _uiState.update { it.copy(errorMessage = error.message) }
                }
                .onSuccess {
                    _uiState.update { state ->
                        val updatedLedgers = state.ledgers.filterNot { it.id == ledgerId }
                        state.copy(
                            ledgers = updatedLedgers,
                            selectedLedgerId = state.selectedLedgerId.takeIf { id ->
                                updatedLedgers.any { it.id == id }
                            } ?: updatedLedgers.firstOrNull()?.id
                        )
                    }
                }
        }
    }

    private fun defaultLedgerTemplate(): Ledger = Ledger(
        name = "",
        description = ""
    )

    suspend fun importLedgerData(jsonString: String): Result<Long> {
        return runCatching {
            importLedgerDataUseCase(jsonString)
        }.onFailure { error ->
            _uiState.update { 
                it.copy(errorMessage = error.message ?: "导入账本失败")
            }
        }
    }
}

