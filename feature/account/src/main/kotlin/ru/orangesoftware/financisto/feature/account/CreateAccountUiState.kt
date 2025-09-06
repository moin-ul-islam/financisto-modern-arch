package ru.orangesoftware.financisto.feature.account

/**
 * UI state for the Create Account screen.
 */
data class CreateAccountUiState(
    val screenState: CreateAccountScreenState = CreateAccountScreenState.Loading,
    val title: String = "",
    val selectedAccountType: AccountTypeOption? = null,
    val selectedCardIssuer: CardIssuerOption? = null,
    val selectedElectronicPaymentType: ElectronicPaymentTypeOption? = null,
    val issuerName: String = "",
    val cardNumber: String = "",
    val closingDay: String = "",
    val paymentDay: String = "",
    val selectedCurrency: CurrencyOption? = null,
    val limitAmount: String = "",
    val openingAmount: String = "",
    val note: String = "",
    val sortOrder: String = "",
    val isIncludedInTotals: Boolean = true,
    val validationErrors: List<ValidationError> = emptyList(),
    val isFormValid: Boolean = false,
    val saveState: SaveState = SaveState.Idle
)

/**
 * Screen states for Create Account.
 */
sealed class CreateAccountScreenState {
    object Loading : CreateAccountScreenState()
    data class Content(val data: CreateAccountContentData) : CreateAccountScreenState()
    data class Error(
        val message: String,
        val exception: Throwable? = null,
        val canRetry: Boolean = true
    ) : CreateAccountScreenState()
}

/**
 * Content data for the Create Account screen.
 */
data class CreateAccountContentData(
    val availableAccountTypes: List<AccountTypeOption> = emptyList(),
    val availableCardIssuers: List<CardIssuerOption> = emptyList(),
    val availableElectronicPaymentTypes: List<ElectronicPaymentTypeOption> = emptyList(),
    val availableCurrencies: List<CurrencyOption> = emptyList()
)

/**
 * Account type option for UI.
 */
data class AccountTypeOption(
    val name: String,
    val displayName: String,
    val iconResId: Int,
    val isCard: Boolean = false,
    val hasIssuer: Boolean = false,
    val isElectronic: Boolean = false,
    val hasNumber: Boolean = false,
    val isCreditCard: Boolean = false
)

/**
 * Card issuer option for UI.
 */
data class CardIssuerOption(
    val name: String,
    val displayName: String,
    val iconResId: Int
)

/**
 * Electronic payment type option for UI.
 */
data class ElectronicPaymentTypeOption(
    val name: String,
    val displayName: String,
    val iconResId: Int
)

/**
 * Currency option for UI.
 */
data class CurrencyOption(
    val id: Long,
    val name: String,
    val symbol: String
)

/**
 * Validation errors for the create account form.
 */
enum class ValidationError {
    TitleRequired,
    CurrencyRequired,
    InvalidClosingDay,
    InvalidPaymentDay,
    InvalidSortOrder,
    InvalidLimitAmount,
    InvalidOpeningAmount
}

/**
 * Save states for the create account operation.
 */
sealed class SaveState {
    object Idle : SaveState()
    object Saving : SaveState()
    data class Success(val accountId: Long) : SaveState()
    data class Error(val message: String, val exception: Throwable? = null) : SaveState()
}

/**
 * User actions for the Create Account screen.
 */
sealed class CreateAccountAction {
    object LoadInitialData : CreateAccountAction()
    data class SetTitle(val title: String) : CreateAccountAction()
    data class SetAccountType(val accountType: AccountTypeOption) : CreateAccountAction()
    data class SetCardIssuer(val cardIssuer: CardIssuerOption) : CreateAccountAction()
    data class SetElectronicPaymentType(val paymentType: ElectronicPaymentTypeOption) : CreateAccountAction()
    data class SetIssuerName(val issuerName: String) : CreateAccountAction()
    data class SetCardNumber(val cardNumber: String) : CreateAccountAction()
    data class SetClosingDay(val closingDay: String) : CreateAccountAction()
    data class SetPaymentDay(val paymentDay: String) : CreateAccountAction()
    data class SetCurrency(val currency: CurrencyOption) : CreateAccountAction()
    data class SetLimitAmount(val amount: String) : CreateAccountAction()
    data class SetOpeningAmount(val amount: String) : CreateAccountAction()
    data class SetNote(val note: String) : CreateAccountAction()
    data class SetSortOrder(val sortOrder: String) : CreateAccountAction()
    data class SetIncludedInTotals(val included: Boolean) : CreateAccountAction()
    object SaveAccount : CreateAccountAction()
    object DismissSaveError : CreateAccountAction()
    object NavigateToAddCurrency : CreateAccountAction()
}
