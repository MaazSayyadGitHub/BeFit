package com.maaz.befit.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.maaz.befit.data.model.WaterLog
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterLogDao {

    @Query("SELECT * FROM water_logs WHERE date = :date ORDER BY timestamp DESC")
    fun getWaterLogsByDate(date: String) : Flow<List<WaterLog>>

    @Query("SELECT SUM(amount) FROM water_logs WHERE date = :date ")
    suspend fun getTotalWaterIntakeForDate(date: String) : Int?

    @Query("SELECT * FROM water_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentWaterLogs(limit : Int = 50) : Flow<List<WaterLog>>

    @Query("SELECT * FROM water_logs WHERE userId = :userId ORDER BY timestamp DESC")
    suspend fun getAllWaterLogsForUser(userId: String) : List<WaterLog>

    @Insert
    suspend fun insertWaterLog(waterLog: WaterLog)

    @Update
    suspend fun updateWaterLog(waterLog: WaterLog)

    @Delete
    suspend fun deleteWaterLog(waterLog: WaterLog)

    @Query("DELETE FROM water_logs WHERE id = :id")
    suspend fun deleteWaterLogById(id: Long)

    @Query("DELETE FROM water_logs")
    suspend fun deleteAllWaterLogs()

    @Query("DELETE FROM water_logs WHERE userId = :userId")
    suspend fun deleteAllWaterLogsForUser(userId: String)

}

/**
 *
 * @Query("SELECT * FROM water_logs WHERE date = :date ORDER BY timestamp DESC")
 *     fun getWaterLogsByDate(date: String): Flow<List<WaterLog>>
 *
 *     @Query("SELECT SUM(amount) FROM water_logs WHERE date = :date")
 *     suspend fun getTotalWaterIntakeForDate(date: String): Int?
 *
 *     @Query("SELECT * FROM water_logs ORDER BY timestamp DESC LIMIT :limit")
 *     fun getRecentWaterLogs(limit: Int = 50): Flow<List<WaterLog>>
 *
 *     @Insert
 *     suspend fun insertWaterLog(waterLog: WaterLog)
 *
 *     @Update
 *     suspend fun updateWaterLog(waterLog: WaterLog)
 *
 *     @Delete
 *     suspend fun deleteWaterLog(waterLog: WaterLog)
 *
 *     @Query("DELETE FROM water_logs WHERE id = :id")
 *     suspend fun deleteWaterLogById(id: Long)
 *
 *     @Query("DELETE FROM water_logs")
 *     suspend fun deleteAllWaterLogs()
 *
 *     @Query("SELECT * FROM water_logs WHERE userId = :userId ORDER BY timestamp DESC")
 *     suspend fun getAllWaterLogsForUser(userId: String): List<WaterLog>
 *
 *     @Query("DELETE FROM water_logs WHERE userId = :userId")
 *     suspend fun deleteAllWaterLogsForUser(userId: String)
 *
 */