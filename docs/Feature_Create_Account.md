# Feature: Create Account

**Status:** Work in Progress (Functional with Limitations)  
**Last Updated:** December 25, 2025  
**Module:** `:feature:account`

## Overview

The Create Account feature allows users to create new financial accounts or edit existing ones in the Financisto modern app. It supports various account types (cash, bank accounts, credit cards, debit cards, electronic payments) with conditional form fields based on the selected account type.

This is a modernized reimplementation of the legacy `AccountActivity` using Jetpack Compose, MVVM architecture, and Clean Architecture principles. The feature supports both create and edit modes within a single implementation.

## Architecture

The feature follows the standard Clean Architecture pattern with these layers:

```
CreateAccountScreen (Compose UI)
    ↓
CreateAccountViewModel 
    ↓
Use Cases (CreateAccountUseCase, UpdateAccountUseCase, GetCurrenciesUseCase)
    ↓
AccountRepository & CurrencyRepository
    ↓
AccountDao & CurrencyDao (Room)
    ↓
SQLite Database
```

### Module Dependencies

- **`:core:common`** - Shared utilities and common code
- **`:core:ui`** - Shared UI components and theming
- **`:usecase`** - Business logic layer
- **`:repository`** - Data access layer

## Implementation Status

### ✅ Implemented Features

1. **Dual Mode Support**
   - Create new account mode
   - Edit existing account mode
   - Mode determined by presence of `accountId` in navigation arguments
   - Pre-populates form when editing

2. **Dynamic Form Fields**
   - Account type selection (Cash, Bank, Credit Card, Debit Card, Electronic)
   - Conditional field visibility based on account type:
     - Card Issuer (for card types)
     - Electronic Payment Type (for electronic accounts)
     - Issuer Name (for accounts with issuers)
     - Card Number (for numbered accounts)
     - Closing Day (for credit cards)
     - Payment Day (for credit cards)
     - Limit Amount (for credit cards)
   - Title field (required)
   - Currency selection (required)
   - Opening amount/balance
   - Note field
   - Sort order field
   - Include in totals checkbox

3. **Account Type Options**
   - **Cash**: Simple account type, no additional fields
   - **Bank Account**: Requires issuer name and account number
   - **Credit Card**: Full set of card-specific fields (issuer, number, closing day, payment day, limit)
   - **Debit Card**: Card issuer and number
   - **Electronic**: Electronic payment type selection

4. **Form Validation**
   - Real-time validation as user types
   - Required field validation (title, currency)
   - Numeric checks only for closing day ≤ 31, payment day ≤ 31, and sort order parse
   - ⚠️ Does **not** validate limit amount or opening amount formats despite error enums existing
   - Save button disabled when form invalid
   - Visual error indicators on invalid fields

5. **Currency Integration**
   - Loads all currencies from database
   - Displays currency list with name and symbol
   - "Add Currency" button to create custom currencies
   - ⚠️ Auto-refresh/auto-select after currency creation is not wired; screen keeps a local flag that is never set from navigation

6. **State Management**
   - Single StateFlow for all UI state
   - Sealed classes for screen states (Loading, Content, Error)
   - Sealed classes for save states (Idle, Saving, Success, Error)
   - Input action pattern for all user interactions
   - State preservation across configuration changes
   - Currency cache to avoid redundant DB queries

7. **Save Functionality**
   - Create new accounts with proper validation
   - Update existing accounts in edit mode
   - Converts UI state to AccountEntity
   - Amount parsing (user-friendly decimal to cents conversion)
   - Success navigation after save
   - Error handling with retry capability

8. **UI/UX Features**
   - Modern Material 3 design
   - Scrollable form for smaller screens
   - Back navigation support
   - Loading indicators during operations
   - Error dialogs for save failures
   - Action buttons (Save/Update and Cancel)
   - Proper keyboard handling for numeric fields

### 🚧 Partially Implemented Features

1. **Account Type Icons**
   - Icon system exists in data models
   - ⚠️ **Missing:** Actual icon resource mapping (using placeholder icons)
   - ⚠️ **Missing:** Visual icon display in account type selector

