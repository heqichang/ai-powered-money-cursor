package com.heqichang.dailymoney2.domain.usecase.transaction

import com.heqichang.dailymoney2.domain.repository.TransactionRepository

class DeleteTransactionUseCase(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(transactionId: Long) {
        transactionRepository.delete(transactionId)
    }
}

