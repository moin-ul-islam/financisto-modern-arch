package ru.orangesoftware.financisto.playground.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.orangesoftware.financisto.data.model.AccountEntity
import ru.orangesoftware.financisto.usecase.modern.CreateAccountUseCase
import ru.orangesoftware.financisto.usecase.modern.GetAccountsUseCase
import javax.inject.Inject

/**
 * ViewModel for the Account List screen in the playground app.
 * 
 * This demonstrates:
 * - Hilt dependency injection with ViewModels
 * - Use case pattern for business logic
 * - StateFlow for UI state management
 * - Coroutines for async operations
 */
@HiltViewModel
class AccountListViewModel @Inject constructor(
    private val getAccountsUseCase: GetAccountsUseCase,
    private val createAccountUseCase: CreateAccountUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    
    data class UiState(
        val isLoading: Boolean = false,
        val        accounts: List<AccountEntity> = emptyList(),
        val error: String? = null
    )
    
    fun loadAccounts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val accounts = getAccountsUseCase.execute()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    accounts = accounts,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }
    
    fun createTestAccount() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                // Create a sample account for testing
                val testAccount = AccountEntity(
                    title = "Test Account ${System.currentTimeMillis()}",
                    type = "CASH",
                    currencyId = 1L,
                    totalAmount = 100000L, // $1000.00 in cents
                    isActive = true,
                    isIncludeIntoTotals = true
                )
                
                val result = createAccountUseCase.execute(testAccount)
                if (result.isSuccess) {
                    // Reload accounts to show the new one
                    loadAccounts()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Failed to create account"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }
}
