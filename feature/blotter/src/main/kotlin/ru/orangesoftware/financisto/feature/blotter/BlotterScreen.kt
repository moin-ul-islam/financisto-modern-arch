package ru.orangesoftware.financisto.feature.blotter

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.orangesoftware.financisto.core.ui.theme.ExpenseRed
import ru.orangesoftware.financisto.core.ui.theme.IncomeGreen
import ru.orangesoftware.financisto.usecase.modern.BlotterItem
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Blotter screen showing list of transactions with running balance.
 *
 * This can show:
 * - All transactions across all accounts (no running balance)
 * - Transactions for a specific account (with running balance)
 *
 * @param accountId If null, shows all accounts. If specified, shows only that account with running balance.
 * @param onNavigateToNewTransaction Callback to navigate to new transaction screen with optional accountId
 * @param viewModel The ViewModel instance (injected by Hilt)
 */
@Composable
fun BlotterScreen(
    accountId: Long? = null,
    onNavigateToNewTransaction: ((Long?) -> Unit)? = null,
    viewModel: BlotterViewModel = hiltViewModel()
) {
    val viewData by viewModel.viewData.collectAsStateWithLifecycle()
    
    // Load data when screen is first displayed
    LaunchedEffect(accountId) {
        if (accountId != null) {
            viewModel.onInput(BlotterViewModel.Input.LoadAccountTransactions(accountId))
        } else {
            viewModel.onInput(BlotterViewModel.Input.LoadAllTransactions)
        }
    }
    
    Scaffold(
        topBar = {
            BlotterTopBar(
                accountId = accountId,
                formattedTotalBalance = viewData.formattedTotalBalance,
                totalBalance = viewData.totalBalance,
                showRunningBalance = viewData.showRunningBalance
            )
        },
        floatingActionButton = {
            if (onNavigateToNewTransaction != null) {
                FloatingActionButton(
                    onClick = { onNavigateToNewTransaction(accountId) }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add transaction"
                    )
                }
            }
        }
    ) { paddingValues ->
        when {
            viewData.isLoading -> {
                LoadingState(modifier = Modifier.padding(paddingValues))
            }
            viewData.error != null -> {
                ErrorState(
                    error = viewData.error!!,
                    onRetry = { viewModel.onInput(BlotterViewModel.Input.Refresh) },
                    onDismiss = { viewModel.onInput(BlotterViewModel.Input.ClearError) },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            viewData.items.isEmpty() -> {
                EmptyState(modifier = Modifier.padding(paddingValues))
            }
            else -> {
                BlotterList(
                    items = viewData.items,
                    showRunningBalance = viewData.showRunningBalance,
                    onItemClick = { item ->
                        viewModel.onInput(BlotterViewModel.Input.ShowTransactionDetails(item.transactionId))
                    },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BlotterTopBar(
    accountId: Long?,
    formattedTotalBalance: String?,
    totalBalance: Long?,
    showRunningBalance: Boolean
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = if (accountId != null) "Account Blotter" else "All Transactions",
                    style = MaterialTheme.typography.titleLarge
                )
                if (showRunningBalance && formattedTotalBalance != null && totalBalance != null) {
                    Text(
                        text = "Balance: $formattedTotalBalance",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (totalBalance >= 0) IncomeGreen else ExpenseRed
                    )
                }
            }
        }
    )
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorState(
    error: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Error",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(onClick = onDismiss) {
                Text("Dismiss")
            }
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No transactions",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun BlotterList(
    items: List<BlotterItem>,
    showRunningBalance: Boolean,
    onItemClick: (BlotterItem) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(items, key = { it.transactionId }) { item ->
            BlotterItemRow(
                item = item,
                showRunningBalance = showRunningBalance,
                onClick = { onItemClick(item) }
            )
            HorizontalDivider()
        }
    }
}

@Composable
private fun BlotterItemRow(
    item: BlotterItem,
    showRunningBalance: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left side: Icon and transaction details
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Transaction type icon
            TransactionIcon(item = item)
            
            // Transaction details
            Column(modifier = Modifier.weight(1f)) {
                // Title (category or "Transfer")
                Text(
                    text = item.getDisplayTitle(),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Subtitle (payee, note, or transfer details)
                val subtitle = item.getDisplaySubtitle()
                if (subtitle.isNotEmpty()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // Date
                Text(
                    text = formatDate(item.datetime),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
        
        // Right side: Amount and balance
        Column(
            horizontalAlignment = Alignment.End
        ) {
            // Transaction amount
            Text(
                text = item.formattedFromAmount,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = when {
                    item.fromAmount > 0 -> IncomeGreen
                    item.fromAmount < 0 -> ExpenseRed
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
            
            // Running balance (only shown for single account view)
            if (showRunningBalance) {
                Text(
                    text = "Balance: ${item.formattedRunningBalance}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun TransactionIcon(item: BlotterItem) {
    val (icon, tint) = when {
        item.isTransferTransaction -> Icons.Default.AccountCircle to MaterialTheme.colorScheme.primary
        item.isSplit -> Icons.Default.AccountCircle to MaterialTheme.colorScheme.secondary
        item.fromAmount > 0 -> Icons.Default.Add to IncomeGreen
        item.fromAmount < 0 -> Icons.Default.Delete to ExpenseRed
        else -> Icons.Default.AccountCircle to MaterialTheme.colorScheme.onSurface
    }
    
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = tint
    )
}

// Helper function for formatting
private fun formatDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    return formatter.format(Date(timestamp))
}
