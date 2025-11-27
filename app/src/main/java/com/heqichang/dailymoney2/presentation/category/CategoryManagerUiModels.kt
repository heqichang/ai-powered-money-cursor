package com.heqichang.dailymoney2.presentation.category

import com.heqichang.dailymoney2.domain.model.Category

data class CategoryManagerUiState(
    val ledgerId: Long? = null,
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val isEditorVisible: Boolean = false,
    val editingCategory: Category? = null,
    val errorMessage: String? = null
)

sealed interface CategoryManagerAction {
    data class EditCategory(val category: Category?) : CategoryManagerAction
    data class SaveCategory(val category: Category) : CategoryManagerAction
    data class DeleteCategory(val categoryId: Long) : CategoryManagerAction
    data object DismissEditor : CategoryManagerAction
}

