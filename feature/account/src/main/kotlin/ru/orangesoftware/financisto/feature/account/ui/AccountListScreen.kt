package ru.orangesoftware.financisto.feature.account.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ru.orangesoftware.financisto.feature.account.AccountAction
import ru.orangesoftware.financisto.feature.account.AccountActionCalloutUtils
import ru.orangesoftware.financisto.feature.account.AccountInfoData
import ru.orangesoftware.financisto.feature.account.AccountListAction
import ru.orangesoftware.financisto.feature.account.AccountListScreenState
import ru.orangesoftware.financisto.feature.account.AccountListUiState
import ru.orangesoftware.financisto.feature.account.AccountListViewModel
import ru.orangesoftware.financisto.feature.account.AccountState
import ru.orangesoftware.financisto.feature.account.ui.components.AccountActionCallout
import ru.orangesoftware.financisto.feature.account.ui.components.AccountList
import ru.orangesoftware.financisto.feature.account.ui.components.BottomToolbar
import ru.orangesoftware.financisto.feature.account.ui.components.EmptyState
import ru.orangesoftware.financisto.feature.account.ui.components.ErrorState
import ru.orangesoftware.financisto.feature.account.ui.components.IntegrityErrorBanner
import ru.orangesoftware.financisto.feature.account.ui.components.LoadingIndicator

/**
 * Main Account List Screen composable that displays the list of accounts.
 * 
 * This composable maintains the same visual appearance and functionality as the legacy
 * AccountListActivity while using modern Compose UI and clean architecture.
 */
@Composable
fun AccountListScreen(
    viewModel: AccountListViewModel = hiltViewModel(),
    backStackEntry: androidx.navigation.NavBackStackEntry? = null,
    onNavigateToAccountDetails: (Long) -> Unit = {},
    onNavigateToCreateAccount: () -> Unit = {},
    onNavigateToAccountTotals: () -> Unit = {},
    onNavigateToBlotter: (Long) -> Unit = {},
    onNavigateToEditAccount: (Long) -> Unit = {},
    onNavigateToAddTransaction: (Long) -> Unit = {},
    onNavigateToAddTransfer: (Long) -> Unit = {},
    onNavigateToUpdateBalance: (Long) -> Unit = {},
    onNavigateToPurgeAccount: (Long) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    viewModel.handleAction(AccountListAction.LoadAccounts)
    
    // Check for refresh signal from navigation
    val shouldRefreshAccounts = backStackEntry
        ?.savedStateHandle
        ?.getLiveData<Boolean>("refresh_accounts")
        ?.value == true
    
    // Refresh accounts when returning from transaction creation
    androidx.compose.runtime.LaunchedEffect(shouldRefreshAccounts) {
        if (shouldRefreshAccounts) {
            viewModel.handleAction(AccountListAction.RefreshAccounts)
            backStackEntry?.savedStateHandle?.set("refresh_accounts", false)
        }
    }
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AccountListContent(
                uiState = uiState,
                onAccountClick = { accountId ->
                    onNavigateToBlotter(accountId)
                },
                onAccountAction = { accountId, actionIndex ->
                    val account = uiState.screenState.let { state ->
                        if (state is AccountListScreenState.Content) {
                            state.data.accounts.find { it.id == accountId }
                        } else {
                            null
                        }
                    }
                    account?.let { acc ->
                        val actions = AccountActionCalloutUtils.createAccountActions(acc.isActive)
                        if (actionIndex < actions.size) {
                            val action = actions[actionIndex].action
                            handleAccountAction(
                                action = action,
                                accountId = accountId,
                                viewModel = viewModel,
                                onNavigateToAccountDetails = onNavigateToAccountDetails,
                                onNavigateToBlotter = onNavigateToBlotter,
                                onNavigateToEditAccount = onNavigateToEditAccount,
                                onNavigateToAddTransaction = onNavigateToAddTransaction,
                                onNavigateToAddTransfer = onNavigateToAddTransfer,
                                onNavigateToUpdateBalance = onNavigateToUpdateBalance,
                                onNavigateToPurgeAccount = onNavigateToPurgeAccount
                            )
                        }
                    }
                },
                onAddClick = {
                    viewModel.handleAction(AccountListAction.CreateNewAccount)
                    onNavigateToCreateAccount()
                },
                onMenuClick = {
                    // Handle menu popup - this will be implemented later
                },
                onTotalClick = {
                    viewModel.handleAction(AccountListAction.ViewAccountTotals)
                    onNavigateToAccountTotals()
                },
                onRetryClick = {
                    viewModel.handleAction(AccountListAction.RetryLoading)
                },
                onDismissIntegrityError = {
                    viewModel.handleAction(AccountListAction.DismissIntegrityError)
                }
            )
            
            // Account Info Dialog
            uiState.accountInfoData?.let { accountInfo ->
                if (uiState.showAccountInfoDialog) {
                    AccountInfoDialog(
                        accountInfo = accountInfo,
                        onDismiss = {
                            viewModel.handleAction(AccountListAction.DismissAccountInfoDialog)
                        },
                        onEditClick = { accountId ->
                            viewModel.handleAction(AccountListAction.DismissAccountInfoDialog)
                            onNavigateToEditAccount(accountId)
                        }
                    )
                }
            }
            
            // Delete Confirmation Dialog
            uiState.accountToDelete?.let { accountToDelete ->
                if (uiState.showDeleteConfirmDialog) {
                    DeleteAccountConfirmDialog(
                        accountName = accountToDelete.title,
                        onConfirm = {
                            viewModel.handleAction(AccountListAction.ConfirmDeleteAccount(accountToDelete.id))
                        },
                        onDismiss = {
                            viewModel.handleAction(AccountListAction.DismissDeleteConfirmDialog)
                        }
                    )
                }
            }
        }
    }
}

