package com.maaz.befit.data.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

// Data class to represent a workout session
data class WorkoutSession(
    val id: String,
    val startTime: Long,
    val endTime: Long? = null,
    val locations: List<LatLng> = emptyList(), // latitude and longitude type
    val totalDistance: Float = 0f,
    val averagePace: Float = 0f,
    val caloriesBurned: Int = 0,
    val workoutType: WorkoutType = WorkoutType.RUNNING
)

// Enum class to represent different types of workouts
enum class WorkoutType {
    RUNNING,
    WALKING,
    CYCLING,
    HIKING
}

/**
 * LocationService class to handle location updates and workout sessions
 * and provide utility functions for formatting distance, pace, and duration.
 * and also check for location permissions.
 * and also provide a function to get the current location.
 * and also provide a function to start and end workout sessions.
 */
class LocationService(private val context: Context) {

    companion object {
        private const val TAG = "LocationService"
        private const val LOCATION_UPDATE_INTERVAL = 1000L // 1 second
        private const val FASTEST_LOCATION_INTERVAL = 500L // 0.5 seconds
    }

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY,
        LOCATION_UPDATE_INTERVAL
    ).apply {
        setMinUpdateIntervalMillis(FASTEST_LOCATION_INTERVAL)
        setMaxUpdateDelayMillis(2000L)
    }.build()

    private var currentWorkoutSession: WorkoutSession? = null
    private var locationCallback: LocationCallback? = null

    /**
     * return true if both permissions granted
     */
    fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    // Get the current location as a suspend function
    suspend fun getCurrentLocation(): Location? = suspendCancellableCoroutine { continuation ->
        if (!hasLocationPermission()) {
            Log.w(TAG, "Location permission not granted")
            continuation.resume(null)
            return@suspendCancellableCoroutine
        }

        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        Log.d(TAG, "Current location: ${location.latitude}, ${location.longitude}")
                        continuation.resume(location)
                    } else {
                        Log.w(TAG, "Last location is null, requesting fresh location")
                        // If last location is null, request a fresh location
                        requestFreshLocation(continuation)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error getting current location", exception)
                    continuation.resumeWithException(exception)
                }
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception: Location permission denied", e)
            continuation.resume(null)
        }
    }

    // Request a fresh location update if the last location is not provided.
    private fun requestFreshLocation(continuation: kotlin.coroutines.Continuation<Location?>) {
        if (!hasLocationPermission()) {
            continuation.resume(null)
            return
        }

        val freshLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    Log.d(TAG, "Fresh location obtained: ${location.latitude}, ${location.longitude}")
                    fusedLocationClient.removeLocationUpdates(this)
                    continuation.resume(location)
                } ?: continuation.resume(null)
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                freshLocationCallback,
                context.mainLooper
            )
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception requesting fresh location", e)
            continuation.resume(null)
        }
    }

    // Start location updates as a Flow of Location objects
    fun startLocationUpdates(): Flow<Location> = callbackFlow {
        if (!hasLocationPermission()) {
            Log.w(TAG, "Location permission not granted")
            close()
            return@callbackFlow
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    Log.d(TAG, "Location update: ${location.latitude}, ${location.longitude}, accuracy: ${location.accuracy}m")
                    trySend(location)
                } ?: Log.w(TAG, "Location result is null")
            }

            override fun onLocationAvailability(locationAvailability: LocationAvailability) {
                Log.d(TAG, "Location availability: ${locationAvailability.isLocationAvailable}")
                if (!locationAvailability.isLocationAvailable) {
                    Log.w(TAG, "Location is not available")
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                context.mainLooper
            )
            Log.d(TAG, "Started location updates")
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception starting location updates", e)
            close()
        }

        awaitClose {
            locationCallback?.let { callback ->
                fusedLocationClient.removeLocationUpdates(callback)
                Log.d(TAG, "Stopped location updates")
            }
            locationCallback = null
        }
    }

    // Start a new workout session with the specified workout type
    fun startWorkoutSession(workoutType: WorkoutType = WorkoutType.RUNNING): WorkoutSession {
        val sessionId = System.currentTimeMillis().toString()
        currentWorkoutSession = WorkoutSession(
            id = sessionId,
            startTime = System.currentTimeMillis(),
            workoutType = workoutType
        )
        Log.d(TAG, "Started workout session: $sessionId")
        return currentWorkoutSession!!
    }

    // Add a new location to the current workout session and update distance, pace, and calories burned
    fun addLocationToWorkout(location: Location) {
        currentWorkoutSession?.let { session ->
            val latLng = LatLng(location.latitude, location.longitude)
            val updatedLocations = session.locations + latLng

            // Calculate distance
            val totalDistance = calculateTotalDistance(updatedLocations)

            // Calculate average pace
            val duration = (System.currentTimeMillis() - session.startTime) / 1000f // seconds
            val averagePace = if (duration > 0) totalDistance / duration else 0f // m/s

            // Estimate calories (rough calculation: 1 calorie per 10 meters for running)
            val caloriesBurned = (totalDistance / 10f).toInt()

            currentWorkoutSession = session.copy(
                locations = updatedLocations,
                totalDistance = totalDistance,
                averagePace = averagePace,
                caloriesBurned = caloriesBurned
            )

            Log.d(TAG, "Updated workout: distance=${totalDistance}m, pace=${averagePace}m/s, calories=$caloriesBurned")
        }
    }

    // End the current workout session and return the completed session data
    fun endWorkoutSession(): WorkoutSession? {
        val session = currentWorkoutSession?.copy(
            endTime = System.currentTimeMillis()
        )
        Log.d(TAG, "Ended workout session: ${session?.id}")
        currentWorkoutSession = null
        return session
    }

    // Get the current workout session if it exists
    fun getCurrentWorkoutSession(): WorkoutSession? = currentWorkoutSession

    // Calculate the total distance in meters from a list of LatLng locations
    private fun calculateTotalDistance(locations: List<LatLng>): Float {
        if (locations.size < 2) return 0f

        var totalDistance = 0f
        for (i in 1 until locations.size) {
            val distance = FloatArray(1)
            Location.distanceBetween(
                locations[i - 1].latitude,
                locations[i - 1].longitude,
                locations[i].latitude,
                locations[i].longitude,
                distance
            )
            totalDistance += distance[0]
        }
        return totalDistance
    }

    // Utility functions to format distance, pace, and duration for display
    fun formatDistance(distanceInMeters: Float): String {
        return when {
            distanceInMeters >= 1000 -> String.format("%.2f km", distanceInMeters / 1000f)
            else -> String.format("%.0f m", distanceInMeters)
        }
    }
    fun formatPace(paceInMetersPerSecond: Float): String {
        if (paceInMetersPerSecond <= 0) return "0:00 /km"

        val paceInMinutesPerKm = 1000f / (paceInMetersPerSecond * 60f)
        val minutes = paceInMinutesPerKm.toInt()
        val seconds = ((paceInMinutesPerKm - minutes) * 60).toInt()

        return String.format("%d:%02d /km", minutes, seconds)
    }
    fun formatDuration(durationInMillis: Long): String {
        val totalSeconds = durationInMillis / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return when {
            hours > 0 -> String.format("%d:%02d:%02d", hours, minutes, seconds)
            else -> String.format("%d:%02d", minutes, seconds)
        }
    }

}