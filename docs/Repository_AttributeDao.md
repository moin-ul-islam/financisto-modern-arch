# Repository: AttributeDao

**Status:** Drafted from current code state (December 25, 2025)  
**Module:** `:repository`

## Source

DAO definition: [repository/src/main/java/ru/orangesoftware/financisto/data/dao/AttributeDao.kt](repository/src/main/java/ru/orangesoftware/financisto/data/dao/AttributeDao.kt)

## Responsibilities

- CRUD for attributes and lookup by type, title search, and counts.
- Provides AttributeView queries that join category metadata for nested-set hierarchy.

## Key Operations

- `getAllAttributesFlow()` / `getAllAttributes()` return active attributes ordered by sort/title.
- `getAttributesByType(type)` filters by attribute type.
- View queries: `getAllAttributesWithCategories()`, `getAttributesForCategory(categoryId)`, `getAttributesForCategoryHierarchy(left, right)` pull from `v_attributes`.
- Delete helpers for ID-based removal.

## Notes

- Uses `AttributeView` to surface category context; hierarchy bounds rely on nested-set left/right values.
- Active filtering is applied to most list/search queries.

## Related Documentation

- Junction table: [Repository_CategoryAttributeDao.md](Repository_CategoryAttributeDao.md)
- Module overview: [Repository_Module.md](Repository_Module.md)
- Database setup: [Repository_Database.md](Repository_Database.md)
- Use cases: [Usecase_Module.md](Usecase_Module.md)
