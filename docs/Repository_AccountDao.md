# Repository: AccountDao

**Status:** Drafted from current code state (December 25, 2025)  
**Module:** `:repository`

## Source

DAO definition: [repository/src/main/java/ru/orangesoftware/financisto/data/dao/AccountDao.kt](repository/src/main/java/ru/orangesoftware/financisto/data/dao/AccountDao.kt)

## Responsibilities

- Full reactive and non-reactive CRUD for accounts.
- Filtering for accounts included in totals.
- Balance and last transaction date updates.

## Key Operations

- `getAllAccountsFlow()`: Reactive `Flow` of all active accounts, ordered by `sort_order` and `title`.
- `getAllAccounts()`: One-time fetch of all active accounts.
- `getAccountById(accountId)`: Fetches a single account by its ID.
- `getAccountsIncludedInTotals()`: Retrieves all active accounts that are included in totals.
- `insertAccount(account)`: Inserts a new account and returns its ID.
- `updateAccount(account)`: Updates an existing account.
- `deleteAccount(account)` / `deleteAccountById(accountId)`: Removes an account.
- `updateAccountBalance(accountId, amount, lastTransactionDate)`: Updates the total amount and last transaction date for an account.
- `getAccountCount()`: Returns the total number of active accounts.
- `searchAccounts(query)`: Searches for active accounts with a title matching the query.

## Notes

- Most queries filter for `is_active = 1`. Direct lookups by ID do not.
- Balance updates are manual and must be triggered by the caller (e.g., a use case).
- Used by `AccountRepositoryImpl` and cascading use cases (e.g., account list feature).

## Related Documentation

- Module overview: [Repository_Module.md](Repository_Module.md)
- Database setup: [Repository_Database.md](Repository_Database.md)
- Use cases: [Usecase_Module.md](Usecase_Module.md)
