# Feature: Blotter

**Status:** Implemented (December 27, 2025)  
**Module:** `:feature:blotter`

## Overview

The Blotter feature displays a list of transactions with running balance support and proper currency formatting. It is one of the core features of Financisto, allowing users to view their transaction history with cumulative balance tracking.

## Key Capabilities

1. **View all transactions** across all accounts
2. **View account-specific transactions** with running balance
3. **Running balance tracking** - shows cumulative balance after each transaction
4. **Transaction details** - category, payee, note, date, amount
5. **Real-time updates** via reactive data streams

## Architecture

### Use Cases ([`usecase/src/main/java/ru/orangesoftware/financisto/usecase/modern/BlotterUseCases.kt`](../usecase/src/main/java/ru/orangesoftware/financisto/usecase/modern/BlotterUseCases.kt))

**1. GetBlotterForAccountUseCase**
- Primary use case for account-specific blotter
- Fetches transactions with running balances
- Ensures running balance is calculated
- Enriches data with account/category/payee names
- Returns `List<BlotterItem>` ordered by most recent first

**2. ObserveBlotterForAccountUseCase**
- Reactive version using Flow
- Provides real-time updates when transactions change
- Automatically recalculates on data changes

**3. GetBlotterAllAccountsUseCase**
- Shows all transactions across accounts
- No running balance (only meaningful per-account)
- Used for global transaction view

### Domain Model: BlotterItem

```kotlin
data class BlotterItem(
    val transactionId: Long,
    val datetime: Long,
    val fromAmount: Long,
    val toAmount: Long,
    val fromAccountId: Long,
    val fromAccountTitle: String,
    val fromAccountCurrencyId: Long,
    val toAccountId: Long,
    val toAccountTitle: String?,
    val toAccountCurrencyId: Long?,
    val categoryId: Long,
    val categoryTitle: String?,
    val payeeId: Long,
    val payeeTitle: String?,
    val note: String?,
    val runningBalance: Long, // Key field for blotter!
    val isTransfer: Boolean,
    val isSplit: Boolean,
    val status: String,
    val originalCurrencyId: Long,
    val originalFromAmount: Long,
    // Formatted amounts for display - respects currency symbol and format
    val formattedFromAmount: String,
    val formattedRunningBalance: String
)
```

### ViewModel ([`feature/blotter/src/main/kotlin/.../BlotterViewModel.kt`](../feature/blotter/src/main/kotlin/ru/orangesoftware/financisto/feature/blotter/BlotterViewModel.kt))

Follows modern MVVM pattern:
- **Input pattern**: Sealed interface with all user actions
- **ViewData**: Single StateFlow for all UI state
- **Delegation**: Only calls use cases, never repositories

ViewData structure:
```kotlin
data class ViewData(
    val items: List<BlotterItem>,
    val isLoading: Boolean,
    val isRefreshing: Boolean,
    val error: String?,
    val accountId: Long?,  // null = all accounts
    val showRunningBalance: Boolean,
    val totalBalance: Long?
)
```

### UI ([`feature/blotter/src/main/kotlin/.../BlotterScreen.kt`](../feature/blotter/src/main/kotlin/ru/orangesoftware/financisto/feature/blotter/BlotterScreen.kt))

Jetpack Compose UI showing:
- Transaction list with LazyColumn
- Running balance displayed for account-specific view
- Transaction icons (income/expense/transfer/split)
- Loading, error, and empty states
- Pull-to-refresh support

## Running Balance

Running balance is the cumulative balance after each transaction, crucial for understanding account state at any point in time.

### How it works:

1. **Calculation**: 
   - Performed by `RebuildRunningBalanceForAccountUseCase`
   - Processes transactions chronologically (oldest first)
   - Maintains cumulative sum
   - Stored in `running_balance` table

2. **Display**:
   - Blotter shows transactions in reverse chronological order (newest first)
   - Each transaction shows its running balance
   - Most recent transaction shows current account balance

3. **Updates**:
   - Auto-recalculated when transactions are added/edited/deleted
   - Ensures data integrity

## Usage Example

### Show blotter for specific account:
```kotlin
@Composable
fun AccountDetailsScreen(accountId: Long) {
    BlotterScreen(accountId = accountId)
}
```

### Show all transactions:
```kotlin
@Composable
fun AllTransactionsScreen() {
    BlotterScreen(accountId = null)
}
```

### With ViewModel directly:
```kotlin
val viewModel: BlotterViewModel = hiltViewModel()

// Load account transactions
viewModel.onInput(BlotterViewModel.Input.LoadAccountTransactions(accountId))

// Observe state
val viewData by viewModel.viewData.collectAsStateWithLifecycle()
```

## Currency Formatting

The blotter properly displays amounts with correct currency symbols and formatting rules.

### Implementation

- **CurrencyFormatter utility** ([`core/common/.../CurrencyFormatter.kt`](../core/common/src/main/java/ru/orangesoftware/financisto/utils/CurrencyFormatter.kt))
  - Centralized utility for formatting currency amounts
  - Respects symbol position (RS, LS, RSP, LSP)
  - Handles decimal places (0-4)
  - Proper sign handling for negative amounts
  
- **Domain model integration**
  - `Currency.formatAmount()` uses proper symbol formatting
  - BlotterItem includes pre-formatted strings (`formattedFromAmount`, `formattedRunningBalance`)
  - Use cases populate formatted amounts during data retrieval
  
- **Benefits**
  - Consistent currency display across all features
  - No UI-level formatting logic needed
  - Domain layer handles all formatting rules
  - Easy to reuse in other features

### Example

For an Indian Rupee account (₹) with symbol format LSP:
```
Amount: 1000 → Display: "1000.00 ₹"
Running Balance: 5000 → Display: "5000.00 ₹"
```

## Related Documentation

- [Currency Formatter Utility](../core/common/src/main/java/ru/orangesoftware/financisto/utils/CurrencyFormatter.kt)
- [Currency Domain Model](../core/common/src/main/java/ru/orangesoftware/financisto/domain/model/Currency.kt)
- [Running Balance DAOs](Repository_RunningBalanceDao.md)
- [Transaction DAOs](Repository_TransactionDao.md)
- [Running Balance Use Cases](../usecase/src/main/java/ru/orangesoftware/financisto/usecase/modern/RunningBalanceUseCases.kt)
- [Legacy Implementation](../legacy-app/src/main/java/ru/orangesoftware/financisto/activity/BlotterActivity.java)

## Testing

(To be implemented)
- Unit tests for BlotterViewModel
- Integration tests for use cases
- UI tests for BlotterScreen
- Currency formatting tests

## Future Enhancements

- Filtering (by date range, category, payee)
- Search functionality
- Sorting options
- Export transactions
- Bulk operations
- Transaction grouping by date
