package com.example.remindme.ui

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale
import com.example.remindme.data.Medicine
import com.example.remindme.data.MedicineSchedule
import com.example.remindme.data.Patient
import com.example.remindme.data.ScheduleWithMedicine
import java.time.LocalDate
import android.content.Intent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleSummaryScreen(
    onNavigateBack: () -> Unit
) {
    val viewModel: MedicineViewModel = viewModel()
    val patientViewModel: PatientViewModel = viewModel()
    val context = LocalContext.current
    
    val selectedPatientId by viewModel.selectedPatientId.collectAsState(initial = null)
    val patients by patientViewModel.patients.collectAsState(initial = emptyList())
    val medicines by viewModel.medicines.collectAsState(initial = emptyList())
    var allSchedules by remember { mutableStateOf<List<MedicineSchedule>>(emptyList()) }
    
    val selectedPatient = patients.find { it.id == selectedPatientId }

    // Collect all schedules for the patient
    LaunchedEffect(selectedPatientId) {
        selectedPatientId?.let { patientId ->
            viewModel.getAllSchedulesForPatient(patientId).collect { schedules: List<MedicineSchedule> ->
                allSchedules = schedules
            }
        }
    }

    // Group schedules by medicine and day
    val schedulesByMedicineAndDay = allSchedules
        .map { schedule ->
            val medicine = medicines.find { medicine -> medicine.id == schedule.medicineId }
            medicine?.let { ScheduleWithMedicine(schedule, it) }
        }
        .filterNotNull()
        .groupBy { scheduleWithMedicine -> scheduleWithMedicine.medicine.name }
        .mapValues { (_, schedules) ->
            schedules.groupBy { scheduleWithMedicine -> 
                DayOfWeek.of(scheduleWithMedicine.schedule.dayOfWeek)
            }
        }

    // Check if all days have the same schedule for each medicine
    val sameScheduleForAllDays = schedulesByMedicineAndDay.all { (_, daySchedules) ->
        val firstDaySchedule = daySchedules.values.firstOrNull()
        daySchedules.values.all { schedule ->
            schedule.size == firstDaySchedule?.size && 
            schedule.zip(firstDaySchedule).all { (a, b) ->
                a.schedule.time == b.schedule.time && 
                a.schedule.dosage == b.schedule.dosage
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Schedule Summary") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Go back")
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        shareScheduleSummary(context, selectedPatient, schedulesByMedicineAndDay, sameScheduleForAllDays)
                    }) {
                        Icon(Icons.Default.Share, "Share Schedule")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            selectedPatient?.let { patient ->
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Patient: ${patient.name}",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = "Age: ${patient.age} | Sex: ${patient.sex}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                if (sameScheduleForAllDays) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Text(
                                text = "All days have the same schedule",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }

                items(schedulesByMedicineAndDay.toList()) { (medicineName, daySchedules) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = medicineName,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            
                            Text(
                                text = "For: ${daySchedules.values.firstOrNull()?.firstOrNull()?.medicine?.illnessType ?: ""}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            if (sameScheduleForAllDays) {
                                // Show schedule once if it's the same for all days
                                daySchedules.values.firstOrNull()?.forEach { scheduleWithMedicine ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Time: ${scheduleWithMedicine.schedule.time}",
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        Text(
                                            text = "Dosage: ${scheduleWithMedicine.schedule.dosage}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            } else {
                                // Show schedule for each day
                                DayOfWeek.values().forEach { day ->
                                    val daySchedule = daySchedules[day]
                                    if (!daySchedule.isNullOrEmpty()) {
                                        Text(
                                            text = day.getDisplayName(TextStyle.FULL, Locale.getDefault()),
                                            style = MaterialTheme.typography.titleSmall,
                                            modifier = Modifier.padding(vertical = 8.dp)
                                        )
                                        daySchedule.forEach { scheduleWithMedicine ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = "Time: ${scheduleWithMedicine.schedule.time}",
                                                    style = MaterialTheme.typography.bodyLarge
                                                )
                                                Text(
                                                    text = "Dosage: ${scheduleWithMedicine.schedule.dosage}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun shareScheduleSummary(
    context: Context,
    patient: Patient?,
    schedulesByMedicineAndDay: Map<String, Map<DayOfWeek, List<ScheduleWithMedicine>>>,
    sameScheduleForAllDays: Boolean
) {
    if (patient == null) return

    val scheduleText = buildString {
        appendLine("Medicine Schedule Summary for ${patient.name}")
        appendLine("Age: ${patient.age} | Sex: ${patient.sex}")
        appendLine()

        if (sameScheduleForAllDays) {
            appendLine("All days have the same schedule:")
            appendLine()
        }

        schedulesByMedicineAndDay.forEach { (medicineName, daySchedules) ->
            appendLine("Medicine: $medicineName")
            appendLine("For: ${daySchedules.values.firstOrNull()?.firstOrNull()?.medicine?.illnessType ?: ""}")
            
            if (sameScheduleForAllDays) {
                daySchedules.values.firstOrNull()?.forEach { scheduleWithMedicine ->
                    appendLine("  • Time: ${scheduleWithMedicine.schedule.time}")
                    appendLine("    Dosage: ${scheduleWithMedicine.schedule.dosage}")
                }
            } else {
                DayOfWeek.values().forEach { day ->
                    val daySchedule = daySchedules[day]
                    if (!daySchedule.isNullOrEmpty()) {
                        appendLine("  ${day.getDisplayName(TextStyle.FULL, Locale.getDefault())}:")
                        daySchedule.forEach { scheduleWithMedicine ->
                            appendLine("    • Time: ${scheduleWithMedicine.schedule.time}")
                            appendLine("      Dosage: ${scheduleWithMedicine.schedule.dosage}")
                        }
                    }
                }
            }
            appendLine()
        }
    }

    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Medicine Schedule Summary for ${patient.name}")
        putExtra(Intent.EXTRA_TEXT, scheduleText)
    }

    context.startActivity(Intent.createChooser(shareIntent, "Share Schedule Summary"))
} 