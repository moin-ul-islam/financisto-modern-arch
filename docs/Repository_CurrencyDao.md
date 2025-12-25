# Repository: CurrencyDao

**Status:** Drafted from current code state (December 25, 2025)  
**Module:** `:repository`

## Source

DAO definition: [repository/src/main/java/ru/orangesoftware/financisto/data/dao/CurrencyDao.kt](repository/src/main/java/ru/orangesoftware/financisto/data/dao/CurrencyDao.kt)

## Responsibilities

- Full reactive and non-reactive CRUD for currencies.
- Lookup for default currency.
- Duplicate name check.

## Key Operations

- `getAllCurrenciesFlow()`: Reactive `Flow` of all currencies, ordered by name.
- `getAllCurrencies()`: One-time fetch of all currencies.
- `getCurrencyById(currencyId)`: Fetches a single currency by its ID.
- `getDefaultCurrency()`: Retrieves the default currency.
- `insertCurrency(currency)`: Inserts a new currency and returns its ID.
- `updateCurrency(currency)`: Updates an existing currency.
- `deleteCurrency(currency)`: Removes a currency.
- `currencyExistsByName(name)`: Checks if a currency with the given name already exists.
- `getCurrenciesCount()`: Returns the total number of currencies.

## Notes

- Unlike other DAOs, currency queries do not filter by an `is_active` flag, as it does not exist on the `currency` table.
- The `is_default` flag is used to identify the single default currency.

## Related Documentation

- Exchange rates: [Repository_CurrencyExchangeRateDao.md](Repository_CurrencyExchangeRateDao.md)
- Module overview: [Repository_Module.md](Repository_Module.md)
- Database setup: [Repository_Database.md](Repository_Database.md)
- Use cases: [Usecase_Module.md](Usecase_Module.md)