/**
 * Handles account action selection from the callout menu.
 */
private fun handleAccountAction(
    action: AccountAction,
    accountId: Long,
    viewModel: AccountListViewModel,
    onNavigateToAccountDetails: (Long) -> Unit,
    onNavigateToBlotter: (Long) -> Unit,
    onNavigateToEditAccount: (Long) -> Unit,
    onNavigateToAddTransaction: (Long) -> Unit,
    onNavigateToAddTransfer: (Long) -> Unit,
    onNavigateToUpdateBalance: (Long) -> Unit,
    onNavigateToPurgeAccount: (Long) -> Unit
) {
    when (action) {
        AccountAction.INFO -> {
            // Use ViewModel to show account info dialog
            viewModel.handleAction(AccountListAction.ShowAccountInfo(accountId))
        }
        AccountAction.BLOTTER -> onNavigateToBlotter(accountId)
        AccountAction.EDIT -> onNavigateToEditAccount(accountId)
        AccountAction.TRANSACTION -> onNavigateToAddTransaction(accountId)
        AccountAction.TRANSFER -> onNavigateToAddTransfer(accountId)
        AccountAction.BALANCE -> onNavigateToUpdateBalance(accountId)
        AccountAction.PURGE -> onNavigateToPurgeAccount(accountId)
        AccountAction.CLOSE_REOPEN -> {
            // Use ViewModel to toggle account status
            viewModel.handleAction(AccountListAction.ToggleAccountStatus(accountId))
        }
        AccountAction.DELETE -> {
            // Use ViewModel to show delete confirmation dialog
            viewModel.handleAction(AccountListAction.DeleteAccount(accountId))
        }
    }
}

/**
 * Content composable that handles the different screen states and layouts.
 */
