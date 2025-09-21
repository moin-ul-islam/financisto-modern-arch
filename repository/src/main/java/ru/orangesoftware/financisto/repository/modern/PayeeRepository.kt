package ru.orangesoftware.financisto.repository.modern

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import ru.orangesoftware.financisto.data.dao.PayeeDao
import ru.orangesoftware.financisto.data.model.PayeeEntity
import ru.orangesoftware.financisto.di.IoDispatcher
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for payee operations.
 */
@Singleton
class PayeeRepository @Inject constructor(
    private val payeeDao: PayeeDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    /**
     * Gets all payees.
     */
    suspend fun getAllPayees(): List<PayeeEntity> = withContext(ioDispatcher) {
        payeeDao.getAllPayees()
    }

    /**
     * Inserts a new payee.
     */
    suspend fun insertPayee(payee: PayeeEntity): Long = withContext(ioDispatcher) {
        payeeDao.insertPayee(payee)
    }

    /**
     * Gets a payee by ID.
     */
    suspend fun getPayeeById(id: Long): PayeeEntity? = withContext(ioDispatcher) {
        payeeDao.getPayeeById(id)
    }
}