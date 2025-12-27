# Repository: CurrencyExchangeRateDao

**Status:** Drafted from current code state (December 25, 2025)  
**Module:** `:repository`

## Source

DAO definition: [repository/src/main/java/ru/orangesoftware/financisto/data/dao/CurrencyExchangeRateDao.kt](repository/src/main/java/ru/orangesoftware/financisto/data/dao/CurrencyExchangeRateDao.kt)

## Responsibilities

- Manage exchange rates between currency pairs with date-aware queries.
- Upsert and cleanup helpers for rate history maintenance.

## Key Operations

- Pair lookups: `getExchangeRatesForPair(from, to)`, `getLatestExchangeRate(from, to)`, `getExchangeRateAtDate(from, to, date)`, `getExchangeRateClosestToDate(from, to, date)`.
- Inserts support single or batch with `OnConflictStrategy.REPLACE`.
- `deleteExchangeRatesForPair(from, to)` and `deleteOldExchangeRates(date)` prune data.

## Notes

- No Flow-based APIs; callers fetch as needed.
- Rates are ordered by `rate_date DESC` for recency-sensitive operations.

## Related Documentation

- Currency base data: [Repository_CurrencyDao.md](Repository_CurrencyDao.md)
- Module overview: [Repository_Module.md](Repository_Module.md)
- Database setup: [Repository_Database.md](Repository_Database.md)
