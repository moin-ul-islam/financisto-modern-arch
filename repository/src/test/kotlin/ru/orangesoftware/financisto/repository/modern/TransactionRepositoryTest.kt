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
import ru.orangesoftware.financisto.data.dao.TransactionDao
import ru.orangesoftware.financisto.data.dao.TransactionWithSplits
import ru.orangesoftware.financisto.data.model.TransactionEntity
import ru.orangesoftware.financisto.repository.CoroutineTestRule
import ru.orangesoftware.financisto.repository.TestConstants
import strikt.api.expectThat
import strikt.assertions.*

/**
 * Sample test class demonstrating MockK + Strikt setup for TransactionRepository testing.
 * 
 * This shows more complex repository testing patterns including:
 * - Testing repositories with multiple DAO dependencies
 * - Testing error handling scenarios
 * - Testing complex business logic
 * - Testing transaction operations
 * 
 * NOTE: This is a sample/template - actual test implementations are not included yet.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TransactionRepositoryTest {

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    // MockK mocks
    private lateinit var mockTransactionDao: TransactionDao
    private lateinit var repository: TransactionRepositoryImpl

    @Before
    fun setUp() {
        mockTransactionDao = mockk()
        
        repository = TransactionRepositoryImpl(
            transactionDao = mockTransactionDao,
            ioDispatcher = coroutineTestRule.testDispatcher
        )
    }

    @Test
    fun `getAllTransactions - should return all transactions from DAO`() = runTest {
        // Arrange
        val mockTransactions = listOf(
            TransactionEntity(
                id = 1L,
                fromAccountId = TestConstants.ACCOUNT_ID_1,
                categoryId = TestConstants.CATEGORY_ID_EXPENSE,
                fromAmount = -5000L, // $50.00 expense
                note = "Coffee shop",
                datetime = System.currentTimeMillis()
            ),
            TransactionEntity(
                id = 2L,
                fromAccountId = TestConstants.ACCOUNT_ID_2,
                categoryId = TestConstants.CATEGORY_ID_INCOME,
                fromAmount = 10000L, // $100.00 income
                note = "Freelance payment",
                datetime = System.currentTimeMillis()
            )
        )
        
        coEvery { mockTransactionDao.getAllTransactions() } returns mockTransactions
        
        // Act
        val result = repository.getAllTransactions()
        
        // Assert: Repository correctly delegates to DAO and returns the same data
        expectThat(result) {
            hasSize(2)
            map { it.id }.containsExactly(1L, 2L)
            any { get { fromAmount }.isLessThan(0) } // Has expense
            any { get { fromAmount }.isGreaterThan(0) } // Has income
            get { first().note }.isEqualTo("Coffee shop")
            get { last().note }.isEqualTo("Freelance payment")
        }
        
        // Verify: Repository calls DAO exactly once
        coVerify(exactly = 1) { mockTransactionDao.getAllTransactions() }
    }

    @Test
    fun `getAllTransactionsFlow - should return Flow from DAO`() = runTest {
        // Arrange
        val mockTransactions = listOf(
            TransactionEntity(
                id = 1L,
                fromAccountId = TestConstants.ACCOUNT_ID_1,
                categoryId = TestConstants.CATEGORY_ID_EXPENSE,
                fromAmount = -2500L, // $25.00 expense
                note = "Lunch"
            )
        )
        val mockFlow = flowOf(mockTransactions)
        
        coEvery { mockTransactionDao.getAllTransactionsFlow() } returns mockFlow
        
        // Act
        val result = repository.getAllTransactionsFlow().toList()
        
        // Assert: Repository correctly exposes DAO Flow and applies IO dispatcher
        expectThat(result) {
            hasSize(1) // One flow emission
            first().hasSize(1) // One transaction in the emission
            first().first().get { note }.isEqualTo("Lunch")
        }
        
        // Verify: Repository gets Flow from DAO exactly once
        coVerify(exactly = 1) { mockTransactionDao.getAllTransactionsFlow() }
    }

    @Test
    fun `getTransactionById - should return transaction when found`() = runTest {
        // Arrange
        val expectedTransaction = TransactionEntity(
            id = 123L,
            fromAccountId = TestConstants.ACCOUNT_ID_1,
            categoryId = TestConstants.CATEGORY_ID_EXPENSE,
            fromAmount = -7500L, // $75.00
            note = "Gas station",
            status = "CL"
        )
        
        coEvery { mockTransactionDao.getTransactionById(123L) } returns expectedTransaction
        
        // Act
        val result = repository.getTransactionById(123L)
        if (result == null) {
            fail("Result is null")
            return@runTest
        }
        // Assert: Repository returns the exact transaction from DAO when found
        expectThat(result) {
            get { id }.isEqualTo(123L)
            get { note }.isEqualTo("Gas station")
            get { fromAmount }.isEqualTo(-7500L)
            get { status }.isEqualTo("CL")
        }
        
        // Verify: Repository passes the correct ID to DAO
        coVerify(exactly = 1) { mockTransactionDao.getTransactionById(123L) }
    }

    @Test
    fun `getTransactionById - should return null when not found`() = runTest {
        // Arrange
        val nonExistentId = 999L
        coEvery { mockTransactionDao.getTransactionById(nonExistentId) } returns null
        
        // Act
        val result = repository.getTransactionById(nonExistentId)
        
        // Assert: Repository correctly propagates null from DAO
        expectThat(result).isNull()
        
        // Verify: Repository attempts to find transaction with correct ID
        coVerify(exactly = 1) { mockTransactionDao.getTransactionById(nonExistentId) }
    }

    @Test
    fun `getTransactionsForAccount - should filter by account ID`() = runTest {
        // Arrange
        val accountId = TestConstants.ACCOUNT_ID_1
        val mockTransactions = listOf(
            TransactionEntity(
                id = 1L,
                fromAccountId = accountId,
                categoryId = TestConstants.CATEGORY_ID_EXPENSE,
                fromAmount = -3000L,
                note = "Account 1 expense 1"
            ),
            TransactionEntity(
                id = 2L,
                fromAccountId = accountId,
                categoryId = TestConstants.CATEGORY_ID_EXPENSE,
                fromAmount = -4000L,
                note = "Account 1 expense 2"
            )
        )
        
        coEvery { mockTransactionDao.getTransactionsForAccount(accountId) } returns mockTransactions
        
        // Act
        val result = repository.getTransactionsForAccount(accountId)
        
        // Assert: Repository returns filtered transactions from DAO
        expectThat(result) {
            hasSize(2)
            all { get { fromAccountId }.isEqualTo(accountId) }
        }
        
        // Verify: Repository passes correct account ID to DAO
        coVerify(exactly = 1) { mockTransactionDao.getTransactionsForAccount(accountId) }
    }

    @Test
    fun `getTransactionsForAccountFlow - should return filtered Flow`() = runTest {
        // Arrange
        val accountId = TestConstants.ACCOUNT_ID_1
        val mockTransactions = listOf(
            TransactionEntity(
                id = 1L,
                fromAccountId = accountId,
                categoryId = TestConstants.CATEGORY_ID_INCOME,
                fromAmount = 5000L,
                note = "Salary"
            )
        )
        val mockFlow = flowOf(mockTransactions)
        
        coEvery { mockTransactionDao.getTransactionsForAccountFlow(accountId) } returns mockFlow
        
        // Act
        val result = repository.getTransactionsForAccountFlow(accountId).toList()
        
        // Assert: Repository returns reactive stream filtered by account
        expectThat(result) {
            hasSize(1) // One emission
            first().hasSize(1) // One transaction
            first().first().get { fromAccountId }.isEqualTo(accountId)
        }
        
        // Verify: Repository passes correct account ID to DAO Flow
        coVerify(exactly = 1) { mockTransactionDao.getTransactionsForAccountFlow(accountId) }
    }

    @Test
    fun `getTransactionsByDateRange - should filter by date range`() = runTest {
        // Arrange
        val startDate = System.currentTimeMillis() - 86400000L // 1 day ago
        val endDate = System.currentTimeMillis()
        val mockTransactions = listOf(
            TransactionEntity(
                id = 1L,
                fromAccountId = TestConstants.ACCOUNT_ID_1,
                categoryId = TestConstants.CATEGORY_ID_EXPENSE,
                fromAmount = -2000L,
                datetime = startDate + 3600000L, // Within range
                note = "Within range"
            )
        )
        
        coEvery { mockTransactionDao.getTransactionsByDateRange(startDate, endDate) } returns mockTransactions
        
        // Act
        val result = repository.getTransactionsByDateRange(startDate, endDate)
        
        // Assert: Repository returns date-filtered transactions from DAO
        expectThat(result) {
            hasSize(1)
            first().get { datetime }.isGreaterThanOrEqualTo(startDate)
            first().get { datetime }.isLessThanOrEqualTo(endDate)
        }
        
        // Verify: Repository passes correct date range to DAO
        coVerify(exactly = 1) { mockTransactionDao.getTransactionsByDateRange(startDate, endDate) }
    }

    @Test
    fun `getTransactionsByCategory - should filter by category ID`() = runTest {
        // Arrange
        val categoryId = TestConstants.CATEGORY_ID_EXPENSE
        val mockTransactions = listOf(
            TransactionEntity(
                id = 1L,
                fromAccountId = TestConstants.ACCOUNT_ID_1,
                categoryId = categoryId,
                fromAmount = -1500L,
                note = "Category expense 1"
            ),
            TransactionEntity(
                id = 2L,
                fromAccountId = TestConstants.ACCOUNT_ID_2,
                categoryId = categoryId,
                fromAmount = -2500L,
                note = "Category expense 2"
            )
        )
        
        coEvery { mockTransactionDao.getTransactionsByCategory(categoryId) } returns mockTransactions
        
        // Act
        val result = repository.getTransactionsByCategory(categoryId)
        
        // Assert: Repository returns category-filtered transactions from DAO
        expectThat(result) {
            hasSize(2)
            all { get { categoryId }.isEqualTo(categoryId) }
        }
        
        // Verify: Repository passes correct category ID to DAO
        coVerify(exactly = 1) { mockTransactionDao.getTransactionsByCategory(categoryId) }
    }

    @Test
    fun `insertTransaction - should return generated ID on success`() = runTest {
        // Arrange
        val newTransaction = TransactionEntity(
            id = 0, // Auto-generated
            fromAccountId = TestConstants.ACCOUNT_ID_1,
            categoryId = TestConstants.CATEGORY_ID_EXPENSE,
            fromAmount = -5000L,
            note = "New expense"
        )
        val generatedId = 42L
        
        coEvery { mockTransactionDao.insertTransaction(newTransaction) } returns generatedId
        
        // Act
        val result = repository.insertTransaction(newTransaction)
        
        // Assert: Repository returns the generated ID from DAO
        expectThat(result).isEqualTo(generatedId)
        
        // Verify: Repository passes the exact transaction entity to DAO
        coVerify(exactly = 1) { mockTransactionDao.insertTransaction(newTransaction) }
    }

    @Test
    fun `updateTransaction - should return true on successful update`() = runTest {
        // Arrange
        val updatedTransaction = TransactionEntity(
            id = 123L,
            fromAccountId = TestConstants.ACCOUNT_ID_1,
            categoryId = TestConstants.CATEGORY_ID_EXPENSE,
            fromAmount = -6000L,
            note = "Updated expense"
        )
        
        coEvery { mockTransactionDao.updateTransaction(updatedTransaction) } just Runs
        
        // Act
        val result = repository.updateTransaction(updatedTransaction)
        
        // Assert: Repository returns true when DAO operation succeeds
        expectThat(result).isTrue()
        
        // Verify: Repository passes the exact updated transaction to DAO
        coVerify(exactly = 1) { mockTransactionDao.updateTransaction(updatedTransaction) }
    }

    @Test
    fun `updateTransaction - should return false on constraint violation`() = runTest {
        // Arrange
        val problematicTransaction = TransactionEntity(
            id = 123L,
            fromAccountId = 999L, // Non-existent account
            categoryId = TestConstants.CATEGORY_ID_EXPENSE,
            fromAmount = -1000L,
            note = "Bad transaction"
        )
        
        coEvery { mockTransactionDao.updateTransaction(problematicTransaction) } throws RuntimeException("Constraint violation")
        
        // Act
        val result = repository.updateTransaction(problematicTransaction)
        
        // Assert: Repository catches exception and returns false (doesn't crash app)
        expectThat(result).isFalse()
        
        // Verify: Repository attempted the update despite the failure
        coVerify(exactly = 1) { mockTransactionDao.updateTransaction(problematicTransaction) }
    }

    @Test
    fun `deleteTransaction - should return true on successful deletion`() = runTest {
        // Arrange
        val transactionIdToDelete = 123L
        coEvery { mockTransactionDao.deleteTransactionById(transactionIdToDelete) } just Runs
        
        // Act
        val result = repository.deleteTransaction(transactionIdToDelete)
        
        // Assert: Repository returns true when DAO deletion succeeds
        expectThat(result).isTrue()
        
        // Verify: Repository calls DAO with the correct transaction ID
        coVerify(exactly = 1) { mockTransactionDao.deleteTransactionById(transactionIdToDelete) }
    }

    @Test
    fun `deleteTransaction - should return false when DAO throws exception`() = runTest {
        // Arrange
        val transactionIdToDelete = 123L
        coEvery { mockTransactionDao.deleteTransactionById(transactionIdToDelete) } throws RuntimeException("Foreign key constraint")
        
        // Act
        val result = repository.deleteTransaction(transactionIdToDelete)
        
        // Assert: Repository handles deletion failure gracefully
        expectThat(result).isFalse()
        
        // Verify: Repository attempted deletion with correct ID
        coVerify(exactly = 1) { mockTransactionDao.deleteTransactionById(transactionIdToDelete) }
    }

    @Test
    fun `getTransactionTemplates - should return template transactions`() = runTest {
        // Arrange
        val mockTemplates = listOf(
            TransactionEntity(
                id = 1L,
                fromAccountId = TestConstants.ACCOUNT_ID_1,
                categoryId = TestConstants.CATEGORY_ID_EXPENSE,
                fromAmount = -5000L,
                note = "Monthly rent",
                isTemplate = true,
                templateName = "Rent Template"
            ),
            TransactionEntity(
                id = 2L,
                fromAccountId = TestConstants.ACCOUNT_ID_1,
                categoryId = TestConstants.CATEGORY_ID_INCOME,
                fromAmount = 200000L,
                note = "Monthly salary",
                isTemplate = true,
                templateName = "Salary Template"
            )
        )
        
        coEvery { mockTransactionDao.getTransactionTemplates() } returns mockTemplates
        
        // Act
        val result = repository.getTransactionTemplates()
        
        // Assert: Repository returns template transactions from DAO
        expectThat(result) {
            hasSize(2)
            all { get { isTemplate }.isTrue() }
            any { get { templateName }.isEqualTo("Rent Template") }
            any { get { templateName }.isEqualTo("Salary Template") }
        }
        
        // Verify: Repository delegates to DAO template method
        coVerify(exactly = 1) { mockTransactionDao.getTransactionTemplates() }
    }

    @Test
    fun `getTotalAmountForAccount - should return calculated total`() = runTest {
        // Arrange
        val accountId = TestConstants.ACCOUNT_ID_1
        val startDate = System.currentTimeMillis() - 86400000L
        val endDate = System.currentTimeMillis()
        val expectedTotal = 125000L // $1,250.00
        
        coEvery { mockTransactionDao.getTotalAmountForAccount(accountId, startDate, endDate) } returns expectedTotal
        
        // Act
        val result = repository.getTotalAmountForAccount(accountId, startDate, endDate)
        
        // Assert: Repository returns the calculated total from DAO
        expectThat(result).isEqualTo(expectedTotal)
        
        // Verify: Repository passes all parameters correctly to DAO
        coVerify(exactly = 1) { mockTransactionDao.getTotalAmountForAccount(accountId, startDate, endDate) }
    }

    @Test
    fun `getTransactionCountForAccount - should return count from DAO`() = runTest {
        // Arrange
        val accountId = TestConstants.ACCOUNT_ID_1
        val expectedCount = 15
        
        coEvery { mockTransactionDao.getTransactionCountForAccount(accountId) } returns expectedCount
        
        // Act
        val result = repository.getTransactionCountForAccount(accountId)
        
        // Assert: Repository returns the count from DAO
        expectThat(result).isEqualTo(expectedCount)
        
        // Verify: Repository passes correct account ID to DAO
        coVerify(exactly = 1) { mockTransactionDao.getTransactionCountForAccount(accountId) }
    }

    @Test
    fun `getSplitTransactions - should return split transactions for parent`() = runTest {
        // Arrange
        val parentId = 100L
        val mockSplits = listOf(
            TransactionEntity(
                id = 101L,
                fromAccountId = TestConstants.ACCOUNT_ID_1,
                categoryId = TestConstants.CATEGORY_ID_EXPENSE,
                fromAmount = -2000L,
                parentId = parentId,
                note = "Split 1: Groceries"
            ),
            TransactionEntity(
                id = 102L,
                fromAccountId = TestConstants.ACCOUNT_ID_1,
                categoryId = TestConstants.CATEGORY_ID_EXPENSE,
                fromAmount = -3000L,
                parentId = parentId,
                note = "Split 2: Gas"
            )
        )
        
        coEvery { mockTransactionDao.getSplitTransactions(parentId) } returns mockSplits
        
        // Act
        val result = repository.getSplitTransactions(parentId)
        
        // Assert: Repository returns split transactions from DAO
        expectThat(result) {
            hasSize(2)
            all { get { parentId }.isEqualTo(parentId) }
            any { get { note.orEmpty() }.contains("Groceries") }
            any { get { note.orEmpty() }.contains("Gas") }
        }
        
        // Verify: Repository passes correct parent ID to DAO
        coVerify(exactly = 1) { mockTransactionDao.getSplitTransactions(parentId) }
    }

    @Test
    fun `getSplitTransactionsFlow - should return reactive split transactions`() = runTest {
        // Arrange
        val parentId = 100L
        val mockSplits = listOf(
            TransactionEntity(
                id = 101L,
                fromAccountId = TestConstants.ACCOUNT_ID_1,
                categoryId = TestConstants.CATEGORY_ID_EXPENSE,
                fromAmount = -1500L,
                parentId = parentId,
                note = "Split transaction"
            )
        )
        val mockFlow = flowOf(mockSplits)
        
        coEvery { mockTransactionDao.getSplitTransactionsFlow(parentId) } returns mockFlow
        
        // Act
        val result = repository.getSplitTransactionsFlow(parentId).toList()
        
        // Assert: Repository returns reactive split transactions from DAO
        expectThat(result) {
            hasSize(1) // One emission
            first().hasSize(1) // One split transaction
            first().first().get { parentId }.isEqualTo(parentId)
        }
        
        // Verify: Repository gets split Flow from DAO
        coVerify(exactly = 1) { mockTransactionDao.getSplitTransactionsFlow(parentId) }
    }

    @Test
    fun `isSplitChild - should correctly identify split transactions`() = runTest {
        // Arrange
        val transactionId = 123L
        coEvery { mockTransactionDao.isSplitChild(transactionId) } returns true
        
        // Act
        val result = repository.isSplitChild(transactionId)
        
        // Assert: Repository correctly delegates boolean logic to DAO
        expectThat(result).isTrue()
        
        // Verify: Repository calls DAO with correct transaction ID
        coVerify(exactly = 1) { mockTransactionDao.isSplitChild(transactionId) }
    }

    @Test
    fun `isSplitParent - should correctly identify parent transactions`() = runTest {
        // Arrange
        val transactionId = 100L
        coEvery { mockTransactionDao.isSplitParent(transactionId) } returns true
        
        // Act
        val result = repository.isSplitParent(transactionId)
        
        // Assert: Repository correctly delegates parent check to DAO
        expectThat(result).isTrue()
        
        // Verify: Repository calls DAO with correct transaction ID
        coVerify(exactly = 1) { mockTransactionDao.isSplitParent(transactionId) }
    }

    @Test
    fun `isSplitTransaction - should use split count to determine if transaction has splits`() = runTest {
        // Arrange
        val transactionId = 123L
        coEvery { mockTransactionDao.getSplitCount(transactionId) } returns 3
        
        // Act
        val result = repository.isSplitTransaction(transactionId)
        
        // Assert: Repository implements business logic (split count > 0 = split transaction)
        expectThat(result).isTrue()
        
        // Verify: Repository uses getSplitCount for the business logic
        coVerify(exactly = 1) { mockTransactionDao.getSplitCount(transactionId) }
    }

    @Test
    fun `isSplitTransaction - should return false when no splits exist`() = runTest {
        // Arrange
        val transactionId = 123L
        coEvery { mockTransactionDao.getSplitCount(transactionId) } returns 0
        
        // Act
        val result = repository.isSplitTransaction(transactionId)
        
        // Assert: Repository correctly identifies non-split transactions
        expectThat(result).isFalse()
        
        // Verify: Repository checks split count
        coVerify(exactly = 1) { mockTransactionDao.getSplitCount(transactionId) }
    }

    @Test
    fun `deleteSplitTransactions - should handle cascade deletion successfully`() = runTest {
        // Arrange
        val parentId = 100L
        coEvery { mockTransactionDao.deleteSplitTransactions(parentId) } just Runs
        
        // Act
        val result = repository.deleteSplitTransactions(parentId)
        
        // Assert: Repository returns true when split deletion succeeds
        expectThat(result).isTrue()
        
        // Verify: Repository calls DAO to delete splits for parent
        coVerify(exactly = 1) { mockTransactionDao.deleteSplitTransactions(parentId) }
    }

    @Test
    fun `deleteSplitTransactions - should return false on deletion failure`() = runTest {
        // Arrange
        val parentId = 100L
        coEvery { mockTransactionDao.deleteSplitTransactions(parentId) } throws RuntimeException("Deletion failed")
        
        // Act
        val result = repository.deleteSplitTransactions(parentId)
        
        // Assert: Repository handles deletion failure gracefully
        expectThat(result).isFalse()
        
        // Verify: Repository attempted deletion despite failure
        coVerify(exactly = 1) { mockTransactionDao.deleteSplitTransactions(parentId) }
    }

    @Test
    fun `getBlotterTransactions - should return blotter view transactions`() = runTest {
        // Arrange
        val mockBlotterTransactions = listOf(
            TransactionEntity(
                id = 1L,
                fromAccountId = TestConstants.ACCOUNT_ID_1,
                categoryId = TestConstants.CATEGORY_ID_EXPENSE,
                fromAmount = -3500L,
                note = "Blotter transaction"
            )
        )
        
        coEvery { mockTransactionDao.getBlotterTransactions() } returns mockBlotterTransactions
        
        // Act
        val result = repository.getBlotterTransactions()
        
        // Assert: Repository returns blotter transactions from DAO
        expectThat(result) {
            hasSize(1)
            first().get { note }.isEqualTo("Blotter transaction")
        }
        
        // Verify: Repository delegates to DAO blotter method
        coVerify(exactly = 1) { mockTransactionDao.getBlotterTransactions() }
    }

    @Test
    fun `getBlotterTransactionsFlow - should return reactive blotter stream`() = runTest {
        // Arrange
        val mockBlotterTransactions = listOf(
            TransactionEntity(
                id = 1L,
                fromAccountId = TestConstants.ACCOUNT_ID_1,
                categoryId = TestConstants.CATEGORY_ID_INCOME,
                fromAmount = 5000L,
                note = "Blotter income"
            )
        )
        val mockFlow = flowOf(mockBlotterTransactions)
        
        coEvery { mockTransactionDao.getBlotterTransactionsFlow() } returns mockFlow
        
        // Act
        val result = repository.getBlotterTransactionsFlow().toList()
        
        // Assert: Repository returns reactive blotter stream from DAO
        expectThat(result) {
            hasSize(1) // One emission
            first().hasSize(1) // One transaction
            first().first().get { note }.isEqualTo("Blotter income")
        }
        
        // Verify: Repository gets blotter Flow from DAO
        coVerify(exactly = 1) { mockTransactionDao.getBlotterTransactionsFlow() }
    }

    @Test
    fun `getTransactionWithSplits - should return transaction with its splits`() = runTest {
        // Arrange
        val transactionId = 100L
        val parentTransaction = TransactionEntity(
            id = transactionId,
            fromAccountId = TestConstants.ACCOUNT_ID_1,
            categoryId = TestConstants.CATEGORY_ID_EXPENSE,
            fromAmount = -10000L,
            note = "Split parent"
        )
        val splits = listOf(
            TransactionEntity(
                id = 101L,
                fromAccountId = TestConstants.ACCOUNT_ID_1,
                categoryId = TestConstants.CATEGORY_ID_EXPENSE,
                fromAmount = -6000L,
                parentId = transactionId,
                note = "Split 1"
            ),
            TransactionEntity(
                id = 102L,
                fromAccountId = TestConstants.ACCOUNT_ID_1,
                categoryId = TestConstants.CATEGORY_ID_EXPENSE,
                fromAmount = -4000L,
                parentId = transactionId,
                note = "Split 2"
            )
        )
        val mockTransactionWithSplits = TransactionWithSplits(
            transaction = parentTransaction,
            splits = splits
        )
        
        coEvery { mockTransactionDao.getTransactionWithSplits(transactionId) } returns mockTransactionWithSplits
        
        // Act
        val result = repository.getTransactionWithSplits(transactionId)
        if (result == null) {
            fail("Result is null")
            return@runTest
        }
        // Assert: Repository returns transaction with splits from DAO
        expectThat(result) {
            get { transaction.id }.isEqualTo(transactionId)
            get { splits }.hasSize(2)
            get { isSplitTransaction }.isTrue()
        }
        
        // Verify: Repository passes correct transaction ID to DAO
        coVerify(exactly = 1) { mockTransactionDao.getTransactionWithSplits(transactionId) }
    }

    @Test
    fun `getTransactionWithSplits - should return null when transaction not found`() = runTest {
        // Arrange
        val nonExistentId = 999L
        coEvery { mockTransactionDao.getTransactionWithSplits(nonExistentId) } returns null
        
        // Act
        val result = repository.getTransactionWithSplits(nonExistentId)
        
        // Assert: Repository correctly propagates null from DAO
        expectThat(result).isNull()
        
        // Verify: Repository attempts to find transaction with splits
        coVerify(exactly = 1) { mockTransactionDao.getTransactionWithSplits(nonExistentId) }
    }

    @Test
    fun `getSplitCount - should return split count from DAO`() = runTest {
        // Arrange
        val transactionId = 100L
        val expectedCount = 3
        
        coEvery { mockTransactionDao.getSplitCount(transactionId) } returns expectedCount
        
        // Act
        val result = repository.getSplitCount(transactionId)
        
        // Assert: Repository returns the split count from DAO
        expectThat(result).isEqualTo(expectedCount)
        
        // Verify: Repository passes correct transaction ID to DAO
        coVerify(exactly = 1) { mockTransactionDao.getSplitCount(transactionId) }
    }

    @Test
    fun `getAllSplitParents - should return all parent transactions`() = runTest {
        // Arrange
        val mockSplitParents = listOf(
            TransactionEntity(
                id = 100L,
                fromAccountId = TestConstants.ACCOUNT_ID_1,
                categoryId = TestConstants.CATEGORY_ID_EXPENSE,
                fromAmount = -10000L, // Total that will be split
                note = "Split parent transaction"
            )
        )
        
        coEvery { mockTransactionDao.getAllSplitParents() } returns mockSplitParents
        
        // Act
        val result = repository.getAllSplitParents()
        
        // Assert: Repository returns all split parent transactions from DAO
        expectThat(result) {
            hasSize(1)
            first().get { note }.isEqualTo("Split parent transaction")
        }
        
        // Verify: Repository delegates to DAO split parents method
        coVerify(exactly = 1) { mockTransactionDao.getAllSplitParents() }
    }
}