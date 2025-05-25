package com.example.remindme

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.remindme.ui.MedicineViewModel
import com.example.remindme.ui.MedicinesTab
import com.example.remindme.ui.TimingsTab
import com.example.remindme.ui.theme.RemindMeTheme
import com.example.remindme.ui.PatientScreen
import com.example.remindme.ui.AddPatientScreen
import com.example.remindme.ui.ScheduleSummaryScreen
import com.example.remindme.ui.AddScheduleScreen
import com.example.remindme.ui.AboutScreen
import java.time.DayOfWeek

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    var currentScreen by remember { mutableStateOf("patients") }
    val viewModel: MedicineViewModel = viewModel()

    fun navigateToScreen(screen: String) {
        currentScreen = screen
    }

    when (currentScreen) {
        "patients" -> PatientScreen(
            onNavigateToAddPatient = { currentScreen = "addPatient" },
            onPatientSelected = { patientId ->
                viewModel.setSelectedPatient(patientId)
                currentScreen = "medicines"
            },
            onNavigateToScreen = { screen -> navigateToScreen(screen) }
        )
        "addPatient" -> AddPatientScreen(
            onNavigateBack = { currentScreen = "patients" }
        )
        "medicines" -> MedicinesTab(
            onNavigateBack = { currentScreen = "patients" },
            onNavigateToTimings = { currentScreen = "timings" },
            onNavigateToScheduleSummary = { currentScreen = "schedule_summary" },
            onNavigateToAddSchedule = { medicineId ->
                viewModel.setSelectedMedicine(medicineId)
                currentScreen = "addSchedule"
            }
        )
        "timings" -> TimingsTab(
            onNavigateBack = { currentScreen = "medicines" }
        )
        "schedule_summary" -> ScheduleSummaryScreen(
            onNavigateBack = { currentScreen = "medicines" }
        )
        "addSchedule" -> AddScheduleScreen(
            onNavigateBack = { currentScreen = "medicines" },
            viewModel = viewModel
        )
        "about" -> AboutScreen(
            onNavigateBack = { currentScreen = "patients" }
        )
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Request notification permission for Android 13 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
            }
        }
        
        setContent {
            RemindMeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScheduleScreen(
    onNavigateBack: () -> Unit,
    viewModel: MedicineViewModel
) {
    var time by remember { mutableStateOf("") }
    var showTimePicker by remember { mutableStateOf(false) }
    var scheduleForAllDays by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    val selectedMedicineId = viewModel.selectedMedicineId.collectAsState().value
    val selectedDay by viewModel.selectedDay.collectAsState()

    // Dosage options
    val dosageOptions = listOf("Full", "Half", "Quarter")
    var selectedDosage by remember { mutableStateOf(dosageOptions[0]) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Add Schedule") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Time Selection
            Text(
                text = "Select Time",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedButton(
                onClick = { showTimePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (time.isEmpty()) "Select Time" else time)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dosage Selection Dropdown
            Text(
                text = "Select Dosage",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedDosage,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    label = { Text("Dosage") }
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    dosageOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                selectedDosage = option
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Schedule Type Selection
            Text(
                text = "Schedule Type",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Schedule options with better visual hierarchy
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { scheduleForAllDays = false }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = !scheduleForAllDays,
                            onClick = { scheduleForAllDays = false }
                        )
                        Text(
                            text = "Schedule for ${selectedDay.name}",
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { scheduleForAllDays = true }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = scheduleForAllDays,
                            onClick = { scheduleForAllDays = true }
                        )
                        Text(
                            text = "Schedule for all days",
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Add Schedule Button
            Button(
                onClick = {
                    if (time.isNotEmpty() && selectedMedicineId != null) {
                        if (scheduleForAllDays) {
                            // Add schedule for all days
                            DayOfWeek.values().forEach { day ->
                                viewModel.addSchedule(
                                    medicineId = selectedMedicineId,
                                    dayOfWeek = day,
                                    time = time,
                                    dosage = selectedDosage
                                )
                            }
                        } else {
                            // Add schedule for selected day only
                            viewModel.addSchedule(
                                medicineId = selectedMedicineId,
                                dayOfWeek = selectedDay,
                                time = time,
                                dosage = selectedDosage
                            )
                        }
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = time.isNotEmpty() && selectedMedicineId != null
            ) {
                Text("Add Schedule")
            }
        }
    }

    if (showTimePicker) {
        TimePickerDialog(
            onDismiss = { showTimePicker = false },
            onConfirm = { hour, minute ->
                time = String.format("%02d:%02d", hour, minute)
                showTimePicker = false
            }
        )
    }
}

@Composable
fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    var selectedHour by remember { mutableStateOf(12) }
    var selectedMinute by remember { mutableStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Time") },
        text = {
            Column {
                // Hour picker
                OutlinedTextField(
                    value = selectedHour.toString(),
                    onValueChange = { 
                        val hour = it.toIntOrNull()
                        if (hour != null && hour in 0..23) {
                            selectedHour = hour
                        }
                    },
                    label = { Text("Hour (0-23)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Minute picker
                OutlinedTextField(
                    value = selectedMinute.toString(),
                    onValueChange = { 
                        val minute = it.toIntOrNull()
                        if (minute != null && minute in 0..59) {
                            selectedMinute = minute
                        }
                    },
                    label = { Text("Minute (0-59)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedHour, selectedMinute) }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}