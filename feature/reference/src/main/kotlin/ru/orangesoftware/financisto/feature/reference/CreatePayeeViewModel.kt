package ru.orangesoftware.financisto.feature.reference

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.orangesoftware.financisto.usecase.modern.CreatePayeeUseCase
import javax.inject.Inject

/**
 * ViewModel for the Create Payee screen.
 */
@HiltViewModel
class CreatePayeeViewModel @Inject constructor(
    private val createPayeeUseCase: CreatePayeeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreatePayeeUiState())
    val uiState: StateFlow<CreatePayeeUiState> = _uiState.asStateFlow()

    // Navigation callback
    var onPayeeCreated: ((Long) -> Unit)? = null

    /**
     * Handles user actions.
     */
    fun handleAction(action: CreatePayeeAction) {
        when (action) {
            is CreatePayeeAction.UpdateName -> updateName(action.name)
            is CreatePayeeAction.CreatePayee -> createPayee()
            is CreatePayeeAction.DismissError -> dismissError()
        }
    }

    private fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(
            payeeName = name,
            errorMessage = null
        )
    }

    private fun createPayee() {
        val name = _uiState.value.payeeName.trim()
        if (name.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Payee name cannot be empty"
            )
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            val result = createPayeeUseCase.execute(name)
            result.onSuccess { payeeId ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true
                )
                onPayeeCreated?.invoke(payeeId)
            }.onFailure { exception ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = exception.message ?: "Failed to create payee"
                )
            }
        }
    }

    private fun dismissError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}