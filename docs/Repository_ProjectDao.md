# Repository: ProjectDao

**Status:** Drafted from current code state (December 25, 2025)  
**Module:** `:repository`

## Source

DAO definition: [repository/src/main/java/ru/orangesoftware/financisto/data/dao/ProjectDao.kt](repository/src/main/java/ru/orangesoftware/financisto/data/dao/ProjectDao.kt)

## Responsibilities

- CRUD for projects with reactive listing, search, and counts.

## Key Operations

- `getAllProjectsFlow()` / `getAllProjects()` for active projects ordered by sort/title.
- `searchProjects(query)` performs LIKE search on titles.
- Standard ID-based CRUD helpers and counts.

## Notes

- Active flag enforced on list/search; ID lookups bypass it.
- Used by transaction/reference features through repository implementations.

## Related Documentation

- Module overview: [Repository_Module.md](Repository_Module.md)
- Database setup: [Repository_Database.md](Repository_Database.md)
- Use cases: [Usecase_Module.md](Usecase_Module.md)
