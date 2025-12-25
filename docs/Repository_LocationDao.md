# Repository: LocationDao

**Status:** Drafted from current code state (December 25, 2025)  
**Module:** `:repository`

## Source

DAO definition: [repository/src/main/java/ru/orangesoftware/financisto/data/dao/LocationDao.kt](repository/src/main/java/ru/orangesoftware/financisto/data/dao/LocationDao.kt)

## Responsibilities

- CRUD for locations with reactive listing and payee-specific filters.
- Maintains a `count` field via incremental updates.

## Key Operations

- `getAllLocationsFlow()` / `getAllLocations()` for active locations ordered by sort/title.
- `getPayeeLocations()` filters `is_payee = 1`.
- `updateLocationCount(locationId, delta)` adjusts usage counters.
- `searchLocations(query)` supports title search.

## Notes

- Active filtering applied to listings/search; ID lookups bypass it.
- No transactional wrappers around count updates; callers should ensure consistency.

## Related Documentation

- Module overview: [Repository_Module.md](Repository_Module.md)
- Database setup: [Repository_Database.md](Repository_Database.md)
- Use cases: [Usecase_Module.md](Usecase_Module.md)
