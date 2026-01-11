package ru.orangesoftware.financisto.feature.transaction.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.orangesoftware.financisto.core.ui.theme.*

/**
 * Beautiful gradient header card for transaction amount input
 * Changes color dynamically based on income/expense selection
 */
@Composable
fun AmountHeaderCard(
    amount: String,
    currencySymbol: String,
    isIncome: Boolean,
    onAmountChanged: (String) -> Unit,
    onToggleIncomeExpense: () -> Unit,
    modifier: Modifier = Modifier,
    isTransfer: Boolean = false
) {
    // Animate background gradient based on income/expense
    val gradientColors = if (isIncome) {
        listOf(IncomeGradientStart, IncomeGradientEnd)
    } else {
        listOf(ExpenseGradientStart, ExpenseGradientEnd)
    }
    
    val animatedGradient by animateColorAsState(
        targetValue = if (isIncome) IncomeGradientEnd else ExpenseGradientEnd,
        animationSpec = tween(durationMillis = 300),
        label = "gradient_animation"
    )
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp), // No rounded corners for header
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = gradientColors
                    )
                )
                .padding(CardDimensions.PaddingLarge)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Amount input display
                AmountDisplayInput(
                    amount = amount,
                    currencySymbol = currencySymbol,
                    isIncome = isIncome,
                    onAmountChanged = onAmountChanged
                )
                
                Spacer(modifier = Modifier.height(Spacing.Large))
                
                // Income/Expense Toggle Buttons (only for non-transfer transactions)
                if (!isTransfer) {
                    IncomeExpenseToggleButtons(
                        isIncome = isIncome,
                        onToggle = onToggleIncomeExpense
                    )
                }
            }
        }
    }
}

/**
 * Large amount display with input field styled like a calculator
 */
@Composable
private fun AmountDisplayInput(
    amount: String,
    currencySymbol: String,
    isIncome: Boolean,
    onAmountChanged: (String) -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    
    // Animate scale when typing
    val scale by animateFloatAsState(
        targetValue = if (isFocused && amount.isNotEmpty()) 1.05f else 1.0f,
        animationSpec = tween(durationMillis = 100),
        label = "scale_animation"
    )
    
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.scale(scale)
    ) {
        Text(
            text = currencySymbol,
            style = MaterialTheme.typography.displayMedium.copy(
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            ),
            color = Color.White,
            modifier = Modifier.padding(end = 8.dp)
        )
        
        // Amount input that looks like a display
        Box(
            modifier = Modifier.defaultMinSize(minWidth = 200.dp),
            contentAlignment = Alignment.Center
        ) {
            // Placeholder text - only show when empty AND not focused
            if (amount.isEmpty() && !isFocused) {
                Text(
                    text = "0.00",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        letterSpacing = (-1).sp
                    ),
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
            
            // Actual input field
            BasicTextField(
                value = amount,
                onValueChange = { newValue ->
                    // Only allow numbers and decimal point
                    if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                        onAmountChanged(newValue)
                    }
                },
                textStyle = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    letterSpacing = (-1).sp
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                cursorBrush = SolidColor(Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        isFocused = focusState.isFocused
                    },
                decorationBox = { innerTextField ->
                    // Track focus state
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        innerTextField()
                    }
                }
            )
        }
    }
}

/**
 * Modern segmented control for Income/Expense toggle
 */
@Composable
private fun IncomeExpenseToggleButtons(
    isIncome: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.Large),
        horizontalArrangement = Arrangement.spacedBy(Spacing.Medium)
    ) {
        // Income Button
        ToggleButton(
            text = "Income",
            isSelected = isIncome,
            onClick = { if (!isIncome) onToggle() },
            selectedColor = Color(0xFF4CAF50),
            modifier = Modifier.weight(1f)
        )
        
        // Expense Button
        ToggleButton(
            text = "Expense",
            isSelected = !isIncome,
            onClick = { if (isIncome) onToggle() },
            selectedColor = Color(0xFFEF5350),
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Individual toggle button with selection state
 */
@Composable
private fun ToggleButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    selectedColor: Color,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) {
                selectedColor
            } else {
                Color.White.copy(alpha = 0.2f)
            },
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (isSelected) 4.dp else 0.dp
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
