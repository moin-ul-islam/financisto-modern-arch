# Repository: CategoryAttributeDao

**Status:** Drafted from current code state (December 25, 2025)  
**Module:** `:repository`

## Source

DAO definition: [repository/src/main/java/ru/orangesoftware/financisto/data/dao/CategoryAttributeDao.kt](repository/src/main/java/ru/orangesoftware/financisto/data/dao/CategoryAttributeDao.kt)

## Responsibilities

- Junction-table operations linking categories and attributes.
- Bulk insert/delete helpers plus presence checks.

## Key Operations

- `getAttributesForCategory(categoryId)` and `getCategoriesForAttribute(attributeId)` navigate relationships.
- `insertCategoryAttributes(list)` supports batching.
- Cleanup helpers: `deleteAttributesForCategory(categoryId)`, `deleteCategoriesForAttribute(attributeId)`.
- Existence check: `categoryHasAttribute(categoryId, attributeId)`.

## Notes

- No cascade triggers; callers must manage consistency when updating category/attribute lifecycles.
- Complements AttributeView queries exposed in AttributeDao.

## Related Documentation

- Attributes: [Repository_AttributeDao.md](Repository_AttributeDao.md)
- Categories: [Repository_CategoryDao.md](Repository_CategoryDao.md)
- Module overview: [Repository_Module.md](Repository_Module.md)
- Database setup: [Repository_Database.md](Repository_Database.md)