@Composable
private fun AccountListContent(
    uiState: AccountListUiState,
    onAccountClick: (Long) -> Unit,
    onAccountAction: (Long, Int) -> Unit,
    onAddClick: () -> Unit,
    onMenuClick: () -> Unit,
    onTotalClick: () -> Unit,
    onRetryClick: () -> Unit,
    onDismissIntegrityError: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Main content area
        Box(modifier = Modifier.weight(1f)) {
            when (val screenState = uiState.screenState) {
                is AccountListScreenState.Loading -> {
                    LoadingIndicator()
                }
                is AccountListScreenState.Empty -> {
                    EmptyState()
                }
                is AccountListScreenState.Content -> {
                    AccountList(
                        accounts = screenState.data.accounts,
                        onAccountClick = onAccountClick,
                        onAccountAction = onAccountAction
                    )
                }
                is AccountListScreenState.Error -> {
                    ErrorState(
                        message = screenState.message,
                        canRetry = screenState.canRetry,
                        onRetryClick = onRetryClick
                    )
                }
            }
            
            // Integrity error overlay
            if (uiState.showIntegrityError) {
                IntegrityErrorBanner(
                    onDismiss = onDismissIntegrityError
                )
            }
        }
        
        // Bottom toolbar
        val totalText = when (val screenState = uiState.screenState) {
            is AccountListScreenState.Content -> screenState.data.totalBalance
            else -> ""
        }
        
        BottomToolbar(
            totalText = totalText,
            showMenuButton = uiState.showMenuButton,
            onAddClick = onAddClick,
            onMenuClick = onMenuClick,
            onTotalClick = onTotalClick
        )
    }
}

/**
 * Account info dialog that displays account details with enhanced visual design.
 */
@Composable
private fun AccountInfoDialog(
    accountInfo: AccountInfoData,
    onDismiss: () -> Unit,
    onEditClick: (Long) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = accountInfo.title,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = accountInfo.accountType,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Primary account information
                AccountDetailRow("Currency", accountInfo.currency)
                AccountDetailRow("Balance", accountInfo.formattedBalance, isAmount = true)
                
                // Card-specific information
                accountInfo.issuer?.let { issuer ->
                    if (issuer.isNotBlank()) {
                        AccountDetailRow("Issuer", issuer)
                    }
                }
                
                accountInfo.cardNumber?.let { cardNumber ->
                    if (cardNumber.isNotBlank()) {
                        // Mask card number for security
                        val maskedNumber = if (cardNumber.length > 4) {
                            "****" + cardNumber.takeLast(4)
                        } else {
                            cardNumber
                        }
                        AccountDetailRow("Card Number", maskedNumber)
                    }
                }
                
                accountInfo.limitAmount?.let { limitAmount ->
                    if (limitAmount.isNotBlank()) {
                        AccountDetailRow("Credit Limit", limitAmount, isAmount = true)
                    }
                }
                
                // Status and transaction information
                AccountDetailRow(
                    "Status", 
                    if (accountInfo.isActive) "Active" else "Closed",
                    isStatus = true
                )
                
                accountInfo.lastTransactionDate?.let { lastDate ->
                    if (lastDate.isNotBlank()) {
                        AccountDetailRow("Last Transaction", lastDate)
                    }
                }
                
                // Note (if present)
                accountInfo.note?.let { note ->
                    if (note.isNotBlank()) {
                        AccountDetailRow("Note", note, isNote = true)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onEditClick(accountInfo.accountId) }
            ) {
                Text("Edit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

/**
 * Enhanced composable for account detail rows with improved visual design.
 * Provides better label/value differentiation and specialized styling for different content types.
 */
@Composable
private fun AccountDetailRow(
    label: String, 
    value: String,
    isAmount: Boolean = false,
    isStatus: Boolean = false,
    isNote: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = if (isNote) Alignment.Top else Alignment.CenterVertically
    ) {
        // Label column
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1.2f)
        )
        
        // Value column with specialized styling
        Text(
            text = value,
            style = when {
                isAmount -> MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
                isNote -> MaterialTheme.typography.bodySmall
                else -> MaterialTheme.typography.bodyMedium
            },
            color = when {
                isAmount -> {
                    // Parse amount to determine if positive or negative
                    when {
                        value.contains("-") -> MaterialTheme.colorScheme.error
                        value.startsWith("$") || value.contains("available") -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                }
                isStatus -> {
                    if (value == "Active") MaterialTheme.colorScheme.primary 
                    else MaterialTheme.colorScheme.onSurfaceVariant
                }
                else -> MaterialTheme.colorScheme.onSurface
            },
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.8f),
            maxLines = if (isNote) 3 else 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Delete account confirmation dialog.
 */
@Composable
private fun DeleteAccountConfirmDialog(
    accountName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                text = "Delete Account",
                style = MaterialTheme.typography.headlineSmall
            ) 
        },
        text = {
            Text(
                text = "Are you sure you want to delete account \"$accountName\"? This action cannot be undone.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
