package com.heqichang.dailymoney2.domain.repository

import com.heqichang.dailymoney2.domain.model.Ledger
import kotlinx.coroutines.flow.Flow

interface LedgerRepository {
    fun observeLedgers(): Flow<List<Ledger>>
    fun observeLedger(ledgerId: Long): Flow<Ledger?>
    suspend fun upsert(ledger: Ledger)
    suspend fun delete(ledgerId: Long)
}

