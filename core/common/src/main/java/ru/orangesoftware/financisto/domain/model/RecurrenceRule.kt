package ru.orangesoftware.financisto.domain.model

import java.time.LocalDate
import java.time.LocalTime

/**
 * Domain model for Recurrence Rule.
 * 
 * Represents rules for recurring transactions (templates that repeat automatically).
 * Supports various recurrence patterns like daily, weekly, monthly, yearly with
 * customizable intervals and end conditions.
 * 
 * Domain models are:
 * - Framework-agnostic (no Android/Room/UI dependencies)
 * - Focused on business logic and validation
 * - Immutable data classes with business methods
 * - Used by use cases for business operations
 */
data class RecurrenceRule(
    val frequency: RecurrenceFrequency,
    val interval: Int = 1,
    val startDate: LocalDate,
    val endDate: LocalDate? = null,
    val maxOccurrences: Int? = null,
    val dayOfWeek: Int? = null, // 1=Monday, 7=Sunday (for weekly)
    val dayOfMonth: Int? = null, // 1-31 (for monthly)
    val monthOfYear: Int? = null, // 1-12 (for yearly)
    val notificationTime: LocalTime? = null,
    val isEnabled: Boolean = true
) {
    
    /**
     * Business logic: Check if recurrence rule is currently active
     */
    fun isActive(): Boolean {
        if (!isEnabled) return false
        
        // Use java.util.Calendar for API level 19+ compatibility
        val today = java.util.Calendar.getInstance()
        val todayYear = today.get(java.util.Calendar.YEAR)
        val todayMonth = today.get(java.util.Calendar.MONTH) + 1 // Calendar.MONTH is 0-based
        val todayDay = today.get(java.util.Calendar.DAY_OF_MONTH)
        
        // Check if today is after or equal to start date
        val afterStart = if (todayYear > startDate.year) {
            true
        } else if (todayYear == startDate.year) {
            if (todayMonth > startDate.monthValue) {
                true
            } else if (todayMonth == startDate.monthValue) {
                todayDay >= startDate.dayOfMonth
            } else {
                false
            }
        } else {
            false
        }
        
        // Check if today is before or equal to end date (if set)
        val beforeEnd = endDate?.let { end ->
            if (todayYear < end.year) {
                true
            } else if (todayYear == end.year) {
                if (todayMonth < end.monthValue) {
                    true
                } else if (todayMonth == end.monthValue) {
                    todayDay <= end.dayOfMonth
                } else {
                    false
                }
            } else {
                false
            }
        } ?: true
        
        return afterStart && beforeEnd
    }
    
    /**
     * Business logic: Calculate the next occurrence date after a given date
     */
    fun getNextOccurrence(afterDate: LocalDate? = null): LocalDate? {
        if (!isActive()) return null
        
        val targetDate = afterDate ?: run {
            val today = java.util.Calendar.getInstance()
            LocalDate.of(
                today.get(java.util.Calendar.YEAR),
                today.get(java.util.Calendar.MONTH) + 1,
                today.get(java.util.Calendar.DAY_OF_MONTH)
            )
        }
        
        var candidate = if (targetDate.isBefore(startDate)) startDate else targetDate.plusDays(1)
        
        // Find the next valid occurrence based on frequency
        while (endDate?.let { candidate.isAfter(it) } != true) {
            if (isValidOccurrenceDate(candidate)) {
                return candidate
            }
            candidate = candidate.plusDays(1)
            
            // Safety check to prevent infinite loops
            if (candidate.isAfter(LocalDate.now().plusYears(2))) {
                break
            }
        }
        
        return null
    }
    
    /**
     * Business logic: Check if a specific date is a valid occurrence
     */
    private fun isValidOccurrenceDate(date: LocalDate): Boolean {
        if (date.isBefore(startDate)) return false
        if (endDate?.let { date.isAfter(it) } == true) return false
        
        return when (frequency) {
            RecurrenceFrequency.DAILY -> {
                val daysBetween = java.time.temporal.ChronoUnit.DAYS.between(startDate, date)
                daysBetween % interval == 0L
            }
            RecurrenceFrequency.WEEKLY -> {
                val weeksBetween = java.time.temporal.ChronoUnit.WEEKS.between(startDate, date)
                val validWeek = weeksBetween % interval == 0L
                val validDay = dayOfWeek?.let { date.dayOfWeek.value == it } ?: true
                validWeek && validDay
            }
            RecurrenceFrequency.MONTHLY -> {
                val monthsBetween = java.time.temporal.ChronoUnit.MONTHS.between(startDate, date)
                val validMonth = monthsBetween % interval == 0L
                val validDay = dayOfMonth?.let { 
                    date.dayOfMonth == it || (it > date.lengthOfMonth() && date.dayOfMonth == date.lengthOfMonth())
                } ?: (date.dayOfMonth == startDate.dayOfMonth)
                validMonth && validDay
            }
            RecurrenceFrequency.YEARLY -> {
                val yearsBetween = java.time.temporal.ChronoUnit.YEARS.between(startDate, date)
                val validYear = yearsBetween % interval == 0L
                val validMonth = monthOfYear?.let { date.monthValue == it } ?: (date.monthValue == startDate.monthValue)
                val validDay = dayOfMonth?.let { date.dayOfMonth == it } ?: (date.dayOfMonth == startDate.dayOfMonth)
                validYear && validMonth && validDay
            }
        }
    }
    
    /**
     * Business logic: Get all occurrences within a date range
     */
    fun getOccurrencesInRange(from: LocalDate, to: LocalDate): List<LocalDate> {
        val occurrences = mutableListOf<LocalDate>()
        var current = getNextOccurrence(from.minusDays(1))
        
        while (current != null && !current.isAfter(to)) {
            occurrences.add(current)
            
            // Check max occurrences limit
            if (maxOccurrences != null && occurrences.size >= maxOccurrences) {
                break
            }
            
            current = getNextOccurrence(current)
        }
        
        return occurrences
    }
    
    /**
     * Business logic: Check if recurrence has reached its end
     */
    fun hasEnded(): Boolean {
        return endDate?.isBefore(LocalDate.now()) == true
    }
    
    /**
     * Business logic: Get human-readable description of recurrence
     */
    fun getDescription(): String {
        val freq = when (frequency) {
            RecurrenceFrequency.DAILY -> if (interval == 1) "daily" else "every $interval days"
            RecurrenceFrequency.WEEKLY -> if (interval == 1) "weekly" else "every $interval weeks"
            RecurrenceFrequency.MONTHLY -> if (interval == 1) "monthly" else "every $interval months"
            RecurrenceFrequency.YEARLY -> if (interval == 1) "yearly" else "every $interval years"
        }
        
        val end = when {
            endDate != null -> " until $endDate"
            maxOccurrences != null -> " for $maxOccurrences times"
            else -> ""
        }
        
        return freq.capitalize() + end
    }
    
    /**
     * Business logic: Validate recurrence rule
     */
    fun validate(): RecurrenceValidationResult {
        val errors = mutableListOf<String>()
        
        if (interval <= 0) {
            errors.add("Interval must be positive")
        }
        
        if (interval > 1000) {
            errors.add("Interval is too large")
        }
        
        if (endDate != null && endDate.isBefore(startDate)) {
            errors.add("End date cannot be before start date")
        }
        
        if (maxOccurrences != null && maxOccurrences <= 0) {
            errors.add("Max occurrences must be positive")
        }
        
        if (dayOfWeek != null && (dayOfWeek < 1 || dayOfWeek > 7)) {
            errors.add("Day of week must be between 1 and 7")
        }
        
        if (dayOfMonth != null && (dayOfMonth < 1 || dayOfMonth > 31)) {
            errors.add("Day of month must be between 1 and 31")
        }
        
        if (monthOfYear != null && (monthOfYear < 1 || monthOfYear > 12)) {
            errors.add("Month of year must be between 1 and 12")
        }
        
        return if (errors.isEmpty()) {
            RecurrenceValidationResult.Valid
        } else {
            RecurrenceValidationResult.Invalid(errors)
        }
    }
    
    companion object {
        /**
         * Factory method for daily recurrence
         */
        fun daily(
            startDate: LocalDate,
            interval: Int = 1,
            endDate: LocalDate? = null,
            maxOccurrences: Int? = null
        ): RecurrenceRule {
            return RecurrenceRule(
                frequency = RecurrenceFrequency.DAILY,
                interval = interval,
                startDate = startDate,
                endDate = endDate,
                maxOccurrences = maxOccurrences
            )
        }
        
        /**
         * Factory method for weekly recurrence
         */
        fun weekly(
            startDate: LocalDate,
            interval: Int = 1,
            dayOfWeek: Int? = null,
            endDate: LocalDate? = null,
            maxOccurrences: Int? = null
        ): RecurrenceRule {
            return RecurrenceRule(
                frequency = RecurrenceFrequency.WEEKLY,
                interval = interval,
                startDate = startDate,
                dayOfWeek = dayOfWeek ?: startDate.dayOfWeek.value,
                endDate = endDate,
                maxOccurrences = maxOccurrences
            )
        }
        
        /**
         * Factory method for monthly recurrence
         */
        fun monthly(
            startDate: LocalDate,
            interval: Int = 1,
            dayOfMonth: Int? = null,
            endDate: LocalDate? = null,
            maxOccurrences: Int? = null
        ): RecurrenceRule {
            return RecurrenceRule(
                frequency = RecurrenceFrequency.MONTHLY,
                interval = interval,
                startDate = startDate,
                dayOfMonth = dayOfMonth ?: startDate.dayOfMonth,
                endDate = endDate,
                maxOccurrences = maxOccurrences
            )
        }
    }
}

/**
 * Business enumeration for recurrence frequencies
 */
enum class RecurrenceFrequency(val displayName: String) {
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    YEARLY("Yearly");
    
    /**
     * Business logic: Get typical intervals for this frequency
     */
    fun getTypicalIntervals(): List<Int> = when (this) {
        DAILY -> listOf(1, 2, 3, 7, 14)
        WEEKLY -> listOf(1, 2, 3, 4)
        MONTHLY -> listOf(1, 2, 3, 6)
        YEARLY -> listOf(1, 2)
    }
}

/**
 * Validation result for recurrence rule
 */
sealed class RecurrenceValidationResult {
    object Valid : RecurrenceValidationResult()
    data class Invalid(val errors: List<String>) : RecurrenceValidationResult()
}