2. **Card Issuer Options**
   - Basic issuers defined (Visa, MasterCard, Amex, Other)
   - ⚠️ **Missing:** Complete list of card issuers
   - ⚠️ **Missing:** Actual icon resources for issuers

3. **Electronic Payment Types**
   - Basic types defined (PayPal, WebMoney, Yandex.Money)
   - ⚠️ **Missing:** Complete list of payment types
   - ⚠️ **Missing:** Modern payment providers (Apple Pay, Google Pay, etc.)

5. **Option Sources**
   - Account types, issuers, and payment types are hardcoded placeholder lists in the ViewModel
   - ⚠️ **Missing:** Shared enums/resources and icon assets for these options

4. **Amount Formatting**
   - Basic decimal to cents conversion works
   - ⚠️ **Missing:** Currency-specific decimal places support
   - ⚠️ **Missing:** Localized number formatting
   - ⚠️ **Missing:** Thousands separator display

### ❌ Not Yet Implemented Features

1. **Testing**
   - No test files found for create account feature
   - ⚠️ **Missing:** ViewModel tests
   - ⚠️ **Missing:** UI tests
   - ⚠️ **Missing:** Form validation tests
   - ⚠️ **Missing:** Use case tests

2. **Advanced Validation**
   - ⚠️ **Missing:** Card number format validation (Luhn algorithm)
   - ⚠️ **Missing:** Card number masking during input
   - ⚠️ **Missing:** Duplicate account name detection
   - ⚠️ **Missing:** Minimum/maximum amount validation

3. **Enhanced UX**
   - ⚠️ **Missing:** Field-specific error messages
   - ⚠️ **Missing:** Input field hints/placeholders
   - ⚠️ **Missing:** Field focus management
   - ⚠️ **Missing:** Unsaved changes warning on back navigation

4. **Account Type Enums**
   - Currently using hardcoded strings
   - ⚠️ **Missing:** Proper enum definitions from legacy code
   - ⚠️ **Missing:** Centralized account type management

5. **Edit Mode Enhancements**
   - ⚠️ **Missing:** Change tracking (only save if changed)
   - ⚠️ **Missing:** Delete account from edit screen
   - ⚠️ **Missing:** Activity log/audit trail

## File Structure

```
feature/account/
├── CreateAccountUiState.kt          # UI state models and sealed classes
├── CreateAccountViewModel.kt        # ViewModel with business logic
├── ui/
│   ├── CreateAccountScreen.kt       # Main screen composable
│   └── components/
│       └── createaccount/
│           ├── SelectionComponents.kt  # Account type, currency selectors
│           ├── FormFieldComponents.kt  # Input fields
│           └── ActionComponents.kt     # Save/cancel buttons
```

## Data Models

### CreateAccountUiState

```kotlin
data class CreateAccountUiState(
    val screenState: CreateAccountScreenState,
    val title: String,
    val selectedAccountType: AccountTypeOption?,
    val selectedCardIssuer: CardIssuerOption?,
    val selectedElectronicPaymentType: ElectronicPaymentTypeOption?,
    val issuerName: String,
    val cardNumber: String,
    val closingDay: String,
    val paymentDay: String,
    val selectedCurrency: CurrencyOption?,
    val limitAmount: String,
    val openingAmount: String,
    val note: String,
    val sortOrder: String,
    val isIncludedInTotals: Boolean,
    val validationErrors: List<ValidationError>,
    val isFormValid: Boolean,
    val saveState: SaveState
)
```

### CreateAccountScreenState (Sealed Class)

```kotlin
sealed class CreateAccountScreenState {
    object Loading
    data class Content(val data: CreateAccountContentData)
    data class Error(val message: String, val exception: Throwable?, val canRetry: Boolean)
}
```

### SaveState (Sealed Class)

```kotlin
sealed class SaveState {
    object Idle
    object Saving
    data class Success(val accountId: Long)
    data class Error(val message: String, val exception: Throwable?)
}
```

### AccountTypeOption

```kotlin
data class AccountTypeOption(
    val name: String,              // Internal identifier (e.g., "CREDIT_CARD")
    val displayName: String,       // User-facing name (e.g., "Credit Card")
    val iconResId: Int,           // Icon resource ID
    val isCard: Boolean,          // Requires card issuer selection
    val hasIssuer: Boolean,       // Shows issuer name field
    val isElectronic: Boolean,    // Requires payment type selection
    val hasNumber: Boolean,       // Shows number/account number field
    val isCreditCard: Boolean     // Shows credit card specific fields
)
```

