# Repository: CategoryDao

**Status:** Drafted from current code state (December 25, 2025)  
**Module:** `:repository`

## Source

DAO definition: [repository/src/main/java/ru/orangesoftware/financisto/data/dao/CategoryDao.kt](repository/src/main/java/ru/orangesoftware/financisto/data/dao/CategoryDao.kt)

## Responsibilities

- Full reactive and non-reactive CRUD for categories.
- Type-based filtering (expense/income).
- Queries against `v_category` view to get hierarchical data.

## Key Operations

- `getAllCategoriesFlow()`: Reactive `Flow` of all active categories.
- `getAllCategories()`: One-time fetch of all active categories.
- `getCategoryById(categoryId)`: Fetches a single category by its ID.
- `getCategoriesByType(type)`: Retrieves all active categories of a specific type.
- `getExpenseCategories()` / `getIncomeCategories()`: Convenience methods for fetching expense or income categories.
- `insertCategory(category)`: Inserts a new category and returns its ID.
- `updateCategory(category)`: Updates an existing category.
- `deleteCategory(category)` / `deleteCategoryById(categoryId)`: Removes a category.
- `getCategoryCount()`: Returns the total number of active categories.
- `searchCategories(query)`: Searches for active categories with a title matching the query.
- `getAllCategoriesWithLevel()`: Fetches all categories with their hierarchy level from the `v_category` view.
- `getCategoryWithLevelById(categoryId)`: Fetches a single category with its hierarchy level.
- `getCategoryChildren(categoryId)`: Fetches the direct children of a category.
- `getCategorySubtree(left, right)`: Fetches a category and all its descendants using nested set bounds.

## Notes

- The `v_category` view provides denormalized hierarchy information (level, parent ID).
- Standard queries filter for `is_active = 1`. View-based queries and direct ID lookups do not.

## Related Documentation

- Category hierarchy use cases reference these views: [Usecase_Module.md](Usecase_Module.md)
- Module overview: [Repository_Module.md](Repository_Module.md)
- Database setup: [Repository_Database.md](Repository_Database.md)
