package ru.orangesoftware.financisto.modern.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.orangesoftware.financisto.usecase.modern.GetLastRunningBalanceForAccountUseCase
import ru.orangesoftware.financisto.usecase.modern.RebuildAllRunningBalancesUseCase
import ru.orangesoftware.financisto.usecase.modern.RebuildRunningBalanceForAccountUseCase
import javax.inject.Inject

/**
 * ViewModel for testing running balance use cases in the playground app.
 * 
 * This verifies that the running balance logic implemented in Phase 2.3
 * works correctly with proper Hilt dependency injection.
 */
@HiltViewModel
class RunningBalanceTestViewModel @Inject constructor(
    private val rebuildRunningBalanceForAccountUseCase: RebuildRunningBalanceForAccountUseCase,
    private val getLastRunningBalanceForAccountUseCase: GetLastRunningBalanceForAccountUseCase,
    private val rebuildAllRunningBalancesUseCase: RebuildAllRunningBalancesUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    
    data class UiState(
        val isLoading: Boolean = false,
        val result: String = "No operations performed yet"
    )
    
    fun rebuildRunningBalance(accountId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                result = "Rebuilding running balance for account $accountId..."
            )
            
            try {
                val result = rebuildRunningBalanceForAccountUseCase.execute(accountId)
                result.fold(
                    onSuccess = { success ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            result = if (success) {
                                "✅ Successfully rebuilt running balance for account $accountId"
                            } else {
                                "❌ Failed to rebuild running balance for account $accountId"
                            }
                        )
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            result = "❌ Error rebuilding running balance: ${exception.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    result = "❌ Unexpected error: ${e.message}"
                )
            }
        }
    }
    
    fun getLastRunningBalance(accountId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                result = "Getting last running balance for account $accountId..."
            )
            
            try {
                val result = getLastRunningBalanceForAccountUseCase.execute(accountId)
                result.fold(
                    onSuccess = { balance ->
                        val formattedBalance = balance / 100.0 // Convert from cents
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            result = "💰 Last running balance for account $accountId: $${String.format("%.2f", formattedBalance)}"
                        )
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            result = "❌ Error getting running balance: ${exception.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    result = "❌ Unexpected error: ${e.message}"
                )
            }
        }
    }
    
    fun rebuildAllRunningBalances() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                result = "Rebuilding all running balances..."
            )
            
            try {
                val result = rebuildAllRunningBalancesUseCase.execute()
                result.fold(
                    onSuccess = { success ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            result = if (success) {
                                "✅ Successfully rebuilt all running balances"
                            } else {
                                "❌ Failed to rebuild all running balances"
                            }
                        )
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            result = "❌ Error rebuilding all running balances: ${exception.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    result = "❌ Unexpected error: ${e.message}"
                )
            }
        }
    }
}
