package com.example.remindme.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.remindme.data.MedicineRefill
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineRefillScreen(
    onNavigateBack: () -> Unit
) {
    val viewModel: MedicineViewModel = viewModel()
    val medicines by viewModel.medicines.collectAsState()
    val refills by viewModel.refills.collectAsState()
    var showAddRefillDialog by remember { mutableStateOf(false) }
    var selectedMedicineId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(Unit) {
        viewModel.selectedPatientId.value?.let { patientId ->
            viewModel.loadRefillsForPatient(patientId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Medicine Refills") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Go back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddRefillDialog = true }) {
                        Icon(Icons.Default.Add, "Add Refill")
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
            items(refills) { refill ->
                val medicine = medicines.find { it.id == refill.medicineId }
                medicine?.let {
                    RefillCard(
                        medicine = it,
                        refill = refill,
                        onDelete = { viewModel.deleteRefill(refill) }
                    )
                }
            }
        }
    }

    if (showAddRefillDialog) {
        AddRefillDialog(
            medicines = medicines,
            onDismiss = { showAddRefillDialog = false },
            onConfirm = { medicineId, weeklyCount, lastRefillDate, nextRefillDate, notes ->
                viewModel.addRefill(medicineId, weeklyCount, lastRefillDate, nextRefillDate, notes)
                showAddRefillDialog = false
            }
        )
    }
}

@Composable
fun RefillCard(
    medicine: com.example.remindme.data.Medicine,
    refill: MedicineRefill,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val daysUntilRefill = ChronoUnit.DAYS.between(LocalDate.now(), refill.nextRefillDate)
    val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = medicine.name,
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(Icons.Default.Delete, "Delete refill")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Weekly Count: ${refill.weeklyCount}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Last Refill: ${refill.lastRefillDate.format(formatter)}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Next Refill: ${refill.nextRefillDate.format(formatter)}",
                style = MaterialTheme.typography.bodyMedium
            )
            if (daysUntilRefill <= 7) {
                Text(
                    text = "Refill due in $daysUntilRefill days",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
            if (refill.notes.isNotEmpty()) {
                Text(
                    text = "Notes: ${refill.notes}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Refill") },
            text = { Text("Are you sure you want to delete this refill record?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRefillDialog(
    medicines: List<com.example.remindme.data.Medicine>,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int, LocalDate, LocalDate, String) -> Unit
) {
    var selectedMedicineId by remember { mutableStateOf<Int?>(null) }
    var weeklyCount by remember { mutableStateOf("") }
    var lastRefillDate by remember { mutableStateOf(LocalDate.now()) }
    var nextRefillDate by remember { mutableStateOf(LocalDate.now().plusWeeks(1)) }
    var notes by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Medicine Refill") },
        text = {
            Column {
                // Medicine Selection
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    TextField(
                        value = medicines.find { it.id == selectedMedicineId }?.name ?: "Select Medicine",
                        onValueChange = { },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor()
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

                // Weekly Count
                OutlinedTextField(
                    value = weeklyCount,
                    onValueChange = { weeklyCount = it },
                    label = { Text("Weekly Count") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    selectedMedicineId?.let { medicineId ->
                        onConfirm(
                            medicineId,
                            weeklyCount.toIntOrNull() ?: 0,
                            lastRefillDate,
                            nextRefillDate,
                            notes
                        )
                    }
                },
                enabled = selectedMedicineId != null && weeklyCount.isNotEmpty()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
} 