### ValidationError (Enum)

```kotlin
enum class ValidationError {
    TitleRequired,
    CurrencyRequired,
    InvalidClosingDay,
    InvalidPaymentDay,
    InvalidSortOrder,
    InvalidLimitAmount,
    InvalidOpeningAmount
}
```

## User Actions

The ViewModel exposes a sealed class hierarchy for all user actions:

```kotlin
sealed class CreateAccountAction {
    object LoadInitialData
    data class SetTitle(val title: String)
    data class SetAccountType(val accountType: AccountTypeOption)
    data class SetCardIssuer(val cardIssuer: CardIssuerOption)
    data class SetElectronicPaymentType(val paymentType: ElectronicPaymentTypeOption)
    data class SetIssuerName(val issuerName: String)
    data class SetCardNumber(val cardNumber: String)
    data class SetClosingDay(val closingDay: String)
    data class SetPaymentDay(val paymentDay: String)
    data class SetCurrency(val currency: CurrencyOption)
    data class SetCurrencyById(val currencyId: Long)    // For auto-selection
    data class SetLimitAmount(val amount: String)
    data class SetOpeningAmount(val amount: String)
    data class SetNote(val note: String)
    data class SetSortOrder(val sortOrder: String)
    data class SetIncludedInTotals(val included: Boolean)
    object SaveAccount
    object DismissSaveError
    object NavigateToAddCurrency
    object RefreshCurrencies
}
```

## Use Cases

The ViewModel depends on the following use cases:

1. **CreateAccountUseCase** - Creates new account in database
2. **UpdateAccountUseCase** - Updates existing account
3. **GetAccountByIdUseCase** - Retrieves account for editing
4. **GetCurrenciesUseCase** - Loads all available currencies

## UI Components

### CreateAccountScreen

Main screen composable that:
- Observes ViewModel state via `collectAsState()`
- Handles navigation (back, to currency selection)
- Manages screen states (Loading, Content, Error)
- Triggers navigation on successful save

### Form Field Components

Reusable input fields with validation:
- `TitleField` - Required text input
- `IssuerNameField` - Optional text input
- `CardNumberField` - Numeric input for card numbers
- `ClosingDayField` - Day of month (1-31)
- `PaymentDayField` - Day of month (1-31)
- `LimitAmountField` - Decimal amount input
- `OpeningAmountField` - Decimal amount input
- `NoteField` - Multi-line text input
- `SortOrderField` - Numeric input for custom ordering
- `IncludeInTotalsCheckbox` - Boolean toggle

### Selection Components

Specialized selectors:
- `AccountTypeSelector` - Choose account type with icons
- `CardIssuerSelector` - Choose card brand (Visa, MasterCard, etc.)
- `ElectronicPaymentTypeSelector` - Choose payment provider
- `CurrencySelector` - Choose currency with "Add Currency" option

### Action Components

Bottom action bar:
- `ActionButtons` - Save/Update and Cancel buttons
- Disabled state when form invalid
- Loading state during save operation
- `SaveErrorDialog` - Displays save errors

## Navigation Integration

The feature integrates with the app's navigation graph in `AccountListComposeActivity`:

```kotlin
NavHost(navController, startDestination = "account_list") {
    // Create Account
    composable("create_account") {
        CreateAccountScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToAddCurrency = { navController.navigate("currency_selection") },
            onAccountCreated = { accountId -> 
                navController.popBackStack()
            }
        )
    }
    
    // Edit Account
    composable("edit_account/{accountId}") { backStackEntry ->
        val accountId = backStackEntry.arguments?.getString("accountId")?.toLongOrNull()
        CreateAccountScreen(
            // Same screen, different mode based on accountId
            onNavigateBack = { navController.popBackStack() },
            onNavigateToAddCurrency = { navController.navigate("currency_selection") },
            onAccountCreated = { navController.popBackStack() }
        )
    }
    
    // Currency Selection
    composable("currency_selection") {
        CurrencySelectionScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToCustomCurrency = { navController.navigate("create_custom_currency") },
            onCurrencySelected = { currencyId ->
                // Signal refresh and auto-select
                navController.previousBackStackEntry?.savedStateHandle?.set("refresh_currencies", true)
                navController.previousBackStackEntry?.savedStateHandle?.set("selected_currency_id", currencyId)
                navController.popBackStack()
            }
        )
    }
}
```

