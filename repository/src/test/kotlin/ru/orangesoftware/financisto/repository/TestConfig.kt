package ru.orangesoftware.financisto.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Test configuration utilities for repository module tests.
 * 
 * This provides common test setup including:
 * - Coroutine test dispatcher setup
 * - Test rules for consistent test execution
 * - Common test utilities
 */

/**
 * JUnit rule to set up test coroutine dispatcher for all repository tests.
 * Use this rule in test classes that use coroutines.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CoroutineTestRule(
    val testDispatcher: TestDispatcher = StandardTestDispatcher()
) : TestWatcher() {
    
    override fun starting(description: Description) {
        super.starting(description)
        Dispatchers.setMain(testDispatcher)
    }
    
    override fun finished(description: Description) {
        super.finished(description)
        Dispatchers.resetMain()
    }
}

/**
 * Test constants and common test data
 */
object TestConstants {
    const val TEST_TIMEOUT_MS = 5000L
    
    // Common test entity IDs
    const val ACCOUNT_ID_1 = 1L
    const val ACCOUNT_ID_2 = 2L
    const val CURRENCY_ID_USD = 1L
    const val CURRENCY_ID_EUR = 2L
    const val CATEGORY_ID_EXPENSE = 1L
    const val CATEGORY_ID_INCOME = 2L
}