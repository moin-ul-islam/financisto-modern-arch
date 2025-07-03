# Financisto Refactoring Path: MVC to MVVM with Room

## Overview

This document provides a step-by-step guide for refactoring Financisto from MVC to MVVM architecture with Room database, while preserving functionality and leveraging existing tests.

## Refactoring Strategy

### Principles
1. **Preserve Functionality**: All features must work identically
2. **Test-Driven**: Use existing tests to validate each step
3. **Incremental**: Small, manageable steps
4. **Backward Compatible**: Ability to rollback if needed

### Phases Overview
1. **Preparation** - Setup and analysis
2. **MVVM Foundation** - Introduce ViewModels and LiveData
3. **Repository Pattern** - Abstract data access
4. **Room Migration** - Replace custom ORM
5. **UI Modernization** - Decouple Activities from business logic
6. **Testing & Cleanup** - Expand test coverage and remove legacy code

## Phase 1: Preparation (Weeks 1-2)

### 1.1 Setup Modern Development Environment
**Tasks**:
- Update to latest Android Gradle Plugin
- Add Room, Hilt, ViewModel dependencies
- Configure Kotlin (optional)
- Set up code quality tools

**Dependencies**:
```gradle
dependencies {
    // Room
    implementation "androidx.room:room-runtime:2.5.0"
    implementation "androidx.room:room-ktx:2.5.0"
    kapt "androidx.room:room-compiler:2.5.0"
    
    // ViewModel and LiveData
    implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1"
    implementation "androidx.lifecycle:lifecycle-livedata-ktx:2.6.1"
    
    // Dependency Injection
    implementation "com.google.dagger:hilt-android:2.44"
    kapt "com.google.dagger:hilt-compiler:2.44"
}
```

### 1.2 Analyze Existing Tests
**Key Test Classes to Focus On**:
- `RunningBalanceTest.java` - Complex balance calculations
- `AccountTotalTest.java` - Account balance logic
- `DatabaseAdapterTest.java` - Core database operations
- `TransactionTest.java` - Transaction entity logic

### 1.3 Create Migration Plan
**Migration Order**:
1. **Models** - Convert to Room entities
2. **Data Access** - Create DAOs and repositories
3. **Business Logic** - Move to ViewModels
4. **UI** - Decouple Activities from business logic

## Phase 2: MVVM Foundation (Weeks 3-6)

### 2.1 Create Base Architecture Classes
```kotlin
// Base ViewModel
abstract class BaseViewModel : ViewModel() {
    protected val _uiState = MutableLiveData<UiState>()
    val uiState: LiveData<UiState> = _uiState
}

// Base Repository
interface BaseRepository<T> {
    suspend fun getAll(): List<T>
    suspend fun getById(id: Long): T?
    suspend fun insert(item: T): Long
    suspend fun update(item: T): Int
    suspend fun delete(id: Long): Int
}
```

### 2.2 Start with Simple Models
**Start with**: Currency, Project, Payee, MyLocation

**Example: Currency Migration**:
```kotlin
@Entity(tableName = "currency")
data class Currency(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val title: String,
    val symbol: String,
    val isDefault: Boolean = false
)

@Dao
interface CurrencyDao {
    @Query("SELECT * FROM currency ORDER BY name")
    suspend fun getAll(): List<Currency>
    
    @Insert
    suspend fun insert(currency: Currency): Long
}
```

### 2.3 Create First ViewModel
```kotlin
@HiltViewModel
class CurrencyViewModel @Inject constructor(
    private val currencyRepository: CurrencyRepository
) : BaseViewModel() {
    
    private val _currencies = MutableLiveData<List<Currency>>()
    val currencies: LiveData<List<Currency>> = _currencies
    
    fun loadCurrencies() {
        viewModelScope.launch {
            setLoading()
            try {
                val result = currencyRepository.getAll()
                _currencies.value = result
                _uiState.value = UiState.Success
            } catch (e: Exception) {
                setError(e.message ?: "Unknown error")
            }
        }
    }
}
```

## Phase 3: Repository Pattern (Weeks 7-10)

### 3.1 Create Repository Interfaces
```kotlin
interface TransactionRepository {
    suspend fun getTransactions(filter: TransactionFilter): List<Transaction>
    suspend fun getTransaction(id: Long): Transaction?
    suspend fun insertTransaction(transaction: Transaction): Long
    suspend fun getSplitsForTransaction(transactionId: Long): List<Transaction>
    suspend fun rebuildRunningBalance(accountId: Long)
}
```

### 3.2 Implement Repository Layer
**Strategy**: Wrap current DatabaseAdapter as implementation detail

```kotlin
class TransactionRepositoryImpl @Inject constructor(
    private val databaseAdapter: DatabaseAdapter
) : TransactionRepository {
    
    override suspend fun getTransactions(filter: TransactionFilter): List<Transaction> {
        return withContext(Dispatchers.IO) {
            val cursor = databaseAdapter.getBlotter(filter.toWhereFilter())
            cursor.use { cursor.toTransactionList() }
        }
    }
}
```

