package com.example.remindme.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import java.time.LocalDate

@Entity(
    tableName = "medicine_taken",
    indices = [
        Index(value = ["medicineId", "scheduleId", "date"], unique = true)
    ]
)
data class MedicineTaken(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val medicineId: Int,
    val scheduleId: Int,
    val date: LocalDate
) 