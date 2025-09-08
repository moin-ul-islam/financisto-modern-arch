package ru.orangesoftware.financisto.feature.account.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.hilt.navigation.compose.hiltViewModel
import ru.orangesoftware.financisto.feature.account.AccountAction
import ru.orangesoftware.financisto.feature.account.AccountActionCalloutUtils
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
    
    // Callout state management
    var showCallout by remember { mutableStateOf(false) }
    var calloutAnchorBounds by remember { mutableStateOf(IntOffset.Zero) }
    var selectedAccount by remember { mutableStateOf<AccountState?>(null) }
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AccountListContent(
                uiState = uiState,
                onAccountClick = { accountId ->
                    // For testing: also show callout on regular click
                    // TODO: Remove this after testing - normally only long click should show callout
                    android.util.Log.d("AccountCallout", "Regular click detected for account: $accountId")
                    val screenState = uiState.screenState
                    if (screenState is AccountListScreenState.Content) {
                        val account = screenState.data.accounts.find { it.id == accountId }
                        account?.let {
                            val accountState = AccountState(
                                id = it.id,
                                isActive = it.isActive,
                                title = it.title
                            )
                            selectedAccount = accountState
                            calloutAnchorBounds = IntOffset(100, 100) // Dummy position for testing
                            showCallout = true
                        }
                    }
                    // Original behavior
                    onNavigateToBlotter(accountId)
                },
                onAccountLongClick = { accountId, accountState, bounds ->
                    // Show callout for account actions
                    android.util.Log.d("AccountCallout", "Long click detected for account: ${accountState.title}")
                    selectedAccount = accountState
                    calloutAnchorBounds = bounds
                    showCallout = true
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
            
            // Account Action Callout
            selectedAccount?.let { account ->
                android.util.Log.d("AccountCallout", "Showing callout for account: ${account.title}, visible: $showCallout")
                val actions = AccountActionCalloutUtils.createAccountActions(account.isActive)
                AccountActionCallout(
                    actions = actions,
                    isVisible = showCallout,
                    anchorBounds = calloutAnchorBounds,
                    onActionClick = { actionIndex ->
                        val action = actions[actionIndex].action
                        handleAccountAction(
                            action = action,
                            accountId = account.id,
                            onNavigateToAccountDetails = onNavigateToAccountDetails,
                            onNavigateToBlotter = onNavigateToBlotter,
                            onNavigateToEditAccount = onNavigateToEditAccount,
                            onNavigateToAddTransaction = onNavigateToAddTransaction,
                            onNavigateToAddTransfer = onNavigateToAddTransfer,
                            onNavigateToUpdateBalance = onNavigateToUpdateBalance,
                            onNavigateToPurgeAccount = onNavigateToPurgeAccount
                        )
                        showCallout = false
                        selectedAccount = null
                    },
                    onDismiss = {
                        showCallout = false
                        selectedAccount = null
                    }
                )
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
    onNavigateToAccountDetails: (Long) -> Unit,
    onNavigateToBlotter: (Long) -> Unit,
    onNavigateToEditAccount: (Long) -> Unit,
    onNavigateToAddTransaction: (Long) -> Unit,
    onNavigateToAddTransfer: (Long) -> Unit,
    onNavigateToUpdateBalance: (Long) -> Unit,
    onNavigateToPurgeAccount: (Long) -> Unit
) {
    when (action) {
        AccountAction.INFO -> onNavigateToAccountDetails(accountId)
        AccountAction.BLOTTER -> onNavigateToBlotter(accountId)
        AccountAction.EDIT -> onNavigateToEditAccount(accountId)
        AccountAction.TRANSACTION -> onNavigateToAddTransaction(accountId)
        AccountAction.TRANSFER -> onNavigateToAddTransfer(accountId)
        AccountAction.BALANCE -> onNavigateToUpdateBalance(accountId)
        AccountAction.PURGE -> onNavigateToPurgeAccount(accountId)
        AccountAction.CLOSE_REOPEN -> {
            // TODO: Implement close/reopen functionality
            android.util.Log.d("AccountAction", "Close/Reopen account: $accountId")
        }
        AccountAction.DELETE -> {
            // TODO: Implement delete functionality with confirmation
            android.util.Log.d("AccountAction", "Delete account: $accountId")
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
    onAccountLongClick: (Long, AccountState, IntOffset) -> Unit,
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
                        onAccountLongClick = onAccountLongClick
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
