# Repository: BudgetDao

**Status:** Drafted from current code state (December 25, 2025)  
**Module:** `:repository`

## Source

DAO definition: [repository/src/main/java/ru/orangesoftware/financisto/data/dao/BudgetDao.kt](repository/src/main/java/ru/orangesoftware/financisto/data/dao/BudgetDao.kt)

## Responsibilities

- CRUD for budgets with filtering by account and current flag.
- Reactive and one-shot list retrieval plus search and counts.

## Key Operations

- `getAllBudgetsFlow()` / `getAllBudgets()` ordered by sort/title.
- `getBudgetsForAccount(accountId)` filters by `budget_account_id`.
- `getCurrentBudgets()` fetches `is_current = 1`.
- `searchBudgets(query)` performs LIKE search.

## Notes

- No active/inactive flag present; queries do not filter by status.
- Used by potential budget features; repository layer currently exposes entities directly.

## Related Documentation

- Module overview: [Repository_Module.md](Repository_Module.md)
- Database setup: [Repository_Database.md](Repository_Database.md)
- Use cases: [Usecase_Module.md](Usecase_Module.md)
