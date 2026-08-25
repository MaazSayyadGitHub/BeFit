package com.maaz.befit.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.maaz.befit.data.model.HealthData
import kotlinx.coroutines.flow.Flow
import kotlin.jvm.JvmSuppressWildcards

@Dao
interface HealthDataDao {

    // GET HEALTH DATA FROM ROOM DB

    @Query("SELECT * FROM health_data WHERE date = :date")
    fun getHealthDataByDate(date: String) : HealthData?

    @Query("SELECT * FROM health_data WHERE date = :date AND userId = :userId")
    suspend fun getHealthDataByDateAndUser(date: String, userId: String) : HealthData?


    /**
     * Flow -
     * Flow ek stream of data return krta hai..
     * means agr jaise hi data(DB tables) me dataValues change hogi.. flow use fauran return
     * kar dege jabtak wo pura data return na karde..and ham usi data ko lekar UI update
     * kardege(automatically hojayega state change per) fauran.
     *
     * Suspend fun -
     * suspend function wait krta hai till task done, in the background becos we are using it in coroutine
     *
     */

    @Query("SELECT * FROM health_data WHERE date = :date")
    fun getHealthDataByDateFlow(date: String) : Flow<HealthData?>

    @Query("SELECT * FROM health_data WHERE date = :date AND userId = :userId")
    fun getHealthDataByDateAndUserFlow(date: String, userId: String) : Flow<HealthData?>

    /** get all data but order should be decided on date column and data will be in desc(latest first) order.
    ORDER BY date DESC - order depend on date
    */
    @Query("SELECT * FROM health_data ORDER BY date DESC")
    fun getAllHealthData() : Flow<List<HealthData>>

    @Query("SELECT * FROM health_data WHERE userId = :userId ORDER BY date DESC")
    suspend fun getAllHealthDataForUser(userId: String) : List<HealthData>


    @Query("SELECT * FROM health_data WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getHealthDataBetweenDates(startDate : String, endDate : String) : Flow<List<HealthData>>


    // INSERT HEALTH DATA IN ROOM DB

    // we can use just simple (@Upsert) - (if record is not present then insert, and if present then update)
    // annotation to avoid (onConflict = OnConflictStrategy.REPLACE)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHealthData(healthData: HealthData)

    // UPDATE DATA

    @Update
    suspend fun updateHealthData(healthData: HealthData)

    // DELETE DATA
    @Delete
    suspend fun deleteHealthData(healthData: HealthData)

    @Query("DELETE FROM health_data WHERE userId = :userId")
    suspend fun deleteAllHealthDataForUser(userId: String)

    @Query("DELETE FROM health_data")
    suspend fun deleteAllHealthData()

    // GET OTHER HEALTH DATA

    @Query("SELECT AVG(steps) FROM health_data WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getAverageSteps(startDate: String, endDate: String) : Float?

    @Query("SELECT AVG(waterIntake) FROM health_data WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getAverageWaterIntake(startDate: String, endDate: String) : Float?

    @Query("SELECT AVG(sleepHours) FROM health_data WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getAverageSleepHours(startDate: String, endDate: String) : Float?
}