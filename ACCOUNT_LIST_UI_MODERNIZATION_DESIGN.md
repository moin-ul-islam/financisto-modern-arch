# Account List Screen UI Modernization Design Document

## Overview

This document outlines the design for modernizing the Account List screen UI layer while maintaining the same visual appearance and functionality as the legacy implementation. The legacy code is in `AccountListActivity.java` and we need to create a modern, clean architecture implementation.

## Legacy Code Analysis

### Current Implementation Structure

The legacy `AccountListActivity` extends `AbstractListActivity` and contains:

1. **UI Components**: ListView with custom adapter, bottom toolbar with buttons
2. **Business Logic**: Database operations, account management, calculations
3. **Navigation Logic**: Activity transitions and intent handling
4. **View Logic**: UI updates, state management, adapter binding

### Key UI Elements Identified

#### Main Layout (`account_list.xml`)
- **ListView**: Displays list of accounts
- **Empty State**: Shows "No accounts" message when list is empty
- **Integrity Error Bar**: Red banner for data integrity warnings
- **Bottom Toolbar**: Contains Add button, Menu button, and Total text

#### Account List Item (`account_list_item.xml`)
- **Icon**: Account type/card issuer icon with active/inactive overlay
- **Text Layout**: 
  - Top: Account type/issuer info
  - Center: Account title (main text)
  - Bottom: Creation/last transaction date
- **Amount Display**: 
  - Right Center: Current balance
  - Right: Credit limit info (for credit cards)
- **Progress Bar**: Credit card utilization indicator

#### Bottom Toolbar Elements
- **Add Button**: Creates new account
- **Menu Button**: Shows popup menu (backup, go to menu)
- **Total Text**: Shows total balance, clickable for details

### User Interactions

#### Primary Actions
1. **Tap Account**: Navigate to account transactions (BlotterActivity)
2. **Long Press Account**: Show quick action menu
3. **Add Button**: Create new account
4. **Menu Button**: Show popup menu
5. **Total Text**: Show account totals details

#### Quick Action Menu (Long Press)
- Info: Show account details dialog
- Blotter: View account transactions
- Edit: Edit account
- Transaction: Add new transaction
- Transfer: Add new transfer
- Balance: Update account balance
- Purge: Delete old transactions
- Close/Open: Toggle account status
- Delete: Delete account

#### Additional Features
- **Integrity Check**: Background task with error display
- **Total Calculation**: Async calculation with progress
- **Hide Closed Accounts**: User preference filtering

## Modern UI Design

### Architecture Approach

We will follow the clean architecture pattern already established in the new modules:

```
View Layer (Compose UI) 
    ↓
ViewModel (AccountListViewModel - already exists)
    ↓  
Use Cases (already exists in usecase module)
    ↓
Repository (already exists in repository module)
```

### Compose UI Structure

#### 1. Main Screen Composable

```kotlin
@Composable
fun AccountListScreen(
    viewModel: AccountListViewModel,
    onNavigateToAccountDetails: (Long) -> Unit,
    onNavigateToCreateAccount: () -> Unit,
    onNavigateToAccountTotals: () -> Unit,
    onNavigateToBlotter: (Long) -> Unit
)
```

#### 2. Screen States

Based on `AccountListScreenState` (already defined):
- **Loading**: Show loading indicator
- **Empty**: Show empty state message
- **Content**: Show account list with data
- **Error**: Show error message with retry option

#### 3. UI Components Breakdown

##### Main Layout Components
```kotlin
@Composable
fun AccountListContent(
    uiState: AccountListUiState,
    onAccountClick: (Long) -> Unit,
    onAccountLongClick: (Long) -> Unit,
    onAddClick: () -> Unit,
    onMenuClick: () -> Unit,
    onTotalClick: () -> Unit
) {
    Column {
        // Main content area
        Box(modifier = Modifier.weight(1f)) {
            when (uiState.screenState) {
                is Loading -> LoadingIndicator()
                is Empty -> EmptyState()
                is Content -> AccountList(...)
                is Error -> ErrorState(...)
            }
            
            // Integrity error overlay
            if (uiState.showIntegrityError) {
                IntegrityErrorBanner()
            }
        }
        
        // Bottom toolbar
        BottomToolbar(
            totalText = uiState.totalBalance,
            showMenuButton = uiState.showMenuButton,
            onAddClick = onAddClick,
            onMenuClick = onMenuClick,
            onTotalClick = onTotalClick
        )
    }
}
```

##### Account List Component
```kotlin
@Composable
fun AccountList(
    accounts: List<AccountListItem>,
    onAccountClick: (Long) -> Unit,
    onAccountLongClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(accounts) { account ->
            AccountListItem(
                account = account,
                onClick = { onAccountClick(account.id) },
                onLongClick = { onAccountLongClick(account.id) }
            )
        }
    }
}
```

##### Account Item Component
```kotlin
@Composable
fun AccountListItem(
    account: AccountListItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Account icon with overlay
            AccountIcon(
                iconRes = account.iconRes,
                isActive = account.isActive
            )
            
            // Vertical divider
            VerticalDivider()
            
            // Account info
            Column(modifier = Modifier.weight(1f)) {
                AccountInfoSection(account)
            }
            
            // Amount section
            AmountSection(account)
        }
        
        // Credit card progress bar
        if (account.showProgressBar) {
            CreditCardProgressBar(
                progress = account.creditUtilization
            )
        }
    }
}
```

