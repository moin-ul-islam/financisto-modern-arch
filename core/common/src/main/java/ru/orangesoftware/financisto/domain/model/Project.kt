package ru.orangesoftware.financisto.domain.model

/**
 * Domain model for Project entity.
 * 
 * Represents a project or initiative that transactions can be associated with.
 * Projects help in tracking expenses and income related to specific goals,
 * work projects, or personal initiatives.
 * 
 * Domain models are:
 * - Framework-agnostic (no Android/Room/UI dependencies)
 * - Focused on business logic and validation
 * - Immutable data classes with business methods
 * - Used by use cases for business operations
 */
data class Project(
    val id: ProjectId = ProjectId.NONE,
    val name: String,
    val description: String? = null,
    val isActive: Boolean = true,
    val sortOrder: Int = 0,
    val startDate: Long? = null, // milliseconds since epoch
    val endDate: Long? = null, // milliseconds since epoch
    val budget: Money? = null,
    val notes: String? = null
) {
    
    /**
     * Business logic: Check if project can be used in transactions
     */
    fun canBeUsedInTransactions(): Boolean = isActive && name.isNotBlank()
    
    /**
     * Business logic: Get display name with formatting
     */
    fun getDisplayName(): String {
        return if (name.isBlank()) "<No Project>" else name
    }
    
    /**
     * Business logic: Check if project is currently active (within date range)
     */
    fun isCurrentlyActive(): Boolean {
        if (!isActive) return false
        
        val now = System.currentTimeMillis()
        val todayStart = now - (now % (24 * 60 * 60 * 1000)) // Start of today
        
        val afterStart = startDate?.let { todayStart >= it } ?: true
        val beforeEnd = endDate?.let { todayStart <= it } ?: true
        
        return afterStart && beforeEnd
    }
    
    /**
     * Business logic: Check if project has a defined budget
     */
    fun hasBudget(): Boolean = budget != null && !budget.isZero()
    
    /**
     * Business logic: Check if project has defined date range
     */
    fun hasDateRange(): Boolean = startDate != null || endDate != null
    
    /**
     * Business logic: Get project duration in days
     */
    fun getDurationInDays(): Long? {
        return if (startDate != null && endDate != null) {
            val durationMillis = endDate - startDate
            durationMillis / (24 * 60 * 60 * 1000) // Convert to days
        } else null
    }
    
    /**
     * Business logic: Check if project is overdue (past end date)
     */
    fun isOverdue(): Boolean {
        val now = System.currentTimeMillis()
        val todayStart = now - (now % (24 * 60 * 60 * 1000)) // Start of today
        return endDate?.let { todayStart > it } ?: false
    }
    
    /**
     * Business logic: Get project status based on dates and active flag
     */
    fun getStatus(): ProjectStatus {
        return when {
            !isActive -> ProjectStatus.INACTIVE
            isOverdue() -> ProjectStatus.COMPLETED
            isCurrentlyActive() -> ProjectStatus.ACTIVE
            isPlanned() -> ProjectStatus.PLANNED
            else -> ProjectStatus.ACTIVE
        }
    }
    
    /**
     * Helper method: Check if project is planned (start date in future)
     */
    private fun isPlanned(): Boolean {
        val now = System.currentTimeMillis()
        val todayStart = now - (now % (24 * 60 * 60 * 1000)) // Start of today
        return startDate?.let { todayStart < it } ?: false
    }
    
    /**
     * Business logic: Validate project data
     */
    fun validate(): ProjectValidationResult {
        val errors = mutableListOf<String>()
        
        if (name.isBlank()) {
            errors.add("Project name cannot be empty")
        }
        
        if (name.length > 255) {
            errors.add("Project name cannot exceed 255 characters")
        }
        
        if (description != null && description.length > 1000) {
            errors.add("Project description cannot exceed 1000 characters")
        }
        
        if (sortOrder < 0) {
            errors.add("Sort order cannot be negative")
        }
        
        if (startDate != null && endDate != null && startDate > endDate) {
            errors.add("Start date cannot be after end date")
        }
        
        if (budget != null && budget.isNegative()) {
            errors.add("Budget cannot be negative")
        }
        
        return if (errors.isEmpty()) {
            ProjectValidationResult.Valid
        } else {
            ProjectValidationResult.Invalid(errors)
        }
    }
    
    companion object {
        val NO_PROJECT = Project(
            id = ProjectId.NO_PROJECT,
            name = "<NO_PROJECT>",
            isActive = true
        )
        
        /**
         * Factory method for creating new projects
         */
        fun create(
            name: String,
            description: String? = null,
            budget: Money? = null,
            startDate: Long? = null, // milliseconds since epoch
            endDate: Long? = null // milliseconds since epoch
        ): Project {
            return Project(
                name = name.trim(),
                description = description?.trim(),
                budget = budget,
                startDate = startDate,
                endDate = endDate
            )
        }
    }
}

/**
 * Value object for Project ID
 */
@JvmInline
value class ProjectId(val value: Long) {
    companion object {
        val NONE = ProjectId(0L)
        val NO_PROJECT = ProjectId(0L)
    }
}

/**
 * Business enumeration for project status
 */
enum class ProjectStatus(val displayName: String) {
    PLANNED("Planned"),
    ACTIVE("Active"),
    COMPLETED("Completed"),
    INACTIVE("Inactive");
    
    /**
     * Business logic: Check if project can accept new transactions
     */
    fun canAcceptTransactions(): Boolean = this == ACTIVE
    
    /**
     * Business logic: Check if project is in a final state
     */
    fun isFinal(): Boolean = this == COMPLETED || this == INACTIVE
}

/**
 * Validation result for project
 */
sealed class ProjectValidationResult {
    object Valid : ProjectValidationResult()
    data class Invalid(val errors: List<String>) : ProjectValidationResult()
}
