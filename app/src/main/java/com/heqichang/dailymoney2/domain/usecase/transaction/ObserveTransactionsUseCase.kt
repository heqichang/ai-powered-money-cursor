package com.heqichang.dailymoney2.domain.usecase.transaction

import com.heqichang.dailymoney2.domain.model.MoneyTransaction
import com.heqichang.dailymoney2.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

class ObserveTransactionsUseCase(
    private val transactionRepository: TransactionRepository
) {
    operator fun invoke(ledgerId: Long): Flow<List<MoneyTransaction>> =
        transactionRepository.observeTransactions(ledgerId)
}