##### Supporting Components
```kotlin
@Composable
fun AccountIcon(iconRes: Int, isActive: Boolean) {
    Box {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            tint = if (isActive) Color.Unspecified 
                   else Color.Unspecified.copy(alpha = 0.47f)
        )
        
        if (!isActive) {
            Icon(
                painter = painterResource(R.drawable.icon_lock),
                contentDescription = "Inactive",
                modifier = Modifier.align(Alignment.Center),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun AccountInfoSection(account: AccountListItem) {
    Column {
        // Top line: Account type/issuer
        Text(
            text = account.topText,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        
        // Center line: Account title
        Text(
            text = account.title,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        
        // Bottom line: Date
        Text(
            text = account.formattedDate,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1
        )
    }
}

@Composable
fun AmountSection(account: AccountListItem) {
    Column(
        horizontalAlignment = Alignment.End
    ) {
        // Main balance
        Text(
            text = account.formattedBalance,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = if (account.balanceAmount >= 0) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.error
        )
        
        // Credit limit (if applicable)
        if (account.showCreditInfo) {
            Text(
                text = account.formattedCreditBalance,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun BottomToolbar(
    totalText: String,
    showMenuButton: Boolean,
    onAddClick: () -> Unit,
    onMenuClick: () -> Unit,
    onTotalClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Add button
            IconButton(onClick = onAddClick) {
                Icon(
                    painter = painterResource(R.drawable.actionbar_add_big),
                    contentDescription = "Add Account"
                )
            }
            
            // Menu button
            if (showMenuButton) {
                IconButton(onClick = onMenuClick) {
                    Icon(
                        painter = painterResource(R.drawable.actionbar_dot_menu),
                        contentDescription = "Menu"
                    )
                }
            }
            
            // Spacer
            Spacer(modifier = Modifier.weight(1f))
            
            // Total text
            Text(
                text = totalText,
                modifier = Modifier
                    .clickable { onTotalClick() }
                    .padding(10.dp),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.End
            )
        }
    }
}
```

#### 4. Dialog Components

##### Quick Action Menu
```kotlin
@Composable
fun AccountQuickActionMenu(
    account: AccountListItem,
    onDismiss: () -> Unit,
    onActionSelected: (AccountAction) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(account.title) },
        text = {
            LazyColumn {
                items(getAccountActions(account)) { action ->
                    QuickActionItem(
                        action = action,
                        onClick = {
                            onActionSelected(action)
                            onDismiss()
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
```

##### Account Info Dialog
```kotlin
@Composable
fun AccountInfoDialog(
    account: AccountListItem,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Account Information") },
        text = {
            Column {
                AccountDetailRow("Title", account.title)
                AccountDetailRow("Type", account.accountType)
                AccountDetailRow("Balance", account.formattedBalance)
                if (account.note.isNotEmpty()) {
                    AccountDetailRow("Note", account.note)
                }
                // Add more details as needed
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
```

### State Management Integration

The UI will connect to the existing `AccountListViewModel` which already provides:

```kotlin
data class AccountListUiState(
    val screenState: AccountListScreenState,
    val isRefreshing: Boolean,
    val totalCalculationState: TotalCalculationState,
    val selectedSortOrder: AccountSortOrder,
    val showIntegrityError: Boolean,
    val showMenuButton: Boolean
)
```

### Navigation Integration

The Compose UI will integrate with the navigation system:

```kotlin
// In the navigation graph
composable("account_list") {
    val viewModel: AccountListViewModel = hiltViewModel()
    
    AccountListScreen(
        viewModel = viewModel,
        onNavigateToAccountDetails = { accountId ->
            navController.navigate("account_details/$accountId")
        },
        onNavigateToCreateAccount = {
            navController.navigate("account_create")
        },
        onNavigateToAccountTotals = {
            navController.navigate("account_totals")
        },
        onNavigateToBlotter = { accountId ->
            navController.navigate("blotter/$accountId")
        }
    )
}
```

## Implementation Strategy

### Phase 1: Core UI Components
1. Create main `AccountListScreen` composable
2. Implement `AccountListItem` component
3. Create supporting UI components (icons, amount display, etc.)
4. Implement basic state handling

### Phase 2: Interactions
1. Add click and long-click handling
2. Implement quick action menu
3. Add account info dialog
4. Integrate with existing ViewModel

### Phase 3: Polish & Features
1. Add animations and transitions
2. Implement pull-to-refresh
3. Add accessibility support
4. Performance optimization

### Phase 4: Integration Testing
1. Test with existing ViewModel
2. Verify all user interactions work
3. Test navigation flows
4. Validate visual consistency with legacy

## Key Considerations

### Visual Consistency
- Match exact layout and spacing from legacy implementation
- Preserve icon styles and colors
- Maintain the same visual hierarchy
- Keep the same bottom toolbar design

### Behavioral Consistency
- Preserve all user interaction patterns
- Maintain the same menu structures
- Keep the same navigation flows
- Preserve quick action functionality

### Performance
- Use LazyColumn for efficient list rendering
- Implement proper state management
- Minimize recompositions
- Handle large account lists efficiently

### Accessibility
- Add proper content descriptions
- Support screen readers
- Implement keyboard navigation
- Ensure sufficient color contrast

### Testing Strategy
- Unit tests for UI components
- Integration tests with ViewModel
- Screenshot tests for visual regression
- Accessibility testing

## Migration Path

1. **Create Compose UI in parallel**: Build the new UI without removing the old one
2. **Feature flag**: Use a feature flag to switch between old and new UI
3. **Gradual rollout**: Test with subset of users first
4. **Full migration**: Replace the old Activity with new Compose implementation
5. **Cleanup**: Remove legacy code after successful migration

This design maintains the exact functionality and appearance of the legacy Account List screen while providing a modern, maintainable, and testable implementation using Jetpack Compose and clean architecture principles.
