package ru.orangesoftware.financisto.feature.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.orangesoftware.financisto.usecase.modern.CreateCurrencyUseCase
import ru.orangesoftware.financisto.usecase.modern.GetCurrenciesUseCase
import ru.orangesoftware.financisto.data.model.CurrencyEntity
import javax.inject.Inject

/**
 * ViewModel for the Create Currency screen.
 */
@HiltViewModel
class CreateCurrencyViewModel @Inject constructor(
    private val createCurrencyUseCase: CreateCurrencyUseCase,
    private val getCurrenciesUseCase: GetCurrenciesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateCurrencyUiState())
    val uiState: StateFlow<CreateCurrencyUiState> = _uiState.asStateFlow()

    init {
        // Check if this should be the default currency
        viewModelScope.launch {
            val currencies = getCurrenciesUseCase.execute()
            if (currencies.isEmpty()) {
                _uiState.value = _uiState.value.copy(isDefault = true)
            }
        }
    }

    /**
     * Public method to handle user actions.
     */
    fun handleAction(action: CreateCurrencyAction) {
        when (action) {
            is CreateCurrencyAction.SetName -> setName(action.name)
            is CreateCurrencyAction.SetTitle -> setTitle(action.title)
            is CreateCurrencyAction.SetSymbol -> setSymbol(action.symbol)
            is CreateCurrencyAction.SetDecimals -> setDecimals(action.decimals)
            is CreateCurrencyAction.SetDecimalSeparator -> setDecimalSeparator(action.separator)
            is CreateCurrencyAction.SetGroupSeparator -> setGroupSeparator(action.separator)
            is CreateCurrencyAction.SetSymbolFormat -> setSymbolFormat(action.format)
            is CreateCurrencyAction.SetIsDefault -> setIsDefault(action.isDefault)
            is CreateCurrencyAction.SaveCurrency -> saveCurrency()
            is CreateCurrencyAction.DismissSaveError -> dismissSaveError()
        }
    }

    private fun setName(name: String) {
        _uiState.value = _uiState.value.copy(name = name)
        validateForm()
    }

    private fun setTitle(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
        validateForm()
    }

    private fun setSymbol(symbol: String) {
        _uiState.value = _uiState.value.copy(symbol = symbol)
        validateForm()
    }

    private fun setDecimals(decimals: String) {
        _uiState.value = _uiState.value.copy(decimals = decimals)
        validateForm()
    }

    private fun setDecimalSeparator(separator: String) {
        _uiState.value = _uiState.value.copy(decimalSeparator = separator)
        validateForm()
    }

    private fun setGroupSeparator(separator: String) {
        _uiState.value = _uiState.value.copy(groupSeparator = separator)
        validateForm()
    }

    private fun setSymbolFormat(format: String) {
        _uiState.value = _uiState.value.copy(symbolFormat = format)
        validateForm()
    }

    private fun setIsDefault(isDefault: Boolean) {
        _uiState.value = _uiState.value.copy(isDefault = isDefault)
        validateForm()
    }

    private fun saveCurrency() {
        val currentState = _uiState.value
        if (!currentState.isFormValid) {
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(saveState = CurrencySaveState.Saving)

            try {
                val currencyEntity = CurrencyEntity(
                    name = currentState.name.trim(),
                    title = currentState.title.trim(),
                    symbol = currentState.symbol.trim(),
                    decimals = currentState.decimals.toIntOrNull() ?: 2,
                    decimalSeparator = currentState.decimalSeparator,
                    groupSeparator = currentState.groupSeparator,
                    symbolFormat = currentState.symbolFormat,
                    isDefault = currentState.isDefault
                )

                val result = createCurrencyUseCase.execute(currencyEntity)

                result.fold(
                    onSuccess = { currencyId ->
                        _uiState.value = _uiState.value.copy(
                            saveState = CurrencySaveState.Success(currencyId)
                        )
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            saveState = CurrencySaveState.Error(
                                message = exception.message ?: "Failed to create currency",
                                exception = exception
                            )
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    saveState = CurrencySaveState.Error(
                        message = "Failed to save currency: ${e.message}",
                        exception = e
                    )
                )
            }
        }
    }

    private fun dismissSaveError() {
        _uiState.value = _uiState.value.copy(saveState = CurrencySaveState.Idle)
    }

    private fun validateForm() {
        val state = _uiState.value
        val errors = mutableListOf<CurrencyValidationError>()

        // Required fields validation
        if (state.name.isBlank()) {
            errors.add(CurrencyValidationError.NameRequired)
        }

        if (state.title.isBlank()) {
            errors.add(CurrencyValidationError.TitleRequired)
        }

        if (state.symbol.isBlank()) {
            errors.add(CurrencyValidationError.SymbolRequired)
        }

        // Validate decimals
        val decimalsInt = state.decimals.toIntOrNull()
        if (decimalsInt == null || decimalsInt < 0 || decimalsInt > 8) {
            errors.add(CurrencyValidationError.InvalidDecimals)
        }

        _uiState.value = _uiState.value.copy(
            validationErrors = errors,
            isFormValid = errors.isEmpty()
        )
    }
}