package com.example.remindme.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medicines")
data class Medicine(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val illnessType: String,
    val patientId: Int
)

@Entity(tableName = "medicine_schedules")
data class MedicineSchedule(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val medicineId: Int?,
    val dayOfWeek: Int, // 1 (Monday) to 7 (Sunday)
    val time: String, // HH:mm format
    val dosage: String // "FULL", "HALF", "QUARTER"
) 