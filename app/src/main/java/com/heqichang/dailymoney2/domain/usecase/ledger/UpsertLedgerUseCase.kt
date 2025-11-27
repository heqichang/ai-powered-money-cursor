package com.heqichang.dailymoney2.domain.usecase.ledger

import com.heqichang.dailymoney2.domain.model.Ledger
import com.heqichang.dailymoney2.domain.repository.LedgerRepository

class UpsertLedgerUseCase(
    private val ledgerRepository: LedgerRepository
) {
    suspend operator fun invoke(ledger: Ledger) {
        ledgerRepository.upsert(ledger)
    }
}

