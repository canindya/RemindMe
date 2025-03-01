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

    private val _selectedPatientId = MutableStateFlow<Int?>(null)
    val selectedPatientId = _selectedPatientId.asStateFlow()

    val medicines = _selectedPatientId.flatMapLatest { patientId ->
        if (patientId != null) {
            dao.getMedicinesForPatient(patientId)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    private val _selectedDay = MutableStateFlow(DayOfWeek.MONDAY)
    val selectedDay = _selectedDay.asStateFlow()

    val schedulesForSelectedDay = _selectedDay
        .flatMapLatest { day ->
            dao.getSchedulesForDay(day.value).map { schedules ->
                schedules.sortedBy { it.time }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    private val _takenMedicines = MutableStateFlow<List<MedicineTaken>>(emptyList())
    val takenMedicines = _takenMedicines.asStateFlow()

    init {
        _selectedDay.value = LocalDate.now().dayOfWeek
        
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
        viewModelScope.launch {
            _takenMedicines.value = dao.getTakenMedicinesForDateSync(LocalDate.now())
        }
    }

    fun setSelectedPatient(patientId: Int) {
        _selectedPatientId.value = patientId
    }

    fun addMedicine(name: String, illnessType: String) {
        viewModelScope.launch {
            _selectedPatientId.value?.let { patientId ->
                dao.insertMedicine(Medicine(
                    name = name,
                    illnessType = illnessType,
                    patientId = patientId
                ))
            }
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
                val currentDate = LocalDate.now()
                val medicineTaken = MedicineTaken(
                    medicineId = medicineId,
                    scheduleId = scheduleId,
                    date = currentDate
                )
                dao.markMedicineTaken(medicineTaken)
                
                // Update the UI with latest taken medicines
                _takenMedicines.value = dao.getTakenMedicinesForDateSync(currentDate)
                
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

    fun getSchedulesForMedicine(medicineId: Int): Flow<List<MedicineSchedule>> {
        return dao.getSchedulesForMedicine(medicineId)
    }

    fun getAllSchedulesForPatient(patientId: Int): Flow<List<MedicineSchedule>> {
        return dao.getAllSchedulesForPatient(patientId)
    }
} 