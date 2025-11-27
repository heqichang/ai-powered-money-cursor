package com.heqichang.dailymoney2.presentation.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heqichang.dailymoney2.domain.model.Category
import com.heqichang.dailymoney2.domain.usecase.category.DeleteCategoryUseCase
import com.heqichang.dailymoney2.domain.usecase.category.ObserveCategoriesUseCase
import com.heqichang.dailymoney2.domain.usecase.category.UpsertCategoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryManagerViewModel @Inject constructor(
    private val observeCategoriesUseCase: ObserveCategoriesUseCase,
    private val upsertCategoryUseCase: UpsertCategoryUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryManagerUiState())
    val uiState: StateFlow<CategoryManagerUiState> = _uiState.asStateFlow()

    private var observeJob: Job? = null

    fun setLedgerId(ledgerId: Long) {
        if (uiState.value.ledgerId == ledgerId) return
        _uiState.update { it.copy(ledgerId = ledgerId) }
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            observeCategoriesUseCase(ledgerId).collect { categories ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        categories = categories
                    )
                }
            }
        }
    }

    fun onAction(action: CategoryManagerAction) {
        when (action) {
            is CategoryManagerAction.EditCategory -> openEditor(action.category ?: defaultCategoryTemplate())
            CategoryManagerAction.DismissEditor -> closeEditor()
            is CategoryManagerAction.DeleteCategory -> deleteCategory(action.categoryId)
            is CategoryManagerAction.SaveCategory -> saveCategory(action.category)
        }
    }

    private fun openEditor(category: Category) {
        _uiState.update {
            it.copy(
                isEditorVisible = true,
                editingCategory = category
            )
        }
    }

    private fun closeEditor() {
        _uiState.update {
            it.copy(
                isEditorVisible = false,
                editingCategory = null
            )
        }
    }

    private fun saveCategory(category: Category) {
        viewModelScope.launch {
            runCatching { upsertCategoryUseCase(category) }
                .onFailure { error ->
                    _uiState.update { it.copy(errorMessage = error.message) }
                }
                .onSuccess { closeEditor() }
        }
    }

    private fun deleteCategory(categoryId: Long) {
        viewModelScope.launch {
            runCatching { deleteCategoryUseCase(categoryId) }
                .onFailure { error ->
                    _uiState.update { it.copy(errorMessage = error.message) }
                }
        }
    }

    private fun defaultCategoryTemplate(): Category = Category(
        ledgerId = uiState.value.ledgerId ?: 0,
        name = "",
        iconName = null,
        colorHex = null
    )
}

