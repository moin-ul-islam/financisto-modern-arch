package ru.orangesoftware.financisto.feature.account.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.orangesoftware.financisto.feature.account.AccountListItem

/**
 * Individual account list item that maintains the exact visual appearance 
 * of the legacy account_list_item.xml layout.
 * 
 * Layout structure:
 * - Icon (with active/inactive overlay)
 * - Vertical divider  
 * - Account info (top, center, bottom text)
 * - Amount section (balance, credit info)
 * - Progress bar (for credit cards)
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AccountListItem(
    account: AccountListItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = 12.dp, vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Account icon with overlay
            AccountIcon(
                iconRes = account.iconResId,
                isActive = account.isActive,
                modifier = Modifier.padding(end = 5.dp)
            )
            
            // Vertical divider (simplified for older Compose version)
            VerticalDivider(
                modifier = Modifier.padding(end = 5.dp)
            )
            
            // Account info section
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Top line: Account type/issuer
                Text(
                    text = account.topText.ifEmpty { account.accountType },
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Center line: Account title (main text)
                Text(
                    text = account.title,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                // Bottom line: Date
                Text(
                    text = account.formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Amount section
            AmountSection(
                account = account,
                modifier = Modifier.padding(start = 5.dp)
            )
        }
        
        // Credit card progress bar (only shown for credit cards)
        if (account.showProgressBar) {
            CreditCardProgressBar(
                progress = account.creditUtilization,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 32.dp, top = 2.dp)
            )
        }
    }
}

/**
 * Account icon with active/inactive state overlay.
 * Matches the behavior of the legacy icon implementation.
 */
@Composable
private fun AccountIcon(
    iconRes: Int,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.size(32.dp)) {
        // Main account icon
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier
                .size(32.dp)
                .alpha(if (isActive) 1f else 0.47f),
            tint = Color.Unspecified
        )
        
        // Inactive overlay icon (lock)
        if (!isActive) {
            Icon(
                painter = painterResource(id = android.R.drawable.ic_lock_lock), // Using system lock icon as placeholder
                contentDescription = "Inactive account",
                modifier = Modifier
                    .size(16.dp)
                    .align(Alignment.Center),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Vertical divider between icon and content.
 * Replicates the visual divider from the legacy layout.
 * Simplified implementation for older Compose version.
 */
@Composable
private fun VerticalDivider(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(1.dp)
            .height(48.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    )
}

/**
 * Amount display section showing balance and credit info.
 * Handles both regular accounts and credit cards with limits.
 */
@Composable
private fun AmountSection(
    account: AccountListItem,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // Main balance (always shown)
        Text(
            text = account.formattedBalance,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = getBalanceColor(account.balanceAmount),
            textAlign = TextAlign.End,
            maxLines = 1
        )
        
        // Credit limit info (only for credit cards)
        if (account.showCreditInfo && account.formattedCreditBalance.isNotEmpty()) {
            Text(
                text = account.formattedCreditBalance,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End,
                maxLines = 1
            )
        }
    }
}

/**
 * Progress bar for credit card utilization.
 * Only shown for credit cards with defined limits.
 */
@Composable
private fun CreditCardProgressBar(
    progress: Float,
    modifier: Modifier = Modifier
) {
    LinearProgressIndicator(
        progress = progress.coerceIn(0f, 1f),
        modifier = modifier.height(12.dp),
        color = getProgressColor(progress),
        trackColor = MaterialTheme.colorScheme.surfaceVariant
    )
}

/**
 * Determines the color for balance text based on amount.
 * Positive amounts are shown in primary color, negative in error color.
 */
@Composable
private fun getBalanceColor(amount: Long): Color {
    return if (amount >= 0) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.error
    }
}

/**
 * Determines the progress bar color based on utilization.
 * Higher utilization (approaching limit) shows in warning/error colors.
 */
@Composable
private fun getProgressColor(progress: Float): Color {
    return when {
        progress < 0.7f -> MaterialTheme.colorScheme.primary
        progress < 0.9f -> Color(0xFFFFA726) // Orange for warning
        else -> MaterialTheme.colorScheme.error
    }
}
