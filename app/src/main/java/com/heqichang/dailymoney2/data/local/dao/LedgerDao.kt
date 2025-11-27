package com.heqichang.dailymoney2.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.heqichang.dailymoney2.data.local.entity.LedgerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LedgerDao {
    @Query("SELECT * FROM ledgers ORDER BY created_at_epoch_millis DESC")
    fun observeLedgers(): Flow<List<LedgerEntity>>

    @Query("SELECT * FROM ledgers WHERE id = :ledgerId")
    fun observeLedger(ledgerId: Long): Flow<LedgerEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(ledger: LedgerEntity)

    @Query("DELETE FROM ledgers WHERE id = :ledgerId")
    suspend fun deleteById(ledgerId: Long)
}

