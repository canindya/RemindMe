package com.example.remindme.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface MedicineDao {
    @Query("SELECT * FROM medicines")
    fun getAllMedicines(): Flow<List<Medicine>>

    @Query("SELECT * FROM medicine_schedules WHERE dayOfWeek = :dayOfWeek")
    fun getSchedulesForDay(dayOfWeek: Int): Flow<List<MedicineSchedule>>

    @Insert
    suspend fun insertMedicine(medicine: Medicine): Long

    @Insert
    suspend fun insertSchedule(schedule: MedicineSchedule): Long

    @Delete
    suspend fun deleteSchedule(schedule: MedicineSchedule)

    @Query("SELECT * FROM medicines WHERE id = :id")
    suspend fun getMedicineById(id: Int): Medicine?

    @Query("DELETE FROM medicine_schedules")
    suspend fun deleteAllSchedules()

    @Query("DELETE FROM medicines")
    suspend fun deleteAllMedicines()

    @Insert
    suspend fun markMedicineTaken(medicineTaken: MedicineTaken)

    @Query("""
        SELECT * FROM medicine_taken 
        WHERE date = :date 
        ORDER BY id DESC
    """)
    fun getTakenMedicinesForDate(date: LocalDate): Flow<List<MedicineTaken>>

    @Query("DELETE FROM medicine_taken WHERE date < :date")
    suspend fun clearOldTakenMedicines(date: LocalDate)

    @Delete
    suspend fun deleteMedicine(medicine: Medicine)

    @Query("DELETE FROM medicine_schedules WHERE medicineId = :medicineId")
    suspend fun deleteSchedulesForMedicine(medicineId: Int)

    @Query("DELETE FROM medicine_taken WHERE medicineId = :medicineId")
    suspend fun deleteTakenForMedicine(medicineId: Int)

    @Query("""
        SELECT * FROM medicine_taken 
        WHERE date = :date 
        ORDER BY id DESC
    """)
    suspend fun getTakenMedicinesForDateSync(date: LocalDate): List<MedicineTaken>
} 