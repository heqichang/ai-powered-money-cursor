package com.heqichang.dailymoney2.domain.usecase.ledger

import com.heqichang.dailymoney2.domain.model.Ledger
import com.heqichang.dailymoney2.domain.repository.LedgerRepository
import kotlinx.coroutines.flow.Flow

class ObserveLedgerUseCase(
    private val ledgerRepository: LedgerRepository
) {
    operator fun invoke(ledgerId: Long): Flow<Ledger?> = ledgerRepository.observeLedger(ledgerId)
}

