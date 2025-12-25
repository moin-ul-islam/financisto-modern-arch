# Repository: TransactionAttributeDao

**Status:** Drafted from current code state (December 25, 2025)  
**Module:** `:repository`

## Source

DAO definition: [repository/src/main/java/ru/orangesoftware/financisto/data/dao/TransactionAttributeDao.kt](repository/src/main/java/ru/orangesoftware/financisto/data/dao/TransactionAttributeDao.kt)

## Responsibilities

- Junction-table operations linking transactions to attributes.
- Batch upsert/delete helpers and targeted value updates.

## Key Operations

- Relationship navigation: `getAttributesForTransaction(transactionId)`, `getTransactionsForAttribute(attributeId)`.
- Upsert: `insertTransactionAttributes(list)` with `OnConflictStrategy.REPLACE`.
- Cleanup: `deleteAttributesForTransaction(transactionId)`, `deleteTransactionsForAttribute(attributeId)`.
- Value update/query: `updateAttributeValue(transactionId, attributeId, value)`, `getAttributeValue(transactionId, attributeId)`.

## Notes

- Duplicate delete method alias (`deleteTransactionAttributesForTransaction`) exists for convenience.
- Used by transaction management use cases when updating attributes alongside transactions.

## Related Documentation

- Attributes: [Repository_AttributeDao.md](Repository_AttributeDao.md)
- Transactions: [Repository_TransactionDao.md](Repository_TransactionDao.md)
- Use cases: [Usecase_Module.md](Usecase_Module.md)
- Module overview: [Repository_Module.md](Repository_Module.md)
- Database setup: [Repository_Database.md](Repository_Database.md)
