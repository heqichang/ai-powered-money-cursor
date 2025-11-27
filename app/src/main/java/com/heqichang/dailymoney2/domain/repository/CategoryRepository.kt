package com.heqichang.dailymoney2.domain.repository

import com.heqichang.dailymoney2.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun observeCategories(ledgerId: Long): Flow<List<Category>>
    fun observeCategory(categoryId: Long): Flow<Category?>
    suspend fun upsert(category: Category)
    suspend fun delete(categoryId: Long)
}

