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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.filled.ArrowBack
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.CardDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimingsTab(
    onNavigateBack: () -> Unit,
    viewModel: MedicineViewModel = viewModel()
) {
    val selectedDay by viewModel.selectedDay.collectAsState()
    val schedules by viewModel.schedulesForSelectedDay.collectAsState()
    val takenMedicines by viewModel.takenMedicines.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Timings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(top = 32.dp)  // Add more padding to move the days of the week down
        ) {
            // Day selector
            DaySelector(
                selectedDay = selectedDay,
                onDaySelected = { viewModel.setSelectedDay(it) }
            )

            // Schedules list
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                items(schedules) { schedule ->
                    val isTaken = takenMedicines.any { 
                        it.medicineId == schedule.medicineId && 
                        it.scheduleId == schedule.id 
                    }

                    ScheduleItem(
                        schedule = schedule,
                        isTaken = isTaken,
                        onMarkTaken = { 
                            if (!isTaken) {
                                viewModel.markMedicineTaken(schedule.medicineId, schedule.id)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ScheduleItem(
    schedule: MedicineSchedule,
    isTaken: Boolean,
    onMarkTaken: () -> Unit
) {
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
            Column {
                Text(
                    text = schedule.time,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Dosage: ${schedule.dosage}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (isTaken) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Taken",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(32.dp)
                        .padding(4.dp)
                )
            } else {
                Button(
                    onClick = onMarkTaken,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Mark as Taken")
                }
            }
        }
    }
}

@Composable
fun DaySelector(
    selectedDay: DayOfWeek,
    onDaySelected: (DayOfWeek) -> Unit
) {
    val days = DayOfWeek.values()
    
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        items(days) { day ->
            DayButton(
                day = day,
                isSelected = day == selectedDay,
                onClick = { onDaySelected(day) }
            )
        }
    }
}

@Composable
fun DayButton(
    day: DayOfWeek,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val currentDay = LocalDate.now().dayOfWeek
    val buttonColor = if (day == currentDay) {
        Color(0xFFFFA500) // Orange color for the current day
    } else if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.secondary
    }

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonColor
        ),
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Text(
            text = day.name.take(3),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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