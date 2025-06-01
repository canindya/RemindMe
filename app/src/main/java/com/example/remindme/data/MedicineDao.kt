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

    @Query("SELECT * FROM medicines WHERE patientId = :patientId")
    fun getMedicinesForPatient(patientId: Int): Flow<List<Medicine>>

    @Query("SELECT * FROM medicine_schedules WHERE medicineId = :medicineId")
    fun getSchedulesForMedicine(medicineId: Int): Flow<List<MedicineSchedule>>

    @Query("""
        SELECT ms.* FROM medicine_schedules ms
        INNER JOIN medicines m ON m.id = ms.medicineId
        WHERE m.patientId = :patientId
    """)
    fun getAllSchedulesForPatient(patientId: Int): Flow<List<MedicineSchedule>>

    // Medicine Refill Operations
    @Insert
    suspend fun insertRefill(refill: MedicineRefill): Long

    @Update
    suspend fun updateRefill(refill: MedicineRefill)

    @Delete
    suspend fun deleteRefill(refill: MedicineRefill)

    @Query("SELECT * FROM medicine_refills WHERE medicineId = :medicineId")
    fun getRefillsForMedicine(medicineId: Int): Flow<List<MedicineRefill>>

    @Query("""
        SELECT mr.* FROM medicine_refills mr
        INNER JOIN medicines m ON m.id = mr.medicineId
        WHERE m.patientId = :patientId
    """)
    fun getRefillsForPatient(patientId: Int): Flow<List<MedicineRefill>>

    @Query("""
        SELECT mr.* FROM medicine_refills mr
        INNER JOIN medicines m ON m.id = mr.medicineId
        WHERE m.patientId = :patientId AND mr.nextRefillDate <= :date
    """)
    fun getUpcomingRefills(patientId: Int, date: LocalDate): Flow<List<MedicineRefill>>

    @Query("""
        WITH DuplicateRefills AS (
            SELECT mr.medicineId, MIN(mr.id) as keepId
            FROM medicine_refills mr
            INNER JOIN medicines m ON m.id = mr.medicineId
            WHERE m.patientId = :patientId
            GROUP BY mr.medicineId
            HAVING COUNT(*) > 1
        )
        DELETE FROM medicine_refills
        WHERE medicineId IN (SELECT medicineId FROM DuplicateRefills)
        AND id NOT IN (SELECT keepId FROM DuplicateRefills)
    """)
    suspend fun cleanupDuplicateRefills(patientId: Int)
} 