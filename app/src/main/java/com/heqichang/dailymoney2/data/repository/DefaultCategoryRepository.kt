package com.heqichang.dailymoney2.data.repository

import com.heqichang.dailymoney2.data.local.dao.CategoryDao
import com.heqichang.dailymoney2.data.mapper.toDomain
import com.heqichang.dailymoney2.data.mapper.toEntity
import com.heqichang.dailymoney2.domain.model.Category
import com.heqichang.dailymoney2.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DefaultCategoryRepository(
    private val categoryDao: CategoryDao
) : CategoryRepository {

    override fun observeCategories(ledgerId: Long): Flow<List<Category>> =
        categoryDao.observeCategories(ledgerId).map { entities ->
            entities.map { it.toDomain() }
        }

    override fun observeCategory(categoryId: Long): Flow<Category?> =
        categoryDao.observeCategory(categoryId).map { it?.toDomain() }

    override suspend fun upsert(category: Category) {
        categoryDao.upsert(category.toEntity())
    }

    override suspend fun delete(categoryId: Long) {
        categoryDao.deleteById(categoryId)
    }
}

