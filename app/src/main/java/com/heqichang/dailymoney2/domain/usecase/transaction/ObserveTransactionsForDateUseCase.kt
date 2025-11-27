package com.heqichang.dailymoney2.domain.usecase.transaction

import com.heqichang.dailymoney2.domain.model.MoneyTransaction
import com.heqichang.dailymoney2.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class ObserveTransactionsForDateUseCase(
    private val transactionRepository: TransactionRepository
) {
    operator fun invoke(
        ledgerId: Long,
        date: LocalDate
    ): Flow<List<MoneyTransaction>> =
        transactionRepository.observeTransactionsForDate(ledgerId, date)
}