### 3.3 Create Caching Layer
```kotlin
@Singleton
class TransactionCache @Inject constructor() {
    private val cache = LruCache<String, List<Transaction>>(100)
    
    fun get(key: String): List<Transaction>? = cache.get(key)
    fun put(key: String, transactions: List<Transaction>) {
        cache.put(key, transactions)
    }
    fun invalidate() = cache.evictAll()
}
```

## Phase 4: Room Migration (Weeks 11-18)

### 4.1 Create Room Database
```kotlin
@Database(
    entities = [
        Account::class,
        Transaction::class,
        Category::class,
        Currency::class
    ],
    version = 1
)
abstract class FinancistoDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun currencyDao(): CurrencyDao
}
```

### 4.2 Migrate Complex Entities
**Priority Order**:
1. **Account** - Core entity with relationships
2. **Category** - Hierarchical structure
3. **Transaction** - Most complex entity
4. **Budget** - Complex calculations

**Example: Account Entity**:
```kotlin
@Entity(
    tableName = "account",
    foreignKeys = [
        ForeignKey(
            entity = Currency::class,
            parentColumns = ["id"],
            childColumns = ["currency_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Account(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val currencyId: Long,
    val type: String = AccountType.CASH.name,
    val totalAmount: Long = 0,
    val isActive: Boolean = true
)
```

### 4.3 Handle Complex Relationships
**Challenges**:
- **Split Transactions**: Parent-child relationships
- **Hierarchical Categories**: Nested set model
- **Multi-currency Transactions**: Exchange rate relationships

**Solution**:
```kotlin
@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val parentId: Long = 0, // For split transactions
    val fromAccountId: Long,
    val toAccountId: Long = 0,
    val categoryId: Long = 0
)

@Dao
interface CategoryDao {
    @Query("""
        SELECT * FROM category 
        WHERE left BETWEEN :left AND :right 
        ORDER BY left
    """)
    suspend fun getSubcategories(left: Int, right: Int): List<Category>
}
```

### 4.4 Create Migration Scripts
```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE account ADD COLUMN note TEXT")
        database.execSQL("ALTER TABLE account ADD COLUMN is_active INTEGER DEFAULT 1")
    }
}
```

### 4.5 Implement Complex Queries
**Critical Queries**:
- Running Balance Calculations
- Budget Calculations
- Transaction Filtering
- Category Hierarchy

```kotlin
@Dao
interface TransactionDao {
    @Query("""
        SELECT 
            t.*,
            COALESCE(SUM(t2.from_amount), 0) as running_balance
        FROM transactions t
        LEFT JOIN transactions t2 ON t2.from_account_id = t.from_account_id 
            AND t2.datetime <= t.datetime
        WHERE t.from_account_id = :accountId
        GROUP BY t.id
        ORDER BY t.datetime DESC, t.id DESC
    """)
    suspend fun getTransactionsWithRunningBalance(accountId: Long): List<TransactionWithBalance>
}
```

## Phase 5: UI Modernization (Weeks 19-24)

### 5.1 Decouple Activities from Business Logic
**Strategy**: Move business logic to ViewModels

```kotlin
// Before: Activity with business logic
class TransactionActivity : AppCompatActivity() {
    private lateinit var db: DatabaseAdapter
    
    private fun saveTransaction() {
        val transaction = createTransactionFromUI()
        val id = db.insertOrUpdate(transaction)
        if (id > 0) finish() else showError("Failed to save")
    }
}

// After: Activity with ViewModel
@AndroidEntryPoint
class TransactionActivity : AppCompatActivity() {
    private val viewModel: TransactionViewModel by viewModels()
    
    private fun saveTransaction() {
        val transaction = createTransactionFromUI()
        viewModel.saveTransaction(transaction)
    }
    
    private fun observeViewModel() {
        viewModel.saveResult.observe(this) { result ->
            when (result) {
                is Result.Success -> finish()
                is Result.Error -> showError(result.exception.message)
            }
        }
    }
}
```

### 5.2 Create ViewModels for Complex Features
```kotlin
@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : BaseViewModel() {
    
    private val _transaction = MutableLiveData<Transaction>()
    val transaction: LiveData<Transaction> = _transaction
    
    fun loadTransaction(id: Long) {
        viewModelScope.launch {
            setLoading()
            try {
                val transaction = transactionRepository.getTransaction(id)
                _transaction.value = transaction
                _uiState.value = UiState.Success
            } catch (e: Exception) {
                setError(e.message ?: "Failed to load transaction")
            }
        }
    }
    
    fun saveTransaction(transaction: Transaction) {
        viewModelScope.launch {
            setLoading()
            try {
                val id = if (transaction.id > 0) {
                    transactionRepository.updateTransaction(transaction)
                } else {
                    transactionRepository.insertTransaction(transaction)
                }
                
                if (id > 0) {
                    _uiState.value = UiState.Success
                } else {
                    setError("Failed to save transaction")
                }
            } catch (e: Exception) {
                setError(e.message ?: "Failed to save transaction")
            }
        }
    }
}
```