## Business Logic

### Initial Data Loading

1. ViewModel checks if `accountId` is present (edit mode)
2. Calls `GetCurrenciesUseCase.execute()` to load currencies
3. Transforms currency entities to `CurrencyOption` list
4. Loads account type options (hardcoded list)
5. Loads card issuer options (hardcoded list)
6. Loads electronic payment options (hardcoded list)
7. Updates UI state to Content
8. If edit mode: loads account data and pre-populates form

### Form Pre-population (Edit Mode)

1. Calls `GetAccountByIdUseCase.execute(accountId)`
2. Maps `AccountEntity` fields to UI state:
   - Finds matching account type in available options
   - Finds matching currency in available currencies
   - Finds matching card issuer if applicable
   - Converts long amounts to decimal strings
   - Handles null/empty fields appropriately
3. Updates UI state with pre-populated values
4. Runs validation to determine initial form validity

### Dynamic Field Visibility

Based on selected `AccountTypeOption` flags:

```kotlin
// Card Issuer shown if:
accountType.isCard == true

// Electronic Payment Type shown if:
accountType.isElectronic == true

// Issuer Name shown if:
accountType.hasIssuer == true

// Card/Account Number shown if:
accountType.hasNumber == true

// Credit card fields shown if:
accountType.isCreditCard == true
// (closingDay, paymentDay, limitAmount)
```

When account type changes, conditional fields are reset to prevent invalid data.

### Form Validation

Validation runs after every field change:

1. **Title** - Must not be blank
2. **Currency** - Must be selected
3. **Closing Day** - If provided, must be 1-31
4. **Payment Day** - If provided, must be 1-31
5. **Sort Order** - If provided, must be numeric
6. All validation errors collected in list
7. `isFormValid = validationErrors.isEmpty()`
8. Save button enabled only when `isFormValid == true`

### Save Process

1. User clicks Save/Update button
2. ViewModel checks `isFormValid` (safety check)
3. Sets `saveState = SaveState.Saving`
4. Converts UI state to `AccountEntity`:
   - Trims string fields
   - Parses amounts (decimal → cents via `× 100`)
   - Sets system fields (timestamps, defaults)
   - Handles null vs. empty distinction
5. Calls appropriate use case:
   - **Create mode**: `CreateAccountUseCase.execute(entity)`
   - **Edit mode**: `UpdateAccountUseCase.execute(entity.copy(id = accountId))`
6. Handles result:
   - **Success**: Sets `saveState = SaveState.Success(accountId)`
   - **Failure**: Sets `saveState = SaveState.Error(message)`
7. UI observes `saveState` and triggers navigation on success

### Amount Parsing

User input (decimal string) → Database storage (long, cents):

```kotlin
"100" → 10000     // Whole number
"99.99" → 9999    // Two decimals
"" → 0            // Empty string
"invalid" → 0     // Parse error
```

Edit mode reversal (long → decimal string):

```kotlin
10000 → "100"     // Whole number, no decimals shown
9999 → "99.99"    // Decimals shown when present
0 → ""            // Zero shown as empty
```

### Currency Integration Flow

1. User clicks "Add Currency" in currency selector
2. Navigates to `CurrencySelectionScreen`
3. User selects common currency or creates custom
4. Currency saved to database, returns currency ID
5. Navigation back with currency ID in saved state
6. CreateAccountScreen observes saved state
7. Calls `RefreshCurrencies` action
8. After refresh, calls `SetCurrencyById` to auto-select
9. User sees newly created currency selected

## Known Issues and TODOs

### Critical

1. **No Tests** - Feature has no unit or UI tests
2. **Account Type Enums** - Using hardcoded strings instead of proper enums
3. **Icon Resources** - Placeholder icons instead of actual assets

### Important

