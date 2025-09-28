package ru.orangesoftware.financisto.repository.modern

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.Runs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import ru.orangesoftware.financisto.data.dao.CategoryDao
import ru.orangesoftware.financisto.data.model.CategoryEntity
import ru.orangesoftware.financisto.data.model.CategoryView
import ru.orangesoftware.financisto.repository.CoroutineTestRule
import ru.orangesoftware.financisto.repository.TestConstants
import strikt.api.expectThat
import strikt.assertions.*

/**
 * Unit tests for CategoryRepositoryImpl.
 * 
 * These tests verify that the repository correctly:
 * 1. Delegates operations to the CategoryDao
 * 2. Handles both success and error scenarios gracefully
 * 3. Applies proper IO dispatcher context switching
 * 4. Provides reactive data streams via Flow
 * 5. Supports hierarchical category operations via CategoryView
 * 
 * The repository acts as a bridge between use cases and data access,
 * primarily focusing on context switching and error handling rather than
 * complex business logic.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CategoryRepositoryTest {

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    // MockK mocks
    private lateinit var mockCategoryDao: CategoryDao
    private lateinit var repository: CategoryRepositoryImpl

    @Before
    fun setUp() {
        // Setup MockK mocks
        mockCategoryDao = mockk()
        
        // Create repository instance with mocked dependencies
        repository = CategoryRepositoryImpl(
            categoryDao = mockCategoryDao,
            ioDispatcher = coroutineTestRule.testDispatcher
        )
    }

    @Test
    fun `getAllCategories - should return categories from DAO`() = runTest {
        // Arrange
        val mockCategories = listOf(
            CategoryEntity(
                id = TestConstants.CATEGORY_ID_EXPENSE,
                title = "Food & Dining",
                type = 0, // Expense
                left = 1,
                right = 4,
                isActive = true
            ),
            CategoryEntity(
                id = TestConstants.CATEGORY_ID_INCOME,
                title = "Salary",
                type = 1, // Income
                left = 5,
                right = 6,
                isActive = true
            )
        )
        
        coEvery { mockCategoryDao.getAllCategories() } returns mockCategories
        
        // Act
        val result = repository.getAllCategories()
        
        // Assert: Repository correctly delegates to DAO and returns the same data
        expectThat(result) {
            hasSize(2)
            get { first().id }.isEqualTo(TestConstants.CATEGORY_ID_EXPENSE)
            get { first().title }.isEqualTo("Food & Dining")
            get { first().type }.isEqualTo(0)
            get { last().id }.isEqualTo(TestConstants.CATEGORY_ID_INCOME)
            get { last().title }.isEqualTo("Salary")
            get { last().type }.isEqualTo(1)
        }
        
        // Verify: Repository calls DAO exactly once with no parameters
        coVerify(exactly = 1) { mockCategoryDao.getAllCategories() }
    }

    @Test
    fun `getAllCategoriesFlow - should return Flow from DAO`() = runTest {
        // Arrange
        val mockCategories = listOf(
            CategoryEntity(
                id = TestConstants.CATEGORY_ID_EXPENSE,
                title = "Groceries",
                type = 0,
                left = 2,
                right = 3,
                isActive = true
            )
        )
        val mockFlow = flowOf(mockCategories)
        
        coEvery { mockCategoryDao.getAllCategoriesFlow() } returns mockFlow
        
        // Act
        val result = repository.getAllCategoriesFlow().toList()
        
        // Assert: Repository correctly exposes DAO Flow and applies IO dispatcher
        expectThat(result) {
            hasSize(1) // One flow emission
            first().hasSize(1) // One category in the emission
            first().first().get { title }.isEqualTo("Groceries")
            first().first().get { type }.isEqualTo(0)
        }
        
        // Verify: Repository gets Flow from DAO exactly once
        coVerify(exactly = 1) { mockCategoryDao.getAllCategoriesFlow() }
    }

    @Test
    fun `getCategoryById - should return category when found`() = runTest {
        // Arrange
        val expectedCategory = CategoryEntity(
            id = TestConstants.CATEGORY_ID_EXPENSE,
            title = "Transportation",
            type = 0,
            left = 10,
            right = 15,
            lastLocationId = 100L,
            lastProjectId = 200L,
            isActive = true
        )
        
        coEvery { mockCategoryDao.getCategoryById(TestConstants.CATEGORY_ID_EXPENSE) } returns expectedCategory
        
        // Act
        val result = repository.getCategoryById(TestConstants.CATEGORY_ID_EXPENSE)

        if (result == null) {
            fail("Got null result")
            return@runTest
        }
        // Assert: Repository returns the exact category from DAO when found
        expectThat(result) {
            get { id }.isEqualTo(TestConstants.CATEGORY_ID_EXPENSE)
            get { title }.isEqualTo("Transportation")
            get { type }.isEqualTo(0)
            get { lastLocationId }.isEqualTo(100L)
            get { lastProjectId }.isEqualTo(200L)
        }
        
        // Verify: Repository passes the correct ID to DAO
        coVerify(exactly = 1) { mockCategoryDao.getCategoryById(TestConstants.CATEGORY_ID_EXPENSE) }
    }

    @Test
    fun `getCategoryById - should return null when not found`() = runTest {
        // Arrange
        val nonExistentId = 999L
        coEvery { mockCategoryDao.getCategoryById(nonExistentId) } returns null
        
        // Act
        val result = repository.getCategoryById(nonExistentId)
        
        // Assert: Repository correctly propagates null from DAO
        expectThat(result).isNull()
        
        // Verify: Repository attempts to find category with correct ID
        coVerify(exactly = 1) { mockCategoryDao.getCategoryById(nonExistentId) }
    }

    @Test
    fun `getCategoriesByType - should filter categories by type`() = runTest {
        // Arrange
        val categoryType = 0 // Expense
        val mockExpenseCategories = listOf(
            CategoryEntity(
                id = 1L,
                title = "Restaurant",
                type = categoryType,
                left = 1,
                right = 2,
                isActive = true
            ),
            CategoryEntity(
                id = 2L,
                title = "Gas",
                type = categoryType,
                left = 3,
                right = 4,
                isActive = true
            )
        )
        
        coEvery { mockCategoryDao.getCategoriesByType(categoryType) } returns mockExpenseCategories
        
        // Act
        val result = repository.getCategoriesByType(categoryType)
        
        // Assert: Repository returns type-filtered categories from DAO
        expectThat(result) {
            hasSize(2)
            all { get { type }.isEqualTo(categoryType) }
            any { get { title }.isEqualTo("Restaurant") }
            any { get { title }.isEqualTo("Gas") }
        }
        
        // Verify: Repository passes correct type to DAO
        coVerify(exactly = 1) { mockCategoryDao.getCategoriesByType(categoryType) }
    }

    @Test
    fun `getExpenseCategories - should return expense categories from DAO`() = runTest {
        // Arrange
        val mockExpenseCategories = listOf(
            CategoryEntity(
                id = 1L,
                title = "Home Expenses",
                type = 0, // Expense
                left = 1,
                right = 6,
                isActive = true
            ),
            CategoryEntity(
                id = 2L,
                title = "Utilities",
                type = 0, // Expense
                left = 2,
                right = 3,
                isActive = true
            )
        )
        
        coEvery { mockCategoryDao.getExpenseCategories() } returns mockExpenseCategories
        
        // Act
        val result = repository.getExpenseCategories()
        
        // Assert: Repository returns expense categories from DAO
        expectThat(result) {
            hasSize(2)
            all { get { type }.isEqualTo(0) } // All are expense type
            any { get { title }.isEqualTo("Home Expenses") }
            any { get { title }.isEqualTo("Utilities") }
        }
        
        // Verify: Repository delegates to expense-specific DAO method
        coVerify(exactly = 1) { mockCategoryDao.getExpenseCategories() }
    }

    @Test
    fun `getIncomeCategories - should return income categories from DAO`() = runTest {
        // Arrange
        val mockIncomeCategories = listOf(
            CategoryEntity(
                id = TestConstants.CATEGORY_ID_INCOME,
                title = "Employment Income",
                type = 1, // Income
                left = 10,
                right = 15,
                isActive = true
            ),
            CategoryEntity(
                id = 2L,
                title = "Investment Returns",
                type = 1, // Income
                left = 16,
                right = 17,
                isActive = true
            )
        )
        
        coEvery { mockCategoryDao.getIncomeCategories() } returns mockIncomeCategories
        
        // Act
        val result = repository.getIncomeCategories()
        
        // Assert: Repository returns income categories from DAO
        expectThat(result) {
            hasSize(2)
            all { get { type }.isEqualTo(1) } // All are income type
            any { get { title }.isEqualTo("Employment Income") }
            any { get { title }.isEqualTo("Investment Returns") }
        }
        
        // Verify: Repository delegates to income-specific DAO method
        coVerify(exactly = 1) { mockCategoryDao.getIncomeCategories() }
    }

    @Test
    fun `insertCategory - should return generated ID on success`() = runTest {
        // Arrange
        val newCategory = CategoryEntity(
            id = 0, // Auto-generated
            title = "New Category",
            type = 0,
            left = 20,
            right = 21,
            isActive = true
        )
        val generatedId = 42L
        
        coEvery { mockCategoryDao.insertCategory(newCategory) } returns generatedId
        
        // Act
        val result = repository.insertCategory(newCategory)
        
        // Assert: Repository returns the generated ID from DAO
        expectThat(result).isEqualTo(generatedId)
        
        // Verify: Repository passes the exact category entity to DAO
        coVerify(exactly = 1) { mockCategoryDao.insertCategory(newCategory) }
    }

    @Test
    fun `updateCategory - should return true on successful update`() = runTest {
        // Arrange
        val updatedCategory = CategoryEntity(
            id = TestConstants.CATEGORY_ID_EXPENSE,
            title = "Updated Category Name",
            type = 0,
            left = 1,
            right = 2,
            isActive = true
        )
        
        // DAO update methods return void, so we just mock successful execution
        coEvery { mockCategoryDao.updateCategory(updatedCategory) } just Runs
        
        // Act
        val result = repository.updateCategory(updatedCategory)
        
        // Assert: Repository returns true when DAO operation succeeds
        expectThat(result).isTrue()
        
        // Verify: Repository passes the exact updated category to DAO
        coVerify(exactly = 1) { mockCategoryDao.updateCategory(updatedCategory) }
    }

    @Test
    fun `updateCategory - should return false when DAO throws exception`() = runTest {
        // Arrange
        val problematicCategory = CategoryEntity(
            id = TestConstants.CATEGORY_ID_EXPENSE,
            title = "Category With Issues",
            type = 0,
            left = 1,
            right = -1, // Invalid hierarchy bounds
            isActive = true
        )
        
        coEvery { mockCategoryDao.updateCategory(problematicCategory) } throws RuntimeException("Hierarchy constraint violation")
        
        // Act
        val result = repository.updateCategory(problematicCategory)
        
        // Assert: Repository catches exception and returns false (doesn't crash app)
        expectThat(result).isFalse()
        
        // Verify: Repository attempted the update despite the failure
        coVerify(exactly = 1) { mockCategoryDao.updateCategory(problematicCategory) }
    }

    @Test
    fun `deleteCategory - should return true on successful deletion`() = runTest {
        // Arrange
        val categoryIdToDelete = TestConstants.CATEGORY_ID_EXPENSE
        coEvery { mockCategoryDao.deleteCategoryById(categoryIdToDelete) } just Runs
        
        // Act
        val result = repository.deleteCategory(categoryIdToDelete)
        
        // Assert: Repository returns true when DAO deletion succeeds
        expectThat(result).isTrue()
        
        // Verify: Repository calls DAO with the correct category ID
        coVerify(exactly = 1) { mockCategoryDao.deleteCategoryById(categoryIdToDelete) }
    }

    @Test
    fun `deleteCategory - should return false when DAO throws exception`() = runTest {
        // Arrange
        val categoryIdToDelete = TestConstants.CATEGORY_ID_EXPENSE
        coEvery { mockCategoryDao.deleteCategoryById(categoryIdToDelete) } throws RuntimeException("Category has child categories")
        
        // Act
        val result = repository.deleteCategory(categoryIdToDelete)
        
        // Assert: Repository handles deletion failure gracefully
        expectThat(result).isFalse()
        
        // Verify: Repository attempted deletion with correct ID
        coVerify(exactly = 1) { mockCategoryDao.deleteCategoryById(categoryIdToDelete) }
    }

    @Test
    fun `searchCategories - should return matching categories from DAO`() = runTest {
        // Arrange
        val searchQuery = "food"
        val matchingCategories = listOf(
            CategoryEntity(
                id = 1L,
                title = "Fast Food",
                type = 0,
                left = 1,
                right = 2,
                isActive = true
            ),
            CategoryEntity(
                id = 2L,
                title = "Food & Dining",
                type = 0,
                left = 3,
                right = 8,
                isActive = true
            )
        )
        
        coEvery { mockCategoryDao.searchCategories(searchQuery) } returns matchingCategories
        
        // Act
        val result = repository.searchCategories(searchQuery)
        
        // Assert: Repository returns search results from DAO
        expectThat(result) {
            hasSize(2)
            all { get { title }.contains("Food") }
        }
        
        // Verify: Repository passes exact search query to DAO
        coVerify(exactly = 1) { mockCategoryDao.searchCategories(searchQuery) }
    }

    // ========== CategoryView Tests ==========

    @Test
    fun `getAllCategoriesWithLevel - should return hierarchical categories with levels`() = runTest {
        // Arrange
        val mockCategoriesWithLevel = listOf(
            CategoryView(
                id = 1L,
                title = "Parent Category",
                left = 1,
                right = 6,
                type = 0,
                lastLocationId = 0,
                lastProjectId = 0,
                sortOrder = 0,
                level = 0 // Root level
            ),
            CategoryView(
                id = 2L,
                title = "Child Category",
                left = 2,
                right = 3,
                type = 0,
                lastLocationId = 0,
                lastProjectId = 0,
                sortOrder = 0,
                level = 1 // One level deeper
            )
        )
        
        coEvery { mockCategoryDao.getAllCategoriesWithLevel() } returns mockCategoriesWithLevel
        
        // Act
        val result = repository.getAllCategoriesWithLevel()
        
        // Assert: Repository returns hierarchical categories with level information
        expectThat(result) {
            hasSize(2)
            first().get { level }.isEqualTo(0) // Parent level
            last().get { level }.isEqualTo(1) // Child level
            first().get { title }.isEqualTo("Parent Category")
            last().get { title }.isEqualTo("Child Category")
        }
        
        // Verify: Repository delegates to hierarchical DAO method
        coVerify(exactly = 1) { mockCategoryDao.getAllCategoriesWithLevel() }
    }

    @Test
    fun `getCategoryWithLevelById - should return category with level when found`() = runTest {
        // Arrange
        val categoryId = TestConstants.CATEGORY_ID_EXPENSE
        val expectedCategoryView = CategoryView(
            id = categoryId,
            title = "Travel",
            left = 10,
            right = 15,
            type = 0,
            lastLocationId = 0,
            lastProjectId = 0,
            sortOrder = 0,
            level = 1
        )
        
        coEvery { mockCategoryDao.getCategoryWithLevelById(categoryId) } returns expectedCategoryView
        
        // Act
        val result = repository.getCategoryWithLevelById(categoryId)
        if (result == null) {
            fail("Result is null")
            return@runTest
        }
        // Assert: Repository returns category with hierarchy information
        expectThat(result) {
            get { id }.isEqualTo(categoryId)
            get { title }.isEqualTo("Travel")
            get { level }.isEqualTo(1)
        }
        
        // Verify: Repository passes correct ID to hierarchical DAO method
        coVerify(exactly = 1) { mockCategoryDao.getCategoryWithLevelById(categoryId) }
    }

    @Test
    fun `getCategoryWithLevelById - should return null when category not found`() = runTest {
        // Arrange
        val nonExistentId = 999L
        coEvery { mockCategoryDao.getCategoryWithLevelById(nonExistentId) } returns null
        
        // Act
        val result = repository.getCategoryWithLevelById(nonExistentId)
        
        // Assert: Repository correctly propagates null from DAO
        expectThat(result).isNull()
        
        // Verify: Repository attempts to find category with level
        coVerify(exactly = 1) { mockCategoryDao.getCategoryWithLevelById(nonExistentId) }
    }

    @Test
    fun `getCategoriesWithLevelByType - should filter hierarchical categories by type`() = runTest {
        // Arrange
        val categoryType = 1 // Income
        val mockIncomeCategoriesWithLevel = listOf(
            CategoryView(
                id = TestConstants.CATEGORY_ID_INCOME,
                title = "Salary Income",
                left = 1,
                right = 4,
                type = categoryType,
                lastLocationId = 0,
                lastProjectId = 0,
                sortOrder = 0,
                level = 0
            ),
            CategoryView(
                id = 2L,
                title = "Base Salary",
                left = 2,
                right = 3,
                type = categoryType,
                lastLocationId = 0,
                lastProjectId = 0,
                sortOrder = 0,
                level = 1
            )
        )
        
        coEvery { mockCategoryDao.getCategoriesWithLevelByType(categoryType) } returns mockIncomeCategoriesWithLevel
        
        // Act
        val result = repository.getCategoriesWithLevelByType(categoryType)
        
        // Assert: Repository returns type-filtered hierarchical categories
        expectThat(result) {
            hasSize(2)
            all { get { type }.isEqualTo(categoryType) }
            first().get { level }.isEqualTo(0) // Parent
            last().get { level }.isEqualTo(1) // Child
        }
        
        // Verify: Repository passes correct type to hierarchical DAO method
        coVerify(exactly = 1) { mockCategoryDao.getCategoriesWithLevelByType(categoryType) }
    }

    @Test
    fun `getExpenseCategoriesWithLevel - should return expense categories with hierarchy`() = runTest {
        // Arrange
        val mockExpenseCategoriesWithLevel = listOf(
            CategoryView(
                id = 1L,
                title = "Living Expenses",
                left = 1,
                right = 8,
                type = 0, // Expense
                lastLocationId = 0,
                lastProjectId = 0,
                sortOrder = 0,
                level = 0
            ),
            CategoryView(
                id = 2L,
                title = "Housing",
                left = 2,
                right = 5,
                type = 0, // Expense
                lastLocationId = 0,
                lastProjectId = 0,
                sortOrder = 0,
                level = 1
            ),
            CategoryView(
                id = 3L,
                title = "Rent",
                left = 3,
                right = 4,
                type = 0, // Expense
                lastLocationId = 0,
                lastProjectId = 0,
                sortOrder = 0,
                level = 2
            )
        )
        
        coEvery { mockCategoryDao.getExpenseCategoriesWithLevel() } returns mockExpenseCategoriesWithLevel
        
        // Act
        val result = repository.getExpenseCategoriesWithLevel()
        
        // Assert: Repository returns expense categories with 3-level hierarchy
        expectThat(result) {
            hasSize(3)
            all { get { type }.isEqualTo(0) } // All expense type
            get { map { it.level } }.containsExactly(0, 1, 2) // Proper hierarchy levels
            any { get { title }.isEqualTo("Living Expenses") }
            any { get { title }.isEqualTo("Housing") }
            any { get { title }.isEqualTo("Rent") }
        }
        
        // Verify: Repository delegates to expense hierarchical DAO method
        coVerify(exactly = 1) { mockCategoryDao.getExpenseCategoriesWithLevel() }
    }

    @Test
    fun `getIncomeCategoriesWithLevel - should return income categories with hierarchy`() = runTest {
        // Arrange
        val mockIncomeCategoriesWithLevel = listOf(
            CategoryView(
                id = TestConstants.CATEGORY_ID_INCOME,
                title = "Employment",
                left = 10,
                right = 15,
                type = 1, // Income
                lastLocationId = 0,
                lastProjectId = 0,
                sortOrder = 0,
                level = 0
            ),
            CategoryView(
                id = 2L,
                title = "Full-time Job",
                left = 11,
                right = 12,
                type = 1, // Income
                lastLocationId = 0,
                lastProjectId = 0,
                sortOrder = 0,
                level = 1
            )
        )
        
        coEvery { mockCategoryDao.getIncomeCategoriesWithLevel() } returns mockIncomeCategoriesWithLevel
        
        // Act
        val result = repository.getIncomeCategoriesWithLevel()
        
        // Assert: Repository returns income categories with hierarchy information
        expectThat(result) {
            hasSize(2)
            all { get { type }.isEqualTo(1) } // All income type
            first().get { level }.isEqualTo(0) // Parent level
            last().get { level }.isEqualTo(1) // Child level
            any { get { title }.isEqualTo("Employment") }
            any { get { title }.isEqualTo("Full-time Job") }
        }
        
        // Verify: Repository delegates to income hierarchical DAO method
        coVerify(exactly = 1) { mockCategoryDao.getIncomeCategoriesWithLevel() }
    }
}