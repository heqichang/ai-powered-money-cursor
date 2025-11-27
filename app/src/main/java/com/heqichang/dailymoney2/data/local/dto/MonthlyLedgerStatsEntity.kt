package com.heqichang.dailymoney2.data.local.dto

data class MonthlyLedgerStatsEntity(
    val ledgerId: Long,
    val yearMonth: String,
    val incomeInCents: Long,
    val expenseInCents: Long
)

