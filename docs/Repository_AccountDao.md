# Repository: AccountDao

**Status:** Drafted from current code state (December 25, 2025)  
**Module:** `:repository`

## Source

DAO definition: [repository/src/main/java/ru/orangesoftware/financisto/data/dao/AccountDao.kt](repository/src/main/java/ru/orangesoftware/financisto/data/dao/AccountDao.kt)

## Responsibilities

- CRUD for accounts plus balance/last-transaction updates.
- Reactive and one-shot retrieval of active accounts, totals-included accounts, and search by title.

## Key Operations

- `getAllAccountsFlow()` / `getAllAccounts()` return active accounts ordered by sort/title.
- `getAccountsIncludedInTotals()` filters by `is_include_into_totals`.
- `updateAccountBalance(accountId, amount, lastTransactionDate)` writes totals and last activity.
- `searchAccounts(query)` performs LIKE search on titles.

## Notes

- All queries filter to active accounts except ID-based lookups and deletes.
- Used by `AccountRepositoryImpl` and cascading use cases (e.g., account list feature).

## Related Documentation

- Module overview: [Repository_Module.md](Repository_Module.md)
- Database setup: [Repository_Database.md](Repository_Database.md)
- Use cases: [Usecase_Module.md](Usecase_Module.md)
