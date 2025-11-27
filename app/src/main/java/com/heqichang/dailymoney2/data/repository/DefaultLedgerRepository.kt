package com.heqichang.dailymoney2.data.repository

import com.heqichang.dailymoney2.data.local.dao.LedgerDao
import com.heqichang.dailymoney2.data.mapper.toDomain
import com.heqichang.dailymoney2.data.mapper.toEntity
import com.heqichang.dailymoney2.domain.model.Ledger
import com.heqichang.dailymoney2.domain.repository.LedgerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DefaultLedgerRepository(
    private val ledgerDao: LedgerDao
) : LedgerRepository {

    override fun observeLedgers(): Flow<List<Ledger>> =
        ledgerDao.observeLedgers().map { entities -> entities.map { it.toDomain() } }

    override fun observeLedger(ledgerId: Long): Flow<Ledger?> =
        ledgerDao.observeLedger(ledgerId).map { it?.toDomain() }

    override suspend fun upsert(ledger: Ledger) {
        ledgerDao.upsert(ledger.toEntity())
    }

    override suspend fun delete(ledgerId: Long) {
        ledgerDao.deleteById(ledgerId)
    }
}

