package ru.orangesoftware.financisto.domain.model

/**
 * Domain model for Location entity.
 * 
 * Represents a geographical location where a transaction took place.
 * Locations can be automatically detected via GPS or manually entered,
 * helping to track spending patterns by location.
 * 
 * Uses Long timestamps (milliseconds since epoch) for Android compatibility.
 * 
 * Domain models are:
 * - Framework-agnostic (no Android/Room/UI dependencies)
 * - Focused on business logic and validation
 * - Immutable data classes with business methods
 * - Used by use cases for business operations
 */
data class Location(
    val id: LocationId = LocationId.NONE,
    val name: String,
    val coordinates: GeoCoordinates? = null,
    val address: String? = null,
    val provider: LocationProvider = LocationProvider.MANUAL,
    val accuracy: Float? = null,
    val isActive: Boolean = true,
    val sortOrder: Int = 0,
    val usageCount: Int = 0,
    val lastUsed: Long? = null,
    val notes: String? = null
) {
    
    /**
     * Business logic: Check if location can be used in transactions
     */
    fun canBeUsedInTransactions(): Boolean = isActive && name.isNotBlank()
    
    /**
     * Business logic: Get display name with formatting
     */
    fun getDisplayName(): String {
        return if (name.isBlank()) "<No Location>" else name
    }
    
    /**
     * Business logic: Check if location has GPS coordinates
     */
    fun hasCoordinates(): Boolean = coordinates != null
    
    /**
     * Business logic: Check if location has a resolved address
     */
    fun hasAddress(): Boolean = !address.isNullOrBlank()
    
    /**
     * Business logic: Check if location was automatically detected
     */
    fun isAutoDetected(): Boolean = provider == LocationProvider.GPS || provider == LocationProvider.NETWORK
    
    /**
     * Business logic: Check if location is frequently used
     */
    fun isFrequentlyUsed(threshold: Int = 5): Boolean = usageCount >= threshold
    
    /**
     * Business logic: Calculate distance to another location in meters
     */
    fun distanceTo(other: Location): Double? {
        return if (hasCoordinates() && other.hasCoordinates()) {
            coordinates!!.distanceTo(other.coordinates!!)
        } else null
    }
    
    /**
     * Business logic: Check if location is within a certain radius of another location
     */
    fun isNear(other: Location, radiusInMeters: Double): Boolean {
        val distance = distanceTo(other)
        return distance != null && distance <= radiusInMeters
    }
    
    /**
     * Business logic: Update usage statistics
     */
    fun recordUsage(): Location {
        return copy(
            usageCount = usageCount + 1,
            lastUsed = System.currentTimeMillis()
        )
    }
    
    /**
     * Business logic: Get accuracy description
     */
    fun getAccuracyDescription(): String? {
        return accuracy?.let { acc ->
            when {
                acc <= 5f -> "Very High"
                acc <= 10f -> "High"
                acc <= 20f -> "Medium"
                acc <= 50f -> "Low"
                else -> "Very Low"
            }
        }
    }
    
    /**
     * Business logic: Validate location data
     */
    fun validate(): LocationValidationResult {
        val errors = mutableListOf<String>()
        
        if (name.isBlank()) {
            errors.add("Location name cannot be empty")
        }
        
        if (name.length > 255) {
            errors.add("Location name cannot exceed 255 characters")
        }
        
        if (address != null && address.length > 500) {
            errors.add("Address cannot exceed 500 characters")
        }
        
        if (sortOrder < 0) {
            errors.add("Sort order cannot be negative")
        }
        
        if (accuracy != null && accuracy < 0) {
            errors.add("Accuracy cannot be negative")
        }
        
        if (usageCount < 0) {
            errors.add("Usage count cannot be negative")
        }
        
        coordinates?.let { coords ->
            if (!coords.isValid()) {
                errors.add("Invalid GPS coordinates")
            }
        }
        
        return if (errors.isEmpty()) {
            LocationValidationResult.Valid
        } else {
            LocationValidationResult.Invalid(errors)
        }
    }
    
    companion object {
        val CURRENT_LOCATION = Location(
            id = LocationId.CURRENT,
            name = "<CURRENT_LOCATION>",
            provider = LocationProvider.GPS,
            isActive = true
        )
        
        /**
         * Factory method for creating manual locations
         */
        fun createManual(
            name: String,
            address: String? = null,
            notes: String? = null
        ): Location {
            return Location(
                name = name.trim(),
                address = address?.trim(),
                provider = LocationProvider.MANUAL,
                notes = notes?.trim()
            )
        }
        
        /**
         * Factory method for creating GPS locations
         */
        fun createFromGps(
            name: String,
            latitude: Double,
            longitude: Double,
            accuracy: Float? = null,
            address: String? = null
        ): Location {
            return Location(
                name = name.trim(),
                coordinates = GeoCoordinates(latitude, longitude),
                address = address?.trim(),
                provider = LocationProvider.GPS,
                accuracy = accuracy
            )
        }
    }
}

/**
 * Value object for Location ID
 */
@JvmInline
value class LocationId(val value: Long) {
    companion object {
        val NONE = LocationId(0L)
        val CURRENT = LocationId(0L)
    }
}

/**
 * Value object for GPS coordinates
 */
data class GeoCoordinates(
    val latitude: Double,
    val longitude: Double
) {
    /**
     * Business logic: Validate GPS coordinates
     */
    fun isValid(): Boolean {
        return latitude in -90.0..90.0 && longitude in -180.0..180.0
    }
    
    /**
     * Business logic: Calculate distance to another coordinate using Haversine formula
     */
    fun distanceTo(other: GeoCoordinates): Double {
        val earthRadius = 6371000.0 // Earth radius in meters
        
        val lat1Rad = Math.toRadians(latitude)
        val lat2Rad = Math.toRadians(other.latitude)
        val deltaLat = Math.toRadians(other.latitude - latitude)
        val deltaLng = Math.toRadians(other.longitude - longitude)
        
        val a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                Math.sin(deltaLng / 2) * Math.sin(deltaLng / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        
        return earthRadius * c
    }
    
    override fun toString(): String = "$latitude,$longitude"
}

/**
 * Business enumeration for location providers
 */
enum class LocationProvider(val displayName: String) {
    MANUAL("Manual"),
    GPS("GPS"),
    NETWORK("Network"),
    PASSIVE("Passive");
    
    /**
     * Business logic: Check if provider requires device permissions
     */
    fun requiresPermissions(): Boolean = this != MANUAL
    
    /**
     * Business logic: Get typical accuracy range for provider
     */
    fun getTypicalAccuracy(): Pair<Float, Float>? = when (this) {
        GPS -> Pair(3f, 10f)
        NETWORK -> Pair(10f, 100f)
        PASSIVE -> Pair(20f, 200f)
        MANUAL -> null
    }
}

/**
 * Validation result for location
 */
sealed class LocationValidationResult {
    object Valid : LocationValidationResult()
    data class Invalid(val errors: List<String>) : LocationValidationResult()
}
