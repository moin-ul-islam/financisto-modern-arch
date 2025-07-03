# Financisto Codebase Structure

## Directory Overview

This document provides a detailed breakdown of the Financisto codebase structure, explaining the purpose and contents of each major directory.

## Main Package Structure

### `app/src/main/java/ru/orangesoftware/financisto/`

#### Core Application
- **`app/`** - Application-level components
  - `FinancistoApp.java` - Main Application class with dependency injection setup
  - Uses Android Annotations for DI (`@EBean`, `@Bean`)

#### Model Layer (`model/`)
Core data entities and business logic:

- **`Account.java`** - Financial account entity
  - Properties: currency, type, balance, limits, closing/payment days
  - Supports different account types (cash, bank, card)
  
- **`Transaction.java`** - Core transaction entity
  - Properties: accounts, amounts, category, project, location, date/time
  - Supports splits, transfers, templates, recurring transactions
  - Complex business logic for multi-currency transactions
  
- **`Category.java`** - Hierarchical category system
  - Uses nested set model (left/right values) for hierarchy
  - Supports income/expense types
  - Extends `CategoryEntity<T>` for tree structure
  
- **`Project.java`** - Project categorization
  - Simple entity for grouping transactions by project
  
- **`Budget.java`** - Budget management
  - Complex entity with categories, projects, date ranges
  - Supports recurring budgets and subcategory inclusion
  
- **`Currency.java`** - Currency definitions
  - Exchange rates, symbols, formatting
  
- **`MyEntity.java`** - Base class for all entities
  - Common properties: id, title, isActive, sortOrder
  - Utility methods for entity management

#### Data Access Layer (`db/`)
Database abstraction and data access:

- **`DatabaseAdapter.java`** - Main database interface (75KB, 1851 lines)
  - CRUD operations for all entities
  - Complex business logic for transactions, splits, transfers
  - Account balance calculations
  - Running balance management
  
- **`MyEntityManager.java`** - Base entity manager
  - Extends custom ORM framework
  - Generic entity operations
  - Query building and filtering
  
- **`DatabaseHelper.java`** - SQLite database helper
  - Database creation and schema management
  - Column definitions and constants
  
- **`DatabaseSchemaEvolution.java`** - Schema migration
  - Handles database version upgrades
  - Migration scripts for schema changes

#### Custom ORM (`orb/`)
Custom Object-Relational Mapping framework:

- **`EntityManager.java`** - Base ORM functionality
- **`Query.java`** - Query builder
- **`Expressions.java`** - SQL expression building
- **`EntityDefinition.java`** - Entity metadata
- **`FieldType.java`** - Field type definitions

#### JPA-like Annotations (`javax/persistence/`)
Custom JPA-like annotations for entity mapping:

- **`@Entity`** - Marks entity classes
- **`@Table`** - Specifies table names
- **`@Column`** - Maps fields to columns
- **`@JoinColumn`** - Foreign key relationships
- **`@Id`** - Primary key identification

#### Controller Layer (`activity/`)
Android Activities handling UI and business logic:

- **`AbstractTransactionActivity.java`** - Base for transaction screens (682 lines)
  - Common transaction UI logic
  - Form handling, validation, saving
  - Split transaction management
  
- **`BlotterActivity.java`** - Main transaction list (22KB)
  - Transaction display and filtering
  - Bulk operations
  - Search and sorting
  
- **`AccountActivity.java`** - Account management (17KB)
  - Account creation, editing, deletion
  - Balance management
  
- **`BudgetActivity.java`** - Budget management (11KB)
  - Budget creation and editing
  - Category and project selection
  
- **`CategoryActivity.java`** - Category management (17KB)
  - Hierarchical category editing
  - Tree structure management
  
- **`MainActivity.java`** - Main navigation hub
  - Tab-based navigation
  - Menu management

#### View Layer (`adapter/`)
List adapters for displaying data:

- **`BlotterListAdapter.java`** - Main transaction list adapter (15KB, 340 lines)
  - Transaction display logic
  - Transfer and split handling
  - Status indicators and colors
  
- **`TransactionsListAdapter.java`** - Transaction display
  - Amount formatting
  - Currency display
  - Date/time formatting
  
- **`AccountListAdapter2.java`** - Account list display
  - Account balance display
  - Account type icons
  
- **`CategoryListAdapter2.java`** - Category tree display
  - Hierarchical category display
  - Indentation and icons

#### Custom Views (`view/`, `widget/`)
Specialized UI components:

- **`AmountInput.java`** - Currency amount input widget
  - Amount formatting and validation
  - Currency symbol display
  - Income/expense toggle
  
- **`RateLayoutView.java`** - Exchange rate display
  - Multi-currency transaction support
  - Rate calculation display
  
- **`AttributeView.java`** - Dynamic attribute display
  - Custom attribute rendering
  - Different input types (text, number, list, checkbox)

