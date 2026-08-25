package com.maaz.befit.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.maaz.befit.data.model.StepLog
import kotlinx.coroutines.flow.Flow

@Dao
interface StepLogDao {

    // get all steps data from room db

    @Query("SELECT * FROM step_logs WHERE date = :date ORDER BY timestamp DESC")
    fun getStepLogsByDate(date : String) : Flow<List<StepLog>>

    @Query("SELECT SUM(steps) FROM step_logs WHERE date = :date")
    suspend fun getTotalStepsForDate(date: String) : Int?

    @Query("SELECT * FROM step_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentStepLogs(limit: Int = 50) : Flow<List<StepLog>>

    @Query("SELECT * FROM step_logs WHERE userId = :userId ORDER BY timestamp DESC")
    suspend fun getAllStepLogsForUser(userId: String) : List<StepLog>

    // insert
    @Insert
    suspend fun insertStepLog(stepLog: StepLog)

    @Update
    suspend fun updateStepLog(stepLog: StepLog)

    // delete

    @Delete
    suspend fun deleteStepLog(stepLog: StepLog)

    @Query("DELETE FROM step_logs WHERE id = :id")
    suspend fun deleteStepLogById(id: Long)

    @Query("DELETE FROM step_logs")
    suspend fun deleteAllStepLogs()

    @Query("DELETE FROM step_logs WHERE userId = :userId")
    suspend fun deleteAllStepLogsForUser(userId: String)
}
