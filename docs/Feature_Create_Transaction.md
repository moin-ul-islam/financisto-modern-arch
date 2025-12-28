# Feature: Create Transaction

**Status:** Work in Progress (Functional with Limitations)  
**Last Updated:** December 25, 2025  
**Module:** `:feature:transaction`

## Overview

The Create Transaction feature allows users to create new financial transactions or edit existing ones in the Financisto modern app. It supports regular transactions, transfers between accounts, and complex split transactions with multiple categories.

This is a modernized reimplementation of the legacy `TransactionActivity` using Jetpack Compose, MVVM architecture, and Clean Architecture principles. The feature supports create mode, edit mode, and template mode.

## Architecture

The feature follows the standard Clean Architecture pattern with these layers:

```
TransactionFormScreen (Compose UI)
    ↓
TransactionFormViewModel 
    ↓
Use Cases (CreateTransactionUseCase, GetAccountsUseCase, GetCategoryTreeUseCase, etc.)
    ↓
Repositories (TransactionRepository, AccountRepository, CategoryRepository, etc.)
    ↓
DAOs (Room)
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
   - Create new transaction mode
   - ⚠️ Edit mode not wired: transaction ID is read but the ViewModel never loads or pre-populates an existing transaction
   - ⚠️ Template flag exists in state but no template-specific behavior
   - Mode is determined by navigation arguments but only create flow currently works

2. **Account Selection**
   - Select source account from all available accounts
   - Display account balance with currency symbol
   - Currency caching to avoid repeated DB queries
   - Formatted balance display

3. **Amount Input**
   - Large, prominent amount input field
   - Beautiful gradient header card
   - Currency symbol display
   - Income/Expense toggle button
   - Real-time formatting
   - Decimal amount support

4. **Transfer Functionality**
   - Toggle between transaction and transfer modes
   - Select destination account and prevent same-account transfers
   - Exchange rate input exists but multi-currency detection flag (`isDifferentCurrency`) is never set, so multi-currency UI paths do not activate

5. **Category Management**
   - Hierarchical category selection
   - Income and expense categories
   - Special "Split Transaction" category
   - "Add New Category" option
   - Auto-refresh after category creation
   - Auto-select newly created category
   - Icon support (placeholders currently)

6. **Split Transactions**
   - Select "Split Transaction" as category
   - Add multiple split items with different categories
   - Separate edit screen for each split
   - Display remaining amount to be allocated
   - Validation ensures splits match total amount
   - Edit and delete individual splits
   - Visual indication of split completeness

7. **Payee Selection**
   - Select from existing payees
   - "Add New Payee" option
   - Auto-refresh after payee creation
   - Auto-select newly created payee
   - Optional field (can be left empty)

8. **Project Selection**
   - Select from active projects
   - "Add New Project" option
   - Auto-refresh after project creation
   - Auto-select newly created project
   - Optional field (can be left empty)

9. **Transaction Details**
   - Status selection (Cleared, Reconciled, Pending, Unreconciled)
   - Date/time picker (TODO: not yet functional)
   - Note field (multi-line text input)
   - All fields properly validated

10. **Form Validation**
    - Required field validation (account, amount, category)
    - Amount format validation
    - Positive amount validation
    - Transfer-specific validation (destination account required)
    - Split transaction validation (amounts must match total)
    - Same-account transfer prevention
    - Real-time validation feedback
    - Save button disabled when invalid

11. **UI/UX Features**
    - Modern Material 3 design
    - Scrollable form for smaller screens
    - Gradient header with amount input
    - Card-based layout with overlap effect
    - Loading indicators during save
    - Error handling with retry capability
    - Navigation callbacks for entity creation
    - Saved state preservation

### 🚧 Partially Implemented Features

1. **Date/Time Picker**
   - Field exists and displays current date/time
   - ⚠️ **Missing:** Actual picker dialog implementation
   - Currently uses current timestamp

2. **Status Picker**
   - Field exists with current status display
   - ⚠️ **Missing:** Picker dialog to change status
   - Currently defaults to "Cleared" (CL)

3. **Account Type Icons**
   - Icon system exists in data models
   - ⚠️ **Missing:** Actual icon resource mapping (using placeholder)

4. **Category Icons**
   - Icon system exists in data models
   - ⚠️ **Missing:** Actual icon resource mapping (using placeholder)

5. **Edit Mode**
   - ⚠️ **Missing:** `GetTransactionByIdUseCase` is injected but never used; no pre-population or update logic

6. **Template Support**
   - Template flag exists in UI state
   - ⚠️ **Missing:** Template-specific UI/behavior and save-as-template handling

### ❌ Not Yet Implemented Features

1. **Testing**
   - No test files found for transaction form feature
   - ⚠️ **Missing:** ViewModel tests
   - ⚠️ **Missing:** UI tests
   - ⚠️ **Missing:** Form validation tests
   - ⚠️ **Missing:** Split transaction tests
   - ⚠️ **Missing:** Use case tests

2. **Location Support**
   - Legacy feature supported location tagging
   - ⚠️ **Missing:** Location field
   - ⚠️ **Missing:** GPS integration
   - ⚠️ **Missing:** Location selection

3. **Attachments**
   - ⚠️ **Missing:** Photo/receipt attachment
   - ⚠️ **Missing:** File attachment
   - ⚠️ **Missing:** Attachment preview

4. **Recurrence**
   - ⚠️ **Missing:** Recurring transaction setup
   - ⚠️ **Missing:** Recurrence pattern configuration
   - ⚠️ **Missing:** Recurring transaction management

5. **Advanced Features**
   - ⚠️ **Missing:** Original currency tracking (started but incomplete)
   - ⚠️ **Missing:** Transaction duplication
   - ⚠️ **Missing:** Quick entry mode
   - ⚠️ **Missing:** Calculator integration

6. **Balance Update Integration**
   - Transaction creation updates account balance
   - Split transaction save uses `InsertSplitTransactionUseCase` with incremental balance updates
   - Incremental balance updates mirror legacy approach for efficiency
   - Parent transaction updates fromAccount balance
   - Transfer children update toAccount balance
   - Running balance updated incrementally (insert entry + update subsequent entries)
   - All operations wrapped in atomic transaction (all-or-nothing)
   - Balance calculation tested and verified correct

## File Structure

```
feature/transaction/
├── TransactionFormUiState.kt          # UI state models and sealed classes
├── TransactionFormViewModel.kt        # ViewModel with business logic
├── ui/
│   ├── TransactionFormScreen.kt       # Main form screen composable
│   ├── SplitEditScreen.kt             # Split item edit screen
│   └── components/
│       ├── AmountHeaderCard.kt        # Gradient header with amount
│       ├── AmountFields.kt            # Amount input components
│       ├── SelectionFields.kt         # Account, category selectors
│       ├── TransactionFormFields.kt   # Form input fields
│       └── SplitAndValidation.kt      # Split list and validation
```

## Data Models

### TransactionFormUiState

```kotlin
data class TransactionFormUiState(
    val screenState: TransactionFormScreenState,
    val isLoadingData: Boolean,
    val isSaving: Boolean,
    val saveState: SaveState,
    
    // Transaction mode
    val transactionId: Long,
    val isEditMode: Boolean,
    val isTemplate: Boolean,
    
    // Main transaction fields
    val selectedAccount: AccountOption?,
    val availableAccounts: List<AccountOption>,
    val amount: String,
    val formattedAmount: String,
    val selectedCategory: CategoryOption?,
    val availableCategories: List<CategoryOption>,
    val selectedPayee: PayeeOption?,
    val availablePayees: List<PayeeOption>,
    val selectedProject: ProjectOption?,
    val availableProjects: List<ProjectOption>,
    val note: String,
    val dateTime: Long,
    val formattedDateTime: String,
    val status: String,              // CL, RC, PN, UR
    val isIncome: Boolean,
    
    // Transfer fields
    val isTransfer: Boolean,
    val selectedToAccount: AccountOption?,
    val exchangeRate: String,
    val toAmount: String,
    val isDifferentCurrency: Boolean,
    
    // Split transaction fields
    val isSplitTransaction: Boolean,
    val splitTransactions: List<SplitTransactionItem>,
    val remainingAmount: String,
    val editingSplit: SplitTransactionItem?,
    
    // Validation
    val validationErrors: List<ValidationError>,
    val isFormValid: Boolean
)
```

### TransactionFormScreenState (Sealed Class)

```kotlin
sealed class TransactionFormScreenState {
    object Loading
    object Empty
    data class Content(val data: TransactionFormContentData)
    data class Error(val message: String, val exception: Throwable?, val canRetry: Boolean)
}
```

### SaveState (Sealed Class)

```kotlin
sealed class SaveState {
    object Idle
    object Saving
    data class Success(val transactionId: Long, val message: String)
    data class Failed(val error: String, val canRetry: Boolean)
}
```

### SplitTransactionItem

```kotlin
@Parcelize
data class SplitTransactionItem(
    val id: Long,
    val categoryId: Long,
    val categoryName: String?,
    val accountId: Long,
    val accountName: String?,
    val amount: Long,               // In cents
    val formattedAmount: String,
    val note: String?,
    val projectId: Long?,
    val projectName: String?,
    val type: Int                   // -1 = expense, 1 = income
) : Parcelable
```

### ValidationError (Sealed Class)

```kotlin
sealed class ValidationError(val message: String) {
    object AmountRequired
    object AccountRequired
    object CategoryRequired
    object InvalidAmount
    object NegativeAmount
    object ToAccountRequired
    object SameAccountTransfer
    object SplitAmountMismatch
}
```

## User Actions

The ViewModel exposes actions for all user interactions:

```kotlin
sealed class TransactionFormAction {
    object ShowStatusPicker
    object ShowDateTimePicker
    data class SelectAccount(val account: AccountOption)
    data class SelectToAccount(val account: AccountOption)
    data class ToggleTransfer(val isTransfer: Boolean)
    data class UpdateAmount(val amount: String)
    object ToggleIncomeExpense
    data class UpdateExchangeRate(val rate: String)
    data class SelectCategory(val category: CategoryOption)
    data class SelectPayee(val payee: PayeeOption)
    data class SelectProject(val project: ProjectOption)
    object AddNewCategory
    object AddNewPayee
    object AddNewProject
    data class UpdateNote(val note: String)
    object AddSplit
    data class EditSplit(val split: SplitTransactionItem)
    data class DeleteSplit(val split: SplitTransactionItem)
}
```

## Use Cases

The ViewModel depends on the following use cases:

1. **GetTransactionByIdUseCase** - Retrieves transaction for editing
2. **CreateTransactionWithBalanceUpdateUseCase** - Creates transaction and updates account balance
3. **UpdateTransactionUseCase** - Updates existing transaction
4. **InsertOrUpdateTransactionUseCase** - Upserts transaction (for splits)
5. **GetAccountsUseCase** - Loads all accounts
6. **GetCategoryTreeUseCase** - Loads hierarchical category structure
7. **GetPayeesUseCase** - Loads all payees
8. **GetProjectsUseCase** - Loads all projects
9. **GetCurrencyByIdUseCase** - Retrieves currency information

## UI Components

### TransactionFormScreen

Main screen composable that:
- Observes ViewModel state via `collectAsState()`
- Handles navigation callbacks (to create category, payee, project, edit split)
- Manages save result and navigation on success
- Handles refresh after entity creation
- Sets navigation callbacks on ViewModel

### AmountHeaderCard

Beautiful gradient header component with:
- Large amount input field
- Currency symbol display
- Income/Expense toggle button
- Gradient background (green for income, red for expense)
- Prominent placement at top of screen

### Selection Field Components

Specialized selectors:
- `AccountSelectionField` - Choose source/destination account with balance
- `CategorySelectionField` - Choose category with "Add New" option
- `PayeeSelectionField` - Choose payee with "Add New" option
- `ProjectSelectionField` - Choose project with "Add New" option

### Form Field Components

Input components:
- `StatusDateTimeRow` - Status and date/time display
- `TransferToggle` - Toggle between transaction and transfer
- `ExchangeRateField` - Exchange rate for multi-currency transfers
- Note input field (standard TextField)

### Split Transaction Components

Split-specific UI:
- `SplitTransactionSection` - List of splits with add/edit/delete
- Split item cards showing category, amount, note
- Remaining amount indicator
- Visual validation of split completion

### SplitEditScreen

Separate screen for editing split items:
- Category selection
- Amount input
- Note input
- Project selection
- Income/expense toggle
- Save/Cancel buttons

## Navigation Integration

The feature integrates with the app's navigation graph:

```kotlin
NavHost(navController, startDestination = "account_list") {
    // Create Transaction
    composable("transaction_form?accountId={accountId}") { backStackEntry ->
        val accountId = backStackEntry.arguments?.getString("accountId")?.toLongOrNull()
        TransactionFormScreen(
            onNavigateBack = { navController.popBackStack() },
            onTransactionSaved = { transactionId -> 
                navController.popBackStack()
            },
            onNavigateToCreateCategory = { navController.navigate("create_category") },
            onNavigateToCreatePayee = { navController.navigate("create_payee") },
            onNavigateToCreateProject = { navController.navigate("create_project") },
            onNavigateToEditSplit = { split ->
                navController.currentBackStackEntry?.savedStateHandle?.set("split_item", split)
                navController.navigate("split_edit")
            },
            navBackStackEntry = backStackEntry
        )
    }
    
    // Edit Split
    composable("split_edit") { backStackEntry ->
        val splitItem = navController.previousBackStackEntry?.savedStateHandle
            ?.get<SplitTransactionItem>("split_item")
        
        SplitEditScreen(
            splitItem = splitItem ?: SplitTransactionItem(id = -1),
            onNavigateBack = { navController.popBackStack() },
            onSplitSaved = { split ->
                navController.previousBackStackEntry?.savedStateHandle?.set("saved_split", split)
                navController.popBackStack()
            }
        )
    }
}
```

## Business Logic

### Initial Data Loading

1. ViewModel checks navigation arguments:
   - `transactionId` - If present, load existing transaction (edit mode)
   - `accountId` - If present, pre-select account
   - `isTemplate` - If true, enter template mode
2. Calls multiple use cases in parallel:
   - `GetAccountsUseCase` - Load all accounts
   - `GetCategoryTreeUseCase` - Load category hierarchy
   - `GetPayeesUseCase` - Load all payees
   - `GetProjectsUseCase` - Load all projects
3. Transforms entities to option models
4. Adds special "Split Transaction" category (ID = -1)
5. Updates UI state to Content
6. Pre-selects account if ID provided

### Transfer Mode Handling

When user toggles transfer mode:

1. Sets `isTransfer = true/false`
2. Shows/hides destination account field
3. Clears destination account when disabling transfer
4. On destination account selection:
   - Compares currency IDs
   - Sets `isDifferentCurrency` flag
   - Shows exchange rate field if currencies differ
5. Validates that source ≠ destination account

### Multi-Currency Transfer Calculation

When exchange rate or amount changes:

```kotlin
// Example: 100 USD at rate 1.2 → 120 EUR
fromAmount = amount * 100        // Convert to cents
toAmount = fromAmount * exchangeRate
formattedToAmount = toAmount / 100  // Display with 2 decimals
```

### Split Transaction Flow

1. **User selects "Split Transaction" category**:
   - Sets `isSplitTransaction = true`
   - Shows split transaction section
   - Hides single category field

2. **Add Split**:
   - Navigates to `SplitEditScreen`
   - Passes empty split item (ID = -1)

3. **Edit Split**:
   - Navigates to `SplitEditScreen`
   - Passes existing split item

4. **Save Split** (from SplitEditScreen):
   - Returns split via SavedStateHandle
   - Main screen observes saved split
   - Calls `viewModel.saveSplit(split)`
   - Updates split list
   - Recalculates remaining amount
   - Validates total

5. **Delete Split**:
   - Removes from list
   - Recalculates remaining amount
   - Revalidates

### Split Validation

```kotlin
totalAmount = transaction.amount
splitTotal = sum(split.amount for all splits)
remaining = totalAmount - splitTotal

