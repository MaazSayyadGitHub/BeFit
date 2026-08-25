package com.maaz.befit.data.service

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Device
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.gms.fitness.request.DataSourcesRequest
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * GoogleFitService for Integrating with Google Fit API
 *
 * This service handles:
 * - Google Fit Authentication and permissions
 * - Reading Health Data (Steps, distance, calories, Heart Rate, Sleep, etc.)
 * - Data synchronization with Google Fit
 * - Wearable device data integration
 *
 * @param context Android context for accessing Google Fit services
 */

class GoogleFitService(private val context: Context) {

    companion object {
        private const val TAG = "GoogleFitService"
    }

    // Google Fit Permissions configuration for accessing health data
    private val fitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_ACTIVITY_SEGMENT, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE, FitnessOptions.ACCESS_READ)
        .build()

    // Google Sign-In Configuration for Google Fit API
    private val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .addExtension(fitnessOptions)
        .requestEmail()
        .requestId()
        .requestProfile()
        .build()

    private val googleSignInClient = GoogleSignIn.getClient(context, googleSignInOptions)


    /**
     * Checks if Google Fit is available and the user is signed in.
     * @return true if Google Fit is available and the user is signed in, false otherwise.
     */
    suspend fun isGoogleFitAvailable(): Boolean = withContext(Dispatchers.IO) {
        try {
            val account = GoogleSignIn.getLastSignedInAccount(context)
            if (account == null) {
                Log.d(TAG, "No Google account signed-in")
                return@withContext false
            }

            val hasPermissions = GoogleSignIn.hasPermissions(account, fitnessOptions)
            Log.d(TAG, "Google Fit available: $hasPermissions")
            hasPermissions // Return true if the user has granted permissions, false otherwise

        } catch (e: Exception) {
            Log.e(TAG, "Error occurred while checking Google Fit availability", e)
            false// Return false in case of any exception and app should not crash.
        }
    }

    /**
     * Requests Google Fit permissions from the user if not already granted.
     * @return true if permissions are granted, false otherwise.
     */
    suspend fun requestGoogleFitPermissions(): Boolean = withContext(Dispatchers.IO) {
        try {
            val account = GoogleSignIn.getLastSignedInAccount(context)
            if (account == null) {
                Log.d(TAG, "No Google account signed-in")
                return@withContext false
            }

            val hasPermissions = GoogleSignIn.hasPermissions(account, fitnessOptions)
            if (hasPermissions) {
                Log.d(TAG, "Google Fit permissions already granted")
                return@withContext true
            }

            Log.d(TAG, "Google Fit permissions not granted - need to request permissions")
            false
        } catch (e: Exception) {
            Log.e(TAG, "Error occurred while requesting Google Fit permissions", e)
            false // Return false in case of any exception and app should not crash.
        }
    }

    /**
     * Get the Intent to request Google Fit permissions from the user.
     * @return Intent for requesting Google Fit permissions
     */
    fun getGoogleFitPermissionsIntent() : Intent {
        return GoogleSignIn.getClient(context, googleSignInOptions)
            .signInIntent
    }

    /**
     * Checks if the user needs to sign in with Google.
     * @return true if the user needs to sign in, false otherwise.
     */
    fun needsGoogleSignIn(): Boolean {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        Log.d(TAG, "Checking Google Sign-In status: account = ${account?.email ?: "null"}")
        return account == null
    }

    suspend fun handleSignInResult(data: Intent?) : Boolean = withContext(Dispatchers.IO) {
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            Log.d(TAG, "Google Sign-In successful: ${account?.email}")
            true
        } catch (e : ApiException) {
            Log.e(TAG, "Google Sign-In failed with status code: ${e.statusCode}", e)
            when(e.statusCode) {
                CommonStatusCodes.SIGN_IN_REQUIRED -> {
                    Log.e(TAG, "Sign-in required")
                }

                CommonStatusCodes.INVALID_ACCOUNT -> {
                    Log.e(TAG, "Invalid account")
                }

                CommonStatusCodes.NETWORK_ERROR -> {
                    Log.e(TAG, "Network error during sign-in")
                }

                else -> {
                    Log.e(TAG, "Unknown error during Google Sign-In", e)
                }
            }
            false
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during Google Sign-In", e)
            false
        }
    }

    /**
     * Get Google Sign-In Intent
     * @return Intent for Google Sign-In
     */
    fun getGoogleSignInIntent(): Intent {
        return googleSignInClient.signInIntent
    }

    /**
     * it will start day from 12 AM
     * will use it base on requirement
     * so we will get data of relevant day not mixed.
     */
    private fun getStartOfDay(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    /**
     * Get today's step count from Google Fit.
     * @return Total steps for today, or 0 if not available or an error occurs.
     */
    suspend fun getTodaySteps() : Int = withContext(Dispatchers.IO) {
        try {
            val account = GoogleSignIn.getLastSignedInAccount(context)
            if (account == null || !GoogleSignIn.hasPermissions(account, fitnessOptions)) {
                Log.d(TAG, "No Google account signed-in or permissions not granted")
                return@withContext 0
            }

            val endTime = System.currentTimeMillis()
            val startTime = endTime - TimeUnit.DAYS.toMillis(1) // Get data for the last 24 hours

            val readRequest = DataReadRequest.Builder()
                .read(DataType.TYPE_STEP_COUNT_DELTA)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build()

            val response = Tasks.await(
                Fitness.getHistoryClient(context, account).readData(readRequest)
            )

            var totalSteps = 0
            for (dataSet in response.dataSets) {
                for (dataPoint in dataSet.dataPoints) {
                    for (field in dataPoint.dataType.fields) {
                        if (field.name == Field.FIELD_STEPS.name) {
                            totalSteps += dataPoint.getValue(field).asInt()
                        }
                    }
                }
            }

            Log.d(TAG, "Retrieved ${totalSteps} steps from Google Fit")
            totalSteps
        } catch (e: Exception) {
            Log.e(TAG, "Error Reading steps from Google Fit", e)
            0 // Return 0 in case of any exception and app should not crash.
        }
    }

    /**
     * Get today's distance from Google Fit.
     * @return Total distance for today in meters, or 0 if not available or an error occurs.
     */
    suspend fun getTodayDistance() : Float = withContext(Dispatchers.IO) {
        try {
            val account = GoogleSignIn.getLastSignedInAccount(context)
            if (account == null || !GoogleSignIn.hasPermissions(account, fitnessOptions)) {
                Log.d(TAG, "No Google account signed-in or permissions not granted")
                return@withContext 0f
            }

            val endTime = System.currentTimeMillis()
            val startTime = endTime - TimeUnit.DAYS.toMillis(1)

            val readRequest = DataReadRequest.Builder()
                .read(DataType.TYPE_DISTANCE_DELTA)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build()

            val response = Tasks.await(
                Fitness.getHistoryClient(context, account).readData(readRequest)
            )

            var totalDistance = 0f
            for (dataSet in response.dataSets) {
                for (dataPoint in dataSet.dataPoints) {
                    for (field in dataPoint.dataType.fields) {
                        if (field.name == Field.FIELD_DISTANCE.name) {
                            totalDistance += dataPoint.getValue(field).asFloat()
                        }
                    }
                }
            }

            Log.d(TAG, "Retrieved ${totalDistance} meters distance from Google Fit")
            totalDistance
        } catch (e: Exception) {
            Log.e(TAG, "Error Reading distance from Google Fit", e)
            0f // Return 0f in case of any exception and app should not crash.
        }
    }

    /**
     * Get today's calories burned from Google Fit.
     * @return Total calories burned for today, or 0 if not available or an error occurs.
     */
    suspend fun getTodayCalories() : Int = withContext(Dispatchers.IO) {
        try {
            val account = GoogleSignIn.getLastSignedInAccount(context)
            if (account == null || !GoogleSignIn.hasPermissions(account, fitnessOptions)) {
                Log.d(TAG, "No Google account signed-in or permissions not granted")
                return@withContext 0
            }

            val endTime = System.currentTimeMillis()
            val startTime = endTime - TimeUnit.DAYS.toMillis(1)

            val readRequest = DataReadRequest.Builder()
                .read(DataType.TYPE_CALORIES_EXPENDED)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build()

            val response = Tasks.await(
                Fitness.getHistoryClient(context, account).readData(readRequest)
            )

            var totalCalories = 0f
            for (dataSet in response.dataSets) {
                for (dataPoint in dataSet.dataPoints) {
                    for (field in dataPoint.dataType.fields) {
                        if (field.name == Field.FIELD_CALORIES.name) {
                            totalCalories += dataPoint.getValue(field).asFloat()
                        }
                    }
                }
            }

            Log.d(TAG, "Retrieved ${totalCalories} calories from Google Fit")
            totalCalories.toInt()
        } catch (e: Exception) {
            Log.e(TAG, "Error Reading calories from Google Fit", e)
            0 // Return 0 in case of any exception and app should not crash.
        }
    }

    /**
     * Get today's average heart rate from Google Fit.
     * @return Average heart rate for today in BPM, or 0 if not available or an error occurs.
     */
    suspend fun getTodayHeartRate() : Int = withContext(Dispatchers.IO) {
        try {
            val account = GoogleSignIn.getLastSignedInAccount(context)
            if (account == null || !GoogleSignIn.hasPermissions(account, fitnessOptions)) {
                Log.d(TAG, "No Google account signed-in or permissions not granted for Heart Rate")
                return@withContext 0
            }

            val endTime = System.currentTimeMillis()
            val startTime = endTime - TimeUnit.DAYS.toMillis(1)

            val readRequest = DataReadRequest.Builder()
                .read(DataType.TYPE_HEART_RATE_BPM)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build()

            val response = Tasks.await(
                Fitness.getHistoryClient(context, account).readData(readRequest)
            )

            var totalHeartRate = 0f
            var heartRateCount = 0

            for (dataSet in response.dataSets) {
                for (dataPoint in dataSet.dataPoints) {
                    for (field in dataPoint.dataType.fields) {
                        if (field.name == Field.FIELD_BPM.name) {
                            totalHeartRate += dataPoint.getValue(field).asFloat()
                            heartRateCount++
                        }
                    }
                }
            }

            val averageHeartRate = if (heartRateCount > 0) {
                (totalHeartRate / heartRateCount).toInt()
            } else {
                0
            }

            Log.d(TAG, "Retrieved average heart rate: ${averageHeartRate} BPM from Google Fit")
            averageHeartRate

        } catch (e: Exception) {
            Log.e(TAG, "Error Reading heart rate from Google Fit", e)
            0 // Return 0 in case of any exception and app should not crash.
        }
    }

    /**
     * Get weekly steps from Google Fit for the last 7 days.
     * @return List of steps for each of the last 7 days, or a list
     * of 7 zeros if not available or an error occurs.
     */
    suspend fun getWeeklySteps() : List<Int> = withContext(Dispatchers.IO) {
        try {
            val account = GoogleSignIn.getLastSignedInAccount(context)
            if (account == null || !GoogleSignIn.hasPermissions(account, fitnessOptions)) {
                Log.d(TAG, "No Google account signed-in or permissions not granted for Weekly Steps")
                return@withContext List(7) { 0 } // Return a list of 7 zeros if not signed in or permissions not granted
            }

            val endTime = System.currentTimeMillis()
            val startTime = endTime - TimeUnit.DAYS.toMillis(7)

            val readRequest = DataReadRequest.Builder()
                .read(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build()

            val response = Tasks.await(
                Fitness.getHistoryClient(context, account).readData(readRequest)
            )

            val dailySteps = mutableListOf<Int>()
            val calendar = Calendar.getInstance()

            // Initialize with 0 steps for each of the last 7 days
            repeat(7) { dailySteps.add(0) }

            for (dataSet in response.dataSets) {
                for (dataPoint in dataSet.dataPoints) {
                    val timestamp = dataPoint.getStartTime(TimeUnit.MILLISECONDS)
                    calendar.timeInMillis = timestamp
                    val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

                    // convert dayOfWeek to index (0 = Sunday, 1 = Monday, ..., 6 = Saturday)
                    val dayIndex = (dayOfWeek - 1) % 7

                    for (field in dataPoint.dataType.fields) {
                        if (field.name == Field.FIELD_STEPS.name) {
                            val steps = dataPoint.getValue(field).asInt()
                            dailySteps[dayIndex] += steps
                        }
                    }
                }
            }

            Log.d(TAG, "Retrieved weekly steps: $dailySteps from Google Fit")
            dailySteps
        } catch (e: Exception) {
            Log.e(TAG, "Error Reading weekly steps from Google Fit", e)
            List(7) { 0 } // Return a list of 7 zeros in case of any exception and app should not crash.
        }
    }

    /**
     * Signs out the user from Google Fit and revokes permissions.
     * This will clear the user's Google account from the app and require re-authentication.
     */
    fun signOut() {
        googleSignInClient.signOut()
    }

    /**
     * Checks if a wearable device is connected and providing data to Google Fit.
     * @return true if a wearable device is connected, false otherwise.
     * Checks for data from devices of type TYPE_WATCH or TYPE_PHONE in the last hour.
     */
    suspend fun isWearableDeviceConnected(): Boolean = withContext(Dispatchers.IO) {
        try {
            val account = GoogleSignIn.getLastSignedInAccount(context)
            if (account == null || !GoogleSignIn.hasPermissions(account, fitnessOptions)) {
                Log.d(TAG, "No Google account signed-in or permissions not granted for wearable device check")
                return@withContext false
            }

            val endTime = System.currentTimeMillis()
            val startTime = endTime - TimeUnit.HOURS.toMillis(1) // Check for the last hour

            // Create a DataReadRequest to check for wearable device data
            val readRequest = DataReadRequest.Builder()
                .read(DataType.TYPE_STEP_COUNT_DELTA)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build()

            // Await the response from Google Fit for wearable device data
            val response = Tasks.await(
                Fitness.getHistoryClient(context, account).readData(readRequest)
            )


            var hasWearableData = false
            for (dataSet in response.dataSets) {
                for (dataPoint in dataSet.dataPoints) {
                    val dataSource = dataPoint.originalDataSource
                    if (dataSource != null) {
                        val deviceType = dataSource.device?.type
                        if (deviceType == Device.TYPE_WATCH || deviceType == Device.TYPE_PHONE) {
                            hasWearableData = true
                            break
                        }
                    }
                }
                if (hasWearableData) break
            }

            Log.d(TAG, "Wearable device connected: $hasWearableData")
            hasWearableData

        } catch (e: Exception) {
            Log.e(TAG, "Error checking wearable device connection", e)
            false // Return false in case of any exception and app should not crash.
        }
    }

    suspend fun getAvailableDataSources(): List<String> = withContext(Dispatchers.IO) {
        try {
            val account = GoogleSignIn.getLastSignedInAccount(context)
            if (account == null || !GoogleSignIn.hasPermissions(account, fitnessOptions)) {
                Log.d(TAG, "No Google account signed-in or permissions not granted for data sources check")
                return@withContext emptyList()
            }

            val dataSources = mutableListOf<String>()

            val dataSourceRequest = DataSourcesRequest.Builder()
                .setDataTypes(DataType.TYPE_STEP_COUNT_DELTA)
                .build()

            val response = Tasks.await(
                Fitness.getSensorsClient(context, account).findDataSources(dataSourceRequest)
            )

            for (dataSource in response) {
                val deviceName = dataSource.device?.model ?: "Unknown Device"
                val deviceType = when(dataSource.device?.type) {
                    Device.TYPE_WATCH -> "SmartWatch"
                    Device.TYPE_PHONE -> "Phone"
                    Device.TYPE_TABLET -> "Tablet"
                    else -> "Other"
                }

                dataSources.add("$deviceName ($deviceType)")
            }

            Log.d(TAG, "Available data sources: $dataSources")
            dataSources

        } catch (e : Exception) {
            Log.e(TAG, "Error retrieving available data sources", e)
            emptyList() // Return an empty list in case of any exception and app should not crash.
        }
    }

}