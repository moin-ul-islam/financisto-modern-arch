# Feature: Account List

**Status:** Work in Progress (Partially Functional)  
**Last Updated:** December 25, 2025  
**Module:** `:feature:account`

## Overview

The Account List feature is the main screen for viewing and managing financial accounts in the Financisto modern app. It displays all user accounts (bank accounts, credit cards, cash, etc.) with their current balances, provides quick actions for each account, and allows navigation to related features.

This is a modernized reimplementation of the legacy `AccountListActivity` using Jetpack Compose, MVVM architecture, and Clean Architecture principles.

## Architecture

The feature follows the standard Clean Architecture pattern with these layers:

```
AccountListScreen (Compose UI)
    ↓
AccountListViewModel 
    ↓
Use Cases (GetAccountsUseCase, DeleteAccountUseCase, etc.)
    ↓
AccountRepository
    ↓
AccountDao (Room)
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

1. **Account List Display**
   - Loads accounts via use cases and shows them in a scrollable list
   - Modern card-based UI design with Material 3
   - Placeholder account type text/icons (display names are capitalized type strings)
   - Account type, title, and last transaction date display
   - Formatted balance display with currency symbols

2. **Screen State Management**
   - Loading state (spinner)
   - Empty state (no accounts message)
   - Content state (list of accounts)
   - Error state (with retry functionality)
   - Pull-to-refresh support

3. **Total Balance Card**
   - Card renders at the top of the list
   - ⚠️ Currently always shows `$0.00` because list items leave `balanceAmount` at zero even though the ViewModel computes totals separately
   - Total calculation state tracking (Idle, Calculating, Completed, Failed)

4. **Account Actions**
   - Long-press on account shows action bottom sheet
   - 9 actions available: Info, Blotter, Edit, Transaction, Transfer, Balance, Purge, Close/Reopen, Delete
   - Action availability based on account state (active vs. closed)
   - Modern Material 3 modal bottom sheet design

5. **Account Info Dialog**
   - Shows detailed account information
   - Displays: title, type, currency, balance, issuer, card number (masked), credit limit, status, last transaction date, notes
   - Edit button to navigate to edit screen
   - Enhanced visual design with proper styling

6. **Delete Account Confirmation**
   - Confirmation dialog before deletion
   - Shows account name in confirmation message
   - Actually deletes account from database
   - Refreshes list after deletion

7. **Toggle Account Status**
   - Close/Reopen active accounts
   - Updates database and refreshes list
   - Action label changes based on current state

8. **Sorting**
   - Four sort orders: Name, Balance, Type, Last Transaction Date
   - Sort order preserved in UI state

9. **UI State Management**
   - Single StateFlow for all UI state (`uiState`)
   - Sealed classes for screen states
   - Input action pattern for all user interactions
   - Proper separation of concerns

### 🚧 Partially Implemented Features

1. **Currency Handling**
   - Currency symbols are loaded and displayed
   - Currency formatting works via domain model
   - In-memory currency cache to avoid repeated DB calls
   - ⚠️ **Missing:** Multi-currency total warnings
   - ⚠️ **Missing:** Proper currency conversion for totals

2. **Account Type Icons**
   - Icon/color system exists in UI
   - ⚠️ **Missing:** Actual icon resource mapping; ViewModel returns placeholder icons and capitalized type strings

3. **Navigation**
   - Navigation callbacks are defined in `AccountListScreen`
   - ⚠️ **Missing:** ViewModel handlers are TODOs for edit/create/totals; navigation only works when callers wire callbacks externally
   - ⚠️ **Missing:** Some navigation destinations not yet implemented (Account Totals, Blotter filtering, Update Balance, Purge Account)

4. **Transaction Count**
   - Field exists in `AccountListItem`
   - ⚠️ **Missing:** Actual transaction count retrieval (hardcoded to 0)
   - ⚠️ **Missing:** Use case to get transaction count per account

### ❌ Not Yet Implemented Features

1. **Integrity Check**
   - Action exists but not implemented
   - TODO: Implement when use case is available
   - Should verify database integrity and show banner if issues found

2. **Account Totals Screen**
   - Navigation defined but destination not implemented
   - Should show detailed breakdown of totals by currency

3. **Account Balance Update**
   - Action exists but not implemented
   - Should allow manual balance adjustment with transaction creation

4. **Purge Account**
   - Action exists but not implemented
   - Should delete old transactions from account

5. **Credit Card Features**
   - UI components exist for credit progress bars
   - ⚠️ **Missing:** Credit utilization calculation
   - ⚠️ **Missing:** Available credit display
   - ⚠️ **Missing:** Closing day and payment day display

6. **Menu Actions**
   - Menu button exists in bottom toolbar
   - ⚠️ **Missing:** Menu implementation (settings, preferences, etc.)

7. **Pull-to-Refresh & Testing**
   - No swipe-to-refresh container; list reloads only via explicit actions/recomposition
   - No test files found for account list feature
   - ⚠️ **Missing:** ViewModel tests
   - ⚠️ **Missing:** UI tests
   - ⚠️ **Missing:** Use case tests

## File Structure

```
feature/account/
├── AccountListUiState.kt           # UI state models and sealed classes
├── AccountListViewModel.kt         # ViewModel with business logic
├── AccountActionCalloutItem.kt     # Action menu data models
├── AccountActionCalloutUtils.kt    # Action menu creation logic
├── ui/
│   ├── AccountListScreen.kt        # Main screen composable
│   └── components/
│       ├── AccountList.kt          # List with bottom sheet
│       ├── AccountListItem.kt      # Individual account card
│       ├── TotalBalanceCard.kt     # Total balance header
│       ├── BottomToolbar.kt        # Bottom action bar
│       ├── AccountActionCallout.kt # Action bottom sheet
│       ├── StateComponents.kt      # Loading, empty, error states
│       └── IntegrityErrorBanner.kt # Error overlay banner
```

## Data Models

### AccountListUiState

```kotlin
data class AccountListUiState(
    val screenState: AccountListScreenState,
    val isRefreshing: Boolean,
    val totalCalculationState: TotalCalculationState,
    val selectedSortOrder: AccountSortOrder,
    val showIntegrityError: Boolean,
    val showMenuButton: Boolean,
    val showAccountInfoDialog: Boolean,
    val accountInfoData: AccountInfoData?,
    val showDeleteConfirmDialog: Boolean,
    val accountToDelete: AccountListItem?
)
```

### AccountListScreenState (Sealed Class)

```kotlin
sealed class AccountListScreenState {
    object Loading
    object Empty
    data class Content(val data: AccountListContentData)
    data class Error(val message: String, val exception: Throwable?, val canRetry: Boolean)
}
```

### AccountListItem

```kotlin
data class AccountListItem(
    val id: Long,
    val title: String,
    val balance: String,
    val formattedBalance: String,
    val currencySymbol: String,
    val accountType: String,
    val iconResId: Int,
    val isActive: Boolean,
    val isIncludeIntoTotals: Boolean,
    val lastTransactionDate: Long,
    val formattedLastTransactionDate: String,
    val transactionCount: Int,  // Currently hardcoded to 0
    val note: String,
    // Additional UI-specific fields...
)
```

## User Actions

The ViewModel exposes a sealed class hierarchy for all user actions:

```kotlin
sealed class AccountListAction {
    object LoadAccounts
    object RefreshAccounts
    object RetryLoading
    data class EditAccount(val accountId: Long)
    data class DeleteAccount(val accountId: Long)
    data class ToggleAccountStatus(val accountId: Long)
    data class ViewAccountTransactions(val accountId: Long)
    data class SortBy(val sortOrder: AccountSortOrder)
    data class ShowAccountInfo(val accountId: Long)
    data class UpdateAccountBalance(val accountId: Long)     // Not implemented
    data class PurgeAccount(val accountId: Long)             // Not implemented
    object CreateNewAccount
    object ViewAccountTotals                                 // Not implemented
    object IntegrityCheck                                    // Not implemented
    object DismissIntegrityError
    object CalculateTotals
    object DismissAccountInfoDialog
    object DismissDeleteConfirmDialog
    data class ConfirmDeleteAccount(val accountId: Long)
}
```

## Use Cases

The ViewModel depends on the following use cases:

1. **GetAccountsUseCase** - Retrieves all accounts (one-time and Flow)
2. **GetAccountByIdUseCase** - Gets specific account details
3. **DeleteAccountUseCase** - Deletes an account
4. **UpdateAccountUseCase** - Updates account properties
5. **GetCurrencyByIdUseCase** - Retrieves currency information
6. **GetCurrenciesUseCase** - Gets all currencies (injected but not actively used)

### Missing Use Cases

- **GetTransactionCountByAccountUseCase** - Would provide transaction counts
- **GetAccountTotalsUseCase** - Would calculate multi-currency totals
- **IntegrityCheckUseCase** - Would verify database integrity
- **PurgeAccountUseCase** - Would delete old transactions

## UI Components

### AccountListScreen

Main screen composable that:
- Observes ViewModel state via `collectAsState()`
- Handles navigation callbacks
- Shows dialogs (Account Info, Delete Confirmation)
- Renders appropriate state (Loading, Empty, Content, Error)

### AccountList

Lazy column containing:
- `TotalBalanceCard` at the top
- List of `AccountListItem` cards
- Modal bottom sheet for account actions
- Manages bottom sheet state

### AccountListItem

Modern card design with:
- Colored circular icon background
- Account type/issuer label
- Account title (bold)
- Last transaction date
- Formatted balance (right-aligned)
- Long-press gesture for actions
- Disabled appearance for closed accounts

### BottomToolbar

Fixed bottom bar with:
- Total balance display (tappable)
- Add button (FAB-style)
- Menu button (not yet functional)

## Navigation Integration

The feature integrates with the app's navigation graph in `AccountListComposeActivity`:

```kotlin
NavHost(navController, startDestination = "account_list") {
    composable("account_list") {
        AccountListScreen(
            onNavigateToCreateAccount = { navController.navigate("create_account") },
            onNavigateToBlotter = { accountId -> /* Navigate to transactions */ },
            onNavigateToEditAccount = { accountId -> navController.navigate("edit_account/$accountId") },
            // ... other navigation callbacks
        )
    }
}
```

## Business Logic

### Account Loading

1. ViewModel calls `GetAccountsUseCase.execute()`
2. Returns list of `AccountEntity` from database
3. ViewModel loads all unique currencies (with caching)
4. Transforms entities to `AccountListItem` with:
   - Formatted balance using currency formatting
   - Currency symbols
   - Account type display names
   - Formatted dates
5. Sorts accounts by selected sort order
6. Updates UI state to Content with sorted list
7. Triggers total calculation

### Total Calculation

1. Filters accounts where `isIncludeIntoTotals = true`
2. Sums all balances
3. Formats total using currency formatting
4. Updates `totalCalculationState`
5. **Note:** Current implementation doesn't handle multi-currency properly (uses default currency)

### Account Deletion

1. User long-presses account → selects Delete
2. ViewModel finds account in current list
3. Sets `showDeleteConfirmDialog = true` with account data
4. User confirms in dialog
5. ViewModel calls `DeleteAccountUseCase.execute(accountId)`
6. On success: dismisses dialog, refreshes account list
7. On failure: shows error state

### Toggle Account Status

1. ViewModel calls `GetAccountByIdUseCase.execute(accountId)`
2. Creates updated account with `isActive = !account.isActive`
3. Calls `UpdateAccountUseCase.execute(updatedAccount)`
4. Refreshes account list
5. Action label in menu changes (Close ↔ Reopen)

## Known Issues and TODOs

### Critical

1. **No Tests** - Feature has no unit or UI tests
2. **Transaction Count** - Always shows 0, needs use case implementation
3. **Account Type Mapping** - Using placeholder icons and capitalized type strings

### Important

4. **Multi-Currency Totals** - Total calculation assumes single currency
5. **Credit Card Features** - Credit utilization not calculated
6. **Integrity Check** - Not implemented
7. **Account Totals Screen** - Navigation defined but screen doesn't exist

### Nice to Have

8. **Currency Warnings** - Should warn when totaling different currencies
9. **Account Type Icons** - Need proper icon resource mapping
10. **Menu Implementation** - Menu button exists but has no functionality
11. **Update Balance Feature** - Action exists but not implemented
12. **Purge Account Feature** - Action exists but not implemented

## Testing Strategy

When tests are added, they should cover:

### ViewModel Tests
- Account loading (success, error, empty)
- Screen state transitions
- Total calculation
- Account deletion flow
- Account status toggle
- Action handling
- Dialog state management
- Currency caching

1. Filters accounts where `isIncludeIntoTotals = true`
2. Sums balances as longs based on the `balance` string value
3. Formats total using currency formatting
4. Updates `totalCalculationState` and `content` totalBalance
5. **Note:** Multi-currency is not handled; the UI Total Balance card separately sums `balanceAmount` (never set) so it displays `$0.00`
- Delete confirmation dialog
- Empty state display
- Error state with retry
- Loading state

### Use Case Tests
- Account retrieval
- Account deletion
- Account updates
- Currency loading

## Migration Notes

This feature replaces the legacy `AccountListActivity` which used:
- Custom XML layouts with RecyclerView
- AsyncTask for data loading
- QuickActionGrid for account actions
- Manual state management
- Event bus for communication

The modern implementation uses:
- Jetpack Compose for UI
- Kotlin Coroutines and Flow for async operations
- Material 3 Modal Bottom Sheet for actions
- StateFlow for reactive state management
- Navigation Component for screen navigation

## Future Enhancements

1. **Search and Filter**
   - Add search bar to filter accounts by name
   - Filter by account type, status, currency

2. **Drag-to-Reorder**
   - Allow manual sorting by dragging accounts
   - Persist custom sort order

3. **Swipe Actions**
   - Quick swipe gestures for common actions
   - Customizable swipe actions

4. **Account Groups**
   - Group accounts by type or custom categories
   - Collapsible group headers
   - Group totals

5. **Account Analytics**
   - Show spending trends per account
   - Visual charts in account list

6. **Multi-Select Mode**
   - Select multiple accounts for batch operations
   - Bulk delete, status change, etc.

## Related Features

- **[Create Account](./Feature_Create_Account.md)** - Create and edit accounts
- **[Create Transaction](./Feature_Create_Transaction.md)** - Create and edit transactions
- **Account Totals** (Not yet implemented)
- **Blotter/Transaction List** ([feature/blotter](../feature/blotter))

## References

- Use case overview: [docs/Usecase_Module.md](Usecase_Module.md)
- Repository overview: [docs/Repository_Module.md](Repository_Module.md)
- Legacy Implementation: `legacy-app/src/main/java/.../AccountListActivity.java`
- Architecture Guidelines: [CODING_PRINCIPLES.md](./CODING_PRINCIPLES.md)
- Project Instructions: [.github/copilot-instructions.md](../.github/copilot-instructions.md)
