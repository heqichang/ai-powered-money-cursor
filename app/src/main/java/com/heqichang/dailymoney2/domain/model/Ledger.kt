package com.heqichang.dailymoney2.domain.model

data class Ledger(
    val id: Long = 0,
    val name: String,
    val description: String,
    val createdAtEpochMillis: Long = System.currentTimeMillis()
)

