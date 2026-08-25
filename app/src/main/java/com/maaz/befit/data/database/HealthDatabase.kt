package com.maaz.befit.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
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
import dagger.hilt.android.qualifiers.ApplicationContext


@Database(
    entities = [
        HealthData::class, // Daily Health metrics aggregation
        WaterLog::class,   // individual water intake logs
        StepLog::class,    // Individual step count logs
        SleepLog::class,   // individual sleep session logs
        UserGoals::class   // user-defined health goals
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class HealthDatabase : RoomDatabase() {

    /*
    * Dao Classes for All Crud Operations on Room db.
    * */

    abstract fun healthDataDao() : HealthDataDao
    abstract fun waterLogDao() : WaterLogDao
    abstract fun stepLogDao() : StepLogDao
    abstract fun sleepLogDao() : SleepLogDao
    abstract fun userGoalsDao() : UserGoalsDao

    companion object {
        /**
         * SINGLETON DATABASE ACCESS
         *
         * This method ensures only one database instance exists throughout the app lifecycle.
         * (agr ham singleton instance use nhi karege to OS and RAM kafi kharch hogi bar bar instance
         * banane me, to ham ek hi instance banayege jo pure app me spread hoga bar bar room ka instance nhi
         * banega.. and resources and RAM and performance bhi fast hoga.. and app crash/freez bhi nhi karega)
         *
         * Uses double-checked locking pattern for thread safety.
         *
         * @param context Application context for database creation
         * @param userId Optional user ID for user-specific database isolation
         * @return HealthDatabase instance
         *
         */

        @Volatile
        private var INSTANCE : HealthDatabase? = null

        fun getDatabase(context: Context, userId: String? = null) : HealthDatabase {

            // database name based on userID
            val databaseName = if (userId != null) {
                "health_database_$userId"
            } else {
                "health_database"
            }

            return INSTANCE ?:              // if INSTANCE created already then don't create
            synchronized(this) {      // if not created then create
                val instance = Room.databaseBuilder(
                    context.applicationContext,     // use applicationContext to prevent memoryLeak
                    HealthDatabase::class.java,
                    databaseName
                ).build()

                INSTANCE = instance
                instance
            }
        }

        /*
        * Clear the current database instance
        * This should be called when user logged out, to ensure fresh database for next user
        */
        fun clearInstance() {
            synchronized(this) {
                INSTANCE?.close()
                INSTANCE = null
            }
        }
    }



}