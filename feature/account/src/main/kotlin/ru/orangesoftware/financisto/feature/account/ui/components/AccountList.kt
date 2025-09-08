package ru.orangesoftware.financisto.feature.account.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import ru.orangesoftware.financisto.feature.account.AccountListItem
import ru.orangesoftware.financisto.feature.account.AccountState

/**
 * Lazy list of accounts that efficiently renders large lists.
 * Maintains the same visual appearance as the legacy ListView implementation.
 */
@Composable
fun AccountList(
    accounts: List<AccountListItem>,
    onAccountClick: (Long) -> Unit,
    onAccountLongClick: (Long, AccountState, IntOffset) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 2.dp)
    ) {
        items(
            items = accounts,
            key = { account -> account.id }
        ) { account ->
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
                    // For now, use a dummy IntOffset. In a real implementation, 
                    // we would capture the actual bounds
                    onAccountLongClick(account.id, accountState, IntOffset(100, 100))
                }
            )
        }
    }
}
