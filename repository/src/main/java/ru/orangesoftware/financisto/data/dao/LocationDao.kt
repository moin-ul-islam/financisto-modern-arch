package ru.orangesoftware.financisto.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.orangesoftware.financisto.data.model.LocationEntity

/**
 * Room DAO for Location operations.
 */
@Dao
interface LocationDao {

    /**
     * Get all locations as a Flow for reactive updates
     */
    @Query("SELECT * FROM locations WHERE is_active = 1 ORDER BY sort_order, title")
    fun getAllLocationsFlow(): Flow<List<LocationEntity>>

    /**
     * Get all locations as a one-time operation
     */
    @Query("SELECT * FROM locations WHERE is_active = 1 ORDER BY sort_order, title")
    suspend fun getAllLocations(): List<LocationEntity>

    /**
     * Get location by ID
     */
    @Query("SELECT * FROM locations WHERE _id = :locationId")
    suspend fun getLocationById(locationId: Long): LocationEntity?

    /**
     * Get locations that are payees
     */
    @Query("SELECT * FROM locations WHERE is_payee = 1 AND is_active = 1 ORDER BY sort_order, title")
    suspend fun getPayeeLocations(): List<LocationEntity>

    /**
     * Insert a new location
     */
    @Insert
    suspend fun insertLocation(location: LocationEntity): Long

    /**
     * Update an existing location
     */
    @Update
    suspend fun updateLocation(location: LocationEntity)

    /**
     * Delete a location
     */
    @Delete
    suspend fun deleteLocation(location: LocationEntity)

    /**
     * Delete location by ID
     */
    @Query("DELETE FROM locations WHERE _id = :locationId")
    suspend fun deleteLocationById(locationId: Long)

    /**
     * Get location count
     */
    @Query("SELECT COUNT(*) FROM locations WHERE is_active = 1")
    suspend fun getLocationCount(): Int

    /**
     * Search locations by title
     */
    @Query("SELECT * FROM locations WHERE is_active = 1 AND title LIKE '%' || :query || '%' ORDER BY title")
    suspend fun searchLocations(query: String): List<LocationEntity>

    /**
     * Update location count
     */
    @Query("UPDATE locations SET count = count + :delta WHERE _id = :locationId")
    suspend fun updateLocationCount(locationId: Long, delta: Int)
}