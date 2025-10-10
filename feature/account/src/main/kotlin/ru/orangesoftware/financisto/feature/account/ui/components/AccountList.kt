package ru.orangesoftware.financisto.feature.account.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import ru.orangesoftware.financisto.core.ui.theme.Spacing
import ru.orangesoftware.financisto.feature.account.AccountListItem
import ru.orangesoftware.financisto.feature.account.AccountState

/**
 * Modern account list with total balance card at the top
 * and beautiful card-based account items
 */
@Composable
fun AccountList(
    accounts: List<AccountListItem>,
    onAccountClick: (Long) -> Unit,
    onAccountLongClick: (Long, AccountState, IntOffset) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    
    // Calculate total balance from all accounts
    val totalBalance = accounts.sumOf { it.balanceAmount }
    val formattedTotal = formatBalance(totalBalance)
    
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
        ) { index, account ->
            AccountListItem(
                account = account,
                onClick = { onAccountClick(account.id) },
                onLongClick = { 
                    // Convert AccountListItem to AccountState
                    val accountState = AccountState(
                        id = account.id,
                        isActive = account.isActive,
                        title = account.title
                    )
                    
                    // Calculate position based on list index and estimated item height
                    val estimatedItemHeight = with(density) { 72.dp.toPx() } 
                    val listPadding = with(density) { 2.dp.toPx() }
                    
                    // Point to the bottom border of the item instead of center
                    val yPosition = (listPadding + (index + 1) * estimatedItemHeight).toInt()
                    
                    // X position should be roughly in the center of the account item
                    val screenWidth = with(density) { 360.dp.toPx() } // Approximate screen width
                    val xPosition = (screenWidth * 0.3f).toInt() // Position towards left-center of account item
                    
                    val bounds = IntOffset(x = xPosition, y = yPosition)
                    onAccountLongClick(account.id, accountState, bounds)
                }
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
