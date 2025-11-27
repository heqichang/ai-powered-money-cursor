package com.heqichang.dailymoney2.domain.model

import com.heqichang.dailymoney2.data.local.entity.CategoryType

data class Category(
    val id: Long = 0,
    val ledgerId: Long,
    val name: String,
    val type: CategoryType = CategoryType.EXPENSE,
    val colorHex: String? = null,
    val iconName: String? = null,
    val isDefault: Boolean = false
)

