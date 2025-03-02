package com.example.remindme.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.remindme.data.MedicineDatabase
import com.example.remindme.data.MedicineTaken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class MedicineTakenReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "MEDICINE_TAKEN") {
            val medicineId = intent.getIntExtra("medicineId", -1)
            val scheduleId = intent.getIntExtra("scheduleId", -1)

            if (medicineId != -1 && scheduleId != -1) {
                // Cancel the notification
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(medicineId)

                // Mark the medicine as taken in the database
                CoroutineScope(Dispatchers.IO).launch {
                    val database = MedicineDatabase.getDatabase(context)
                    val medicineTaken = MedicineTaken(
                        medicineId = medicineId,
                        scheduleId = scheduleId,
                        date = LocalDate.now()
                    )
                    database.medicineDao().markMedicineTaken(medicineTaken)
                }
            }
        }
    }
} 