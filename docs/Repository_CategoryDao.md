# Repository: CategoryDao

**Status:** Drafted from current code state (December 25, 2025)  
**Module:** `:repository`

## Source

DAO definition: [repository/src/main/java/ru/orangesoftware/financisto/data/dao/CategoryDao.kt](repository/src/main/java/ru/orangesoftware/financisto/data/dao/CategoryDao.kt)

## Responsibilities

- CRUD for categories with type filtering (expense/income) and search.
- Exposes hierarchical views (`v_category`) for nested-set operations and level metadata.

## Key Operations

- `getAllCategoriesFlow()` / `getAllCategories()` for active categories ordered by sort/title.
- Type filters: `getCategoriesByType(type)`, `getExpenseCategories()`, `getIncomeCategories()`.
- View-backed tree queries: `getAllCategoriesWithLevel()`, `getCategoryWithLevelById(id)`, `getCategoriesWithLevelByType(type)`, `getExpenseCategoriesWithLevel()`, `getIncomeCategoriesWithLevel()`.

## Notes

- Uses `CategoryView` for left/right bounds enabling subtree exclusion (e.g., in use cases for move/validation).
- Active flag enforced on most table queries; view queries do not filter by active status.

## Related Documentation

- Category hierarchy use cases reference these views: [Usecase_Module.md](Usecase_Module.md)
- Module overview: [Repository_Module.md](Repository_Module.md)
- Database setup: [Repository_Database.md](Repository_Database.md)
