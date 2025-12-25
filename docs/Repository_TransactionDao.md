# Repository: TransactionDao

**Status:** Drafted from current code state (December 25, 2025)  
**Module:** `:repository`

## Source

DAO definition: [repository/src/main/java/ru/orangesoftware/financisto/data/dao/TransactionDao.kt](repository/src/main/java/ru/orangesoftware/financisto/data/dao/TransactionDao.kt)

## Responsibilities

- Comprehensive transaction access: CRUD, filters, blotter queries, split handling, templates, search, and running-balance feeds.
- Provides both reactive and one-shot retrievals.

## Key Operations

- Listings: `getAllTransactionsFlow()`, `getAllTransactions()` (non-templates) ordered by datetime DESC.
- Account filters: `getTransactionsForAccountFlow(accountId)` and suspend variant.
- Range/category filters: `getTransactionsByDateRange(start, end)`, `getTransactionsByCategory(categoryId)`.
- Aggregates: `getTotalAmountForAccount(accountId, start, end)`, `getTransactionCountForAccount(accountId)`.
- Running balance feed: `getTransactionsForRunningBalance(accountId)` ordered by datetime ASC then id.
- Splits: `getTransactionWithSplits(id)`, `getSplitTransactions(parentId)` (+ Flow), `deleteSplitTransactions(parentId)`, helpers `isSplitParent`, `isSplitChild`, `getAllSplitParents()`.
- Blotter: `getBlotterTransactions()` / Flow for parent-only items.
- Templates/search: `getTransactionTemplates()`, `searchTransactionsByNote(query)`.

## Notes

- Templates are excluded from most business queries via `is_template = 0` checks.
- Split parents use `parent_id = 0`; child splits share parent id and may carry category_id = -1 in blotter contexts.
- Ordering for running balance is ascending to support cumulative processing.

## Related Documentation

- Running balance consumer: [Repository_RunningBalanceDao.md](Repository_RunningBalanceDao.md)
- Transaction attributes: [Repository_TransactionAttributeDao.md](Repository_TransactionAttributeDao.md)
- Use cases: [Usecase_Module.md](Usecase_Module.md)
- Module overview: [Repository_Module.md](Repository_Module.md)
- Database setup: [Repository_Database.md](Repository_Database.md)
