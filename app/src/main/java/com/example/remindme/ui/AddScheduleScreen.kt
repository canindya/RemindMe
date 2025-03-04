package com.example.remindme.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScheduleScreen(
    onNavigateBack: () -> Unit,
    viewModel: MedicineViewModel = viewModel()
) {
    var time by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var showTimePicker by remember { mutableStateOf(false) }

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
        ) {
            OutlinedButton(
                onClick = { showTimePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (time.isEmpty()) "Select Time" else time)
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = dosage,
                onValueChange = { dosage = it },
                label = { Text("Dosage") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (time.isNotEmpty() && dosage.isNotEmpty()) {
                        viewModel.addSchedule(
                            medicineId = viewModel.selectedMedicineId.value,
                            dayOfWeek = viewModel.selectedDay.value,
                            time = time,
                            dosage = dosage
                        )
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = time.isNotEmpty() && dosage.isNotEmpty()
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