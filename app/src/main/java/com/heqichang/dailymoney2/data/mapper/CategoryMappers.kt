package com.heqichang.dailymoney2.data.mapper

import com.heqichang.dailymoney2.data.local.entity.CategoryEntity
import com.heqichang.dailymoney2.domain.model.Category

fun CategoryEntity.toDomain(): Category = Category(
    id = id,
    ledgerId = ledgerId,
    name = name,
    type = type,
    colorHex = colorHex,
    iconName = iconName,
    isDefault = isDefault
)

fun Category.toEntity(): CategoryEntity = CategoryEntity(
    id = id,
    ledgerId = ledgerId,
    name = name,
    type = type,
    colorHex = colorHex,
    iconName = iconName,
    isDefault = isDefault
)

