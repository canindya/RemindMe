package com.example.remindme.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.remindme.R
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import com.example.remindme.ui.components.ConfirmationDialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.IconButton
import com.example.remindme.data.Medicine
import com.example.remindme.data.MedicineSchedule
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimingsTab(viewModel: MedicineViewModel) {
    var selectedMedicineId by remember { mutableStateOf<Int?>(null) }
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedTime by remember { mutableStateOf(LocalTime.now()) }
    var expanded by remember { mutableStateOf(false) }
    var dosageExpanded by remember { mutableStateOf(false) }
    var selectedDosage by remember { mutableStateOf("FULL") }
    var applyToAllDays by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var scheduleToDelete: MedicineSchedule? by remember { mutableStateOf(null) }
    
    val medicines by viewModel.medicines.collectAsState()
    val selectedDay by viewModel.selectedDay.collectAsState()
    val schedules by viewModel.schedulesForSelectedDay.collectAsState()
    val takenMedicines by viewModel.takenMedicines.collectAsState()

    val dosageOptions = listOf("FULL", "HALF", "QUARTER")

    // Time Picker Dialog
    if (showTimePicker) {
        TimePickerDialog(
            onDismiss = { showTimePicker = false },
            onConfirm = { hour, minute ->
                selectedTime = LocalTime.of(hour, minute)
                showTimePicker = false
            }
        )
    }

    if (showConfirmDialog) {
        ConfirmationDialog(
            title = "Clear Schedules",
            message = "This will delete all medicine schedules. This action cannot be undone. Are you sure?",
            onConfirm = { viewModel.clearAllSchedules() },
            onDismiss = { showConfirmDialog = false }
        )
    }

    // Show delete confirmation dialog
    scheduleToDelete?.let { schedule ->
        val medicine = medicines.find { it.id == schedule.medicineId }
        ConfirmationDialog(
            title = "Delete Schedule",
            message = "This will delete the schedule for '${medicine?.name}' at ${schedule.time}. Are you sure?",
            onConfirm = {
                viewModel.deleteSchedule(schedule)
                scheduleToDelete = null
            },
            onDismiss = { scheduleToDelete = null }
        )
    }

    Column(modifier = Modifier.padding(16.dp)) {
        // Days of week tabs
        ScrollableTabRow(
            selectedTabIndex = selectedDay.ordinal,
            modifier = Modifier.fillMaxWidth()
        ) {
            DayOfWeek.values().forEach { day ->
                Tab(
                    selected = selectedDay == day,
                    onClick = { viewModel.setSelectedDay(day) },
                    text = { 
                        Text(day.getDisplayName(TextStyle.SHORT, Locale.getDefault()))
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Medicine selection
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            TextField(
                value = medicines.find { it.id == selectedMedicineId }?.name ?: "",
                onValueChange = { },
                readOnly = true,
                label = { Text("Select Medicine") },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                medicines.forEach { medicine ->
                    DropdownMenuItem(
                        text = { Text(medicine.name) },
                        onClick = {
                            selectedMedicineId = medicine.id
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Time selection
        OutlinedButton(
            onClick = { showTimePicker = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(selectedTime.format(DateTimeFormatter.ofPattern("HH:mm")))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Dosage selection
        ExposedDropdownMenuBox(
            expanded = dosageExpanded,
            onExpandedChange = { dosageExpanded = it }
        ) {
            TextField(
                value = selectedDosage,
                onValueChange = { },
                readOnly = true,
                label = { Text("Select Dosage") },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = dosageExpanded,
                onDismissRequest = { dosageExpanded = false }
            ) {
                dosageOptions.forEach { dosage ->
                    DropdownMenuItem(
                        text = { Text(dosage) },
                        onClick = {
                            selectedDosage = dosage
                            dosageExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Add checkbox before the Add Schedule button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = applyToAllDays,
                onCheckedChange = { applyToAllDays = it }
            )
            Text(
                text = "Same schedule for all days",
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Button(
            onClick = {
                if (selectedMedicineId != null) {
                    if (applyToAllDays) {
                        // Add schedule for all days
                        DayOfWeek.values().forEach { day ->
                            viewModel.addSchedule(
                                selectedMedicineId!!,
                                day,
                                selectedTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                                selectedDosage
                            )
                        }
                    } else {
                        // Add schedule for selected day only
                        viewModel.addSchedule(
                            selectedMedicineId!!,
                            selectedDay,
                            selectedTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                            selectedDosage
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Schedule")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display schedules for selected day
        LazyColumn {
            items(schedules) { schedule ->
                val medicine = medicines.find { it.id == schedule.medicineId }
                if (medicine != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Medicine: ${medicine.name}")
                                Text("Time: ${schedule.time}")
                                Text("Dosage: ${schedule.dosage}")
                            }
                            
                            val isTaken = takenMedicines.any { taken -> 
                                taken.medicineId == medicine.id && 
                                taken.scheduleId == schedule.id && 
                                taken.date == LocalDate.now()
                            }
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_medicine_alarm),
                                    contentDescription = "Reminder set",
                                    modifier = Modifier.size(24.dp)
                                )
                                
                                Text(
                                    text = if (isTaken) "Taken" else "Due",
                                    color = if (isTaken) Color.Green else Color.Red,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                            }
                            
                            IconButton(
                                onClick = { scheduleToDelete = schedule }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete schedule",
                                    tint = Color.Red
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { showConfirmDialog = true },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Red,
                contentColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Clear All Schedules")
        }
    }
}

@Composable
fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    var selectedHour by remember { mutableStateOf(LocalTime.now().hour) }
    var selectedMinute by remember { mutableStateOf(LocalTime.now().minute) }
    var showHourDropdown by remember { mutableStateOf(false) }
    var showMinuteDropdown by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Time") },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Hours
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(
                        onClick = { showHourDropdown = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(String.format("%02d", selectedHour))
                    }
                    DropdownMenu(
                        expanded = showHourDropdown,
                        onDismissRequest = { showHourDropdown = false }
                    ) {
                        (0..23).forEach { hour ->
                            DropdownMenuItem(
                                text = { Text(String.format("%02d", hour)) },
                                onClick = {
                                    selectedHour = hour
                                    showHourDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Minutes
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(
                        onClick = { showMinuteDropdown = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(String.format("%02d", selectedMinute))
                    }
                    DropdownMenu(
                        expanded = showMinuteDropdown,
                        onDismissRequest = { showMinuteDropdown = false }
                    ) {
                        (0..59 step 5).forEach { minute ->
                            DropdownMenuItem(
                                text = { Text(String.format("%02d", minute)) },
                                onClick = {
                                    selectedMinute = minute
                                    showMinuteDropdown = false
                                }
                            )
                        }
                    }
                }
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