if abs(remaining) > 0.01:  // Allow floating point tolerance
    error: "Split amounts don't match transaction amount"
```

### Entity Creation Integration

When user creates new category/payee/project:

1. Navigates to creation screen
2. After save, navigation returns with entity ID
3. Sets refresh flag in SavedStateHandle
4. Main screen observes flag
5. Calls `viewModel.refreshDataAfterCreation(type, id)`
6. Refreshes appropriate list (categories/payees/projects)
7. Auto-selects newly created entity

### Save Process

**Regular Transaction:**
1. Validates form
2. Converts UI state to `TransactionEntity`
3. Converts amount: `decimal → cents`
4. Sets income/expense sign
5. Calls `CreateTransactionWithBalanceUpdateUseCase`
6. Updates account balance automatically
7. Returns transaction ID
8. Navigates back on success

**Transfer:**
1. Same as regular but:
2. Sets `toAccountId`
3. Calculates `toAmount` with exchange rate
4. Updates both account balances

**Split Transaction:**
1. Creates parent transaction with `categoryId = -1`
2. Creates child transactions for each split
3. Sets `parentId` on child transactions
4. Saves parent first, then children
5. Needs proper rollback on failure (TODO)

### Form Validation

Validates in real-time on every field change:

1. **Amount**: Required, must be valid number, must be positive
2. **Account**: Required
3. **Category**: Required (unless split or transfer)
4. **To Account**: Required if transfer
5. **Same Account**: Cannot transfer to same account
6. **Split Total**: Must match transaction amount (±0.01 tolerance)

Error list updated in state, save button disabled if any errors exist.

## Known Issues and TODOs

### Critical

1. **No Tests** - Feature has no unit or UI tests
2. **Edit Mode Incomplete** - Loading transaction but not populating form
3. **Date/Time Picker** - Field exists but picker not implemented
4. **Status Picker** - Field exists but picker not implemented

### Important

5. **Balance Update Verification** - No confirmation that balances updated correctly
6. **Split Transaction Rollback** - If split save fails, parent transaction not rolled back
7. **Exchange Rate Calculation** - May have rounding errors
8. **Category/Account Icons** - Using placeholders instead of actual icons
9. **Original Currency** - Started but not fully implemented

### Nice to Have

10. **Location Support** - Legacy feature not yet ported
11. **Attachments** - No photo/file attachment support
12. **Recurrence** - No recurring transaction support
13. **Templates** - Template support incomplete
14. **Calculator** - No integrated calculator for amount input
15. **Quick Entry** - No quick entry mode
16. **Transaction Duplication** - Cannot duplicate existing transactions

## Testing Strategy

When tests are added, they should cover:

### ViewModel Tests
- Initial data loading
- Account selection and pre-selection
- Amount validation and formatting
- Transfer mode toggle
- Multi-currency detection
- Exchange rate calculation
- Split transaction management
- Category/Payee/Project creation integration
- Form validation rules
- Save process (regular, transfer, split)
- Error handling

### UI Tests
- Amount input and formatting
- Income/expense toggle
- Account selection
- Category selection with "Add New"
- Transfer mode UI changes
- Exchange rate field visibility
- Split transaction UI
- Split add/edit/delete
- Validation error display
- Save button enable/disable
- Navigation flows

### Use Case Tests
- Transaction creation
- Transaction updates
- Balance updates
- Account retrieval
- Category tree retrieval
- Payee/Project retrieval

## Migration Notes

This feature replaces the legacy `TransactionActivity` which used:
- XML layouts with ScrollView
- Manual field visibility management
- AsyncTask for data loading
- SQLite queries directly in activity
- Intent-based navigation
- Manual state restoration
- Event bus for communication

The modern implementation uses:
- Jetpack Compose for declarative UI
- Kotlin Coroutines and Flow for async operations
- Clean Architecture with use cases
- Navigation Component with SavedStateHandle
- Reactive state management with StateFlow
- Automatic state preservation
- Proper separation of concerns

## Integration with Related Features

### Account List
- Navigates to transaction form with pre-selected account
- Account list refreshes after transaction save
- [Account List Documentation](./Feature_Account_List.md)

### Account Creation
- Account must exist before creating transaction
- New accounts immediately available for selection
- [Create Account Documentation](./Feature_Create_Account.md)

### Category Management
- Hierarchical categories loaded from repository
- "Add New Category" creates category and auto-selects
- Category type determines income/expense

### Split Transactions
- Separate screen for editing individual splits
- Maintains parent-child relationship
- Validates total matches

## Best Practices Demonstrated

1. **Input Action Pattern** - Single `handleAction()` entry point
2. **Form Validation** - Real-time validation with clear error states
3. **Split Screen Pattern** - Separate screen for complex sub-forms
4. **Currency Caching** - Avoid repeated DB queries
5. **Navigation Callbacks** - Clean separation between ViewModel and navigation
6. **Entity Creation Integration** - Seamless flow for creating related entities
7. **Gradient Header Design** - Modern, visually appealing amount input
8. **Validation Feedback** - Immediate user feedback on errors

## Future Enhancements

1. **Smart Suggestions**
   - Category suggestions based on payee
   - Amount suggestions based on history
   - Payee suggestions based on category
   - Auto-complete for note based on similar transactions

2. **Quick Entry Mode**
   - Minimal fields for fast entry
   - Common transactions quick access
   - Gesture-based amount input

3. **Enhanced Split Management**
   - Split by percentage
   - Equal split option
   - Copy split from previous transaction
   - Split templates

4. **Receipt Management**
   - Photo capture
   - OCR for amount extraction
   - Receipt gallery
   - Receipt search

5. **Recurring Transactions**
   - Pattern configuration (daily, weekly, monthly)
   - Auto-posting with notification
   - Skip/modify occurrences
   - End date management

6. **Location Integration**
   - GPS auto-tagging
   - Location history
   - Location-based suggestions
   - Map view of transactions

7. **Calculator Integration**
   - In-field calculator
   - Expression evaluation (10+5*2)
   - Currency conversion
   - Tip calculator

## Related Features

- **[Account List](./Feature_Account_List.md)** - Initiate transaction from account
- **[Create Account](./Feature_Create_Account.md)** - Account must exist first
- **Blotter/Transaction List** ([feature/blotter](../feature/blotter)) - View all transactions
- **Category Management** (Not yet documented)
- **Payee Management** (Not yet documented)
- **Project Management** (Not yet documented)

## Known Issues and TODOs

### Implementation Complete: Split Transaction Balance Updates ✅

**Status:** RESOLVED (December 25, 2025)

The split transaction balance update issue has been completely resolved by implementing an incremental balance update approach that mirrors the legacy application:

**Implementation Details:**
1. **New Use Cases Created:**
   - `UpdateAccountBalanceIncrementallyUseCase` - Updates account balance by delta amount (atomic UPDATE SQL)
   - `UpdateRunningBalanceIncrementallyUseCase` - Incremental running balance updates (insert entry + update subsequent)
   - `InsertSplitTransactionUseCase` - Handles split transaction insertion with proper balance logic

2. **Balance Update Logic:**
   - Parent transaction updates `fromAccount` balance (decrements by fromAmount)
   - Transfer children update `toAccount` balance (increments by toAmount)
   - Non-transfer children skip balance updates (already counted in parent)
   - All operations are atomic (success/failure as a unit)

3. **Infrastructure Added:**
   - `AccountDao.incrementAccountBalance()` - Atomic SQL UPDATE for delta-based balance changes
   - `AccountRepository.incrementAccountBalance()` - Repository wrapper
   - `TransactionEntity.isSplitChild` extension property

4. **ViewModel Integration:**
   - `TransactionFormViewModel.saveSplitTransaction()` now uses `InsertSplitTransactionUseCase`
   - Simplified from 30+ lines to 5 lines
   - All business logic moved to use case layer (proper separation of concerns)

**Testing:**
- All existing unit tests pass (10 transaction book-keeping tests)
- Code compiles successfully
- Balance calculation verified correct

**Documentation:**
- Implementation documented in this file
- See `InsertSplitTransactionUseCase` for detailed code documentation
- See `UpdateAccountBalanceIncrementallyUseCase` for incremental balance logic
- See `UpdateRunningBalanceIncrementallyUseCase` for running balance logic

### Test Coverage

✅ **Completed:**
- UseCase layer tests for transaction book-keeping (10 tests, all passing)
- See `usecase/src/test/kotlin/.../TransactionBookkeepingTests.kt`
- See `docs/Transaction_Bookkeeping_Test_Results.md` for results

❌ **Missing:**
- ViewModel layer tests
- Repository layer tests (specific to transaction creation)
- UI/Integration tests

## References

- Legacy Implementation: `legacy-app/src/main/java/.../TransactionActivity.java`
- Architecture Guidelines: [CODING_PRINCIPLES.md](./CODING_PRINCIPLES.md)
- Project Instructions: [.github/copilot-instructions.md](../.github/copilot-instructions.md)
