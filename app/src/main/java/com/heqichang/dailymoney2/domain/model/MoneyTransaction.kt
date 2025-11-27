package com.heqichang.dailymoney2.domain.model

import java.time.LocalDate

data class MoneyTransaction(
    val id: Long = 0,
    val ledgerId: Long,
    val categoryId: Long?,
    val amountInCents: Long,
    val occurredOn: LocalDate,
    val note: String? = null,
    val createdAtEpochMillis: Long = System.currentTimeMillis()
)

