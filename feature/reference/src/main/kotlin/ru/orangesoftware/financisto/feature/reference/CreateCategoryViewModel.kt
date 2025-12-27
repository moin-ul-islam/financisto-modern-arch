package ru.orangesoftware.financisto.feature.reference

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.orangesoftware.financisto.data.model.CategoryEntity
import ru.orangesoftware.financisto.usecase.modern.InsertCategoryUseCase
import javax.inject.Inject

/**
 * ViewModel for the Create Category screen.
 */
@HiltViewModel
class CreateCategoryViewModel @Inject constructor(
    private val insertCategoryUseCase: InsertCategoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateCategoryUiState())
    val uiState: StateFlow<CreateCategoryUiState> = _uiState.asStateFlow()

    // Navigation callback
    var onCategoryCreated: ((Long) -> Unit)? = null

    /**
     * Handles user actions.
     */
    fun handleAction(action: CreateCategoryAction) {
        when (action) {
            is CreateCategoryAction.UpdateName -> updateName(action.name)
            is CreateCategoryAction.CreateCategory -> createCategory()
            is CreateCategoryAction.DismissError -> dismissError()
        }
    }

    private fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(
            categoryName = name,
            errorMessage = null
        )
    }

    private fun createCategory() {
        val name = _uiState.value.categoryName.trim()
        if (name.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Category name cannot be empty"
            )
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                val categoryEntity = CategoryEntity(
                    title = name,
                    left = 0, // Will be set by the use case for hierarchy
                    right = 0, // Will be set by the use case for hierarchy
                    type = 0, // 0 = expense, 1 = income
                    isActive = true
                )

                val result = insertCategoryUseCase.execute(categoryEntity)
                result.onSuccess { categoryId ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true
                    )
                    onCategoryCreated?.invoke(categoryId)
                }.onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Failed to create category"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to create category"
                )
            }
        }
    }

    private fun dismissError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}