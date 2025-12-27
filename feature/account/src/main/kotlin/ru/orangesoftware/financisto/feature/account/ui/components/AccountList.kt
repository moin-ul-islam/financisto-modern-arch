package ru.orangesoftware.financisto.feature.account.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ru.orangesoftware.financisto.core.ui.theme.Spacing
import ru.orangesoftware.financisto.feature.account.AccountActionCalloutItem
import ru.orangesoftware.financisto.feature.account.AccountActionCalloutUtils
import ru.orangesoftware.financisto.feature.account.AccountListItem

/**
 * Modern account list with total balance card at the top
 * and beautiful card-based account items with action bottom sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountList(
    accounts: List<AccountListItem>,
    onAccountClick: (Long) -> Unit,
    onAccountAction: (Long, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // Calculate total balance from all accounts
    val totalBalance = accounts.sumOf { it.balanceAmount }
    val formattedTotal = formatBalance(totalBalance)
    
    // State for bottom sheet
    var selectedAccountId by remember { mutableStateOf<Long?>(null) }
    var selectedAccountActions by remember { mutableStateOf<List<AccountActionCalloutItem>>(emptyList()) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = Spacing.Small)
    ) {
        // Total Balance Card at the top
        item(key = "total_balance") {
            TotalBalanceCard(
                totalBalance = formattedTotal,
                monthlyChange = "", // Can be calculated from transactions
                isPositiveChange = totalBalance >= 0
            )
            Spacer(modifier = Modifier.height(Spacing.Small))
        }
        
        // Account items
        itemsIndexed(
            items = accounts,
            key = { _, account -> account.id }
        ) { _, account ->
            AccountListItem(
                account = account,
                onClick = { onAccountClick(account.id) },
                onLongClick = { 
                    selectedAccountId = account.id
                    selectedAccountActions = AccountActionCalloutUtils.createAccountActions(account.isActive)
                }
            )
        }
    }
    
    // Bottom sheet for account actions
    if (selectedAccountId != null) {
        ModalBottomSheet(
            onDismissRequest = { 
                selectedAccountId = null
                selectedAccountActions = emptyList()
            },
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            AccountActionCallout(
                actions = selectedAccountActions,
                onActionClick = { actionIndex ->
                    selectedAccountId?.let { accountId ->
                        onAccountAction(accountId, actionIndex)
                    }
                    scope.launch {
                        sheetState.hide()
                        selectedAccountId = null
                        selectedAccountActions = emptyList()
                    }
                },
                modifier = Modifier
            )
        }
    }
}

/**
 * Helper function to format balance amount
 */
private fun formatBalance(amount: Long): String {
    val absAmount = kotlin.math.abs(amount) / 100.0
    val sign = if (amount < 0) "-" else ""
    return String.format("%s$%,.2f", sign, absAmount)
}
