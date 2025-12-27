package ru.orangesoftware.financisto.repository.modern

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import ru.orangesoftware.financisto.data.dao.ProjectDao
import ru.orangesoftware.financisto.data.model.ProjectEntity
import ru.orangesoftware.financisto.di.IoDispatcher
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for project operations.
 */
@Singleton
class ProjectRepository @Inject constructor(
    private val projectDao: ProjectDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    /**
     * Gets all projects.
     */
    suspend fun getAllProjects(): List<ProjectEntity> = withContext(ioDispatcher) {
        projectDao.getAllProjects()
    }

    /**
     * Inserts a new project.
     */
    suspend fun insertProject(project: ProjectEntity): Long = withContext(ioDispatcher) {
        projectDao.insertProject(project)
    }

    /**
     * Gets a project by ID.
     */
    suspend fun getProjectById(id: Long): ProjectEntity? = withContext(ioDispatcher) {
        projectDao.getProjectById(id)
    }
}