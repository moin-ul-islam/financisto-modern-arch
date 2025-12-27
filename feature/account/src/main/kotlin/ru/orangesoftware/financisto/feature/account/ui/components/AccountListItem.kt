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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.orangesoftware.financisto.core.ui.components.IconWithBackground
import ru.orangesoftware.financisto.core.ui.theme.*
import ru.orangesoftware.financisto.feature.account.AccountListItem

/**
 * Modern account list item with beautiful card design
 * 
 * Layout structure:
 * - Card with elevation and rounded corners
 * - Large colorful icon with circular background
 * - Account info (type, title, number, last transaction)
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
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.Medium, vertical = Spacing.Small)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(CardDimensions.RadiusLarge),
        elevation = CardDefaults.cardElevation(
            defaultElevation = CardDimensions.ElevationMedium
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (account.isActive) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(CardDimensions.PaddingMedium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Account icon with colored circular background
                AccountIconModern(
                    iconRes = account.iconResId,
                    accountType = account.accountType,
                    isActive = account.isActive,
                    modifier = Modifier.padding(end = Spacing.Medium)
                )
                
                // Account info section
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // Account type/issuer
                    Text(
                        text = account.topText.ifEmpty { account.accountType },
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(2.dp))
                    
                    // Account title (main text)
                    Text(
                        text = account.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(modifier = Modifier.height(2.dp))
                    
                    // Account number (masked) and date
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.Small),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (account.formattedDate.isNotEmpty()) {
                            Text(
                                text = "Last: ${account.formattedDate}",
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                // Amount section
                AmountSectionModern(
                    account = account,
                    modifier = Modifier.padding(start = Spacing.Small)
                )
            }
            
            // Credit card progress bar (only shown for credit cards)
            if (account.showProgressBar) {
                Spacer(modifier = Modifier.height(Spacing.Small))
                CreditCardProgressBarModern(
                    progress = account.creditUtilization,
                    used = 0f, // Not needed as we only use progress
                    limit = 1f, // Not needed as we only use progress
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Modern account icon with colored circular background based on account type
 */
@Composable
private fun AccountIconModern(
    iconRes: Int,
    accountType: String,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val backgroundColor = getAccountTypeColor(accountType)
    val iconTint = if (isActive) Color.White else Color.White.copy(alpha = 0.5f)
    
    Box(modifier = modifier) {
        IconWithBackground(
            iconRes = iconRes,
            backgroundColor = if (isActive) backgroundColor else InactiveGray,
            iconTint = iconTint,
            size = IconSize.Large
        )
        
        // Inactive overlay icon (lock)
        if (!isActive) {
            Box(
                modifier = Modifier
                    .size(IconSize.Small)
                    .align(Alignment.BottomEnd)
                    .background(MaterialTheme.colorScheme.surface, shape = androidx.compose.foundation.shape.CircleShape)
            ) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_lock_lock),
                    contentDescription = "Inactive account",
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.Center),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/**
 * Get color for account type
 */
@Composable
private fun getAccountTypeColor(accountType: String): Color {
    return when {
        accountType.contains("checking", ignoreCase = true) || 
        accountType.contains("debit", ignoreCase = true) -> CheckingBlue
        accountType.contains("credit", ignoreCase = true) -> CreditPurple
        accountType.contains("savings", ignoreCase = true) -> SavingsOrange
        accountType.contains("cash", ignoreCase = true) -> CashGold
        accountType.contains("investment", ignoreCase = true) -> InvestmentTeal
        else -> PrimaryBlue
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
 * Modern amount display section with larger fonts and better colors
 */
@Composable
private fun AmountSectionModern(
    account: AccountListItem,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Main balance (larger and bolder)
        Text(
            text = account.formattedBalance,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = getBalanceColor(account.balanceAmount),
            textAlign = TextAlign.End,
            maxLines = 1
        )
        
        // Credit limit info (only for credit cards)
        if (account.showCreditInfo && account.formattedCreditBalance.isNotEmpty()) {
            Text(
                text = "Available: ${account.formattedCreditBalance}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End,
                maxLines = 1
            )
        }
    }
}

/**
 * Modern progress bar with rounded corners and detailed info
 */
@Composable
private fun CreditCardProgressBarModern(
    progress: Float,
    used: Float,
    limit: Float,
    modifier: Modifier = Modifier
) {
    val progressClamped = progress.coerceIn(0f, 1f)
    val progressColor = getProgressColorModern(progressClamped)
    
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = String.format("%.0f%% utilized", progressClamped * 100),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = progressClamped,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = progressColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round
        )
    }
}

/**
 * Determines the color for balance text based on amount
 */
@Composable
private fun getBalanceColor(amount: Long): Color {
    return if (amount >= 0) {
        IncomeGreen
    } else {
        ExpenseRed
    }
}

/**
 * Determines the progress bar color based on utilization
 */
@Composable
private fun getProgressColorModern(progress: Float): Color {
    return when {
        progress < 0.7f -> ProgressLow
        progress < 0.9f -> ProgressMedium
        else -> ProgressHigh
    }
}
