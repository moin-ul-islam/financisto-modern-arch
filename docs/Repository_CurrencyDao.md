# Repository: CurrencyDao

**Status:** Drafted from current code state (December 25, 2025)  
**Module:** `:repository`

## Source

DAO definition: [repository/src/main/java/ru/orangesoftware/financisto/data/dao/CurrencyDao.kt](repository/src/main/java/ru/orangesoftware/financisto/data/dao/CurrencyDao.kt)

## Responsibilities

- CRUD for currencies, default currency lookup, and duplicate-name checks.
- Reactive and one-shot list retrieval ordered by name.

## Key Operations

- `getAllCurrenciesFlow()` / `getAllCurrencies()` for listing.
- `getDefaultCurrency()` returns first `is_default = 1`.
- `currencyExistsByName(name)` supports validation prior to inserts.

## Notes

- No cascading delete protections; callers must ensure currencies are not in use.
- Used by currency use cases for formatting and account totals.

## Related Documentation

- Exchange rates: [Repository_CurrencyExchangeRateDao.md](Repository_CurrencyExchangeRateDao.md)
- Module overview: [Repository_Module.md](Repository_Module.md)
- Database setup: [Repository_Database.md](Repository_Database.md)
- Use cases: [Usecase_Module.md](Usecase_Module.md)
