package com.heqichang.dailymoney2.data.mapper

import com.heqichang.dailymoney2.data.local.dto.MonthlyLedgerStatsEntity
import com.heqichang.dailymoney2.domain.model.MonthlyLedgerStats
import java.time.YearMonth

fun MonthlyLedgerStatsEntity.toDomain(): MonthlyLedgerStats = MonthlyLedgerStats(
    ledgerId = ledgerId,
    yearMonth = YearMonth.parse(yearMonth),
    incomeInCents = incomeInCents,
    expenseInCents = expenseInCents
)

