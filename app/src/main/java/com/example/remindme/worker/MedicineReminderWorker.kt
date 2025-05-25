package com.example.remindme.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.remindme.MainActivity
import com.example.remindme.R
import com.example.remindme.data.MedicineDatabase
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class MedicineReminderWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val CHANNEL_ID = "medicine_reminders"
        const val CHANNEL_NAME = "Medicine Reminders"
        const val CHANNEL_DESCRIPTION = "Reminders for taking medicines"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                setShowBadge(true)
                enableLights(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override suspend fun doWork(): Result {
        try {
            val medicineId = inputData.getInt("medicineId", -1)
            val scheduleId = inputData.getInt("scheduleId", -1)
            val dosage = inputData.getString("dosage") ?: "Unknown dosage"

            if (medicineId == -1 || scheduleId == -1) {
                return Result.failure()
            }

            // Get medicine details from database
            val database = MedicineDatabase.getDatabase(context)
            val medicine = database.medicineDao().getMedicineById(medicineId)

            if (medicine != null) {
                // Check if medicine is already taken for today
                val today = LocalDate.now()
                val isTaken = database.medicineDao().getTakenMedicinesForDateSync(today)
                    .any { it.medicineId == medicineId && it.scheduleId == scheduleId }

                if (!isTaken) {
                    // Create intent for when notification is tapped
                    val intent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        putExtra("medicineId", medicineId)
                        putExtra("scheduleId", scheduleId)
                    }

                    val pendingIntent = PendingIntent.getActivity(
                        context,
                        medicineId,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    // Create intent for "Taken" action
                    val takenIntent = Intent(context, MedicineReminderReceiver::class.java).apply {
                        action = "MEDICINE_TAKEN"
                        putExtra("medicineId", medicineId)
                        putExtra("scheduleId", scheduleId)
                    }

                    val takenPendingIntent = PendingIntent.getBroadcast(
                        context,
                        scheduleId * 10 + 1,
                        takenIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    // Build the notification
                    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification_medicine)
                        .setContentTitle("Medicine Reminder")
                        .setContentText("Time to take ${medicine.name} - $dosage")
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_REMINDER)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .addAction(R.drawable.ic_check, "Taken", takenPendingIntent)
                        .setOngoing(true)
                        .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
                        .setFullScreenIntent(pendingIntent, true)

                    // Show the notification
                    with(NotificationManagerCompat.from(context)) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            if (context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                                notify(scheduleId, builder.build())
                            }
                        } else {
                            notify(scheduleId, builder.build())
                        }
                    }
                }
                return Result.success()
            }

            return Result.failure()
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.retry()
        }
    }
} 