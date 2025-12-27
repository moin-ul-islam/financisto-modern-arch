# Repository: Room Database

**Status:** Drafted from current code state (December 25, 2025)  
**Module:** `:repository`

## Overview

`FinancistoDatabase` is the Room entry point for the modern stack, exposing DAOs used by repository implementations. It is provided via Hilt in [repository/src/main/java/ru/orangesoftware/financisto/di/DatabaseModule.kt](repository/src/main/java/ru/orangesoftware/financisto/di/DatabaseModule.kt) and configured in [repository/src/main/java/ru/orangesoftware/financisto/data/database/FinancistoDatabase.kt](repository/src/main/java/ru/orangesoftware/financisto/data/database/FinancistoDatabase.kt).

## Composition

- **Entities:** AccountEntity, CurrencyEntity, TransactionEntity, RunningBalanceEntity, CategoryEntity, BudgetEntity, ProjectEntity, AttributeEntity, SmsTemplateEntity, LocationEntity, PayeeEntity, CategoryAttributeEntity, TransactionAttributeEntity, CreditCardClosingDateEntity, CurrencyExchangeRateEntity.
- **Views:** CategoryView, AttributeView.
- **Version:** 4 (schemas exported to repository/schemas/ru.orangesoftware.financisto.data.database.FinancistoDatabase).
- **Name:** financisto_modern.db (fallback-to-destructive migration enabled for development).

## Provided DAOs

AccountDao, TransactionDao, RunningBalanceDao, CategoryDao, BudgetDao, ProjectDao, AttributeDao, SmsTemplateDao, LocationDao, PayeeDao, CategoryAttributeDao, TransactionAttributeDao, CreditCardClosingDateDao, CurrencyExchangeRateDao, CurrencyDao. Each DAO has dedicated documentation (see Related Docs).

## DI Wiring

- Database and DAO providers live in [repository/src/main/java/ru/orangesoftware/financisto/di/DatabaseModule.kt](repository/src/main/java/ru/orangesoftware/financisto/di/DatabaseModule.kt) with `@SingletonComponent` scope. 
- Repository interface bindings are in [repository/src/main/java/ru/orangesoftware/financisto/di/RepositoryModule.kt](repository/src/main/java/ru/orangesoftware/financisto/di/RepositoryModule.kt).

## Notes

- `fallbackToDestructiveMigration()` is enabled; production should replace this with real migrations.
- Database exposes an in-memory builder toggle used only for test/dev scenarios.
- No prepackaged data or callbacks are registered yet.

## Related Documentation

- Module overview: [docs/Repository_Module.md](Repository_Module.md)
- Use cases consuming repository layer: [docs/Usecase_Module.md](Usecase_Module.md)
- DAO references: Account, Transaction, RunningBalance, Category, Budget, Project, Attribute, SmsTemplate, Location, Payee, CategoryAttribute, TransactionAttribute, CreditCardClosingDate, CurrencyExchangeRate, Currency (see individual DAO docs).
