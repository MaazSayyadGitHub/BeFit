package com.maaz.befit.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.maaz.befit.data.dao.HealthDataDao
import com.maaz.befit.data.dao.SleepLogDao
import com.maaz.befit.data.dao.StepLogDao
import com.maaz.befit.data.dao.UserGoalsDao
import com.maaz.befit.data.dao.WaterLogDao
import com.maaz.befit.data.database.HealthDatabase
import com.maaz.befit.data.model.HealthData
import com.maaz.befit.data.repository.HealthRepository
import com.maaz.befit.data.service.ExportService
import com.maaz.befit.data.service.FirebaseDataService
import com.maaz.befit.data.service.GoogleFitService
import com.maaz.befit.data.service.HealthTipsService
import com.maaz.befit.data.service.NotificationSchedular
import com.maaz.befit.data.service.NotificationService
import com.maaz.befit.data.service.WearableDeviceService
import com.maaz.befit.viewmodel.ThemeViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule  {

    @Provides
    @Singleton
    fun provideHealthDatabase(@ApplicationContext context: Context) : HealthDatabase {
        return HealthDatabase.getDatabase(context)
    }

    fun provideUserSpecificDatabase(context: Context, userId: String): HealthDatabase {
        return HealthDatabase.getDatabase(context, userId)
    }

    @Provides
    fun provideHealthDataDao(database: HealthDatabase): HealthDataDao {
        return database.healthDataDao()
    }

    @Provides
    fun provideWaterLogDao(database: HealthDatabase): WaterLogDao {
        return database.waterLogDao()
    }

    @Provides
    fun provideStepLogDao(database: HealthDatabase): StepLogDao {
        return database.stepLogDao()
    }

    @Provides
    fun provideSleepLogDao(database: HealthDatabase): SleepLogDao {
        return database.sleepLogDao()
    }

    @Provides
    fun provideUserGoalsDao(database: HealthDatabase): UserGoalsDao {
        return database.userGoalsDao()
    }

    @Provides
    @Singleton
    fun provideFirebaseDataService(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): FirebaseDataService {
        return FirebaseDataService(firebaseAuth, firestore)
    }

    @Provides
    @Singleton
    fun provideHealthRepository(
        healthDataDao: HealthDataDao,
        waterLogDao: WaterLogDao,
        stepLogDao: StepLogDao,
        sleepLogDao: SleepLogDao,
        userGoalsDao: UserGoalsDao,
        firebaseDataService: FirebaseDataService,
        firebaseAuth: FirebaseAuth
    ): HealthRepository {
        return HealthRepository(
            healthDataDao,
            waterLogDao,
            stepLogDao,
            sleepLogDao,
            userGoalsDao,
            firebaseDataService,
            firebaseAuth
        )
    }

    @Provides
    @Singleton
    fun provideHealthTipsService(): HealthTipsService {
        return HealthTipsService()
    }

    @Provides
    @Singleton
    fun provideExportService(@ApplicationContext context: Context): ExportService {
        return ExportService(context)
    }

    @Provides
    @Singleton
    fun provideNotificationService(@ApplicationContext context: Context): NotificationService {
        return NotificationService(context)
    }

    @Provides
    @Singleton
    fun provideNotificationScheduler(@ApplicationContext context: Context): NotificationSchedular {
        return NotificationSchedular(context)
    }

    @Provides
    @Singleton
    fun provideGoogleFitService(@ApplicationContext context: Context): GoogleFitService {
        return GoogleFitService(context)
    }

    @Provides
    @Singleton
    fun provideWearableDeviceService(@ApplicationContext context: Context): WearableDeviceService {
        return WearableDeviceService(context)
    }

    @Provides
    @Singleton
    fun provideThemeViewModel(@ApplicationContext context: Context): ThemeViewModel {
        return ThemeViewModel(context)
    }


}