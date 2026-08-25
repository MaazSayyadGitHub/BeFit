package com.maaz.befit.data.service

import androidx.compose.ui.graphics.Path
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.maaz.befit.data.model.HealthData
import com.maaz.befit.data.model.SleepLog
import com.maaz.befit.data.model.StepLog
import com.maaz.befit.data.model.UserGoals
import com.maaz.befit.data.model.WaterLog
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class FirebaseDataService @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
){

    /**
     *
     * val userId = getCurrentUserId() ?: return
     * its checks user loggedIn or not by getting userId.
     *
     *
     * SetOptions.merge() ka matlab hai
     * "Purana data udana mat,
     * sirf updated fields ko override/patch kar do."
     * Ye offline-first apps ke liye absolute best practice hai!
     *
     * Previous stored data
     * {
     *   "steps": 5000,
     *   "waterIntake": 2.5,
     *   "calories": 300,
     *   "userNotes": "Felt good today" // <--- Extra Field
     * }
     *
     * new data to store
     * {
     *   "steps": 6000,
     *   "waterIntake": 3.0
     *   // calories aur userNotes blank hain is object me
     * }
     *
     * so calories and userNotes will not wipeout, that just be there and related new data will update.
     */

    // HEALTH SECTION

    suspend fun saveHealthData(healthData : HealthData) {
        val userId = getCurrentUserId() ?: return
        firestore.collection("users") // users node
            .document(userId) // userid data
            .collection("health_data") // health_data node
            .document(healthData.date) // dateWise data
            .set(healthData, SetOptions.merge()) // actual health_data
            .await() // wait till task done - and don't need (addOnSuccessListener, addOnFailureListener)
    }

    suspend fun getHealthDataByDate(date: String) : HealthData? {
        val userId = getCurrentUserId() ?: return null

        return try {
            val document = firestore.collection("users")
                .document(userId)
                .collection("health_data")
                .document(date)
                .get()
                .await()

            if (document.exists()) {
                // convert fetched data into HealthData object and return
                document.toObject(HealthData::class.java)
            } else {
                null
            }
         } catch (e : Exception) {
             null
         }
    }

    suspend fun getAllHealthData() : List<HealthData> {
        val userId = getCurrentUserId() ?: return emptyList()
        return try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("health_data")
                .orderBy("date", Query.Direction.DESCENDING) // filter date documents into DESC Order
                .get()
                .await()

            /** mapNotNull -
             Date ke saare documents ko (HealthData) Kotlin Objects me convert karta hai,
             aur jo corrupted/null hain unhe hata kar ek clean List deta hai.
             only get notNull data and null/corrupted data should not be convert to kotlin object.
            */
            snapshot.documents.mapNotNull { it.toObject(HealthData::class.java) }

        } catch (e : Exception) {
            emptyList()
        }
    }

    suspend fun getHealthDataBetweenDates(startDate : String, endDate : String) : List<HealthData> {
        val userId = getCurrentUserId() ?: return emptyList()

        return try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("health_data")
                .whereGreaterThanOrEqualTo("date", startDate)
                .whereLessThanOrEqualTo("date", endDate)
                .orderBy("date", Query.Direction.ASCENDING) // last data first / serial wise (1.2.3.4)
                .get()
                .await()

            snapshot.documents.mapNotNull { it.toObject(HealthData::class.java) }

        } catch (e : Exception) {
            emptyList()
        }
    }

    // WATER SECTION

    suspend fun saveWaterLog(waterLog: WaterLog) {
        val userId = getCurrentUserId() ?: return

        val logId = if (waterLog.id == 0L) {
            // generate new id for new logs
            System.currentTimeMillis().toString()
        } else {
            waterLog.id.toString()
        }

        firestore.collection("users")
            .document(userId)
            .collection("water_logs")
            .document(logId)
            .set(waterLog.copy(id = logId.toLong()), SetOptions.merge()) // optional added
            .await()
    }

    suspend fun getWaterLogsByDate(date: String) : List<WaterLog> {
        val userId = getCurrentUserId() ?: return emptyList()

        return try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("water_logs")
                .whereEqualTo("date", date)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { it.toObject(WaterLog::class.java) }
        } catch (e : Exception) {
            emptyList()
        }
    }

    // STEP SECTION

    suspend fun saveStepLog(stepLog: StepLog) {
        val userId = getCurrentUserId() ?: return

        val logId = if (stepLog.id == 0L) {
            System.currentTimeMillis().toString()
        } else {
            stepLog.id.toString()
        }

        firestore.collection("users")
            .document(userId)
            .collection("step_logs")
            .document(logId)
            .set(stepLog.copy(id = logId.toLong()))
            .await()
    }

    suspend fun getStepsLogsByDate(date: String) : List<StepLog> {
        val userId = getCurrentUserId() ?: return emptyList()

        return try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("step_logs")
                .whereEqualTo("date", date)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { it.toObject(StepLog::class.java) }
        } catch (e : Exception){
            emptyList()
        }
    }

    // SLEEP SECTION

    suspend fun saveSleepLog(sleepLog: SleepLog) {
        val userId = getCurrentUserId() ?: return

        val logId = if (sleepLog.id == 0L) {
            System.currentTimeMillis().toString()
        } else {
            sleepLog.id.toString()
        }

        firestore.collection("users")
            .document(userId)
            .collection("sleep_logs")
            .document(logId)
            .set(sleepLog.copy(id = logId.toLong()))
            .await()
    }

    suspend fun getSleepLogsByDate(date: String) : List<SleepLog> {
        val userId = getCurrentUserId() ?: return emptyList()

        return try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("sleep_logs")
                .whereEqualTo("date", date)
                .orderBy("sleepStart", Query.Direction.ASCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { it.toObject(SleepLog::class.java) }
        } catch (e : Exception) {
            emptyList()
        }
    }

    suspend fun saveUserGoals(userGoals: UserGoals) {
        val userId = getCurrentUserId() ?: return

        firestore.collection("users")
            .document(userId)
            .collection("user_goals")
            .document("goals")
            .set(userGoals)
            .await()
    }

    suspend fun getUserGoals() : UserGoals? {
        val userId = getCurrentUserId() ?: return null

        return try {
            val document = firestore.collection("users")
                .document(userId)
                .collection("user_goals")
                .document("goals")
                .get()
                .await()

            if (document.exists()) {
                document.toObject(UserGoals::class.java)
            } else {
                null
            }
        } catch (e : Exception) {
            null
        }
    }

    suspend fun syncAllDataToFirebase(
        healthDataList : List<HealthData>,
        waterLogs : List<WaterLog>,
        stepLogs: List<StepLog>,
        sleepLogs: List<SleepLog>,
        userGoals: UserGoals?
    ) {
        val userId = getCurrentUserId() ?: return

        // save health data
        healthDataList.forEach { healthData ->
            saveHealthData(healthData)
        }

        // save water logs
        waterLogs.forEach { waterLog ->
            saveWaterLog(waterLog)
        }

        // save step logs
        stepLogs.forEach { stepLog ->
            saveStepLog(stepLog)
        }

        // save sleep logs
        sleepLogs.forEach { sleepLog ->
            saveSleepLog(sleepLog)
        }

        // save userGoals
        userGoals?.let { userGoals ->
            saveUserGoals(userGoals)
        }
    }

    /**
     * Clear all data for the current user from Firebase
     * This method should be called when user logs out
     */
    suspend fun clearUserData() {
        val userId = getCurrentUserId() ?: return

        try {
            // delete all collections for the user
            val collections = listOf("health_data", "water_logs", "step_logs", "sleep_logs", "user_goals")

            collections.forEach {collectionName ->
                val snapshot = firestore.collection("users")
                    .document(userId)
                    .collection(collectionName)
                    .get()
                    .await()

                val batch = firestore.batch()

                snapshot.documents.forEach { document ->
                    batch.delete(document.reference)
                }

                batch.commit().await()
            }
        } catch (e : Exception) {
            // handle error silently - data will be cleared on next login
        }
    }

    // HELPER METHODS

    /**
     * Get the current authenticated UserID
     * @return UserId if authenticated or null otherwise
     */
    private fun getCurrentUserId() : String? {
        return firebaseAuth.currentUser?.uid // if loggedOut user then it will not return userId
    }

    /**
     * Check if user is authenticated,
     * @return true if user is authenticated else
     */
    fun isUserAuthenticated() : Boolean {
        return firebaseAuth.currentUser != null
    }

}