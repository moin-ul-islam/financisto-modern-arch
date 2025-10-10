package ru.orangesoftware.financisto.core.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.orangesoftware.financisto.core.ui.theme.ExpenseRed
import ru.orangesoftware.financisto.core.ui.theme.IncomeGreen

/**
 * Large amount display with color coding
 */
@Composable
fun AmountDisplay(
    amount: String,
    currencySymbol: String = "$",
    isPositive: Boolean = true,
    fontSize: TextUnit = 36.sp,
    modifier: Modifier = Modifier
) {
    val color = if (isPositive) IncomeGreen else ExpenseRed
    
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = currencySymbol,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = fontSize * 0.7f,
                fontWeight = FontWeight.Bold
            ),
            color = color
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = amount,
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            ),
            color = color,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Compact amount display for list items
 */
@Composable
fun CompactAmountDisplay(
    amount: String,
    currencySymbol: String = "$",
    isPositive: Boolean = true,
    fontSize: TextUnit = 24.sp,
    modifier: Modifier = Modifier
) {
    val color = if (isPositive) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.error
    }
    
    Text(
        text = "$currencySymbol$amount",
        style = MaterialTheme.typography.titleLarge.copy(
            fontSize = fontSize,
            fontWeight = FontWeight.Bold
        ),
        color = color,
        modifier = modifier,
        textAlign = TextAlign.End
    )
}

/**
 * Amount display with custom color
 */
@Composable
fun ColoredAmountDisplay(
    amount: String,
    currencySymbol: String = "$",
    color: Color,
    fontSize: TextUnit = 24.sp,
    fontWeight: FontWeight = FontWeight.Bold,
    modifier: Modifier = Modifier
) {
    Text(
        text = "$currencySymbol$amount",
        style = MaterialTheme.typography.titleLarge.copy(
            fontSize = fontSize,
            fontWeight = fontWeight
        ),
        color = color,
        modifier = modifier
    )
}
