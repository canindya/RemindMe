package com.example.remindme.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.remindme.R
import com.example.remindme.data.MedicineDatabase
import com.example.remindme.service.AlarmService
import com.example.remindme.ui.StopReminderActivity

class MedicineReminderWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val medicineId = inputData.getInt("medicineId", -1)
        val dosage = inputData.getString("dosage") ?: return Result.failure()
        val scheduleId = inputData.getInt("scheduleId", -1)
        
        val database = MedicineDatabase.getDatabase(context)
        val medicine = database.medicineDao().getMedicineById(medicineId)
        
        if (medicine != null) {
            // Start alarm service
            context.startService(Intent(context, AlarmService::class.java))

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Create notification channel for Android O and above
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Medicine Reminders",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    // Set up alarm sound
                    setSound(
                        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    enableVibration(true)
                    setBypassDnd(true) // Bypass Do Not Disturb
                }
                notificationManager.createNotificationChannel(channel)
            }

            // Create intent for stopping the alarm
            val stopIntent = Intent(context, StopReminderActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra("medicineId", medicineId)
                putExtra("scheduleId", scheduleId)
            }
            
            val stopPendingIntent = PendingIntent.getActivity(
                context,
                medicineId,
                stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val fullScreenIntent = Intent(context, StopReminderActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("medicineId", medicineId)
                putExtra("scheduleId", scheduleId)
            }

            val fullScreenPendingIntent = PendingIntent.getActivity(
                context,
                medicineId,
                fullScreenIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_medicine_notification)
                .setContentTitle("Time to take ${medicine.name}")
                .setContentText("Dosage: $dosage dose | Illness: ${medicine.illnessType}")
                .setStyle(NotificationCompat.BigTextStyle()
                    .setBigContentTitle("Medicine Reminder")
                    .bigText("Time to take ${medicine.name}\nDosage: $dosage dose\nFor: ${medicine.illnessType}")
                )
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setOngoing(true)
                .setAutoCancel(false)
                .setFullScreenIntent(fullScreenPendingIntent, true)
                .setSound(null) // Sound will be handled by AlarmService
                .addAction(
                    R.drawable.ic_stop_alarm,
                    "Stop Alarm",
                    stopPendingIntent
                )
                .build()

            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) 
                == PackageManager.PERMISSION_GRANTED) {
                notificationManager.notify(medicineId, notification)
            }
        }

        return Result.success()
    }

    companion object {
        const val CHANNEL_ID = "medicine_reminders"
    }
} 