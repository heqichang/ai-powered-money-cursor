package com.heqichang.dailymoney2.domain.usecase.transaction

import com.heqichang.dailymoney2.domain.model.MoneyTransaction
import com.heqichang.dailymoney2.domain.repository.TransactionRepository

class UpsertTransactionUseCase(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(transaction: MoneyTransaction) {
        transactionRepository.upsert(transaction)
    }
}

