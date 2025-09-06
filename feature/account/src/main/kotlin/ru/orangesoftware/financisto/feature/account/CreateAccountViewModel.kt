package ru.orangesoftware.financisto.feature.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * ViewModel for the Create Account screen.
 * 
 * This ViewModel demonstrates modern MVVM architecture:
 * - Uses StateFlow for reactive UI state management
 * - Handles complex form validation and state management
 * - Coordinates between multiple use cases
 * - Handles loading states, errors, and user actions
 * - Preserves state across configuration changes
 */
@HiltViewModel
class CreateAccountViewModel @Inject constructor(
    // TODO: Inject use cases when they become available
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateAccountUiState())
    val uiState: StateFlow<CreateAccountUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    /**
     * Public method to handle user actions.
     * This is the single entry point for all UI interactions.
     */
    fun handleAction(action: CreateAccountAction) {
        when (action) {
            is CreateAccountAction.LoadInitialData -> loadInitialData()
            is CreateAccountAction.SetTitle -> setTitle(action.title)
            is CreateAccountAction.SetAccountType -> setAccountType(action.accountType)
            is CreateAccountAction.SetCardIssuer -> setCardIssuer(action.cardIssuer)
            is CreateAccountAction.SetElectronicPaymentType -> setElectronicPaymentType(action.paymentType)
            is CreateAccountAction.SetIssuerName -> setIssuerName(action.issuerName)
            is CreateAccountAction.SetCardNumber -> setCardNumber(action.cardNumber)
            is CreateAccountAction.SetClosingDay -> setClosingDay(action.closingDay)
            is CreateAccountAction.SetPaymentDay -> setPaymentDay(action.paymentDay)
            is CreateAccountAction.SetCurrency -> setCurrency(action.currency)
            is CreateAccountAction.SetLimitAmount -> setLimitAmount(action.amount)
            is CreateAccountAction.SetOpeningAmount -> setOpeningAmount(action.amount)
            is CreateAccountAction.SetNote -> setNote(action.note)
            is CreateAccountAction.SetSortOrder -> setSortOrder(action.sortOrder)
            is CreateAccountAction.SetIncludedInTotals -> setIncludedInTotals(action.included)
            is CreateAccountAction.SaveAccount -> saveAccount()
            is CreateAccountAction.DismissSaveError -> dismissSaveError()
            is CreateAccountAction.NavigateToAddCurrency -> {} // Navigation handled by UI
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(screenState = CreateAccountScreenState.Loading)
            
            try {
                // TODO: Replace with actual use case calls when available
                delay(500) // Simulate loading
                
                val currencyOptions = getMockCurrencies()
                val accountTypes = getAvailableAccountTypes()
                val cardIssuers = getAvailableCardIssuers()
                val electronicPaymentTypes = getAvailableElectronicPaymentTypes()
                
                val contentData = CreateAccountContentData(
                    availableCurrencies = currencyOptions,
                    availableAccountTypes = accountTypes,
                    availableCardIssuers = cardIssuers,
                    availableElectronicPaymentTypes = electronicPaymentTypes
                )
                
                _uiState.value = _uiState.value.copy(
                    screenState = CreateAccountScreenState.Content(contentData)
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    screenState = CreateAccountScreenState.Error(
                        message = e.message ?: "Failed to load initial data",
                        canRetry = true
                    )
                )
            }
        }
    }
    
    private fun getMockCurrencies(): List<CurrencyOption> {
        return listOf(
            CurrencyOption(id = 1, name = "US Dollar", symbol = "$"),
            CurrencyOption(id = 2, name = "Euro", symbol = "€"),
            CurrencyOption(id = 3, name = "British Pound", symbol = "£"),
            CurrencyOption(id = 4, name = "Japanese Yen", symbol = "¥")
        )
    }

    private fun setTitle(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
        validateForm()
    }

    private fun setAccountType(accountType: AccountTypeOption) {
        _uiState.value = _uiState.value.copy(
            selectedAccountType = accountType,
            // Reset conditional fields when account type changes
            selectedCardIssuer = if (accountType.isCard) null else _uiState.value.selectedCardIssuer,
            selectedElectronicPaymentType = if (accountType.isElectronic) null else _uiState.value.selectedElectronicPaymentType,
            issuerName = if (accountType.hasIssuer) _uiState.value.issuerName else "",
            cardNumber = if (accountType.hasNumber) _uiState.value.cardNumber else "",
            closingDay = if (accountType.isCreditCard) _uiState.value.closingDay else "",
            paymentDay = if (accountType.isCreditCard) _uiState.value.paymentDay else "",
            limitAmount = if (accountType.isCreditCard) _uiState.value.limitAmount else ""
        )
        validateForm()
    }

    private fun setCardIssuer(cardIssuer: CardIssuerOption) {
        _uiState.value = _uiState.value.copy(selectedCardIssuer = cardIssuer)
        validateForm()
    }

    private fun setElectronicPaymentType(paymentType: ElectronicPaymentTypeOption) {
        _uiState.value = _uiState.value.copy(selectedElectronicPaymentType = paymentType)
        validateForm()
    }

    private fun setIssuerName(issuerName: String) {
        _uiState.value = _uiState.value.copy(issuerName = issuerName)
        validateForm()
    }

    private fun setCardNumber(cardNumber: String) {
        _uiState.value = _uiState.value.copy(cardNumber = cardNumber)
        validateForm()
    }

    private fun setClosingDay(closingDay: String) {
        _uiState.value = _uiState.value.copy(closingDay = closingDay)
        validateForm()
    }

    private fun setPaymentDay(paymentDay: String) {
        _uiState.value = _uiState.value.copy(paymentDay = paymentDay)
        validateForm()
    }

    private fun setCurrency(currency: CurrencyOption) {
        _uiState.value = _uiState.value.copy(selectedCurrency = currency)
        validateForm()
    }

    private fun setLimitAmount(amount: String) {
        _uiState.value = _uiState.value.copy(limitAmount = amount)
        validateForm()
    }

    private fun setOpeningAmount(amount: String) {
        _uiState.value = _uiState.value.copy(openingAmount = amount)
        validateForm()
    }

    private fun setNote(note: String) {
        _uiState.value = _uiState.value.copy(note = note)
        validateForm()
    }

    private fun setSortOrder(sortOrder: String) {
        _uiState.value = _uiState.value.copy(sortOrder = sortOrder)
        validateForm()
    }

    private fun setIncludedInTotals(included: Boolean) {
        _uiState.value = _uiState.value.copy(isIncludedInTotals = included)
        validateForm()
    }

    private fun saveAccount() {
        val currentState = _uiState.value
        if (!currentState.isFormValid) {
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(saveState = SaveState.Saving)
            
            try {
                // TODO: Create Account entity from UI state and save using use case
                // This would use createAccountUseCase.execute() when available
                
                // For now, simulate successful save
                val accountId = System.currentTimeMillis() // Mock account ID
                
                _uiState.value = _uiState.value.copy(
                    saveState = SaveState.Success(accountId)
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    saveState = SaveState.Error(
                        message = "Failed to save account: ${e.message}",
                        exception = e
                    )
                )
            }
        }
    }

    private fun dismissSaveError() {
        _uiState.value = _uiState.value.copy(saveState = SaveState.Idle)
    }

    private fun validateForm() {
        val state = _uiState.value
        val errors = mutableListOf<ValidationError>()

        // Required fields validation
        if (state.title.isBlank()) {
            errors.add(ValidationError.TitleRequired)
        }

        if (state.selectedCurrency == null) {
            errors.add(ValidationError.CurrencyRequired)
        }

        // Validate closing day for credit cards
        if (state.selectedAccountType?.isCreditCard == true && state.closingDay.isNotBlank()) {
            val closingDayInt = state.closingDay.toIntOrNull()
            if (closingDayInt == null || closingDayInt > 31) {
                errors.add(ValidationError.InvalidClosingDay)
            }
        }

        // Validate payment day for credit cards
        if (state.selectedAccountType?.isCreditCard == true && state.paymentDay.isNotBlank()) {
            val paymentDayInt = state.paymentDay.toIntOrNull()
            if (paymentDayInt == null || paymentDayInt > 31) {
                errors.add(ValidationError.InvalidPaymentDay)
            }
        }

        // Validate sort order if provided
        if (state.sortOrder.isNotBlank()) {
            val sortOrderInt = state.sortOrder.toIntOrNull()
            if (sortOrderInt == null) {
                errors.add(ValidationError.InvalidSortOrder)
            }
        }

        _uiState.value = _uiState.value.copy(
            validationErrors = errors,
            isFormValid = errors.isEmpty()
        )
    }

    // Helper functions to get available options
    private fun getAvailableAccountTypes(): List<AccountTypeOption> {
        // TODO: Get from actual AccountType enum
        return listOf(
            AccountTypeOption(
                name = "CASH",
                displayName = "Cash",
                iconResId = android.R.drawable.ic_menu_gallery,
                isCard = false,
                hasIssuer = false,
                isElectronic = false,
                hasNumber = false,
                isCreditCard = false
            ),
            AccountTypeOption(
                name = "BANK",
                displayName = "Bank Account",
                iconResId = android.R.drawable.ic_menu_gallery,
                isCard = false,
                hasIssuer = true,
                isElectronic = false,
                hasNumber = true,
                isCreditCard = false
            ),
            AccountTypeOption(
                name = "CREDIT_CARD",
                displayName = "Credit Card",
                iconResId = android.R.drawable.ic_menu_gallery,
                isCard = true,
                hasIssuer = true,
                isElectronic = false,
                hasNumber = true,
                isCreditCard = true
            ),
            AccountTypeOption(
                name = "DEBIT_CARD",
                displayName = "Debit Card",
                iconResId = android.R.drawable.ic_menu_gallery,
                isCard = true,
                hasIssuer = true,
                isElectronic = false,
                hasNumber = true,
                isCreditCard = false
            ),
            AccountTypeOption(
                name = "ELECTRONIC",
                displayName = "Electronic",
                iconResId = android.R.drawable.ic_menu_gallery,
                isCard = false,
                hasIssuer = false,
                isElectronic = true,
                hasNumber = false,
                isCreditCard = false
            )
        )
    }

    private fun getAvailableCardIssuers(): List<CardIssuerOption> {
        // TODO: Get from actual CardIssuer enum
        return listOf(
            CardIssuerOption("VISA", "Visa", android.R.drawable.ic_menu_gallery),
            CardIssuerOption("MASTERCARD", "MasterCard", android.R.drawable.ic_menu_gallery),
            CardIssuerOption("AMEX", "American Express", android.R.drawable.ic_menu_gallery),
            CardIssuerOption("DEFAULT", "Other", android.R.drawable.ic_menu_gallery)
        )
    }

    private fun getAvailableElectronicPaymentTypes(): List<ElectronicPaymentTypeOption> {
        // TODO: Get from actual ElectronicPaymentType enum
        return listOf(
            ElectronicPaymentTypeOption("PAYPAL", "PayPal", android.R.drawable.ic_menu_gallery),
            ElectronicPaymentTypeOption("WEBMONEY", "WebMoney", android.R.drawable.ic_menu_gallery),
            ElectronicPaymentTypeOption("YANDEX_MONEY", "Yandex.Money", android.R.drawable.ic_menu_gallery)
        )
    }

    companion object {
        private const val TAG = "CreateAccountViewModel"
    }
}