#### Business Logic (`blotter/`, `report/`, `rates/`)
Domain-specific business logic:

- **`blotter/`** - Transaction list business logic
  - Filtering and sorting
  - Total calculations
  
- **`report/`** - Reporting functionality
  - Various report types
  - Data aggregation
  
- **`rates/`** - Exchange rate management
  - Rate downloading and caching
  - Currency conversion

#### Utilities (`utils/`)
Helper classes and utilities:

- **`MyPreferences.java`** - Application preferences (34KB, 767 lines)
  - User settings management
  - Default values and validation
  
- **`TransactionUtils.java`** - Transaction utilities
  - Transaction creation helpers
  - Formatting utilities
  
- **`DateUtils.java`** - Date/time utilities
  - Date formatting and parsing
  - Period calculations

#### Export/Import (`export/`)
Data export and import functionality:

- **`csv/`** - CSV export/import
- **`qif/`** - QIF format support
- **`drive/`** - Google Drive integration
- **`dropbox/`** - Dropbox integration

#### Backup (`backup/`)
Database backup and restore:

- **`Backup.java`** - Backup creation
- **`DatabaseExport.java`** - Database export
- **`DatabaseImport.java`** - Database import

#### Services (`service/`)
Background services:

- **`ScheduledTransactionService.java`** - Recurring transactions
- **`AutoBackupService.java`** - Automatic backups

#### Communication (`bus/`)
Event communication:

- **`GreenRobotBus.java`** - EventBus implementation
- **`RefreshCurrentTab.java`** - UI refresh events

## Database Schema

### Core Tables (`assets/database/create/`)

#### `account.sql`
```sql
create table if not exists account ( 
    _id integer primary key autoincrement, 
    title text not null, 
    creation_date long not null,
    currency_id integer not null,
    total_amount integer not null default 0
);
```

#### `transaction.sql`
```sql
create table if not exists transactions ( 
    _id integer primary key autoincrement,
    from_account_id long not null,
    to_account_id long not null default 0,
    category_id long not null default 0,
    project_id long not null default 0,
    location_id long not null default 0,
    note text,
    from_amount integer not null default 0,
    to_amount integer not null default 0,
    datetime long not null,
    provider text,
    accuracy float,
    latitude double,
    longitude double
);
```

#### `category.sql`
```sql
create table if not exists category ( 
    _id integer primary key autoincrement,
    title text not null,
    left integer not null default 0,
    right integer not null default 0
);
```

#### `budget.sql`
```sql
create table if not exists budget ( 
    _id integer primary key autoincrement,
    title text,
    category_id long not null,        
    currency_id long not null,
    amount integer not null,
    include_subcategories integer not null default 1,
    start_date long,
    end_date long,
    repeat integer
);
```

## Key Design Patterns

### 1. **Builder Pattern**
Used extensively in test classes:
- `TransactionBuilder` - Transaction creation
- `AccountBuilder` - Account creation
- `CategoryBuilder` - Category creation

### 2. **Template Method Pattern**
- `AbstractTransactionActivity` - Common transaction logic
- `AbstractListActivity` - Common list functionality

### 3. **Observer Pattern**
- EventBus for communication between components
- Listener interfaces for UI callbacks

### 4. **Factory Pattern**
- `AttributeViewFactory` - Dynamic view creation
- `RecurrenceViewFactory` - Recurrence UI creation

## Dependencies

### External Libraries
- **Android Annotations** - Dependency injection
- **GreenRobot EventBus** - Event communication
- **RxJava/RxAndroid** - Reactive programming
- **Robolectric** - Unit testing
- **Glide** - Image loading
- **Material DateTime Picker** - Date/time selection

### Internal Dependencies
- **Custom ORM** (`orb/`) - Database abstraction
- **Custom JPA** (`javax/persistence/`) - Entity mapping
- **Custom UI Components** - Specialized widgets

## Code Quality Issues

### 1. **Large Classes**
- `DatabaseAdapter` - 1851 lines
- `MyPreferences` - 767 lines
- `BlotterActivity` - 22KB

### 2. **Tight Coupling**
- Activities directly access database
- UI components tightly coupled to data models
- Hard to test individual components

### 3. **Mixed Responsibilities**
- Activities handle UI, business logic, and data access
- Adapters contain business logic
- Models contain UI logic

### 4. **Inconsistent Patterns**
- Mix of inheritance and composition
- Inconsistent error handling
- No standard approach to async operations

## Migration Considerations

### 1. **Preserve Business Logic**
- Complex transaction handling
- Multi-currency support
- Hierarchical categories
- Running balance calculations

### 2. **Maintain Data Integrity**
- Existing database schema
- User data preservation
- Migration scripts

### 3. **Test Coverage**
- Existing unit tests
- Integration tests
- UI tests

### 4. **Performance**
- Large transaction lists
- Complex queries
- Memory usage optimization 