4. **Limited Validation** - No card number format validation or duplicate detection
5. **Amount Formatting** - No localized formatting or thousands separators
6. **Incomplete Option Lists** - Card issuers and payment types are limited
7. **No Field Hints** - Input fields lack placeholder text

### Nice to Have

8. **Field Error Messages** - Only visual indicators, no detailed messages
9. **Unsaved Changes Warning** - No prompt when navigating away with changes
10. **Change Tracking** - Edit mode doesn't track what changed
11. **Focus Management** - No automatic focus navigation between fields
12. **Input Masks** - Card numbers and amounts lack formatting during input

## Testing Strategy

When tests are added, they should cover:

### ViewModel Tests
- Initial data loading (create and edit modes)
- Form validation rules
- Dynamic field visibility logic
- Account type change resetting conditional fields
- Save process (create and update)
- Amount parsing and formatting
- Currency auto-selection after creation
- Error handling
- State preservation

### UI Tests
- Form field rendering
- Conditional field visibility
- Validation error display
- Save button enable/disable
- Navigation flows
- Currency selection integration
- Error dialogs
- Loading states

### Use Case Tests
- Account creation
- Account updates
- Currency retrieval
- Account retrieval for editing

## Migration Notes

This feature replaces the legacy `AccountActivity` which used:
- XML layouts with ScrollView and LinearLayout
- Manual field visibility management
- AsyncTask for data loading
- Event bus for currency selection callback
- Manual state restoration

The modern implementation uses:
- Jetpack Compose for declarative UI
- Kotlin Coroutines and Flow for async operations
- Reactive state management with StateFlow
- Navigation Component for deep linking
- SavedStateHandle for state preservation
- Automatic field visibility via conditional rendering

## Integration with Related Features

### Currency Management
- Seamless integration with currency selection
- Auto-refresh when returning from currency creation
- Auto-select newly created currency

### Account List
- Navigates back to account list on save
- Account list auto-refreshes via navigation callback
- Edit navigation passes account ID

### Future Features
- Balance update will use opening amount as starting point
- Account type determines available transaction categories
- Card closing/payment days used for statement calculations

## Best Practices Demonstrated

1. **Input Action Pattern** - Single `handleAction()` entry point
2. **Form Validation** - Real-time validation with clear error states
3. **Conditional UI** - Dynamic fields based on account type
4. **Amount Handling** - Consistent decimal ↔ cents conversion
5. **Edit Mode** - Single screen for create and edit
6. **State Preservation** - SavedStateHandle for configuration changes
7. **Error Recovery** - Retry capability for recoverable errors
8. **Navigation Integration** - Proper use of Navigation Component

## Future Enhancements

1. **Field Presets**
   - Common account templates (personal checking, credit card, etc.)
   - Quick setup with pre-filled values

2. **Import Account**
   - Scan card for number extraction
   - Import from banking APIs (future)

3. **Validation Enhancements**
   - Card number Luhn algorithm validation
   - IBAN validation for bank accounts
   - Real-time duplicate detection

4. **UX Improvements**
   - Step-by-step wizard for complex account types
   - Field auto-focus and tab navigation
   - Input masks for formatted fields
   - Rich tooltips explaining field purposes

5. **Advanced Features**
   - Multiple opening balances for reconciliation
   - Account linking (debit card to bank account)
   - Scheduled balance updates
   - Account sharing/collaboration

## Related Features

- **[Account List](./Feature_Account_List.md)** - Main account management screen
- **[Create Transaction](./Feature_Create_Transaction.md)** - Create transactions for accounts
- **Currency Selection** ([feature/account/ui/CurrencySelectionScreen.kt](../feature/account/src/main/kotlin/ru/orangesoftware/financisto/feature/account/ui/CurrencySelectionScreen.kt))
- **Create Custom Currency** ([feature/account/ui/CreateCustomCurrencyScreen.kt](../feature/account/src/main/kotlin/ru/orangesoftware/financisto/feature/account/ui/CreateCustomCurrencyScreen.kt))

## References

- Legacy Implementation: `legacy-app/src/main/java/.../AccountActivity.java`
- Architecture Guidelines: [CODING_PRINCIPLES.md](./CODING_PRINCIPLES.md)
- Project Instructions: [.github/copilot-instructions.md](../.github/copilot-instructions.md)
