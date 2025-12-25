# Usecase Module

**Status:** Drafted from current code state (December 25, 2025)
**Module:** `:usecase`

## Overview

The `:usecase` module houses business logic between ViewModels and repositories following the clean architecture flow described in [docs/CODING_PRINCIPLES.md](docs/CODING_PRINCIPLES.md). Classes predominantly use Kotlin coroutines, `Result` wrappers, and Hilt injection with the `@IoDispatcher` for threading. The code currently operates directly on repository entities (e.g., `AccountEntity`, `TransactionEntity`) rather than separate domain models.

## Directory Structure

- `UseCaseModule.kt` — placeholder object declaring module name; no DI bindings yet.
- `modern/AccountUseCases.kt` — CRUD and fetch flows for accounts.
- `modern/AccountBalanceUseCases.kt` — balance rebuild for a single account, updating running balances and stored totals.
- `modern/CategoryHierarchyUseCases.kt` — category tree retrieval, subtree exclusion, insert, and placeholder move logic.
- `modern/CurrencyUseCases.kt` — CRUD for currencies with duplicate-name guard on create; home currency management.
- `modern/ExchangeRateUseCases.kt` — exchange rate retrieval, currency conversion, and home currency total calculation.
- `modern/ReferenceUseCases.kt` — payee and project CRUD wrappers.
- `modern/RunningBalanceUseCases.kt` — rebuild running balances per account or all accounts; fetch balances at a time or last balance.
- `modern/TransactionUseCases.kt` — CRUD, queries, templates, search stub, and creation with balance updates.
- `modern/TransactionManagementUseCases.kt` — insert/update transactions with attributes, delete with dependencies, and create transfers.

## Patterns and Data Flow

- **DI:** Classes are `@Singleton` and inject repositories/DAOs plus `@IoDispatcher` `CoroutineDispatcher`.
- **Async:** Public APIs run on `withContext(ioDispatcher)`; reactive getters expose `Flow` with `flowOn(ioDispatcher)`.
- **Error Handling:** Most use cases catch `Exception`, returning `Result.failure`, null, or empty lists; limited error typing and no logging.
- **Business Rules:** Running balance use cases mirror legacy `DatabaseAdapter` algorithms, including split/transfer handling and cumulative balance writes. Transfer and transaction management update account last-transaction metadata.
- **Domain Modeling:** No dedicated domain models; repository entities flow through the layer, which may complicate future model separation.

## Per-File Highlights

- **AccountUseCases:** Get all accounts (one-shot or `Flow`), fetch by ID, create/update/delete with `Result` wrapping. Used by account UI features such as [docs/Feature_Account_List.md](docs/Feature_Account_List.md).
- **AccountBalanceUseCases:** Recalculate a single account’s running balances and stored total, skipping splits and zero amounts; updates `totalAmount` in `AccountRepository`.
- **CategoryHierarchyUseCases:** Retrieves category trees with level metadata; subtree exclusion uses left/right bounds; move operation is currently a placeholder that returns success without mutations.
- **CurrencyUseCases:** CRUD plus duplicate-name check on create; delete fetches the entity before removal; includes home currency management with `GetHomeCurrencyUseCase` (returns first currency where `isDefault=true`, also provides reactive `Flow`) and `SetHomeCurrencyUseCase` (sets specified currency as default, unsets all others).
- **ExchangeRateUseCases:** Currency conversion infrastructure with `GetLatestExchangeRateUseCase` (currently returns 1.0 for same currency, needs repository integration for cross-currency rates), `ConvertCurrencyUseCase` (applies exchange rate to amount, returns null if rate unavailable), and `CalculateTotalInHomeCurrencyUseCase` (sums account balances in home currency, tracks unconverted accounts, filters by `includeInTotals` flag).
- **ReferenceUseCases:** Basic payee/project fetch and create; always marks new records active.
- **RunningBalanceUseCases:** Rebuilds running balances per account and all accounts; fetches last balance or balance at timestamp. Logic filters self-transfers and handles split transfer direction.
- **TransactionUseCases:** CRUD plus filters (account, date range, category), templates, search stub, and creation that triggers balance recalculation for affected accounts via `RecalculateAccountBalanceUseCase`.
- **TransactionManagementUseCases:** Upsert transactions with optional attributes (attributes wiped on update), delete with attribute cleanup, and create transfer transactions with default statuses.

## Status and Gaps

- No DI binding module exists beyond the placeholder `UseCaseModule.kt`; Hilt modules may be required to expose use cases to consumers.
- Limited unit test coverage under `:usecase`; new currency and exchange rate use cases have tests.
- Move category logic is stubbed; search and multi-field queries are minimal; transaction templates reuse repository-only data.
- Running balance rebuild and transaction upsert mirror legacy behavior but lack explicit validation or conflict handling.
- Module depends on repository entities directly; future domain models may require conversion layers.
- Exchange rate use cases ready but need repository integration for actual rate data.

## Integration Notes

- **Consumers:** Feature modules (e.g., account, transaction screens) should call these use cases via DI. The account list feature currently references account use cases; see [docs/Feature_Account_List.md](docs/Feature_Account_List.md).
- **Dependencies:** Requires `:core:common` and `:repository`; coroutines, Hilt, and RxJava are available per [usecase/build.gradle](../usecase/build.gradle).
- **Suggested Next Steps:**
  1. Add a Hilt module to bind or provide use cases for injection.
  2. Introduce domain models and mapping to reduce repository-entity leakage.
  3. Backfill unit tests for critical paths (running balance, transaction upsert, category hierarchy).
  4. Replace stubbed operations (category move, search) with full implementations.

## Related Documentation

- Architecture and principles: [docs/CODING_PRINCIPLES.md](docs/CODING_PRINCIPLES.md)
- Account list feature using these use cases: [docs/Feature_Account_List.md](docs/Feature_Account_List.md)
- Repository layer overview: [docs/Repository_Module.md](docs/Repository_Module.md)
