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
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ru.orangesoftware.financisto.core.ui.theme.Spacing
import ru.orangesoftware.financisto.feature.account.AccountListItem
import ru.orangesoftware.financisto.feature.account.AccountActionCalloutUtils
import ru.orangesoftware.financisto.feature.account.ui.components.AccountActionCallout

/**
 * Modern account list with total balance card at the top
 * and beautiful card-based account items with action tooltips
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
            val tooltipState = rememberTooltipState(isPersistent = true)
            val scope = rememberCoroutineScope()
            val actions = AccountActionCalloutUtils.createAccountActions(account.isActive)
            
            TooltipBox(
                positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(8.dp),
                tooltip = {
                    PlainTooltip(
                        modifier = Modifier.fillMaxWidth(0.9f),
                        caretSize = DpSize(width = 24.dp, height = 12.dp),
                        shape = RoundedCornerShape(16.dp),
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        AccountActionCallout(
                            actions = actions,
                            onActionClick = { actionIndex ->
                                onAccountAction(account.id, actionIndex)
                                scope.launch {
                                    tooltipState.dismiss()
                                }
                            },
                            modifier = Modifier
                        )
                    }
                },
                state = tooltipState
            ) {
                AccountListItem(
                    account = account,
                    onClick = { onAccountClick(account.id) },
                    onLongClick = { 
                        scope.launch {
                            tooltipState.show()
                        }
                    }
                )
            }
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
