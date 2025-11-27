package com.heqichang.dailymoney2.data.mapper

import com.heqichang.dailymoney2.data.local.entity.TransactionEntity
import com.heqichang.dailymoney2.domain.model.MoneyTransaction

fun TransactionEntity.toDomain(): MoneyTransaction = MoneyTransaction(
    id = id,
    ledgerId = ledgerId,
    categoryId = categoryId,
    amountInCents = amountInCents,
    occurredOn = occurredOn,
    note = note,
    createdAtEpochMillis = createdAtEpochMillis
)

fun MoneyTransaction.toEntity(): TransactionEntity = TransactionEntity(
    id = id,
    ledgerId = ledgerId,
    categoryId = categoryId,
    amountInCents = amountInCents,
    occurredOn = occurredOn,
    note = note,
    createdAtEpochMillis = createdAtEpochMillis
)

