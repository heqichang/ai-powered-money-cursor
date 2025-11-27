package com.heqichang.dailymoney2.domain.usecase.ledger

import com.heqichang.dailymoney2.domain.model.Ledger
import com.heqichang.dailymoney2.domain.repository.LedgerRepository
import kotlinx.coroutines.flow.Flow

class ObserveLedgersUseCase(
    private val ledgerRepository: LedgerRepository
) {
    operator fun invoke(): Flow<List<Ledger>> = ledgerRepository.observeLedgers()
}

