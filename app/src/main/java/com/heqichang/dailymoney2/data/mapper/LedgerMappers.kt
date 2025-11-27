package com.heqichang.dailymoney2.data.mapper

import com.heqichang.dailymoney2.data.local.entity.LedgerEntity
import com.heqichang.dailymoney2.domain.model.Ledger

fun LedgerEntity.toDomain(): Ledger = Ledger(
    id = id,
    name = name,
    description = description,
    createdAtEpochMillis = createdAtEpochMillis
)

fun Ledger.toEntity(): LedgerEntity = LedgerEntity(
    id = id,
    name = name,
    description = description,
    createdAtEpochMillis = createdAtEpochMillis
)

