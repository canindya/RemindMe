package com.example.remindme.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.WorkManager
import com.example.remindme.data.MedicineDatabase
import com.example.remindme.ui.MedicineViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalTime

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val database = MedicineDatabase.getDatabase(context)
            
            // Launch in a coroutine because database operations are not allowed on main thread
            CoroutineScope(Dispatchers.IO).launch {
                // Get all schedules
                val schedules = database.medicineDao().getAllSchedules()
                
                // Recreate work requests for each schedule
                schedules.forEach { schedule ->
                    val medicine = database.medicineDao().getMedicineById(schedule.medicineId)
                    if (medicine != null) {
                        val viewModel = MedicineViewModel(context.applicationContext as android.app.Application)
                        viewModel.scheduleReminder(
                            medicineId = schedule.medicineId,
                            time = LocalTime.parse(schedule.time),
                            dosage = schedule.dosage,
                            dayOfWeek = java.time.DayOfWeek.of(schedule.dayOfWeek),
                            scheduleId = schedule.id
                        )
                    }
                }
            }
        }
    }
} 