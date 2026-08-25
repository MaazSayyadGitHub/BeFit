package com.maaz.befit.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.maaz.befit.data.model.SleepLog
import kotlinx.coroutines.flow.Flow

@Dao
interface SleepLogDao {

    // GET Data from sleep_logs table

    @Query("SELECT * FROM sleep_logs WHERE date = :date ORDER BY sleepStart DESC")
    fun getSleepLogsByDate(date: String) : Flow<List<SleepLog>>

    @Query("SELECT AVG(duration) FROM sleep_logs WHERE date = :date")
    suspend fun getAverageSleepDurationForDate(date: String) : Float?

    @Query("SELECT * FROM sleep_logs ORDER BY sleepStart DESC LIMIT :limit")
    fun getRecentSleepLogs(limit: Int = 30) : Flow<List<SleepLog>>

    @Query("SELECT * FROM sleep_logs WHERE userId = :userId ORDER BY sleepStart DESC")
    suspend fun getAllSleepLogsForUser(userId : String) : List<SleepLog>

    // insert data
    @Insert
    suspend fun insertSleepLog(sleepLog: SleepLog)

    // update
    @Update
    suspend fun updateSleepLog(sleepLog: SleepLog)

    // delete
    @Delete
    suspend fun deleteSleepLog(sleepLog: SleepLog)

    @Query("DELETE FROM sleep_logs WHERE id = :id")
    suspend fun deleteSleepLogById(id: Long)

    @Query("DELETE FROM sleep_logs")
    suspend fun deleteAllSleepLogs()

    @Query("DELETE FROM sleep_logs WHERE userId = :userId")
    suspend fun deleteAllSleepLogsForUser(userId: String)
}