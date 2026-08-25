package com.maaz.befit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maaz.befit.data.model.HealthData
import com.maaz.befit.data.model.UserGoals
import com.maaz.befit.data.repository.HealthRepository
import com.maaz.befit.data.service.GoogleFitService
import com.maaz.befit.data.service.WearableDeviceService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject


@HiltViewModel
class HealthViewModel @Inject constructor(
    private val healthRepository: HealthRepository,
    private val googleFitService: GoogleFitService,
    private val wearableDeviceService: WearableDeviceService
) : ViewModel(){

    private val _currentHealthData = MutableStateFlow<HealthData?>(null)
    val currentHealthData: StateFlow<HealthData?> = _currentHealthData.asStateFlow()

    // Reactive flow for today's health data that automatically updates
    private val _todayDate = MutableStateFlow(getCurrentDate())
    val todayHealthData: StateFlow<HealthData?> = _todayDate
        .flatMapLatest { date ->
            healthRepository.getHealthDataByDateFlow(date)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _userGoals = MutableStateFlow<UserGoals?>(null)
    val userGoals: StateFlow<UserGoals?> = _userGoals.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Wearable device integration
    private val _connectedWearables = MutableStateFlow<List<String>>(emptyList())
    val connectedWearables: StateFlow<List<String>> = _connectedWearables.asStateFlow()

    private val _isWearableSyncInProgress = MutableStateFlow(false)
    val isWearableSyncInProgress: StateFlow<Boolean> = _isWearableSyncInProgress.asStateFlow()

    init {
        loadUserGoals()
        loadTodayHealthData()
        checkWearableDevices()
    }

    private fun loadUserGoals() {
        viewModelScope.launch {
            healthRepository.getUserGoals().collect { goals ->
                _userGoals.value = goals ?: UserGoals() // Default goals if none exist
            }
        }
    }

    fun loadTodayHealthData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val today = getCurrentDate()
                val healthData = healthRepository.getHealthDataByDate(today)

                if (healthData == null) {
                    // Create new health data for today
                    val newHealthData = HealthData(
                        date = today,
                        createdAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).toString(),
                        updatedAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).toString()
                    )
                    healthRepository.insertOrUpdateHealthData(newHealthData)
                    _currentHealthData.value = newHealthData
                } else {
                    _currentHealthData.value = healthData
                }

            } catch (e: Exception) {
                _errorMessage.value = "Failed to load health data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addWaterIntake(amount: Int) {
        viewModelScope.launch {
            try {
                healthRepository.addWaterIntake(amount)
                // Force refresh to ensure UI updates immediately
                refreshData()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to add water intake: ${e.message}"
            }
        }
    }

    fun addSteps(steps: Int) {
        viewModelScope.launch {
            try {
                healthRepository.addSteps(steps)
                // Force refresh to ensure UI updates immediately
                refreshData()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to add steps: ${e.message}"
            }
        }
    }

    fun addSleepLog(sleepStart: String, sleepEnd: String, duration: Float, quality: Int = 0) {
        viewModelScope.launch {
            try {
                healthRepository.addSleepLog(sleepStart, sleepEnd, duration, quality)
                // Force refresh to ensure UI updates immediately
                refreshData()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to add sleep log: ${e.message}"
            }
        }
    }

    fun addRunningActivity(steps: Int, distance: Float, duration: Int, calories: Int) {
        viewModelScope.launch {
            try {
                healthRepository.addRunningActivity(steps, distance, duration, calories)
                // Force refresh to ensure UI updates immediately
                refreshData()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to add running activity: ${e.message}"
            }
        }
    }

    fun updateUserGoals(goals: UserGoals) {
        viewModelScope.launch {
            try {
                healthRepository.updateUserGoals(goals)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update goals: ${e.message}"
            }
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            try {
                // Force refresh the reactive flow by updating the date
                val currentDate = getCurrentDate()
                _todayDate.value = currentDate

                // Also load current health data to ensure consistency
                loadTodayHealthData()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to refresh data: ${e.message}"
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    // Google Fit Integration
    suspend fun syncWithGoogleFit() {
        try {
            _isLoading.value = true
            _errorMessage.value = null

            if (googleFitService.isGoogleFitAvailable()) {
                val steps = googleFitService.getTodaySteps()
                val distance = googleFitService.getTodayDistance()
                val calories = googleFitService.getTodayCalories()
                val heartRate = googleFitService.getTodayHeartRate()

                // Get current health data or create new
                val currentData = _currentHealthData.value ?: HealthData(
                    date = getCurrentDate(),
                    steps = 0,
                    waterIntake = 0,
                    sleepHours = 0f,
                    distance = 0f,
                    caloriesBurned = 0,
                    heartRate = 0,
                    healthScore = 0,
                    userId = ""
                )

                val updatedData = currentData.copy(
                    steps = steps,
                    distance = distance,
                    caloriesBurned = calories,
                    heartRate = heartRate
                )

                healthRepository.insertOrUpdateHealthData(updatedData)
                _currentHealthData.value = updatedData

                _errorMessage.value = "✅ Google Fit data synced successfully"
            } else {
                _errorMessage.value = "❌ Google Fit not available. Please check your connection and permissions."
            }
        } catch (e: Exception) {
            _errorMessage.value = "❌ Failed to sync with Google Fit: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun isGoogleFitAvailable(): Boolean {
        return googleFitService.isGoogleFitAvailable()
    }

    suspend fun getWeeklyStepsFromGoogleFit(): List<Int> {
        return try {
            googleFitService.getWeeklySteps()
        } catch (e: Exception) {
            _errorMessage.value = "Failed to get weekly steps: ${e.message}"
            emptyList()
        }
    }

    fun resetAllData() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                healthRepository.resetAllData()
                loadTodayHealthData() // Refresh with empty data
                loadUserGoals() // Reset to default goals
            } catch (e: Exception) {
                _errorMessage.value = "Failed to reset data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun getCurrentDate(): String {
        return Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
    }

    // Wearable Device Integration Methods

    /**
     * Check for connected wearable devices
     */
    private fun checkWearableDevices() {
        viewModelScope.launch {
            try {
                val devices = wearableDeviceService.getConnectedDevices()
                _connectedWearables.value = devices
            } catch (e: Exception) {
                _errorMessage.value = "Failed to check wearable devices: ${e.message}"
            }
        }
    }

    /**
     * Refresh wearable devices list (public method)
     */
    fun refreshWearableDevices() {
        checkWearableDevices()
    }

    /**
     * Sync data from all connected wearable devices
     */
    fun syncWearableData() {
        viewModelScope.launch {
            try {
                _isWearableSyncInProgress.value = true
                _errorMessage.value = null

                val connectedDevices = wearableDeviceService.getConnectedDevices()
                if (connectedDevices.isNotEmpty()) {
                    val syncSuccess = wearableDeviceService.syncWearableData()
                    if (syncSuccess) {
                        // Get aggregated data from all wearable devices
                        val wearableData = wearableDeviceService.getAggregatedWearableData()

                        // Get current health data or create new
                        val currentData = _currentHealthData.value ?: HealthData(
                            date = getCurrentDate(),
                            steps = 0,
                            waterIntake = 0,
                            sleepHours = 0f,
                            distance = 0f,
                            caloriesBurned = 0,
                            heartRate = 0,
                            healthScore = 0,
                            userId = ""
                        )

                        if (wearableData.isNotEmpty()) {
                            val updatedData = currentData.copy(
                                steps = (wearableData["steps"] as? Int) ?: currentData.steps,
                                caloriesBurned = (wearableData["calories"] as? Int) ?: currentData.caloriesBurned,
                                heartRate = (wearableData["heartRate"] as? Int) ?: currentData.heartRate,
                                distance = (wearableData["distance"] as? Double)?.toFloat() ?: currentData.distance,
                                sleepHours = (wearableData["sleepHours"] as? Double)?.toFloat() ?: currentData.sleepHours
                            )

                            healthRepository.insertOrUpdateHealthData(updatedData)
                            _currentHealthData.value = updatedData

                            _errorMessage.value = "✅ Synced data from ${connectedDevices.size} device(s)"
                        }
                    } else {
                        _errorMessage.value = "❌ Sync failed. Please check device connections."
                    }
                } else {
                    _errorMessage.value = "❌ No wearable devices found. Please connect a device first."
                }

                // Refresh connected devices list
                checkWearableDevices()
            } catch (e: Exception) {
                _errorMessage.value = "❌ Failed to sync wearable data: ${e.message}"
            } finally {
                _isWearableSyncInProgress.value = false
            }
        }

        /**
         * Check if any wearable devices are connected
         */
        suspend fun hasConnectedWearables(): Boolean {
            return try {
                wearableDeviceService.hasConnectedWearables()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to check wearable connection: ${e.message}"
                false
            }
        }

    }

    /**
     * Get Samsung Health data
     */
    suspend fun getSamsungHealthData(): Map<String, Any> {
        return try {
            wearableDeviceService.getSamsungHealthData()
        } catch (e: Exception) {
            _errorMessage.value = "Failed to get Samsung Health data: ${e.message}"
            emptyMap()
        }
    }

    /**
     * Get Wear OS data
     */
    suspend fun getWearOSData(): Map<String, Any> {
        return try {
            wearableDeviceService.getWearOSData()
        } catch (e: Exception) {
            _errorMessage.value = "Failed to get Wear OS data: ${e.message}"
            emptyMap()
        }
    }

    /**
     * Check if Samsung Health is available
     */
    suspend fun isSamsungHealthAvailable(): Boolean {
        return try {
            wearableDeviceService.isSamsungHealthAvailable()
        } catch (e: Exception) {
            _errorMessage.value = "Failed to check Samsung Health availability: ${e.message}"
            false
        }
    }

    /**
     * Check if Wear OS is connected
     */
    suspend fun isWearOSConnected(): Boolean {
        return try {
            wearableDeviceService.isWearOSConnected()
        } catch (e: Exception) {
            _errorMessage.value = "Failed to check Wear OS connection: ${e.message}"
            false
        }
    }

    /**
     * Get detailed connection status for all services
     */
    suspend fun getConnectionStatus(): Map<String, Any> {
        return try {
            val googleFitAvailable = googleFitService.isGoogleFitAvailable()
            val wearableConnected = wearableDeviceService.hasConnectedWearables()
            val connectedDevices = wearableDeviceService.getConnectedDevices()
            val googleFitDataSources = googleFitService.getAvailableDataSources()

            mapOf(
                "googleFit" to mapOf(
                    "available" to googleFitAvailable,
                    "dataSources" to googleFitDataSources
                ),
                "wearables" to mapOf(
                    "connected" to wearableConnected,
                    "devices" to connectedDevices
                ),
                "lastChecked" to System.currentTimeMillis()
            )
        } catch (e: Exception) {
            _errorMessage.value = "Failed to get connection status: ${e.message}"
            emptyMap()
        }
    }
}