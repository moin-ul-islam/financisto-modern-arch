package ru.orangesoftware.financisto.feature.account

/**
 * UI state for the Create Currency screen.
 */
data class CreateCurrencyUiState(
    val name: String = "",
    val title: String = "",
    val symbol: String = "",
    val decimals: String = "2",
    val decimalSeparator: String = ".",
    val groupSeparator: String = ",",
    val symbolFormat: String = "RS",
    val isDefault: Boolean = false,
    val validationErrors: List<CurrencyValidationError> = emptyList(),
    val isFormValid: Boolean = false,
    val saveState: CurrencySaveState = CurrencySaveState.Idle
)

/**
 * Validation errors for the create currency form.
 */
enum class CurrencyValidationError {
    NameRequired,
    NameAlreadyExists,
    TitleRequired,
    SymbolRequired,
    InvalidDecimals
}

/**
 * Save states for the create currency operation.
 */
sealed class CurrencySaveState {
    object Idle : CurrencySaveState()
    object Saving : CurrencySaveState()
    data class Success(val currencyId: Long) : CurrencySaveState()
    data class Error(val message: String, val exception: Throwable? = null) : CurrencySaveState()
}

/**
 * User actions for the Create Currency screen.
 */
sealed class CreateCurrencyAction {
    data class SetName(val name: String) : CreateCurrencyAction()
    data class SetTitle(val title: String) : CreateCurrencyAction()
    data class SetSymbol(val symbol: String) : CreateCurrencyAction()
    data class SetDecimals(val decimals: String) : CreateCurrencyAction()
    data class SetDecimalSeparator(val separator: String) : CreateCurrencyAction()
    data class SetGroupSeparator(val separator: String) : CreateCurrencyAction()
    data class SetSymbolFormat(val format: String) : CreateCurrencyAction()
    data class SetIsDefault(val isDefault: Boolean) : CreateCurrencyAction()
    object SaveCurrency : CreateCurrencyAction()
    object DismissSaveError : CreateCurrencyAction()
}