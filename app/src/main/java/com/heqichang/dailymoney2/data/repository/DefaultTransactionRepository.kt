package com.heqichang.dailymoney2.data.repository

import com.heqichang.dailymoney2.data.local.dao.TransactionDao
import com.heqichang.dailymoney2.data.mapper.toDomain
import com.heqichang.dailymoney2.data.mapper.toEntity
import com.heqichang.dailymoney2.domain.model.MoneyTransaction
import com.heqichang.dailymoney2.domain.model.MonthlyLedgerStats
import com.heqichang.dailymoney2.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class DefaultTransactionRepository(
    private val transactionDao: TransactionDao
) : TransactionRepository {

    override fun observeTransactions(ledgerId: Long): Flow<List<MoneyTransaction>> =
        transactionDao.observeTransactions(ledgerId).map { entities ->
            entities.map { it.toDomain() }
        }

    override fun observeTransactionsForDate(
        ledgerId: Long,
        date: LocalDate
    ): Flow<List<MoneyTransaction>> =
        transactionDao.observeTransactionsForDate(ledgerId, date).map { entities ->
            entities.map { it.toDomain() }
        }

    override fun observeMonthlyStats(ledgerId: Long): Flow<List<MonthlyLedgerStats>> =
        transactionDao.observeMonthlyStats(ledgerId).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun getTransactions(ledgerId: Long, limit: Int, offset: Int): List<MoneyTransaction> =
        transactionDao.getTransactions(ledgerId, limit, offset).map { it.toDomain() }

    override suspend fun getAllTransactions(ledgerId: Long): List<MoneyTransaction> =
        transactionDao.getAllTransactions(ledgerId).map { it.toDomain() }

    override suspend fun getYearExpense(ledgerId: Long, year: Int): Long =
        transactionDao.getYearExpense(ledgerId, year.toString())

    override suspend fun getMonthExpense(ledgerId: Long, year: Int, month: Int): Long =
        transactionDao.getMonthExpense(ledgerId, String.format("%04d-%02d", year, month))

    override suspend fun upsert(transaction: MoneyTransaction) {
        transactionDao.upsert(transaction.toEntity())
    }

    override suspend fun delete(transactionId: Long) {
        transactionDao.deleteById(transactionId)
    }

    override suspend fun deleteByLedger(ledgerId: Long) {
        transactionDao.deleteByLedgerId(ledgerId)
    }

    override suspend fun deleteByLedgerAndDate(ledgerId: Long, date: LocalDate) {
        transactionDao.deleteByLedgerIdAndDate(ledgerId, date)
    }
}

