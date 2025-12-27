# Repository Module Testing Setup

This document describes the testing setup for the repository module using **MockK** and **Strikt**.

## Testing Dependencies

The following testing libraries are configured:

- **JUnit 4.13.2** - Test runner and basic assertions
- **MockK 1.13.8** - Mocking framework designed for Kotlin
- **Strikt 0.34.1** - Modern assertion library for Kotlin
- **Kotlinx Coroutines Test 1.7.3** - Testing utilities for coroutines
- **Room Testing 2.6.1** - In-memory database testing
- **Hilt Android Testing 2.51.1** - Dependency injection testing

## Key Features

### MockK Benefits
- **Kotlin-first**: Built specifically for Kotlin with full language support
- **Coroutine Support**: Native support for suspend functions and coroutines
- **DSL Syntax**: Intuitive Kotlin DSL for mocking and verification
- **Relaxed Mocks**: Can create relaxed mocks with default return values

### Strikt Benefits
- **Type Safety**: Compile-time type checking for assertions
- **Expressive DSL**: Block-based syntax for complex assertions
- **Better Error Messages**: Detailed failure messages with context
- **Collection Support**: Rich support for testing collections and data structures

## Test Structure

### Test Configuration
- `TestConfig.kt` - Common test utilities and coroutine test rule
- `CoroutineTestRule` - JUnit rule for setting up test dispatchers

### Test Classes
- `AccountRepositoryTest.kt` - Sample test for AccountRepository
- `TransactionRepositoryTest.kt` - Sample test for TransactionRepository

## Testing Patterns

### Basic Repository Test Structure
```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class YourRepositoryTest {
    @get:Rule
    val coroutineTestRule = CoroutineTestRule()
    
    private lateinit var mockDao: YourDao
    private lateinit var repository: YourRepositoryImpl
    
    @Before
    fun setUp() {
        mockDao = mockk()
        repository = YourRepositoryImpl(
            dao = mockDao,
            ioDispatcher = coroutineTestRule.testDispatcher
        )
    }
    
    @Test
    fun `test method`() = runTest {
        // Arrange
        coEvery { mockDao.someMethod() } returns expectedResult
        
        // Act
        val result = repository.someMethod()
        
        // Assert
        expectThat(result) {
            // Strikt assertions here
        }
        
        // Verify
        coVerify { mockDao.someMethod() }
    }
}
```

### MockK Patterns
```kotlin
// Basic mocking
coEvery { mockDao.getData() } returns testData

// Return different values on subsequent calls
coEvery { mockDao.getData() } returnsMany listOf(data1, data2)

// Mock with parameters
coEvery { mockDao.getDataById(any()) } returns testData

// Mock exceptions
coEvery { mockDao.getData() } throws RuntimeException("Test error")

// Verification
coVerify(exactly = 1) { mockDao.getData() }
coVerify { mockDao.insertData(match { it.id > 0 }) }
```

### Strikt Assertion Patterns
```kotlin
// Basic assertions
expectThat(result).isEqualTo(expected)
expectThat(result).isNotNull()

// Collection assertions
expectThat(list) {
    hasSize(3)
    containsExactly(item1, item2, item3)
    any { get { property }.isEqualTo(value) }
}

// Complex object assertions
expectThat(entity) {
    get { id }.isEqualTo(1L)
    get { name }.isNotBlank()
    get { isActive }.isTrue()
}

// Block assertions for multiple checks
expectThat(result) {
    isNotNull()
    hasSize(2)
    first().get { title }.startsWith("Test")
}
```

### Flow Testing
```kotlin
@Test
fun `test flow operations`() = runTest {
    val mockFlow = flowOf(testData)
    coEvery { mockDao.getDataFlow() } returns mockFlow
    
    val emissions = repository.getDataFlow().toList()
    
    expectThat(emissions) {
        hasSize(1)
        first().hasSize(expectedSize)
    }
}
```

## Running Tests

### Command Line
```bash
# Run all repository tests
./gradlew :repository:test

# Run with coverage
./gradlew :repository:testDebugUnitTestCoverage

# Run specific test class
./gradlew :repository:test --tests "AccountRepositoryTest"
```

### Android Studio
- Right-click on test class/method and select "Run"
- Use the test runner panel for detailed results
- View coverage reports in the coverage tab

## Best Practices

1. **Use `runTest`** for all coroutine-based tests
2. **Mock at the DAO level** - test repository logic, not database operations
3. **Use Strikt blocks** for complex assertions with multiple checks
4. **Verify interactions** with `coVerify` to ensure proper DAO calls
5. **Test error scenarios** - exceptions, null returns, constraint violations
6. **Use descriptive test names** - describe the scenario and expected outcome
7. **Arrange-Act-Assert pattern** - keep tests well-structured
8. **Test one thing per test** - focused, single-responsibility tests

## Sample Test Implementation

See the sample test classes for complete examples of:
- Repository constructor injection testing
- Suspend function testing with coroutines
- Flow-based operations testing  
- Error handling scenarios
- Complex business logic validation
- MockK verification patterns
- Strikt assertion techniques

## Next Steps

To implement actual tests:
1. Choose a repository to test
2. Identify the key methods and scenarios
3. Follow the sample test structure
4. Implement arrange-act-assert pattern
5. Add both success and failure test cases
6. Verify DAO interactions with MockK
7. Use Strikt for expressive assertions