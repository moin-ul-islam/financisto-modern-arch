package ru.orangesoftware.financisto.domain.model

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Value object representing money amounts in the domain.
 * 
 * Uses long for internal storage to avoid floating point precision issues.
 * The amount is stored in the smallest currency unit (e.g., cents for USD).
 * 
 * This is framework-agnostic and contains only business logic for money operations.
 */
@JvmInline
value class Money(val amountInCents: Long) {
    
    /**
     * Constructor from BigDecimal amount and Currency
     */
    constructor(amount: BigDecimal, currency: Currency) : this(
        amount.multiply(BigDecimal.TEN.pow(currency.decimals)).longValueExact()
    )
    
    /**
     * Get the amount as BigDecimal for the given currency
     */
    fun getAmount(currency: Currency): BigDecimal = 
        BigDecimal(amountInCents).divide(BigDecimal.TEN.pow(currency.decimals), currency.decimals, RoundingMode.HALF_UP)
    
    /**
     * Convert to decimal representation (assumes 2 decimal places)
     */
    fun toDecimal(): BigDecimal = BigDecimal(amountInCents).divide(BigDecimal(100), 2, RoundingMode.HALF_UP)
    
    /**
     * Check if amount is positive
     */
    fun isPositive(): Boolean = amountInCents > 0
    
    /**
     * Check if amount is negative
     */
    fun isNegative(): Boolean = amountInCents < 0
    
    /**
     * Check if amount is zero
     */
    fun isZero(): Boolean = amountInCents == 0L
    
    /**
     * Get absolute value
     */
    fun abs(): Money = Money(kotlin.math.abs(amountInCents))
    
    /**
     * Negate the amount
     */
    operator fun unaryMinus(): Money = Money(-amountInCents)
    
    /**
     * Add two money amounts
     */
    operator fun plus(other: Money): Money = Money(amountInCents + other.amountInCents)
    
    /**
     * Subtract two money amounts
     */
    operator fun minus(other: Money): Money = Money(amountInCents - other.amountInCents)
    
    /**
     * Multiply by a factor
     */
    operator fun times(factor: Int): Money = Money(amountInCents * factor)
    
    /**
     * Divide by a factor
     */
    operator fun div(divisor: Int): Money = Money(amountInCents / divisor)
    
    /**
     * Compare money amounts
     */
    operator fun compareTo(other: Money): Int = amountInCents.compareTo(other.amountInCents)
    
    companion object {
        val ZERO = Money(0L)
        
        /**
         * Create Money from decimal amount
         */
        fun fromDecimal(amount: BigDecimal): Money {
            return Money((amount * BigDecimal(100)).toLong())
        }
        
        /**
         * Create Money from double (use with caution due to precision)
         */
        fun fromDouble(amount: Double): Money {
            return Money((amount * 100).toLong())
        }
        
        /**
         * Create Money from long representing cents
         */
        fun fromCents(cents: Long): Money = Money(cents)
    }
}
