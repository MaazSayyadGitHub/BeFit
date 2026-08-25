package com.maaz.befit.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/*
* For Room DB Store schema.
* */

@Entity(tableName = "health_data")
data class HealthData(
    @PrimaryKey // (primaryKey for date) is unique (no duplicate on same date) if data inserting again & again on same date), only one record will be there if data changed/update also.
    val date : String, // YY-MM-DD format
    val userId : String = "", // firebase UID for user uniqueness
    val steps : Int = 0,
    val distance : Float = 0f, // in meters
    val caloriesBurned : Int = 0,
    val waterIntake : Int = 0, // in ml
    val sleepHours : Float = 0f,
    val heartRate : Int = 0, // average heart rate
    val healthScore : Int = 0, // calculated score 0 - 100
    val createdAt : String = "", // ISO datetime string
    val updatedAt : String = ""
)


@Entity(tableName = "water_logs")
data class WaterLog(
    @PrimaryKey(autoGenerate = true) //  it will increase as new entry insert
    val id : Long = 0,
    val userId : String = "",
    val amount : Int,
    val timestamp : String,
    val date : String, // YY-MM-DD format
)

@Entity(tableName = "step_logs")
data class StepLog(
    @PrimaryKey(autoGenerate = true)
    val id : Long = 0,
    val userId : String = "",
    val steps : Int,
    val timestamp : String,
    val date : String, // YY-MM-DD format
)

@Entity(tableName = "sleep_logs")
data class SleepLog(
    @PrimaryKey(autoGenerate = true)
    val id : Long = 0,
    val userId : String = "",
    val sleepStart : String,
    val sleepEnd : String,
    val duration : Float,
    val quality : Int = 0,
    val date : String, // YY-MM-DD format
)

@Entity(tableName = "user_goals")
data class UserGoals(
    @PrimaryKey()
    var id : Int = 1,
    var userId : String = "",
    var dailySteps : Int = 10000,
    var dailyWater : Int = 2000,
    var dailySleep : Float = 8f,
    var weeklyExercise : Int = 150,
)