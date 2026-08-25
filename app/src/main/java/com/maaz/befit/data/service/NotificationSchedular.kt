package com.maaz.befit.data.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar
import kotlin.jvm.java


class NotificationSchedular(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleWaterReminders() {
        // Schedule water reminders every 2 hours from 8 AM to 8 PM
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 8)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)

        // Schedule water reminders every 2 hours from 8 AM to 8 PM
        for (hour in 8..20 step 2) {
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            val intent = Intent(context, NotificationService::class.java).apply {
                putExtra("type", "water_reminder")
                putExtra("title", "Stay Hydrated! 💧")
                putExtra("message", "Time to drink some water")
            }
            val pendingIntent = PendingIntent.getService(
                context,
                hour,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        }
    }

    fun scheduleExerciseReminders() {
        // Schedule exercise reminder at 6 PM
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 18)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)

        val intent = Intent(context, NotificationService::class.java).apply {
            putExtra("type", "exercise_reminder")
            putExtra("title", "Time to Move! 🏃‍♂️")
            putExtra("message", "Let's get some steps in!")
        }
        val pendingIntent = PendingIntent.getService(
            context,
            100,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    fun scheduleSleepReminder() {
        // Schedule sleep reminder at 10 PM
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 22)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)

        val intent = Intent(context, NotificationService::class.java).apply {
            putExtra("type", "sleep_reminder")
            putExtra("title", "Time for Bed! 😴")
            putExtra("message", "Get ready for a good night's sleep")
        }
        val pendingIntent = PendingIntent.getService(
            context,
            101,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    fun cancelAllReminders() {
        // Cancel all water reminders
        for (hour in 8..20 step 2) {
            val intent = Intent(context, NotificationService::class.java)
            val pendingIntent = PendingIntent.getService(
                context,
                hour,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }

        // Cancel exercise reminder
        val exerciseIntent = Intent(context, NotificationService::class.java)
        val exercisePendingIntent = PendingIntent.getService(
            context,
            100,
            exerciseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(exercisePendingIntent)

        // Cancel sleep reminder
        val sleepIntent = Intent(context, NotificationService::class.java)
        val sleepPendingIntent = PendingIntent.getService(
            context,
            101,
            sleepIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(sleepPendingIntent)
    }

    private fun calculateDelayUntilBedtime(): Long {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val bedtimeHour = 22 // 10 PM

        return if (currentHour < bedtimeHour) {
            (bedtimeHour - currentHour) * 60 * 60 * 1000L
        } else {
            (24 - currentHour + bedtimeHour) * 60 * 60 * 1000L
        }
    }
}