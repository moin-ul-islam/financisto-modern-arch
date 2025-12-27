# Repository: PayeeDao

**Status:** Drafted from current code state (December 25, 2025)  
**Module:** `:repository`

## Source

DAO definition: [repository/src/main/java/ru/orangesoftware/financisto/data/dao/PayeeDao.kt](repository/src/main/java/ru/orangesoftware/financisto/data/dao/PayeeDao.kt)

## Responsibilities

- CRUD for payees with reactive listing, search, and counts.

## Key Operations

- `getAllPayeesFlow()` / `getAllPayees()` for active payees ordered by sort/title.
- `searchPayees(query)` performs LIKE search on titles.
- Simple ID-based CRUD helpers and counts.

## Notes

- Active flag enforced on list/search; ID lookups and deletes ignore it.
- Project and transaction features consume payee data via repositories and use cases.

## Related Documentation

- Module overview: [Repository_Module.md](Repository_Module.md)
- Database setup: [Repository_Database.md](Repository_Database.md)
- Use cases: [Usecase_Module.md](Usecase_Module.md)
