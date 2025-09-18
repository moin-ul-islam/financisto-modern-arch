package ru.orangesoftware.financisto.feature.transaction.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Split transaction section for handling multiple splits
 */
@Composable
fun SplitTransactionSection(
    splitTransactions: List<ru.orangesoftware.financisto.feature.transaction.SplitTransactionItem>,
    remainingAmount: String,
    currencySymbol: String,
    onAddSplit: () -> Unit,
    onEditSplit: (ru.orangesoftware.financisto.feature.transaction.SplitTransactionItem) -> Unit,
    onDeleteSplit: (ru.orangesoftware.financisto.feature.transaction.SplitTransactionItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Section header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Split Transaction",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                IconButton(onClick = onAddSplit) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Split",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Split items
            if (splitTransactions.isNotEmpty()) {
                for (split in splitTransactions) {
                    SplitTransactionItem(
                        split = split,
                        currencySymbol = currencySymbol,
                        onEdit = { onEditSplit(split) },
                        onDelete = { onDeleteSplit(split) }
                    )
                }
            }
            
            // Remaining amount indicator
            RemainingAmountIndicator(
                remainingAmount = remainingAmount,
                currencySymbol = currencySymbol
            )
            
            // Add split button
            OutlinedButton(
                onClick = onAddSplit,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Split")
            }
        }
    }
}

@Composable
private fun SplitTransactionItem(
    split: ru.orangesoftware.financisto.feature.transaction.SplitTransactionItem,
    currencySymbol: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = split.categoryName ?: split.accountName ?: "Unknown",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                if (split.note?.isNotEmpty() == true) {
                    Text(
                        text = split.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Text(
                text = "$currencySymbol ${split.formattedAmount}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (split.amount >= 0) Color(0xFF4CAF50) else Color(0xFFE53E3E)
            )
            
            Row {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Split",
                        modifier = Modifier.size(16.dp)
                    )
                }
                
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Split",
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFFE53E3E)
                    )
                }
            }
        }
    }
}

@Composable
private fun RemainingAmountIndicator(
    remainingAmount: String,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    val isPositive = remainingAmount.toDoubleOrNull()?.let { it >= 0 } ?: true
    val backgroundColor = if (isPositive) Color(0xFF4CAF50).copy(alpha = 0.1f) else Color(0xFFE53E3E).copy(alpha = 0.1f)
    val textColor = if (isPositive) Color(0xFF4CAF50) else Color(0xFFE53E3E)
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Remaining Amount:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        Text(
            text = "$currencySymbol $remainingAmount",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

/**
 * Validation error card
 */
@Composable
fun ValidationErrorCard(
    errors: List<ru.orangesoftware.financisto.feature.transaction.ValidationError>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE53E3E).copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFE53E3E),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Please fix the following errors:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFE53E3E)
                )
            }
            
            for (error in errors) {
                Text(
                    text = "• ${error.message}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFE53E3E),
                    modifier = Modifier.padding(start = 28.dp)
                )
            }
        }
    }
}