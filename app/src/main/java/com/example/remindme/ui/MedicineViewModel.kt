package com.example.remindme.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.remindme.data.*
import com.example.remindme.worker.MedicineReminderWorker
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.LocalDate

class MedicineViewModel(application: Application) : AndroidViewModel(application) {
    private val database = MedicineDatabase.getDatabase(application)
    private val dao = database.medicineDao()
    private val workManager = WorkManager.getInstance(application)

    val medicines = dao.getAllMedicines()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    private val _selectedDay = MutableStateFlow(DayOfWeek.MONDAY)
    val selectedDay = _selectedDay.asStateFlow()

    val schedulesForSelectedDay = _selectedDay
        .flatMapLatest { day ->
            dao.getSchedulesForDay(day.value)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    private val _takenMedicines = MutableStateFlow<List<MedicineTaken>>(emptyList())
    val takenMedicines = _takenMedicines.asStateFlow()

    init {
        // Clear old taken medicines and observe today's taken medicines
        viewModelScope.launch {
            dao.clearOldTakenMedicines(LocalDate.now().minusDays(1))
        }
        
        viewModelScope.launch {
            dao.getTakenMedicinesForDate(LocalDate.now())
                .collect { taken ->
                    _takenMedicines.value = taken
                }
        }
    }

    fun setSelectedDay(day: DayOfWeek) {
        _selectedDay.value = day
    }

    fun addMedicine(name: String, illnessType: String) {
        viewModelScope.launch {
            dao.insertMedicine(Medicine(name = name, illnessType = illnessType))
        }
    }

    fun addSchedule(medicineId: Int, dayOfWeek: DayOfWeek, time: String, dosage: String) {
        viewModelScope.launch {
            val scheduleId = dao.insertSchedule(
                MedicineSchedule(
                    medicineId = medicineId,
                    dayOfWeek = dayOfWeek.value,
                    time = time,
                    dosage = dosage
                )
            ).toInt()

            scheduleReminder(
                medicineId,
                LocalTime.parse(time),
                dosage,
                dayOfWeek,
                scheduleId
            )
        }
    }

    fun scheduleReminder(
        medicineId: Int,
        time: LocalTime,
        dosage: String,
        dayOfWeek: DayOfWeek?,
        scheduleId: Int
    ) {
        val data = Data.Builder()
            .putInt("medicineId", medicineId)
            .putString("dosage", dosage)
            .putInt("scheduleId", scheduleId)
            .build()

        val currentDate = LocalDateTime.now()
        val scheduledTime = LocalDateTime.of(
            currentDate.toLocalDate(),
            time
        )

        var initialDelay = Duration.between(currentDate, scheduledTime)
        if (initialDelay.isNegative) {
            initialDelay = initialDelay.plusDays(1)
        }

        val workRequest = if (dayOfWeek != null) {
            PeriodicWorkRequestBuilder<MedicineReminderWorker>(7, TimeUnit.DAYS)
                .setInputData(data)
                .setInitialDelay(initialDelay.toMinutes(), TimeUnit.MINUTES)
                .addTag("medicine_$medicineId")
                .build()
        } else {
            PeriodicWorkRequestBuilder<MedicineReminderWorker>(1, TimeUnit.DAYS)
                .setInputData(data)
                .setInitialDelay(initialDelay.toMinutes(), TimeUnit.MINUTES)
                .addTag("medicine_$medicineId")
                .build()
        }

        workManager.enqueueUniquePeriodicWork(
            "medicine_${medicineId}_${time}_${dayOfWeek?.value ?: "daily"}",
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }

    fun clearAllSchedules() {
        viewModelScope.launch {
            dao.deleteAllSchedules()
            // Cancel all reminders
            workManager.cancelAllWorkByTag("medicine_reminder")
        }
    }

    fun clearEverything() {
        viewModelScope.launch {
            dao.deleteAllSchedules()
            dao.deleteAllMedicines()
            // Cancel all reminders
            workManager.cancelAllWorkByTag("medicine_reminder")
        }
    }

    fun markMedicineTaken(medicineId: Int, scheduleId: Int) {
        viewModelScope.launch {
            try {
                val medicineTaken = MedicineTaken(
                    medicineId = medicineId,
                    scheduleId = scheduleId,
                    date = LocalDate.now()
                )
                dao.markMedicineTaken(medicineTaken)
                
                // Instead of collecting, just get the current value
                val taken = dao.getTakenMedicinesForDateSync(LocalDate.now())
                _takenMedicines.value = taken
                
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteMedicine(medicine: Medicine) {
        viewModelScope.launch {
            // Delete related schedules first
            dao.deleteSchedulesForMedicine(medicine.id)
            // Delete taken records
            dao.deleteTakenForMedicine(medicine.id)
            // Delete the medicine
            dao.deleteMedicine(medicine)
            // Cancel any scheduled reminders
            workManager.cancelAllWorkByTag("medicine_${medicine.id}")
        }
    }

    fun deleteSchedule(schedule: MedicineSchedule) {
        viewModelScope.launch {
            dao.deleteSchedule(schedule)
            // Cancel the specific reminder
            workManager.cancelUniqueWork("medicine_${schedule.medicineId}_${schedule.time}_${schedule.dayOfWeek}")
        }
    }
} 