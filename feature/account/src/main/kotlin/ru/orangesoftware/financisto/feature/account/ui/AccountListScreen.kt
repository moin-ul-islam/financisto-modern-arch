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
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import ru.orangesoftware.financisto.feature.account.AccountListAction
import ru.orangesoftware.financisto.feature.account.AccountListScreenState
import ru.orangesoftware.financisto.feature.account.AccountListUiState
import ru.orangesoftware.financisto.feature.account.AccountListViewModel
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
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        AccountListContent(
            uiState = uiState,
            onAccountClick = { accountId ->
                onNavigateToBlotter(accountId)
            },
            onAccountLongClick = { accountId ->
                // This will be handled by showing a quick action menu
                // For now, just navigate to account details
                onNavigateToAccountDetails(accountId)
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
    }
}

/**
 * Content composable that handles the different screen states and layouts.
 */
@Composable
private fun AccountListContent(
    uiState: AccountListUiState,
    onAccountClick: (Long) -> Unit,
    onAccountLongClick: (Long) -> Unit,
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
