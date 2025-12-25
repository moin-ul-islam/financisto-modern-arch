# Repository: SmsTemplateDao

**Status:** Drafted from current code state (December 25, 2025)  
**Module:** `:repository`

## Source

DAO definition: [repository/src/main/java/ru/orangesoftware/financisto/data/dao/SmsTemplateDao.kt](repository/src/main/java/ru/orangesoftware/financisto/data/dao/SmsTemplateDao.kt)

## Responsibilities

- CRUD for SMS templates with income/expense filtering and search.
- Reactive listing of active templates.

## Key Operations

- `getAllSmsTemplatesFlow()` / `getAllSmsTemplates()` ordered by sort/title.
- Type filters: `getSmsTemplatesByType(isIncome)`, `getIncomeSmsTemplates()`, `getExpenseSmsTemplates()`.
- `searchSmsTemplates(query)` performs LIKE search on titles.

## Notes

- Active flag enforced across listings/search; ID lookups bypass it.
- No linkage yet to messaging/parsing features; future use cases can reuse these queries.

## Related Documentation

- Module overview: [Repository_Module.md](Repository_Module.md)
- Database setup: [Repository_Database.md](Repository_Database.md)
- Use cases: [Usecase_Module.md](Usecase_Module.md)
