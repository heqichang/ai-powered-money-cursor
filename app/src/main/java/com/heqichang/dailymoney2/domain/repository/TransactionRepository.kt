package com.heqichang.dailymoney2.domain.repository

import com.heqichang.dailymoney2.domain.model.MoneyTransaction
import com.heqichang.dailymoney2.domain.model.MonthlyLedgerStats
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface TransactionRepository {
    fun observeTransactions(ledgerId: Long): Flow<List<MoneyTransaction>>
    fun observeTransactionsForDate(ledgerId: Long, date: LocalDate): Flow<List<MoneyTransaction>>
    fun observeMonthlyStats(ledgerId: Long): Flow<List<MonthlyLedgerStats>>
    suspend fun getTransactions(ledgerId: Long, limit: Int, offset: Int): List<MoneyTransaction>
    suspend fun getAllTransactions(ledgerId: Long): List<MoneyTransaction>
    suspend fun getYearExpense(ledgerId: Long, year: Int): Long
    suspend fun getMonthExpense(ledgerId: Long, year: Int, month: Int): Long
    suspend fun upsert(transaction: MoneyTransaction)
    suspend fun delete(transactionId: Long)
    suspend fun deleteByLedger(ledgerId: Long)
    suspend fun deleteByLedgerAndDate(ledgerId: Long, date: LocalDate)
}

