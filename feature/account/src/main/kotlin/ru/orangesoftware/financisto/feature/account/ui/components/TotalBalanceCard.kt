package ru.orangesoftware.financisto.feature.account.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.orangesoftware.financisto.core.ui.theme.*

/**
 * Total balance card displayed at the top of the account list
 * Shows combined balance across all accounts with monthly trend
 */
@Composable
fun TotalBalanceCard(
    totalBalance: String,
    monthlyChange: String = "",
    monthlyChangePercent: String = "",
    isPositiveChange: Boolean = true,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.Medium, vertical = Spacing.Small),
        shape = RoundedCornerShape(CardDimensions.RadiusLarge),
        elevation = CardDefaults.cardElevation(
            defaultElevation = CardDimensions.ElevationHigh
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush = GradientUtils.totalBalanceGradient())
                .padding(CardDimensions.PaddingLarge)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // "Total Balance" label
                Text(
                    text = "Total Balance",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(Spacing.Small))
                
                // Large balance display
                Text(
                    text = totalBalance,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                // Monthly change (if available)
                if (monthlyChange.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(Spacing.Small))
                    
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isPositiveChange) "↑ " else "↓ ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isPositiveChange) IncomeGreen else ExpenseRed,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Text(
                            text = "$monthlyChange",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isPositiveChange) IncomeGreen else ExpenseRed,
                            fontWeight = FontWeight.Medium
                        )
                        
                        if (monthlyChangePercent.isNotEmpty()) {
                            Text(
                                text = " ($monthlyChangePercent)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isPositiveChange) IncomeGreen else ExpenseRed
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        Text(
                            text = "this month",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
