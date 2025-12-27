package ru.orangesoftware.financisto.feature.account.ui.preview

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import ru.orangesoftware.financisto.feature.account.AccountListItem
import ru.orangesoftware.financisto.feature.account.ui.components.AccountListItem as AccountListItemComponent
import ru.orangesoftware.financisto.feature.account.ui.components.BottomToolbar
import ru.orangesoftware.financisto.feature.account.ui.components.EmptyState
import ru.orangesoftware.financisto.feature.account.ui.components.ErrorState
import ru.orangesoftware.financisto.feature.account.ui.components.IntegrityErrorBanner
import ru.orangesoftware.financisto.feature.account.ui.components.LoadingIndicator

/**
 * Preview composables for development and testing of account list components.
 */
@Preview(showBackground = true)
@Composable
private fun AccountListItemPreview() {
    MaterialTheme {
        AccountListItemComponent(
            account = createSampleAccount(),
            onClick = {},
            onLongClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AccountListItemCreditCardPreview() {
    MaterialTheme {
        AccountListItemComponent(
            account = createSampleCreditCardAccount(),
            onClick = {},
            onLongClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AccountListItemInactivePreview() {
    MaterialTheme {
        AccountListItemComponent(
            account = createSampleAccount().copy(isActive = false),
            onClick = {},
            onLongClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BottomToolbarPreview() {
    MaterialTheme {
        BottomToolbar(
            totalText = "$1,234.56",
            showMenuButton = true,
            onAddClick = {},
            onMenuClick = {},
            onTotalClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadingIndicatorPreview() {
    MaterialTheme {
        LoadingIndicator()
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyStatePreview() {
    MaterialTheme {
        EmptyState()
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorStatePreview() {
    MaterialTheme {
        ErrorState(
            message = "Failed to load accounts",
            canRetry = true,
            onRetryClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun IntegrityErrorBannerPreview() {
    MaterialTheme {
        IntegrityErrorBanner(
            onDismiss = {}
        )
    }
}

// Sample data for previews
private fun createSampleAccount() = AccountListItem(
    id = 1L,
    title = "Main Checking Account",
    balance = "1500.00",
    formattedBalance = "$1,500.00",
    currencySymbol = "$",
    accountType = "Checking",
    iconResId = android.R.drawable.ic_menu_gallery, // Placeholder icon
    isActive = true,
    isIncludeIntoTotals = true,
    lastTransactionDate = System.currentTimeMillis(),
    formattedLastTransactionDate = "Dec 30, 2024",
    transactionCount = 25,
    note = "Primary checking account",
    topText = "Chase Bank",
    formattedDate = "Dec 30, 2024",
    balanceAmount = 150000L,
    showCreditInfo = false,
    formattedCreditBalance = "",
    showProgressBar = false,
    creditUtilization = 0f
)

private fun createSampleCreditCardAccount() = AccountListItem(
    id = 2L,
    title = "Visa Credit Card",
    balance = "-750.00",
    formattedBalance = "-$750.00",
    currencySymbol = "$",
    accountType = "Credit Card",
    iconResId = android.R.drawable.ic_menu_gallery, // Placeholder icon
    isActive = true,
    isIncludeIntoTotals = true,
    lastTransactionDate = System.currentTimeMillis(),
    formattedLastTransactionDate = "Dec 29, 2024",
    transactionCount = 12,
    note = "Primary credit card",
    topText = "Visa #1234",
    formattedDate = "Dec 29, 2024",
    balanceAmount = -75000L,
    showCreditInfo = true,
    formattedCreditBalance = "$1,250.00 available",
    showProgressBar = true,
    creditUtilization = 0.375f // 37.5% utilization
)
