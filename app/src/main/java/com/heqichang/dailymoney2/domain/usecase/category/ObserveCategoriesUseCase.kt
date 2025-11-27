package com.heqichang.dailymoney2.domain.usecase.category

import com.heqichang.dailymoney2.domain.model.Category
import com.heqichang.dailymoney2.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow

class ObserveCategoriesUseCase(
    private val categoryRepository: CategoryRepository
) {
    operator fun invoke(ledgerId: Long): Flow<List<Category>> =
        categoryRepository.observeCategories(ledgerId)
}

