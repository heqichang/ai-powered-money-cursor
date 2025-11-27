package com.heqichang.dailymoney2.domain.usecase.statistics

import com.heqichang.dailymoney2.domain.model.MonthlyLedgerStats
import com.heqichang.dailymoney2.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

class ObserveMonthlyStatsUseCase(
    private val transactionRepository: TransactionRepository
) {
    operator fun invoke(ledgerId: Long): Flow<List<MonthlyLedgerStats>> =
        transactionRepository.observeMonthlyStats(ledgerId)
}

