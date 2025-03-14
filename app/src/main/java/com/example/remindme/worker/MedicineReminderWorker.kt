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

class MedicineReminderWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val CHANNEL_ID = "medicine_reminders"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Medicine Reminders"
            val descriptionText = "Reminders for taking medicines"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                setShowBadge(true)
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
                    .setOngoing(true) // Makes the notification persistent
                    .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))

                // Show the notification
                with(NotificationManagerCompat.from(context)) {
                    notify(scheduleId, builder.build())
                }

                return Result.success()
            }

            return Result.failure()
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.failure()
        }
    }
} 