package ru.orangesoftware.financisto.feature.transaction.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Amount input field with income/expense toggle
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmountInputField(
    amount: String,
    formattedAmount: String,
    currencySymbol: String,
    isIncome: Boolean,
    onAmountChanged: (String) -> Unit,
    onIncomeExpenseToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Amount",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Income/Expense Toggle
            IncomeExpenseToggle(
                isIncome = isIncome,
                onToggle = onIncomeExpenseToggle
            )
            
            // Amount Input
            OutlinedTextField(
                value = amount,
                onValueChange = onAmountChanged,
                label = { Text("Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                leadingIcon = {
                    Text(
                        text = currencySymbol,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }
        
        // Formatted amount display
        if (formattedAmount.isNotEmpty() && formattedAmount != amount) {
            Text(
                text = "= $currencySymbol $formattedAmount",
                style = MaterialTheme.typography.bodySmall,
                color = if (isIncome) Color(0xFF4CAF50) else Color(0xFFE53E3E),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
fun IncomeExpenseToggle(
    isIncome: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isIncome) Color(0xFF4CAF50) else Color(0xFFE53E3E)
    val icon = if (isIncome) Icons.Default.Add else Icons.Default.Delete
    val text = if (isIncome) "Income" else "Expense"
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor.copy(alpha = 0.1f))
            .border(
                width = 1.dp,
                color = backgroundColor.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onToggle() }
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = backgroundColor,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = backgroundColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Transfer toggle switch
 */
@Composable
fun TransferToggle(
    isTransfer: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Transfer between accounts",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
        
        Switch(
            checked = isTransfer,
            onCheckedChange = onToggle
        )
    }
}

/**
 * Exchange rate field for multi-currency transfers
 */
@Composable
fun ExchangeRateField(
    exchangeRate: String,
    fromCurrency: String,
    toCurrency: String,
    toAmount: String,
    onExchangeRateChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Exchange Rate",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // From currency indicator
            Text(
                text = "1 $fromCurrency =",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Exchange rate input
            OutlinedTextField(
                value = exchangeRate,
                onValueChange = onExchangeRateChanged,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            
            // To currency indicator
            Text(
                text = toCurrency,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // Calculated amount display
        if (toAmount.isNotEmpty()) {
            Text(
                text = "Amount in $toCurrency: $toAmount",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
        }
    }
}