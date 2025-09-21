package ru.orangesoftware.financisto.usecase.modern

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import ru.orangesoftware.financisto.data.model.PayeeEntity
import ru.orangesoftware.financisto.data.model.ProjectEntity
import ru.orangesoftware.financisto.di.IoDispatcher
import ru.orangesoftware.financisto.repository.modern.PayeeRepository
import ru.orangesoftware.financisto.repository.modern.ProjectRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case for getting all payees.
 */
@Singleton
class GetPayeesUseCase @Inject constructor(
    private val payeeRepository: PayeeRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    /**
     * Gets all payees.
     * @return Result with list of payees on success
     */
    suspend fun execute(): Result<List<PayeeEntity>> = withContext(ioDispatcher) {
        try {
            val payees = payeeRepository.getAllPayees()
            Result.success(payees)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Use case for creating a new payee.
 */
@Singleton
class CreatePayeeUseCase @Inject constructor(
    private val payeeRepository: PayeeRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    /**
     * Creates a new payee.
     * @param name The name of the payee
     * @return Result with the new payee ID on success
     */
    suspend fun execute(name: String): Result<Long> = withContext(ioDispatcher) {
        try {
            val payee = PayeeEntity(
                id = 0, // Auto-generated
                title = name,
                isActive = true
            )
            val payeeId = payeeRepository.insertPayee(payee)
            Result.success(payeeId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Use case for getting all projects.
 */
@Singleton
class GetProjectsUseCase @Inject constructor(
    private val projectRepository: ProjectRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    /**
     * Gets all projects.
     * @return Result with list of projects on success
     */
    suspend fun execute(): Result<List<ProjectEntity>> = withContext(ioDispatcher) {
        try {
            val projects = projectRepository.getAllProjects()
            Result.success(projects)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Use case for creating a new project.
 */
@Singleton
class CreateProjectUseCase @Inject constructor(
    private val projectRepository: ProjectRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    /**
     * Creates a new project.
     * @param name The name of the project
     * @return Result with the new project ID on success
     */
    suspend fun execute(name: String): Result<Long> = withContext(ioDispatcher) {
        try {
            val project = ProjectEntity(
                id = 0, // Auto-generated
                title = name,
                isActive = true
            )
            val projectId = projectRepository.insertProject(project)
            Result.success(projectId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}