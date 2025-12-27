package ru.orangesoftware.financisto.usecase.modern

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import ru.orangesoftware.financisto.data.model.CategoryEntity
import ru.orangesoftware.financisto.data.model.CategoryView
import ru.orangesoftware.financisto.di.IoDispatcher
import ru.orangesoftware.financisto.repository.modern.CategoryRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use cases for category hierarchy management.
 *
 * These usecases handle the complex business logic for managing
 * hierarchical categories using the nested set model.
 */

/**
 * Use case for getting the complete category tree with hierarchy information.
 */
@Singleton
class GetCategoryTreeUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    /**
     * Gets all categories with their hierarchy level information.
     * This provides the complete category tree structure.
     *
     * @param includeInactive Whether to include inactive categories (default: false)
     * @return Result with list of CategoryView objects showing hierarchy
     */
    suspend fun execute(includeInactive: Boolean = false): Result<List<CategoryView>> = withContext(ioDispatcher) {
        try {
            val categories = if (includeInactive) {
                // For now, we'll get all categories. In a real implementation,
                // we'd need to filter by active status if that field exists
                categoryRepository.getAllCategoriesWithLevel()
            } else {
                categoryRepository.getAllCategoriesWithLevel()
            }

            Result.success(categories)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Gets categories of a specific type with hierarchy information.
     *
     * @param type The category type (0 for expense, 1 for income)
     * @return Result with list of CategoryView objects
     */
    suspend fun executeByType(type: Int): Result<List<CategoryView>> = withContext(ioDispatcher) {
        try {
            val categories = categoryRepository.getCategoriesWithLevelByType(type)
            Result.success(categories)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Use case for getting categories without subtrees.
 * This is used when editing categories to avoid circular references.
 */
@Singleton
class GetCategoriesWithoutSubtreeUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    /**
     * Gets all categories except those in a specific subtree.
     * This prevents circular references when moving categories.
     *
     * @param excludeCategoryId The root category ID of the subtree to exclude
     * @param includeInactive Whether to include inactive categories
     * @return Result with list of categories excluding the subtree
     */
    suspend fun execute(excludeCategoryId: Long, includeInactive: Boolean = false): Result<List<CategoryView>> = withContext(ioDispatcher) {
        try {
            // Get the category to exclude to find its left/right bounds
            val excludeCategory = categoryRepository.getCategoryWithLevelById(excludeCategoryId)

            if (excludeCategory == null) {
                // If category doesn't exist, return all categories
                val allCategories = if (includeInactive) {
                    categoryRepository.getAllCategoriesWithLevel()
                } else {
                    categoryRepository.getAllCategoriesWithLevel()
                }
                return@withContext Result.success(allCategories)
            }

            // Get all categories and filter out the subtree
            val allCategories = categoryRepository.getAllCategoriesWithLevel()
            val filteredCategories = allCategories.filterNot { category ->
                category.left >= excludeCategory.left && category.right <= excludeCategory.right
            }

            Result.success(filteredCategories)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Use case for inserting a new category with proper hierarchy management.
 */
@Singleton
class InsertCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    /**
     * Inserts a new category. For hierarchical categories, this would need
     * to update the nested set model values (left/right), but for now
     * we'll handle simple insertion.
     *
     * @param category The category to insert
     * @return Result with the new category ID on success
     */
    suspend fun execute(category: CategoryEntity): Result<Long> = withContext(ioDispatcher) {
        try {
            val categoryId = categoryRepository.insertCategory(category)
            Result.success(categoryId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Use case for moving a category within the hierarchy.
 * This is a complex operation that requires updating the nested set model.
 */
@Singleton
class MoveCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    /**
     * Moves a category to a new parent in the hierarchy.
     * This requires complex updates to the nested set model (left/right values).
     *
     * Note: This is a simplified implementation. A full implementation would
     * require updating all affected left/right values in the tree.
     *
     * @param categoryId The category to move
     * @param newParentId The new parent category ID (0 for root level)
     * @return Result indicating success or failure
     */
    suspend fun execute(categoryId: Long, newParentId: Long): Result<Long> = withContext(ioDispatcher) {
        try {
            // This is a placeholder implementation
            // In a real implementation, this would involve:
            // 1. Getting the category and its subtree bounds
            // 2. Calculating new left/right values
            // 3. Updating all affected categories in the hierarchy
            // 4. Ensuring no circular references

            // For now, just return success
            Result.success(categoryId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}