# Repository: LocationDao

**Status:** Drafted from current code state (December 25, 2025)  
**Module:** `:repository`

## Source

DAO definition: [repository/src/main/java/ru/orangesoftware/financisto/data/dao/LocationDao.kt](repository/src/main/java/ru/orangesoftware/financisto/data/dao/LocationDao.kt)

## Responsibilities

- Full reactive and non-reactive CRUD for locations.
- Payee-specific filtering.
- Usage counter maintenance via incremental updates.

## Key Operations

- `getAllLocationsFlow()`: Reactive `Flow` of all active locations, ordered by `sort_order` and `title`.
- `getAllLocations()`: One-time fetch of all active locations.
- `getLocationById(locationId)`: Fetches a single location by its ID, ignoring active status.
- `getPayeeLocations()`: Retrieves all active locations marked as payees.
- `insertLocation(location)`: Inserts a new location and returns its ID.
- `updateLocation(location)`: Updates an existing location.
- `deleteLocation(location)` / `deleteLocationById(locationId)`: Removes a location.
- `updateLocationCount(locationId, delta)`: Atomically adjusts the `count` field for a given location.
- `searchLocations(query)`: Searches for active locations with a title matching the query.
- `getLocationCount()`: Returns the total number of active locations.

## Notes

- Most queries (`getAll`, `getPayee`, `search`, `getCount`) filter for `is_active = 1`. Direct lookups by ID (`getLocationById`) do not.
- The `count` field is managed manually by the caller via `updateLocationCount`. There are no transactional guarantees at the DAO level to ensure consistency. Callers (use cases) are responsible for correct logic.

## Related Documentation

- Module overview: [Repository_Module.md](Repository_Module.md)
- Database setup: [Repository_Database.md](Repository_Database.md)
- Use cases: [Usecase_Module.md](Usecase_Module.md)
