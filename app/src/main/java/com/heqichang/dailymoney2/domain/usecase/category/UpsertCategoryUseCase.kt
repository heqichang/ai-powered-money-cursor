package com.heqichang.dailymoney2.domain.usecase.category

import com.heqichang.dailymoney2.domain.model.Category
import com.heqichang.dailymoney2.domain.repository.CategoryRepository

class UpsertCategoryUseCase(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(category: Category) {
        categoryRepository.upsert(category)
    }
}

