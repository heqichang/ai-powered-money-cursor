package com.heqichang.dailymoney2.domain.usecase.category

import com.heqichang.dailymoney2.domain.repository.CategoryRepository

class DeleteCategoryUseCase(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(categoryId: Long) {
        categoryRepository.delete(categoryId)
    }
}

