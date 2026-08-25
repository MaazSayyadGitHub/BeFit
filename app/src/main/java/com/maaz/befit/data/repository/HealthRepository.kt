package com.maaz.befit.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.maaz.befit.data.dao.HealthDataDao
import com.maaz.befit.data.dao.SleepLogDao
import com.maaz.befit.data.dao.StepLogDao
import com.maaz.befit.data.dao.UserGoalsDao
import com.maaz.befit.data.dao.WaterLogDao
import com.maaz.befit.data.model.HealthData
import com.maaz.befit.data.model.SleepLog
import com.maaz.befit.data.model.StepLog
import com.maaz.befit.data.model.UserGoals
import com.maaz.befit.data.model.WaterLog
import com.maaz.befit.data.service.FirebaseDataService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthRepository @Inject constructor(
    private val healthDataDao : HealthDataDao,
    private val waterLogDao : WaterLogDao,
    private val stepLogDao: StepLogDao,
    private val sleepLogDao: SleepLogDao,
    private val userGoalsDao: UserGoalsDao,
    private val firebaseDataService: FirebaseDataService,
    private val firebaseAuth: FirebaseAuth,
) {

    // ===== HEALTH DATA OPERATIONS =====
    // These methods handle daily aggregated health data

    /**
     * get health data for a specific date
     * @param date Date in YYYY-MM-DD format
     * @return HealthData for the specified date, or null if not found
     */
    suspend fun getHealthDataByDate(date: String): HealthData? {
        val userId = getCurrentUserId()
        return if (userId != null) {
            // Try Firebase first, fallback to local database
            firebaseDataService.getHealthDataByDate(date)
                ?: healthDataDao.getHealthDataByDateAndUser(date, userId)
        } else {
            null
        }
    }

    /**
     * Get health data for a specific date as a Flow for reactive updates
     * @param date Date in YYYY-MM-DD format
     * @return Flow of HealthData for the specified date
     */
    fun getHealthDataByDateFlow(date: String): Flow<HealthData?> {
        val userId = getCurrentUserId()
        return if (userId != null) {
            // Use reactive flow from Room database that automatically updates when data changes
            healthDataDao.getHealthDataByDateAndUserFlow(date, userId)
        } else {
            flow { emit(null) }
        }
    }

    /**
     * Get all health data ordered by date (newest first)
     * @return Flow of all health data for reactive UI updates
     */
    fun getAllHealthData(): Flow<List<HealthData>> {
        return healthDataDao.getAllHealthData()
    }

    /**
     * Get health data between two dates for analytics
     * @param startDate Start date in YYYY-MM-DD format
     * @param endDate End date in YYYY-MM-DD format
     * @return Flow of health data within the date range
     */
    fun getHealthDataBetweenDates(startDate: String, endDate: String): Flow<List<HealthData>> {
        return healthDataDao.getHealthDataBetweenDates(startDate, endDate)
    }


    /**
     * Insert new health data or update existing data for a date
     * @param healthData HealthData object to insert or update
     */
    suspend fun insertOrUpdateHealthData(healthData: HealthData) {
        val userId = getCurrentUserId()
        if (userId != null) {
            val healthDataWithUserId = healthData.copy(userId = userId)

            // Save to Firebase
            firebaseDataService.saveHealthData(healthDataWithUserId)

            // Also save locally for offline access
            val existing = healthDataDao.getHealthDataByDateAndUser(healthData.date, userId)
            if (existing != null) {
                healthDataDao.updateHealthData(healthDataWithUserId)
            } else {
                healthDataDao.insertHealthData(healthDataWithUserId)
            }
        }
    }

    // ===== WATER TRACKING OPERATIONS =====
    // These methods handle water intake logging and aggregation

    /**
     * Get all water logs for a specific date
     * @param date Date in YYYY-MM-DD format
     * @return Flow of water logs for reactive UI updates
     */
    fun getWaterLogsByDate(date: String): Flow<List<WaterLog>> {
        return waterLogDao.getWaterLogsByDate(date)
    }

    /**
     * Get total water intake for a specific date
     * @param date Date in YYYY-MM-DD format
     * @return Total water intake in ml for the date
     */
    suspend fun getTotalWaterIntakeForDate(date: String): Int {
        return waterLogDao.getTotalWaterIntakeForDate(date) ?: 0
    }


    /**
     * Add water intake entry and update daily health data
     * @param amount Water amount in ml
     * @param date Date in YYYY-MM-DD format (defaults to current date)
     */
    suspend fun addWaterIntake(amount: Int, date: String = getCurrentDate()) {
        val userId = getCurrentUserId()
        if (userId != null) {
            val timestamp = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).toString()
            val waterLog = WaterLog(
                userId = userId,
                amount = amount,
                timestamp = timestamp,
                date = date
            )

            // Save to Firebase
            firebaseDataService.saveWaterLog(waterLog)

            // Also save locally
            waterLogDao.insertWaterLog(waterLog)

            // Update aggregated health data for the date
            updateHealthDataForDate(date)
        }
    }

    // Step tracking operations
    fun getStepLogsByDate(date: String): Flow<List<StepLog>> {
        return stepLogDao.getStepLogsByDate(date)
    }

    suspend fun getTotalStepsForDate(date: String): Int {
        return stepLogDao.getTotalStepsForDate(date) ?: 0
    }

    suspend fun addSteps(steps: Int, date: String = getCurrentDate()) {
        val userId = getCurrentUserId()
        if (userId != null) {
            val timestamp = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).toString()
            val stepLog = StepLog(
                userId = userId,
                steps = steps,
                timestamp = timestamp,
                date = date
            )

            // Save to Firebase
            firebaseDataService.saveStepLog(stepLog)

            // Also save locally
            stepLogDao.insertStepLog(stepLog)

            // Update health data
            updateHealthDataForDate(date)
        }
    }

    // Sleep tracking operations
    fun getSleepLogsByDate(date: String): Flow<List<SleepLog>> {
        return sleepLogDao.getSleepLogsByDate(date)
    }

    suspend fun getAverageSleepDurationForDate(date: String): Float {
        return sleepLogDao.getAverageSleepDurationForDate(date) ?: 0f
    }

    suspend fun addSleepLog(sleepStart: String, sleepEnd: String, duration: Float, quality: Int = 0, date: String = getCurrentDate()) {
        val userId = getCurrentUserId()
        if (userId != null) {
            val sleepLog = SleepLog(
                userId = userId,
                sleepStart = sleepStart,
                sleepEnd = sleepEnd,
                duration = duration,
                quality = quality,
                date = date
            )

            // Save to Firebase
            firebaseDataService.saveSleepLog(sleepLog)

            // Also save locally
            sleepLogDao.insertSleepLog(sleepLog)

            // Update health data
            updateHealthDataForDate(date)
        }
    }

    /**
     * Add running activity and update health data
     * @param steps Steps taken during the run
     * @param distance Distance covered in meters
     * @param duration Duration in minutes
     * @param calories Calories burned
     * @param date Date in YYYY-MM-DD format (defaults to current date)
     */
    suspend fun addRunningActivity(steps: Int, distance: Float, duration: Int, calories: Int, date: String = getCurrentDate()) {
        val userId = getCurrentUserId()
        if (userId != null) {
            // Add steps
            addSteps(steps, date)

            // Update health data with running-specific metrics
            val currentData = getHealthDataByDate(date)
            if (currentData != null) {
                val updatedData = currentData.copy(
                    distance = currentData.distance + distance,
                    caloriesBurned = currentData.caloriesBurned + calories
                )
                insertOrUpdateHealthData(updatedData)
            }
        }
    }

    // User goals operations
    fun getUserGoals(): Flow<UserGoals?> {
        return userGoalsDao.getUserGoals()
    }

    suspend fun updateUserGoals(userGoals: UserGoals) {
        val userId = getCurrentUserId()
        if (userId != null) {
            val userGoalsWithUserId = userGoals.copy(userId = userId)

            // Save to Firebase
            firebaseDataService.saveUserGoals(userGoalsWithUserId)

            // Also save locally
            userGoalsDao.insertUserGoals(userGoalsWithUserId)
        }
    }

    // Analytics operations
    suspend fun getAverageSteps(startDate: String, endDate: String): Float {
        return healthDataDao.getAverageSteps(startDate, endDate) ?: 0f
    }

    suspend fun getAverageWaterIntake(startDate: String, endDate: String): Float {
        return healthDataDao.getAverageWaterIntake(startDate, endDate) ?: 0f
    }

    suspend fun getAverageSleepHours(startDate: String, endDate: String): Float {
        return healthDataDao.getAverageSleepHours(startDate, endDate) ?: 0f
    }

    // ===== HELPER FUNCTIONS =====
    // These methods handle data aggregation and health score calculation

    /**
     * Update aggregated health data for a specific date
     * This method is called whenever new health data is added
     * @param date Date in YYYY-MM-DD format
     */
    private suspend fun updateHealthDataForDate(date: String) {
        // Get aggregated data for the date
        val steps = getTotalStepsForDate(date)
        val waterIntake = getTotalWaterIntakeForDate(date)
        val sleepHours = getAverageSleepDurationForDate(date)

        // Calculate health score based on aggregated data
        val healthScore = calculateHealthScore(steps, waterIntake, sleepHours)

        // Create or update health data record
        val healthData = HealthData(
            date = date,
            steps = steps,
            waterIntake = waterIntake,
            sleepHours = sleepHours,
            healthScore = healthScore,
            createdAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).toString(),
            updatedAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).toString()
        )

        insertOrUpdateHealthData(healthData)
    }

    /**
     * Calculate health score based on daily metrics
     * Scoring system: Steps (40%), Water (30%), Sleep (30%)
     * @param steps Daily step count
     * @param waterIntake Daily water intake in ml
     * @param sleepHours Daily sleep duration in hours
     * @return Health score from 0-100
     */
    private fun calculateHealthScore(steps: Int, waterIntake: Int, sleepHours: Float): Int {
        val stepsScore = (steps / 10000f * 40).coerceAtMost(40f) // Max 40 points for steps
        val waterScore = (waterIntake / 2000f * 30).coerceAtMost(30f) // Max 30 points for water
        val sleepScore = (sleepHours / 8f * 30).coerceAtMost(30f) // Max 30 points for sleep

        return (stepsScore + waterScore + sleepScore).toInt()
    }

    suspend fun resetAllData() {
        val userId = getCurrentUserId()
        if (userId != null) {
            // Clear all data from local database for this user only
            // Firebase data is preserved for future login
            healthDataDao.deleteAllHealthDataForUser(userId)
            waterLogDao.deleteAllWaterLogsForUser(userId)
            stepLogDao.deleteAllStepLogsForUser(userId)
            sleepLogDao.deleteAllSleepLogsForUser(userId)

            // Reset user goals to default locally
            val defaultGoals = UserGoals(userId = userId)
            userGoalsDao.insertUserGoals(defaultGoals)
        }
    }

    /**
     * Clear all data from Firebase (use with caution - this permanently deletes user data)
     * This method should only be used for account deletion, not regular logout
     */
    suspend fun deleteAllFirebaseData() {
        val userId = getCurrentUserId()
        if (userId != null) {
            // Clear all data from Firebase
            firebaseDataService.clearUserData()
        }
    }

    /**
     * Sync all local data to Firebase for the current user
     * This method should be called when user logs in
     */
    suspend fun syncLocalDataToFirebase() {
        val userId = getCurrentUserId()
        if (userId != null) {
            // Get all local data for the user
            val healthDataList = healthDataDao.getAllHealthDataForUser(userId)
            val waterLogs = waterLogDao.getAllWaterLogsForUser(userId)
            val stepLogs = stepLogDao.getAllStepLogsForUser(userId)
            val sleepLogs = sleepLogDao.getAllSleepLogsForUser(userId)
            val userGoals = userGoalsDao.getUserGoalsForUser(userId)

            // Sync to Firebase
            firebaseDataService.syncAllDataToFirebase(
                healthDataList, waterLogs, stepLogs, sleepLogs, userGoals
            )
        }
    }

    /**
     * Get the current authenticated user ID
     * @return User ID if authenticated, null otherwise
     */
    private fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }

    private fun getCurrentDate(): String {
        return Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
    }
}