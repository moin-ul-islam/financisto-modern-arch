# Repository: RunningBalanceDao

**Status:** Drafted from current code state (December 25, 2025)  
**Module:** `:repository`

## Source

DAO definition: [repository/src/main/java/ru/orangesoftware/financisto/data/dao/RunningBalanceDao.kt](repository/src/main/java/ru/orangesoftware/financisto/data/dao/RunningBalanceDao.kt)

## Responsibilities

- Persist and query running balance rows per account, mirroring legacy cumulative balance logic.
- Support rebuild workflows and balance lookups at specific times.

## Key Operations

- Batch upserts: `insertRunningBalances(list)` with `OnConflictStrategy.REPLACE`.
- Cleanup: `deleteRunningBalanceForAccount(accountId)`, `deleteAllRunningBalances()`.
- Queries: `getLastRunningBalanceForAccount(accountId)`, `getAccountBalanceAtTime(accountId, datetime)`, `getRunningBalancesForAccount(accountId)`.
- Adjustment: `updateRunningBalancesAfterTime(accountId, deltaAmount, datetime)` for post-transaction updates.

## Notes

- Ordering rules use datetime then transaction_id to match legacy behavior for ties.
- Critical for use cases `RebuildRunningBalanceForAccountUseCase` and `GetAccountBalanceAtTimeUseCase`.

## Related Documentation

- Transaction DAO feeding data: [Repository_TransactionDao.md](Repository_TransactionDao.md)
- Use cases leveraging running balances: [Usecase_Module.md](Usecase_Module.md)
- Module overview: [Repository_Module.md](Repository_Module.md)
- Database setup: [Repository_Database.md](Repository_Database.md)
