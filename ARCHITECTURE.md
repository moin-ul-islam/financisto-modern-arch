# Financisto Architecture Documentation

## Current Architecture Overview

Financisto is built using the **Model-View-Controller (MVC)** pattern, but with significant deviations from clean architecture principles. The current implementation suffers from tight coupling and poor separation of concerns.

### MVC Implementation

#### Model Layer (`model/`, `db/`)
- **Entities**: Core data models in `model/` package
  - `Account`, `Transaction`, `Category`, `Project`, `Budget`, `Currency`, etc.
  - Uses custom JPA-like annotations (`@Entity`, `@Column`, `@Table`)
  - Extends `MyEntity` base class for common functionality
- **Data Access**: Custom ORM implementation
  - `DatabaseAdapter` - Main database interface (75KB, 1851 lines)
  - `MyEntityManager` - Base entity manager extending custom ORM
  - `orb/` package - Custom ORM framework with query builder
  - `javax/persistence/` - Custom JPA-like annotations
- **Database Schema**: SQLite with raw SQL files in `assets/database/create/`

#### View Layer (`adapter/`, `view/`, `res/layout/`)
- **Adapters**: List adapters for displaying data
  - `BlotterListAdapter` - Main transaction list adapter
  - `TransactionsListAdapter` - Transaction display logic
  - `AccountListAdapter2` - Account list display
- **Custom Views**: Specialized UI components
  - `AmountInput` - Currency amount input widget
  - `RateLayoutView` - Exchange rate display
  - `AttributeView` - Dynamic attribute display
- **Layouts**: XML layouts in `res/layout/`

#### Controller Layer (`activity/`)
- **Activities**: Handle both UI logic and business logic
  - `AbstractTransactionActivity` - Base for transaction screens (682 lines)
  - `BlotterActivity` - Main transaction list (22KB)
  - `AccountActivity` - Account management (17KB)
  - `BudgetActivity` - Budget management (11KB)
- **Issues**: Activities are monolithic, handling:
  - UI state management
  - Business logic
  - Data access
  - User input validation
  - Navigation logic

### Key Architectural Problems

#### 1. **Lack of Separation of Concerns**
- Activities combine UI, business logic, and data access
- No clear boundaries between layers
- Business logic scattered across Activities and Adapters

#### 2. **Tight Coupling**
- Direct database access from Activities
- UI components directly manipulate data models
- Hard to test individual components

#### 3. **Custom ORM Instead of Modern Solutions**
- Custom `orb/` framework instead of Room
- Manual SQL query building
- No compile-time SQL validation
- Difficult to maintain and extend

#### 4. **No Reactive Programming**
- No LiveData or StateFlow
- Manual UI updates
- No lifecycle-aware data handling

#### 5. **Poor Testability**
- Activities are hard to unit test
- Business logic mixed with UI code
- No dependency injection

#### 6. **Outdated Patterns**
- Heavy inheritance hierarchies
- No repository pattern
- No ViewModels
- Manual cursor management

### Current Data Flow

```
Activity → DatabaseAdapter → SQLite
    ↓           ↓
  UI Logic   Business Logic
    ↓           ↓
  Adapters   Entity Models
```

### Dependencies and Libraries

- **Android Annotations**: Used for dependency injection (`@EBean`, `@EApplication`)
- **GreenRobot EventBus**: For event communication
- **Custom ORM**: `orb/` package for database abstraction
- **Robolectric**: For unit testing
- **RxJava**: Limited use for reactive programming

### Database Schema

The app uses SQLite with the following core tables:
- `account` - Financial accounts
- `transactions` - Financial transactions
- `category` - Expense/income categories (hierarchical)
- `project` - Project categorization
- `budget` - Budget definitions
- `currency` - Currency definitions
- `attributes` - Custom attributes system

### Migration Challenges

1. **Complex Business Logic**: Running balance calculations, split transactions, multi-currency support
2. **Hierarchical Data**: Categories use nested set model (left/right values)
3. **Custom Attributes**: Dynamic attribute system for transactions
4. **Legacy Code**: Large, monolithic classes that need careful refactoring
5. **Test Coverage**: Existing tests need to be preserved and extended

### Next Steps for Modernization

The refactoring should follow this order:
1. **Introduce MVVM** - Add ViewModels and LiveData
2. **Repository Pattern** - Abstract data access
3. **Room Migration** - Replace custom ORM
4. **Dependency Injection** - Use Hilt or Koin
5. **Reactive UI** - Implement proper data binding
6. **Testing** - Expand test coverage with new architecture 