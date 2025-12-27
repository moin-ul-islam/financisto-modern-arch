# Repository: CreditCardClosingDateDao

**Status:** Drafted from current code state (December 25, 2025)  
**Module:** `:repository`

## Source

DAO definition: [repository/src/main/java/ru/orangesoftware/financisto/data/dao/CreditCardClosingDateDao.kt](repository/src/main/java/ru/orangesoftware/financisto/data/dao/CreditCardClosingDateDao.kt)

## Responsibilities

- Manage credit card closing date rows per account and period.
- Support upsert, account-level cleanup, and targeted retrieval.

## Key Operations

- `getClosingDateForAccount(accountId)` and `getClosingDate(accountId, period)` read specific periods.
- `insertCreditCardClosingDate()` uses `OnConflictStrategy.REPLACE` for upsert semantics.
- `deleteClosingDatesForAccount(accountId)` bulk-removes all rows for an account.

## Notes

- No flows or sorting; intended for direct use in credit card schedule calculations.
- Period semantics follow legacy data model; validation not enforced at DAO level.

## Related Documentation

- Module overview: [Repository_Module.md](Repository_Module.md)
- Database setup: [Repository_Database.md](Repository_Database.md)
- Use cases touching credit cards are not yet implemented; see gaps in [Usecase_Module.md](Usecase_Module.md).
