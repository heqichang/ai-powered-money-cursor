package com.heqichang.dailymoney2.domain.model

import java.time.YearMonth

data class MonthlyLedgerStats(
    val ledgerId: Long,
    val yearMonth: YearMonth,
    val incomeInCents: Long,
    val expenseInCents: Long
) {
    val netInCents: Long = incomeInCents + expenseInCents
}

