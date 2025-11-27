package com.heqichang.dailymoney2.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.heqichang.dailymoney2.data.local.dto.MonthlyLedgerStatsEntity
import com.heqichang.dailymoney2.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface TransactionDao {
    @Query(
        """
        SELECT * FROM transactions
        WHERE ledger_id = :ledgerId
        ORDER BY occurred_on DESC, created_at_epoch_millis DESC
        """
    )
    fun observeTransactions(ledgerId: Long): Flow<List<TransactionEntity>>

    @Query(
        """
        SELECT * FROM transactions
        WHERE ledger_id = :ledgerId
        ORDER BY occurred_on DESC, created_at_epoch_millis DESC
        LIMIT :limit OFFSET :offset
        """
    )
    suspend fun getTransactions(ledgerId: Long, limit: Int, offset: Int): List<TransactionEntity>

    @Query(
        """
        SELECT * FROM transactions
        WHERE ledger_id = :ledgerId
        ORDER BY occurred_on DESC, created_at_epoch_millis DESC
        """
    )
    suspend fun getAllTransactions(ledgerId: Long): List<TransactionEntity>

    @Query(
        """
        SELECT * FROM transactions
        WHERE ledger_id = :ledgerId AND occurred_on = :date
        ORDER BY created_at_epoch_millis DESC
        """
    )
    fun observeTransactionsForDate(
        ledgerId: Long,
        date: LocalDate
    ): Flow<List<TransactionEntity>>

    @Query(
        """
        SELECT 
            ledger_id AS ledgerId,
            substr(occurred_on, 1, 7) AS yearMonth,
            SUM(CASE WHEN amount_in_cents >= 0 THEN amount_in_cents ELSE 0 END) AS incomeInCents,
            SUM(CASE WHEN amount_in_cents < 0 THEN amount_in_cents ELSE 0 END) AS expenseInCents
        FROM transactions
        WHERE ledger_id = :ledgerId
        GROUP BY ledger_id, substr(occurred_on, 1, 7)
        ORDER BY yearMonth DESC
        """
    )
    fun observeMonthlyStats(ledgerId: Long): Flow<List<MonthlyLedgerStatsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :transactionId")
    suspend fun deleteById(transactionId: Long)

    @Query("DELETE FROM transactions WHERE ledger_id = :ledgerId")
    suspend fun deleteByLedgerId(ledgerId: Long)

    @Query("DELETE FROM transactions WHERE ledger_id = :ledgerId AND occurred_on = :date")
    suspend fun deleteByLedgerIdAndDate(ledgerId: Long, date: LocalDate)

    @Query(
        """
        SELECT COALESCE(SUM(CASE WHEN amount_in_cents < 0 THEN -amount_in_cents ELSE 0 END), 0)
        FROM transactions
        WHERE ledger_id = :ledgerId 
        AND substr(occurred_on, 1, 4) = :year
        """
    )
    suspend fun getYearExpense(ledgerId: Long, year: String): Long

    @Query(
        """
        SELECT COALESCE(SUM(CASE WHEN amount_in_cents < 0 THEN -amount_in_cents ELSE 0 END), 0)
        FROM transactions
        WHERE ledger_id = :ledgerId 
        AND substr(occurred_on, 1, 7) = :yearMonth
        """
    )
    suspend fun getMonthExpense(ledgerId: Long, yearMonth: String): Long
}

