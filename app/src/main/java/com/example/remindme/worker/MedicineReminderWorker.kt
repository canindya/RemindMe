package com.example.remindme.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.example.remindme.MainActivity
import com.example.remindme.R
import com.example.remindme.data.MedicineDatabase
import com.example.remindme.receiver.MedicineTakenReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MedicineReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "medicine_reminders"
        private const val WAKE_LOCK_TAG = "RemindMe:MedicineReminder"
    }

    override suspend fun doWork(): Result {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            WAKE_LOCK_TAG
        )

        return try {
            wakeLock.acquire(10 * 60 * 1000L) // 10 minutes timeout

            withContext(Dispatchers.IO) {
                val medicineId = inputData.getInt("medicineId", -1)
                val dosage = inputData.getString("dosage") ?: ""
                val scheduleId = inputData.getInt("scheduleId", -1)

                if (medicineId == -1 || scheduleId == -1) {
                    return@withContext Result.failure()
                }

                val database = MedicineDatabase.getDatabase(context)
                val medicine = database.medicineDao().getMedicineById(medicineId)

                if (medicine != null) {
                    // Create intent to open app when notification is tapped
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
                    val takenIntent = Intent(context, MedicineTakenReceiver::class.java).apply {
                        action = "MEDICINE_TAKEN"
                        putExtra("medicineId", medicineId)
                        putExtra("scheduleId", scheduleId)
                    }

                    val takenPendingIntent = PendingIntent.getBroadcast(
                        context,
                        medicineId,
                        takenIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    // Get default notification sound
                    val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

                    // Create and show notification with action button
                    val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_medicine_notification)
                        .setContentTitle("Time to take ${medicine.name}")
                        .setContentText("Dosage: $dosage")
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setCategory(NotificationCompat.CATEGORY_ALARM)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .setSound(alarmSound)
                        .setVibrate(longArrayOf(0, 1000, 500, 1000))
                        .setDefaults(NotificationCompat.DEFAULT_ALL)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .addAction(
                            R.drawable.ic_check,
                            "Mark as Taken",
                            takenPendingIntent
                        )
                        .build()

                    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    
                    // Show the notification
                    notificationManager.notify(medicineId, notification)

                    // Keep the notification active for a while to ensure it's delivered
                    Thread.sleep(5000)

                    Result.success()
                } else {
                    Result.failure()
                }
            }
        } catch (e: Exception) {
            Result.retry()
        } finally {
            if (wakeLock.isHeld) {
                wakeLock.release()
            }
        }
    }
} 