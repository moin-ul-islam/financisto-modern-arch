package ru.orangesoftware.financisto.core.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Gradient utilities for modern card backgrounds and headers
 */
object GradientUtils {
    
    /**
     * Creates a vertical gradient for income-related UI elements
     */
    fun incomeGradient(): Brush {
        return Brush.verticalGradient(
            colors = listOf(
                IncomeGradientStart,
                IncomeGradientEnd
            )
        )
    }
    
    /**
     * Creates a vertical gradient for expense-related UI elements
     */
    fun expenseGradient(): Brush {
        return Brush.verticalGradient(
            colors = listOf(
                ExpenseGradientStart,
                ExpenseGradientEnd
            )
        )
    }
    
    /**
     * Creates a gradient for checking account cards
     */
    fun checkingAccountGradient(): Brush {
        return Brush.horizontalGradient(
            colors = listOf(
                CheckingGradientStart,
                CheckingGradientEnd
            )
        )
    }
    
    /**
     * Creates a gradient for credit card cards
     */
    fun creditCardGradient(): Brush {
        return Brush.horizontalGradient(
            colors = listOf(
                CreditGradientStart,
                CreditGradientEnd
            )
        )
    }
    
    /**
     * Creates a gradient for savings account cards
     */
    fun savingsAccountGradient(): Brush {
        return Brush.horizontalGradient(
            colors = listOf(
                SavingsGradientStart,
                SavingsGradientEnd
            )
        )
    }
    
    /**
     * Creates a gradient for cash account cards
     */
    fun cashAccountGradient(): Brush {
        return Brush.horizontalGradient(
            colors = listOf(
                CashGradientStart,
                CashGradientEnd
            )
        )
    }
    
    /**
     * Creates a gradient for investment account cards
     */
    fun investmentAccountGradient(): Brush {
        return Brush.horizontalGradient(
            colors = listOf(
                InvestmentGradientStart,
                InvestmentGradientEnd
            )
        )
    }
    
    /**
     * Creates a subtle gradient for the total balance card
     */
    fun totalBalanceGradient(): Brush {
        return Brush.linearGradient(
            colors = listOf(
                Color(0xFFE3F2FD),
                Color(0xFFE8F5E9)
            )
        )
    }
    
    /**
     * Creates a radial gradient for icon backgrounds
     */
    fun iconBackgroundGradient(color: Color): Brush {
        return Brush.radialGradient(
            colors = listOf(
                color.copy(alpha = 0.8f),
                color.copy(alpha = 1.0f)
            )
        )
    }
}
