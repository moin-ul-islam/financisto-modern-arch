# Repository: AttributeDao

**Status:** Drafted from current code state (December 25, 2025)  
**Module:** `:repository`

## Source

DAO definition: [repository/src/main/java/ru/orangesoftware/financisto/data/dao/AttributeDao.kt](repository/src/main/java/ru/orangesoftware/financisto/data/dao/AttributeDao.kt)

## Responsibilities

- Full reactive and non-reactive CRUD for attributes.
- Type-based filtering.
- Queries against `v_attributes` view to get attributes linked with category information.

## Key Operations

- `getAllAttributesFlow()`: Reactive `Flow` of all active attributes.
- `getAllAttributes()`: One-time fetch of all active attributes.
- `getAttributeById(attributeId)`: Fetches a single attribute by its ID.
- `getAttributesByType(type)`: Retrieves all active attributes of a specific type.
- `insertAttribute(attribute)`: Inserts a new attribute and returns its ID.
- `updateAttribute(attribute)`: Updates an existing attribute.
- `deleteAttribute(attribute)` / `deleteAttributeById(attributeId)`: Removes an attribute.
- `getAttributeCount()`: Returns the total number of active attributes.
- `searchAttributes(query)`: Searches for active attributes with a title matching the query.
- `getAllAttributesWithCategories()`: Fetches all attributes with their associated category information from the `v_attributes` view.
- `getAttributesForCategory(categoryId)`: Fetches attributes for a specific category.
- `getAttributesForCategoryHierarchy(left, right)`: Fetches attributes for categories within a nested set range.

## Notes

- The `v_attributes` view joins `attributes` with `category_attribute` and `category` to provide denormalized data.
- Standard queries filter for `is_active = 1`. View-based queries and direct ID lookups do not.

## Related Documentation

- Junction table: [Repository_CategoryAttributeDao.md](Repository_CategoryAttributeDao.md)
- Module overview: [Repository_Module.md](Repository_Module.md)
- Database setup: [Repository_Database.md](Repository_Database.md)
- Use cases: [Usecase_Module.md](Usecase_Module.md)
