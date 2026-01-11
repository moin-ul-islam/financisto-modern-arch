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