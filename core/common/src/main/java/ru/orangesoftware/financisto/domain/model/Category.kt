package ru.orangesoftware.financisto.domain.model

/**
 * Domain model for Category entity.
 * 
 * Represents the hierarchical categorization system for transactions.
 * Categories form a tree structure where each category can have child categories.
 * Categories are typed as either Income or Expense, affecting transaction behavior.
 * 
 * Domain models are:
 * - Framework-agnostic (no Android/Room/UI dependencies)
 * - Focused on business logic and validation
 * - Immutable data classes with business methods
 * - Used by use cases for business operations
 */
data class Category(
    val id: CategoryId = CategoryId.NONE,
    val title: String,
    val type: CategoryType = CategoryType.EXPENSE,
    val parent: Category? = null,
    val children: List<Category> = emptyList(),
    val level: Int = 0,
    val isActive: Boolean = true,
    val lastLocationId: Long? = null,
    val lastProjectId: Long? = null,
    val attributes: List<CategoryAttribute> = emptyList(),
    val leftBound: Int = 0,  // For hierarchical tree management
    val rightBound: Int = 0  // For hierarchical tree management
) {
    
    /**
     * Business logic: Check if this is a root category (no parent)
     */
    fun isRoot(): Boolean = parent == null
    
    /**
     * Business logic: Check if this category has children
     */
    fun hasChildren(): Boolean = children.isNotEmpty()
    
    /**
     * Business logic: Check if this is an income category
     */
    fun isIncome(): Boolean = type == CategoryType.INCOME
    
    /**
     * Business logic: Check if this is an expense category
     */
    fun isExpense(): Boolean = type == CategoryType.EXPENSE
    
    /**
     * Business logic: Get the full hierarchical path of category titles
     */
    fun getFullPath(separator: String = " > "): String {
        val path = mutableListOf<String>()
        var current: Category? = this
        while (current != null) {
            path.add(0, current.title)
            current = current.parent
        }
        return path.joinToString(separator)
    }
    
    /**
     * Business logic: Get all descendant categories (recursive)
     */
    fun getAllDescendants(): List<Category> {
        val descendants = mutableListOf<Category>()
        children.forEach { child ->
            descendants.add(child)
            descendants.addAll(child.getAllDescendants())
        }
        return descendants
    }
    
    /**
     * Business logic: Check if this category contains a specific descendant
     */
    fun containsDescendant(categoryId: CategoryId): Boolean {
        return getAllDescendants().any { it.id == categoryId }
    }
    
    /**
     * Business logic: Add a child category
     */
    fun addChild(child: Category): Category {
        val updatedChild = child.copy(parent = this, level = this.level + 1)
        return copy(children = children + updatedChild)
    }
    
    /**
     * Business logic: Remove a child category
     */
    fun removeChild(childId: CategoryId): Category {
        val updatedChildren = children.filter { it.id != childId }
        return copy(children = updatedChildren)
    }
    
    /**
     * Business logic: Validate category data
     */
    fun validate(): CategoryValidationResult {
        val errors = mutableListOf<String>()
        
        if (title.isBlank()) {
            errors.add("Category title cannot be empty")
        }
        
        if (title.length > 255) {
            errors.add("Category title cannot exceed 255 characters")
        }
        
        if (level < 0) {
            errors.add("Category level cannot be negative")
        }
        
        if (level > 10) {
            errors.add("Category hierarchy cannot exceed 10 levels")
        }
        
        // Check for circular references in parent-child relationships
        var current = parent
        var depth = 0
        while (current != null && depth < 50) {
            if (current.id == this.id) {
                errors.add("Circular reference detected in category hierarchy")
                break
            }
            current = current.parent
            depth++
        }
        
        return if (errors.isEmpty()) {
            CategoryValidationResult.Valid
        } else {
            CategoryValidationResult.Invalid(errors)
        }
    }
    
    /**
     * Business logic: Get the display title with indentation for hierarchy
     */
    fun getDisplayTitle(): String {
        val indent = "  ".repeat(level)
        return "$indent$title"
    }
    
    companion object {
        val NO_CATEGORY = Category(
            id = CategoryId.NO_CATEGORY,
            title = "<NO_CATEGORY>",
            type = CategoryType.EXPENSE
        )
        
        val SPLIT_CATEGORY = Category(
            id = CategoryId.SPLIT_CATEGORY,
            title = "<SPLIT_CATEGORY>",
            type = CategoryType.EXPENSE
        )
        
        /**
         * Factory method for creating new root categories
         */
        fun createRoot(
            title: String,
            type: CategoryType = CategoryType.EXPENSE
        ): Category {
            return Category(
                title = title,
                type = type,
                level = 0
            )
        }
        
        /**
         * Factory method for creating child categories
         */
        fun createChild(
            title: String,
            parent: Category,
            type: CategoryType = parent.type
        ): Category {
            return Category(
                title = title,
                type = type,
                parent = parent,
                level = parent.level + 1
            )
        }
    }
}

/**
 * Value object for Category ID
 */
@JvmInline
value class CategoryId(val value: Long) {
    companion object {
        val NONE = CategoryId(0L)
        val NO_CATEGORY = CategoryId(0L)
        val SPLIT_CATEGORY = CategoryId(-1L)
    }
}

/**
 * Business enumeration for category types
 */
enum class CategoryType(val displayName: String) {
    INCOME("Income"),
    EXPENSE("Expense");
    
    /**
     * Business logic: Check if this type affects account balance positively
     */
    fun isPositiveImpact(): Boolean = this == INCOME
    
    /**
     * Business logic: Get the opposite type
     */
    fun opposite(): CategoryType = when (this) {
        INCOME -> EXPENSE
        EXPENSE -> INCOME
    }
}

/**
 * Domain model for category attributes (custom fields)
 */
data class CategoryAttribute(
    val id: Long = 0L,
    val name: String,
    val type: AttributeType,
    val value: String? = null,
    val isRequired: Boolean = false
) {
    /**
     * Business logic: Validate attribute based on type
     */
    fun validate(): Boolean {
        if (isRequired && value.isNullOrBlank()) {
            return false
        }
        
        return when (type) {
            AttributeType.TEXT -> true
            AttributeType.NUMBER -> {
                value?.toDoubleOrNull() != null
            }
            AttributeType.DATE -> {
                // Simplified date validation - in real implementation would use proper date parsing
                !value.isNullOrBlank()
            }
            AttributeType.LIST -> {
                !value.isNullOrBlank()
            }
        }
    }
}

/**
 * Business enumeration for attribute types
 */
enum class AttributeType(val displayName: String) {
    TEXT("Text"),
    NUMBER("Number"),
    DATE("Date"),
    LIST("List");
}

/**
 * Validation result for category
 */
sealed class CategoryValidationResult {
    object Valid : CategoryValidationResult()
    data class Invalid(val errors: List<String>) : CategoryValidationResult()
}
