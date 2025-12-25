# Repository Module

**Status:** Drafted from current code state (December 25, 2025)  
**Module:** `:repository`

## Purpose

Implements the data layer for the modern app: Room entities/views, DAOs, repository interfaces/impls, and Hilt modules that provide database access. Storage models stay internal to this module and are used by use cases via repository interfaces.

## Structure

- **Database & DI:** Room database in [repository/src/main/java/ru/orangesoftware/financisto/data/database/FinancistoDatabase.kt](repository/src/main/java/ru/orangesoftware/financisto/data/database/FinancistoDatabase.kt), provided through [repository/src/main/java/ru/orangesoftware/financisto/di/DatabaseModule.kt](repository/src/main/java/ru/orangesoftware/financisto/di/DatabaseModule.kt). Repository bindings live in [repository/src/main/java/ru/orangesoftware/financisto/di/RepositoryModule.kt](repository/src/main/java/ru/orangesoftware/financisto/di/RepositoryModule.kt).
- **DAOs:** See DAO docs for Account, Attribute, Budget, CategoryAttribute, Category, CreditCardClosingDate, Currency, CurrencyExchangeRate, Location, Payee, Project, RunningBalance, SmsTemplate, TransactionAttribute, Transaction.
- **Repositories:** Interfaces and implementations under [repository/src/main/java/ru/orangesoftware/financisto/repository/modern](repository/src/main/java/ru/orangesoftware/financisto/repository/modern) (Account, Category, Transaction, Currency, Attribute, Payee, Project, etc.). These convert between storage entities and domain-facing types (currently entities are exposed directly).

## Patterns

- **Room + Hilt:** All DAOs are provided from the singleton database via Hilt modules. Repository bindings use `@Binds` in `RepositoryModule`.
- **Coroutines/Flow:** DAOs expose both suspend functions and reactive `Flow` streams. Repositories largely wrap these with simple pass-throughs and `Result` handling.
- **Legacy Compatibility:** Running balance logic and split transaction handling mirror legacy SQLite behavior. Schema version is 4; destructive migration is enabled for development.

## Gaps / Next Steps

- Real migrations to replace `fallbackToDestructiveMigration()`.
- Clear domain-model boundary to avoid leaking storage entities beyond repository implementations.
- Tests for DAOs and repositories are absent; see TESTING_SETUP.md in module root.

## Related Documentation

- DAO docs: 
  - [Account DAO](Repository_AccountDao.md)
  - [Attribute DAO](Repository_AttributeDao.md)
  - [Budget DAO](Repository_BudgetDao.md)
  - [Category-Attribute DAO](Repository_CategoryAttributeDao.md)
  - [Category DAO](Repository_CategoryDao.md)
  - [Credit Card Closing Date DAO](Repository_CreditCardClosingDateDao.md)
  - [Currency DAO](Repository_CurrencyDao.md)
  - [Currency Exchange Rate DAO](Repository_CurrencyExchangeRateDao.md)
  - [Location DAO](Repository_LocationDao.md)
  - [Payee DAO](Repository_PayeeDao.md)
  - [Project DAO](Repository_ProjectDao.md)
  - [Running Balance DAO](Repository_RunningBalanceDao.md)
  - [SMS Template DAO](Repository_SmsTemplateDao.md)
  - [Transaction Attribute DAO](Repository_TransactionAttributeDao.md)
  - [Transaction DAO](Repository_TransactionDao.md)
- Database setup: [Repository_Database.md](Repository_Database.md)
- Use case layer overview: [Usecase_Module.md](Usecase_Module.md)
