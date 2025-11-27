package com.heqichang.dailymoney2.presentation.shared

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class LedgerSelectionViewModel @Inject constructor() : ViewModel() {

    private val _selectedLedgerId = MutableStateFlow<Long?>(null)
    val selectedLedgerId: StateFlow<Long?> = _selectedLedgerId.asStateFlow()

    fun selectLedger(ledgerId: Long?) {
        _selectedLedgerId.value = ledgerId
    }
}

