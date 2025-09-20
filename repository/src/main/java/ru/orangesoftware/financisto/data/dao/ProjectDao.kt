package ru.orangesoftware.financisto.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.orangesoftware.financisto.data.model.ProjectEntity

/**
 * Room DAO for Project operations.
 */
@Dao
interface ProjectDao {

    /**
     * Get all projects as a Flow for reactive updates
     */
    @Query("SELECT * FROM project WHERE is_active = 1 ORDER BY sort_order, title")
    fun getAllProjectsFlow(): Flow<List<ProjectEntity>>

    /**
     * Get all projects as a one-time operation
     */
    @Query("SELECT * FROM project WHERE is_active = 1 ORDER BY sort_order, title")
    suspend fun getAllProjects(): List<ProjectEntity>

    /**
     * Get project by ID
     */
    @Query("SELECT * FROM project WHERE _id = :projectId")
    suspend fun getProjectById(projectId: Long): ProjectEntity?

    /**
     * Insert a new project
     */
    @Insert
    suspend fun insertProject(project: ProjectEntity): Long

    /**
     * Update an existing project
     */
    @Update
    suspend fun updateProject(project: ProjectEntity)

    /**
     * Delete a project
     */
    @Delete
    suspend fun deleteProject(project: ProjectEntity)

    /**
     * Delete project by ID
     */
    @Query("DELETE FROM project WHERE _id = :projectId")
    suspend fun deleteProjectById(projectId: Long)

    /**
     * Get project count
     */
    @Query("SELECT COUNT(*) FROM project WHERE is_active = 1")
    suspend fun getProjectCount(): Int

    /**
     * Search projects by title
     */
    @Query("SELECT * FROM project WHERE is_active = 1 AND title LIKE '%' || :query || '%' ORDER BY title")
    suspend fun searchProjects(query: String): List<ProjectEntity>
}