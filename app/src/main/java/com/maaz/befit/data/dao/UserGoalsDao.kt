package com.maaz.befit.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.maaz.befit.data.model.UserGoals
import kotlinx.coroutines.flow.Flow

@Dao
interface UserGoalsDao {

    @Query("SELECT * FROM user_goals WHERE id = 1")
    fun getUserGoals() : Flow<UserGoals?>

    @Query("SELECT * FROM user_goals WHERE userId = :userId AND id = 1")
    suspend fun getUserGoalsForUser(userId: String) : UserGoals?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserGoals(userGoals: UserGoals)

    @Update
    suspend fun updateUserGoals(userGoals: UserGoals)

    @Query("DELETE FROM user_goals WHERE id = 1")
    suspend fun deleteUserGoals()

}

