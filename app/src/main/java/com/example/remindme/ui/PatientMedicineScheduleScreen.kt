package com.example.remindme.ui

import android.content.Context
import android.content.Intent
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientMedicineScheduleScreen(
    onNavigateBack: () -> Unit
) {
    val viewModel: MedicineViewModel = viewModel()
    val patientViewModel: PatientViewModel = viewModel()
    val context = LocalContext.current
    
    val selectedPatientId by viewModel.selectedPatientId.collectAsState(initial = null)
    val patients by patientViewModel.patients.collectAsState(initial = emptyList())
    val medicines by viewModel.medicines.collectAsState(initial = emptyList())
    var allSchedules by remember { mutableStateOf<List<MedicineSchedule>>(emptyList()) }
    var selectedDay by remember { mutableStateOf(LocalDate.now().dayOfWeek) }
    
    val selectedPatient = patients.find { it.id == selectedPatientId }

    // Collect all schedules for the patient
    LaunchedEffect(selectedPatientId) {
        selectedPatientId?.let { patientId ->
            viewModel.getAllSchedulesForPatient(patientId).collect { schedules: List<MedicineSchedule> ->
                allSchedules = schedules
            }
        }
    }

    // Group schedules by time
    val schedulesGroupedByMedicine = allSchedules
        .filter { schedule -> schedule.dayOfWeek == selectedDay.value }  // Filter for selected day
        .map { schedule ->
            val medicine = medicines.find { medicine -> medicine.id == schedule.medicineId }
            medicine?.let { ScheduleWithMedicine(schedule, it) }
        }
        .filterNotNull()
        .groupBy { scheduleWithMedicine -> scheduleWithMedicine.medicine.name }
        .mapValues { (_, schedules) -> schedules.sortedBy { scheduleWithMedicine -> scheduleWithMedicine.schedule.time } }
        .toSortedMap()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Medicines for - ${selectedDay.getDisplayName(TextStyle.FULL, Locale.getDefault())}") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Go back")
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        shareDailySchedule(context, selectedPatient, selectedDay, schedulesGroupedByMedicine)
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

                item {
                    ScrollableTabRow(
                        selectedTabIndex = selectedDay.value - 1,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        DayOfWeek.values().forEach { day ->
                            Tab(
                                selected = selectedDay == day,
                                onClick = { selectedDay = day },
                                text = { Text(day.getDisplayName(TextStyle.SHORT, Locale.getDefault())) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                items(schedulesGroupedByMedicine.toList()) { (medicineName: String, schedulesForMedicine: List<ScheduleWithMedicine>) ->
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
                                text = "For: ${schedulesForMedicine.first().medicine.illnessType}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            schedulesForMedicine.forEach { scheduleWithMedicine ->
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
                        }
                    }
                }
            }
        }
    }
}

private fun shareDailySchedule(
    context: Context,
    patient: Patient?,
    selectedDay: DayOfWeek,
    schedulesGroupedByMedicine: Map<String, List<ScheduleWithMedicine>>
) {
    if (patient == null) return

    val scheduleText = buildString {
        appendLine("Medicine Schedule for ${patient.name}")
        appendLine("Day: ${selectedDay.getDisplayName(TextStyle.FULL, Locale.getDefault())}")
        appendLine("Age: ${patient.age} | Sex: ${patient.sex}")
        appendLine()

        schedulesGroupedByMedicine.forEach { (medicineName, schedulesForMedicine) ->
            appendLine("Medicine: $medicineName")
            appendLine("For: ${schedulesForMedicine.first().medicine.illnessType}")
            schedulesForMedicine.forEach { scheduleWithMedicine ->
                appendLine("  • Time: ${scheduleWithMedicine.schedule.time}")
                appendLine("    Dosage: ${scheduleWithMedicine.schedule.dosage}")
            }
            appendLine()
        }
    }

    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Medicine Schedule for ${patient.name}")
        putExtra(Intent.EXTRA_TEXT, scheduleText)
    }

    context.startActivity(Intent.createChooser(shareIntent, "Share Schedule"))
} 