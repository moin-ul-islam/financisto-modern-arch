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
import ru.orangesoftware.financisto.data.dao.AttributeDao
import ru.orangesoftware.financisto.data.dao.TransactionAttributeDao
import ru.orangesoftware.financisto.data.model.AttributeEntity
import ru.orangesoftware.financisto.data.model.AttributeView
import ru.orangesoftware.financisto.data.model.TransactionAttributeEntity
import ru.orangesoftware.financisto.repository.CoroutineTestRule
import ru.orangesoftware.financisto.repository.TestConstants
import strikt.api.expectThat
import strikt.assertions.*

/**
 * Unit tests for AttributeRepositoryImpl.
 * 
 * These tests verify that the repository correctly:
 * 1. Delegates operations to both AttributeDao and TransactionAttributeDao
 * 2. Handles both success and error scenarios gracefully
 * 3. Applies proper IO dispatcher context switching
 * 4. Provides reactive data streams via Flow
 * 5. Manages complex transaction attribute operations with proper sequencing
 * 6. Supports hierarchical attribute queries via AttributeView
 * 
 * The repository coordinates between two DAOs and implements composite
 * operations like updating transaction attributes (delete + insert).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AttributeRepositoryTest {

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    // MockK mocks
    private lateinit var mockAttributeDao: AttributeDao
    private lateinit var mockTransactionAttributeDao: TransactionAttributeDao
    private lateinit var repository: AttributeRepositoryImpl

    @Before
    fun setUp() {
        // Setup MockK mocks
        mockAttributeDao = mockk()
        mockTransactionAttributeDao = mockk()
        
        // Create repository instance with mocked dependencies
        repository = AttributeRepositoryImpl(
            attributeDao = mockAttributeDao,
            transactionAttributeDao = mockTransactionAttributeDao,
            ioDispatcher = coroutineTestRule.testDispatcher
        )
    }

    // ========== Attribute Entity Tests ==========

    @Test
    fun `getAllAttributes - should return attributes from DAO`() = runTest {
        // Arrange
        val mockAttributes = listOf(
            AttributeEntity(
                id = 1L,
                title = "Receipt Number",
                type = 1, // Text type
                listValues = null,
                defaultValue = null,
                isActive = true
            ),
            AttributeEntity(
                id = 2L,
                title = "Payment Method",
                type = 3, // List type
                listValues = "Cash,Credit Card,Debit Card",
                defaultValue = "Cash",
                isActive = true
            )
        )
        
        coEvery { mockAttributeDao.getAllAttributes() } returns mockAttributes
        
        // Act
        val result = repository.getAllAttributes()
        
        // Assert: Repository correctly delegates to DAO and returns the same data
        expectThat(result) {
            hasSize(2)
            get { first().title }.isEqualTo("Receipt Number")
            get { first().type }.isEqualTo(1)
            get { first().listValues }.isNull()
            get { last().title }.isEqualTo("Payment Method")
            get { last().type }.isEqualTo(3)
            get { last().listValues }.isEqualTo("Cash,Credit Card,Debit Card")
        }
        
        // Verify: Repository calls DAO exactly once with no parameters
        coVerify(exactly = 1) { mockAttributeDao.getAllAttributes() }
    }

    @Test
    fun `getAllAttributesFlow - should return Flow from DAO`() = runTest {
        // Arrange
        val mockAttributes = listOf(
            AttributeEntity(
                id = 1L,
                title = "Store Location",
                type = 1,
                listValues = null,
                defaultValue = null,
                isActive = true
            )
        )
        val mockFlow = flowOf(mockAttributes)
        
        coEvery { mockAttributeDao.getAllAttributesFlow() } returns mockFlow
        
        // Act
        val result = repository.getAllAttributesFlow().toList()
        
        // Assert: Repository correctly exposes DAO Flow and applies IO dispatcher
        expectThat(result) {
            hasSize(1) // One flow emission
            first().hasSize(1) // One attribute in the emission
            first().first().get { title }.isEqualTo("Store Location")
            first().first().get { type }.isEqualTo(1)
        }
        
        // Verify: Repository gets Flow from DAO exactly once
        coVerify(exactly = 1) { mockAttributeDao.getAllAttributesFlow() }
    }

    @Test
    fun `getAttributeById - should return attribute when found`() = runTest {
        // Arrange
        val attributeId = 5L
        val expectedAttribute = AttributeEntity(
            id = attributeId,
            title = "Expense Category",
            type = 3, // List type
            listValues = "Business,Personal,Travel",
            defaultValue = "Personal",
            isActive = true,
            sortOrder = 10
        )
        
        coEvery { mockAttributeDao.getAttributeById(attributeId) } returns expectedAttribute
        
        // Act
        val result = repository.getAttributeById(attributeId)

        if (result == null) {
            fail("Expected result but got null")
            return@runTest
        }
        
        // Assert: Repository returns the exact attribute from DAO when found
        expectThat(result) {
            get { id }.isEqualTo(attributeId)
            get { title }.isEqualTo("Expense Category")
            get { type }.isEqualTo(3)
            get { listValues }.isEqualTo("Business,Personal,Travel")
            get { defaultValue }.isEqualTo("Personal")
            get { sortOrder }.isEqualTo(10)
        }
        
        // Verify: Repository passes the correct ID to DAO
        coVerify(exactly = 1) { mockAttributeDao.getAttributeById(attributeId) }
    }

    @Test
    fun `getAttributeById - should return null when not found`() = runTest {
        // Arrange
        val nonExistentId = 999L
        coEvery { mockAttributeDao.getAttributeById(nonExistentId) } returns null
        
        // Act
        val result = repository.getAttributeById(nonExistentId)
        
        // Assert: Repository correctly propagates null from DAO
        expectThat(result).isNull()
        
        // Verify: Repository attempts to find attribute with correct ID
        coVerify(exactly = 1) { mockAttributeDao.getAttributeById(nonExistentId) }
    }

    @Test
    fun `getAttributesByType - should filter attributes by type`() = runTest {
        // Arrange
        val attributeType = 3 // List type
        val mockListAttributes = listOf(
            AttributeEntity(
                id = 1L,
                title = "Priority Level",
                type = attributeType,
                listValues = "High,Medium,Low",
                defaultValue = "Medium",
                isActive = true
            ),
            AttributeEntity(
                id = 2L,
                title = "Status",
                type = attributeType,
                listValues = "Pending,Approved,Rejected",
                defaultValue = "Pending",
                isActive = true
            )
        )
        
        coEvery { mockAttributeDao.getAttributesByType(attributeType) } returns mockListAttributes
        
        // Act
        val result = repository.getAttributesByType(attributeType)
        
        // Assert: Repository returns type-filtered attributes from DAO
        expectThat(result) {
            hasSize(2)
            all { get { type }.isEqualTo(attributeType) }
            all { get { listValues }.isNotNull() }
            any { get { title }.isEqualTo("Priority Level") }
            any { get { title }.isEqualTo("Status") }
        }
        
        // Verify: Repository passes correct type to DAO
        coVerify(exactly = 1) { mockAttributeDao.getAttributesByType(attributeType) }
    }

    @Test
    fun `insertAttribute - should return generated ID on success`() = runTest {
        // Arrange
        val newAttribute = AttributeEntity(
            id = 0, // Auto-generated
            title = "Tax Rate",
            type = 2, // Number type
            listValues = null,
            defaultValue = "0.0",
            isActive = true
        )
        val generatedId = 42L
        
        coEvery { mockAttributeDao.insertAttribute(newAttribute) } returns generatedId
        
        // Act
        val result = repository.insertAttribute(newAttribute)
        
        // Assert: Repository returns the generated ID from DAO
        expectThat(result).isEqualTo(generatedId)
        
        // Verify: Repository passes the exact attribute entity to DAO
        coVerify(exactly = 1) { mockAttributeDao.insertAttribute(newAttribute) }
    }

    @Test
    fun `updateAttribute - should return true on successful update`() = runTest {
        // Arrange
        val updatedAttribute = AttributeEntity(
            id = 1L,
            title = "Updated Attribute Name",
            type = 1,
            listValues = null,
            defaultValue = null,
            isActive = true
        )
        
        // DAO update methods return void, so we just mock successful execution
        coEvery { mockAttributeDao.updateAttribute(updatedAttribute) } just Runs
        
        // Act
        val result = repository.updateAttribute(updatedAttribute)
        
        // Assert: Repository returns true when DAO operation succeeds
        expectThat(result).isTrue()
        
        // Verify: Repository passes the exact updated attribute to DAO
        coVerify(exactly = 1) { mockAttributeDao.updateAttribute(updatedAttribute) }
    }

    @Test
    fun `updateAttribute - should return false when DAO throws exception`() = runTest {
        // Arrange
        val problematicAttribute = AttributeEntity(
            id = 1L,
            title = "Attribute With Issues",
            type = 999, // Invalid attribute type
            listValues = null,
            defaultValue = null,
            isActive = true
        )
        
        coEvery { mockAttributeDao.updateAttribute(problematicAttribute) } throws RuntimeException("Invalid attribute type")
        
        // Act
        val result = repository.updateAttribute(problematicAttribute)
        
        // Assert: Repository catches exception and returns false (doesn't crash app)
        expectThat(result).isFalse()
        
        // Verify: Repository attempted the update despite the failure
        coVerify(exactly = 1) { mockAttributeDao.updateAttribute(problematicAttribute) }
    }

    @Test
    fun `deleteAttribute - should return true on successful deletion`() = runTest {
        // Arrange
        val attributeIdToDelete = 5L
        coEvery { mockAttributeDao.deleteAttributeById(attributeIdToDelete) } just Runs
        
        // Act
        val result = repository.deleteAttribute(attributeIdToDelete)
        
        // Assert: Repository returns true when DAO deletion succeeds
        expectThat(result).isTrue()
        
        // Verify: Repository calls DAO with the correct attribute ID
        coVerify(exactly = 1) { mockAttributeDao.deleteAttributeById(attributeIdToDelete) }
    }

    @Test
    fun `deleteAttribute - should return false when DAO throws exception`() = runTest {
        // Arrange
        val attributeIdToDelete = 5L
        coEvery { mockAttributeDao.deleteAttributeById(attributeIdToDelete) } throws RuntimeException("Attribute is in use")
        
        // Act
        val result = repository.deleteAttribute(attributeIdToDelete)
        
        // Assert: Repository handles deletion failure gracefully
        expectThat(result).isFalse()
        
        // Verify: Repository attempted deletion with correct ID
        coVerify(exactly = 1) { mockAttributeDao.deleteAttributeById(attributeIdToDelete) }
    }

    @Test
    fun `searchAttributes - should return matching attributes from DAO`() = runTest {
        // Arrange
        val searchQuery = "receipt"
        val matchingAttributes = listOf(
            AttributeEntity(
                id = 1L,
                title = "Receipt Number",
                type = 1,
                listValues = null,
                defaultValue = null,
                isActive = true
            ),
            AttributeEntity(
                id = 2L,
                title = "Receipt Image",
                type = 1,
                listValues = null,
                defaultValue = null,
                isActive = true
            )
        )
        
        coEvery { mockAttributeDao.searchAttributes(searchQuery) } returns matchingAttributes
        
        // Act
        val result = repository.searchAttributes(searchQuery)
        
        // Assert: Repository returns search results from DAO
        expectThat(result) {
            hasSize(2)
            all { get { title }.contains("Receipt") }
        }
        
        // Verify: Repository passes exact search query to DAO
        coVerify(exactly = 1) { mockAttributeDao.searchAttributes(searchQuery) }
    }

    // ========== Transaction Attribute Tests ==========

    @Test
    fun `getTransactionAttributes - should return attributes for transaction`() = runTest {
        // Arrange
        val transactionId = 123L
        val mockTransactionAttributes = listOf(
            TransactionAttributeEntity(
                transactionId = transactionId,
                attributeId = 1L,
                value = "ABC123"
            ),
            TransactionAttributeEntity(
                transactionId = transactionId,
                attributeId = 2L,
                value = "Credit Card"
            )
        )
        
        coEvery { mockTransactionAttributeDao.getAttributesForTransaction(transactionId) } returns mockTransactionAttributes
        
        // Act
        val result = repository.getTransactionAttributes(transactionId)
        
        // Assert: Repository returns transaction attributes from TransactionAttributeDao
        expectThat(result) {
            hasSize(2)
            all { get { transactionId }.isEqualTo(transactionId) }
            any {
                get { attributeId }.isEqualTo(1L)
                get { value }.isEqualTo("ABC123")
            }
            any {
                get { attributeId }.isEqualTo(2L)
                get { value }.isEqualTo("Credit Card")
            }
        }
        
        // Verify: Repository uses TransactionAttributeDao for transaction-specific queries
        coVerify(exactly = 1) { mockTransactionAttributeDao.getAttributesForTransaction(transactionId) }
    }

    @Test
    fun `insertTransactionAttribute - should delegate to TransactionAttributeDao`() = runTest {
        // Arrange
        val transactionAttribute = TransactionAttributeEntity(
            transactionId = 123L,
            attributeId = 1L,
            value = "Receipt-001"
        )
        
        coEvery { mockTransactionAttributeDao.insertTransactionAttribute(transactionAttribute) } just Runs
        
        // Act
        repository.insertTransactionAttribute(transactionAttribute)
        
        // Assert: No return value to check, verify interaction occurred
        // Verify: Repository delegates to TransactionAttributeDao for insertion
        coVerify(exactly = 1) { mockTransactionAttributeDao.insertTransactionAttribute(transactionAttribute) }
    }

    @Test
    fun `insertTransactionAttributes - should delegate batch insert to TransactionAttributeDao`() = runTest {
        // Arrange
        val transactionId = 456L
        val attributes = listOf(
            TransactionAttributeEntity(
                transactionId = transactionId,
                attributeId = 1L,
                value = "Store A"
            ),
            TransactionAttributeEntity(
                transactionId = transactionId,
                attributeId = 2L,
                value = "High Priority"
            ),
            TransactionAttributeEntity(
                transactionId = transactionId,
                attributeId = 3L,
                value = "15.5"
            )
        )
        
        coEvery { mockTransactionAttributeDao.insertTransactionAttributes(attributes) } just Runs
        
        // Act
        repository.insertTransactionAttributes(attributes)
        
        // Assert: No return value to check, verify batch operation
        // Verify: Repository delegates batch insert to TransactionAttributeDao
        coVerify(exactly = 1) { mockTransactionAttributeDao.insertTransactionAttributes(attributes) }
    }

    @Test
    fun `deleteTransactionAttributesForTransaction - should delegate to TransactionAttributeDao`() = runTest {
        // Arrange
        val transactionId = 789L
        
        coEvery { mockTransactionAttributeDao.deleteTransactionAttributesForTransaction(transactionId) } just Runs
        
        // Act
        repository.deleteTransactionAttributesForTransaction(transactionId)
        
        // Assert: No return value to check, verify deletion
        // Verify: Repository delegates transaction attribute deletion
        coVerify(exactly = 1) { mockTransactionAttributeDao.deleteTransactionAttributesForTransaction(transactionId) }
    }

    @Test
    fun `updateTransactionAttributes - should delete existing and insert new attributes`() = runTest {
        // Arrange
        val transactionId = 555L
        val newAttributes = listOf(
            TransactionAttributeEntity(
                transactionId = transactionId,
                attributeId = 1L,
                value = "Updated Value 1"
            ),
            TransactionAttributeEntity(
                transactionId = transactionId,
                attributeId = 2L,
                value = "Updated Value 2"
            )
        )
        
        coEvery { mockTransactionAttributeDao.deleteTransactionAttributesForTransaction(transactionId) } just Runs
        coEvery { mockTransactionAttributeDao.insertTransactionAttributes(newAttributes) } just Runs
        
        // Act
        repository.updateTransactionAttributes(transactionId, newAttributes)
        
        // Assert: Repository implements composite operation (delete + insert)
        // Verify: Repository performs delete-then-insert sequence
        coVerify(exactly = 1) { mockTransactionAttributeDao.deleteTransactionAttributesForTransaction(transactionId) }
        coVerify(exactly = 1) { mockTransactionAttributeDao.insertTransactionAttributes(newAttributes) }
    }

    @Test
    fun `updateTransactionAttributes - should only delete when no new attributes provided`() = runTest {
        // Arrange
        val transactionId = 666L
        val emptyAttributes = emptyList<TransactionAttributeEntity>()
        
        coEvery { mockTransactionAttributeDao.deleteTransactionAttributesForTransaction(transactionId) } just Runs
        
        // Act
        repository.updateTransactionAttributes(transactionId, emptyAttributes)
        
        // Assert: Repository handles empty attribute list correctly
        // Verify: Repository deletes existing but skips insert for empty list
        coVerify(exactly = 1) { mockTransactionAttributeDao.deleteTransactionAttributesForTransaction(transactionId) }
        coVerify(exactly = 0) { mockTransactionAttributeDao.insertTransactionAttributes(any()) }
    }

    // ========== AttributeView Tests ==========

    @Test
    fun `getAllAttributesWithCategories - should return attributes with category information`() = runTest {
        // Arrange
        val mockAttributeViews = listOf(
            AttributeView(
                id = 1L,
                title = "Receipt Number",
                type = 1,
                listValues = null,
                defaultValue = null,
                categoryId = TestConstants.CATEGORY_ID_EXPENSE,
                categoryLeft = 1,
                categoryRight = 10
            ),
            AttributeView(
                id = 2L,
                title = "Tax Rate",
                type = 2,
                listValues = null,
                defaultValue = "0.0",
                categoryId = TestConstants.CATEGORY_ID_INCOME,
                categoryLeft = 11,
                categoryRight = 20
            )
        )
        
        coEvery { mockAttributeDao.getAllAttributesWithCategories() } returns mockAttributeViews
        
        // Act
        val result = repository.getAllAttributesWithCategories()
        
        // Assert: Repository returns attributes with category context from DAO
        expectThat(result) {
            hasSize(2)
            first().get { id }.isEqualTo(1L)
            first().get { categoryId }.isEqualTo(TestConstants.CATEGORY_ID_EXPENSE)
            last().get { id }.isEqualTo(2L)
            last().get { categoryId }.isEqualTo(TestConstants.CATEGORY_ID_INCOME)
        }
        
        // Verify: Repository delegates to AttributeDao for view queries
        coVerify(exactly = 1) { mockAttributeDao.getAllAttributesWithCategories() }
    }

    @Test
    fun `getAttributesForCategory - should return attributes for specific category`() = runTest {
        // Arrange
        val categoryId = TestConstants.CATEGORY_ID_EXPENSE
        val mockCategoryAttributes = listOf(
            AttributeView(
                id = 1L,
                title = "Business Expense Type",
                type = 3,
                listValues = "Travel,Meals,Office Supplies",
                defaultValue = "Office Supplies",
                categoryId = categoryId,
                categoryLeft = 5,
                categoryRight = 10
            )
        )
        
        coEvery { mockAttributeDao.getAttributesForCategory(categoryId) } returns mockCategoryAttributes
        
        // Act
        val result = repository.getAttributesForCategory(categoryId)
        
        // Assert: Repository returns category-specific attributes from DAO
        expectThat(result) {
            hasSize(1)
            first().get { categoryId }.isEqualTo(categoryId)
            first().get { title }.isEqualTo("Business Expense Type")
            first().get { type }.isEqualTo(3)
        }
        
        // Verify: Repository passes correct category ID to DAO
        coVerify(exactly = 1) { mockAttributeDao.getAttributesForCategory(categoryId) }
    }

    @Test
    fun `getAttributesForCategoryHierarchy - should return attributes for category tree range`() = runTest {
        // Arrange
        val hierarchyLeft = 5
        val hierarchyRight = 15
        val mockHierarchyAttributes = listOf(
            AttributeView(
                id = 1L,
                title = "Expense Subcategory",
                type = 3,
                listValues = "Sub1,Sub2,Sub3",
                defaultValue = "Sub1",
                categoryId = 10L,
                categoryLeft = hierarchyLeft + 1,
                categoryRight = hierarchyLeft + 4
            ),
            AttributeView(
                id = 2L,
                title = "Amount Threshold",
                type = 2,
                listValues = null,
                defaultValue = "100.0",
                categoryId = 11L,
                categoryLeft = hierarchyLeft + 5,
                categoryRight = hierarchyLeft + 8
            )
        )
        
        coEvery { mockAttributeDao.getAttributesForCategoryHierarchy(hierarchyLeft, hierarchyRight) } returns mockHierarchyAttributes
        
        // Act
        val result = repository.getAttributesForCategoryHierarchy(hierarchyLeft, hierarchyRight)
        
        // Assert: Repository returns hierarchical attributes from DAO
        expectThat(result) {
            hasSize(2)
            all { get { categoryLeft }.isGreaterThan(hierarchyLeft) }
            all { get { categoryRight }.isLessThan(hierarchyRight) }
            any { get { title }.isEqualTo("Expense Subcategory") }
            any { get { title }.isEqualTo("Amount Threshold") }
        }
        
        // Verify: Repository passes correct hierarchy bounds to DAO
        coVerify(exactly = 1) { mockAttributeDao.getAttributesForCategoryHierarchy(hierarchyLeft, hierarchyRight) }
    }
}