### 5.3 Implement Reactive UI Updates
```kotlin
@HiltViewModel
class BlotterViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : BaseViewModel() {
    
    private val _transactions = MutableLiveData<List<Transaction>>()
    val transactions: LiveData<List<Transaction>> = _transactions
    
    fun loadTransactions(filter: TransactionFilter) {
        viewModelScope.launch {
            setLoading()
            try {
                val transactions = transactionRepository.getTransactions(filter)
                _transactions.value = transactions
                _uiState.value = UiState.Success
            } catch (e: Exception) {
                setError(e.message ?: "Failed to load transactions")
            }
        }
    }
}
```

## Phase 6: Testing & Cleanup (Weeks 25-28)

### 6.1 Migrate Existing Tests
**Strategy**: Convert existing tests to work with new architecture

```kotlin
// Old test
@Test
public void should_update_running_balance_for_single_account() {
    Transaction t1 = TransactionBuilder.withDb(db).account(a1).amount(1000).create();
    Transaction t2 = TransactionBuilder.withDb(db).account(a1).amount(1234).create();
    db.rebuildRunningBalanceForAccount(a1);
    assertAccountBalanceForTransaction(t1, a1, 1000);
    assertAccountBalanceForTransaction(t2, a1, 2234);
}

// New test
@Test
fun should_update_running_balance_for_single_account_room() = runTest {
    val t1 = TransactionBuilder.withRoom(transactionDao).account(a1).amount(1000).create()
    val t2 = TransactionBuilder.withRoom(transactionDao).account(a1).amount(1234).create()
    runningBalanceRepository.rebuildRunningBalanceForAccount(a1.id)
    assertAccountBalanceForTransaction(t1, a1, 1000)
    assertAccountBalanceForTransaction(t2, a1, 2234)
}
```

### 6.2 Add ViewModel Tests
```kotlin
@RunWith(MockitoJUnitRunner::class)
class TransactionViewModelTest {
    
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()
    
    @Mock
    private lateinit var transactionRepository: TransactionRepository
    
    private lateinit var viewModel: TransactionViewModel
    
    @Test
    fun `loadTransaction should update transaction`() = runTest {
        // Given
        val transaction = createTestTransaction()
        whenever(transactionRepository.getTransaction(1L)).thenReturn(transaction)
        
        // When
        viewModel.loadTransaction(1L)
        
        // Then
        assertEquals(transaction, viewModel.transaction.value)
    }
}
```

### 6.3 Add Repository Tests
```kotlin
@RunWith(AndroidJUnit4::class)
class TransactionRepositoryTest {
    
    private lateinit var database: FinancistoDatabase
    private lateinit var repository: TransactionRepository
    
    @Test
    fun insertTransaction_shouldReturnId() = runTest {
        // Given
        val transaction = createTestTransaction()
        
        // When
        val id = repository.insertTransaction(transaction)
        
        // Then
        assertTrue(id > 0)
    }
}
```

### 6.4 Performance Testing
```kotlin
@Test
fun largeTransactionList_shouldLoadQuickly() = runTest {
    // Given
    val largeTransactionList = createLargeTransactionList(10000)
    insertTestData(largeTransactionList)
    
    // When
    val startTime = System.currentTimeMillis()
    val result = repository.getTransactions(TransactionFilter())
    val endTime = System.currentTimeMillis()
    
    // Then
    assertTrue(endTime - startTime < 1000) // Should load in under 1 second
    assertEquals(10000, result.size)
}
```

### 6.5 Remove Legacy Code
**Tasks**:
- Remove old DatabaseAdapter usage
- Remove custom ORM framework
- Remove old test classes
- Clean up unused dependencies

## Risk Mitigation

### 1. **Data Loss Risk**
- Comprehensive backup before migration
- Test migrations with real user data
- Rollback plan if issues arise

### 2. **Performance Regression**
- Performance testing at each phase
- Benchmarking against current implementation
- Monitoring during migration

### 3. **Functionality Loss**
- Comprehensive test coverage
- Feature-by-feature validation
- User acceptance testing

## Success Metrics

### 1. **Functionality**
- All existing features work identically
- No data loss during migration
- Performance maintained or improved

### 2. **Code Quality**
- Reduced code complexity
- Improved testability
- Better separation of concerns

### 3. **Maintainability**
- Easier to add new features
- Better error handling
- Clearer code structure

## Conclusion

This refactoring path provides a systematic approach to modernizing Financisto while preserving functionality. Key success factors:

1. **Test-Driven Approach**: Use existing tests to validate each step
2. **Incremental Migration**: Small, manageable changes with frequent validation
3. **Comprehensive Testing**: Ensure no regressions at each phase
4. **Performance Monitoring**: Maintain or improve performance throughout

The result will be a modern, maintainable codebase following current Android development best practices. 