# Repository: TransactionDao

**Status:** Drafted from current code state (December 25, 2025)  
**Module:** `:repository`

## Source

DAO definition: [repository/src/main/java/ru/orangesoftware/financisto/data/dao/TransactionDao.kt](repository/src/main/java/ru/orangesoftware/financisto/data/dao/TransactionDao.kt)

## Responsibilities

- Full reactive and non-reactive CRUD for transactions.
- Complex filtering by account, date range, category, and note.
- Queries for transaction templates.
- Support for split transactions via `TransactionWithSplits`.
- Specialized queries for running balance calculations and blotter views.

## Key Operations

- `getAllTransactionsFlow()`: Reactive `Flow` of all transactions (including templates).
- `getAllTransactions()`: One-time fetch of all non-template transactions.
- `getTransactionsForAccountFlow(accountId)` / `getTransactionsForAccount(accountId)`: Reactive and one-time fetch of transactions for a specific account.
- `getTransactionById(transactionId)`: Fetches a single transaction by its ID.
- `getTransactionsByDateRange(startDate, endDate)`: Fetches transactions within a date range.
- `getTransactionsByCategory(categoryId)`: Fetches transactions for a specific category.
- `insertTransaction(transaction)`: Inserts a new transaction and returns its ID.
- `updateTransaction(transaction)`: Updates an existing transaction.
- `deleteTransaction(transaction)` / `deleteTransactionById(transactionId)`: Removes a transaction.
- `getTransactionTemplates()`: Fetches all transaction templates.
- `getTotalAmountForAccount(accountId, startDate, endDate)`: Calculates the net amount change for an account in a date range.
- `getTransactionCountForAccount(accountId)`: Counts the number of transactions for an account.
- `searchTransactionsByNote(query)`: Searches for transactions with a note matching the query.
- `getTransactionsForRunningBalance(accountId)`: Fetches transactions for an account, ordered correctly for running balance calculation.
- `getTransactionsWithSplits(transactionId)`: Fetches a transaction and its splits.
- `getSplitCount(transactionId)`: Counts the number of splits for a transaction.
- `getBlotterEntries(...)`: A complex query to fetch a list of transactions for the main blotter screen, with various filtering options.

## Notes

- The `getAllTransactionsFlow()` method **includes templates**, while `getAllTransactions()` and most other queries explicitly filter for `is_template = 0`. This is a key distinction.
- The `TransactionWithSplits` data class is used to model parent-child relationships for split transactions.
- The `getBlotterEntries` query is highly complex and forms the backbone of the main transaction list UI. It joins multiple tables and supports filtering by account, category, project, payee, and more.
- The `getTransactionsForRunningBalance` query has a specific ordering (`datetime ASC`, `_id ASC`) that is critical for correct balance calculation.

## Related Documentation

- Running balance consumer: [Repository_RunningBalanceDao.md](Repository_RunningBalanceDao.md)
- Transaction attributes: [Repository_TransactionAttributeDao.md](Repository_TransactionAttributeDao.md)
- Use cases: [Usecase_Module.md](Usecase_Module.md)
- Module overview: [Repository_Module.md](Repository_Module.md)
- Database setup: [Repository_Database.md](Repository_Database.md)
