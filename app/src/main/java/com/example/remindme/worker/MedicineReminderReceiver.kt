package com.example.remindme.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.example.remindme.data.MedicineDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class MedicineReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val medicineId = intent.getIntExtra("medicineId", -1)
        val scheduleId = intent.getIntExtra("scheduleId", -1)

        if (medicineId != -1 && scheduleId != -1) {
            when (intent.action) {
                "MEDICINE_TAKEN" -> {
                    handleMedicineTaken(context, medicineId, scheduleId)
                    // Cancel the notification
                    NotificationManagerCompat.from(context).cancel(scheduleId)
                }
            }
        }
    }

    private fun handleMedicineTaken(context: Context, medicineId: Int, scheduleId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val database = MedicineDatabase.getDatabase(context)
            val dao = database.medicineDao()
            
            try {
                dao.markMedicineTaken(
                    com.example.remindme.data.MedicineTaken(
                        medicineId = medicineId,
                        scheduleId = scheduleId,
                        date = LocalDate.now()
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
} 