package com.heqichang.dailymoney2.domain.usecase.ledger

import com.heqichang.dailymoney2.domain.repository.LedgerRepository
import com.heqichang.dailymoney2.domain.repository.TransactionRepository

class DeleteLedgerUseCase(
    private val ledgerRepository: LedgerRepository,
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(ledgerId: Long) {
        transactionRepository.deleteByLedger(ledgerId)
        ledgerRepository.delete(ledgerId)
    }
}

