package com.example.remindme.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.example.remindme.ui.components.ConfirmationDialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.List
import com.example.remindme.data.Medicine
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.foundation.clickable
import com.example.remindme.data.MedicineSuggestion
import com.example.remindme.util.MedicineSuggestions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicinesTab(
    onNavigateBack: () -> Unit,
    onNavigateToTimings: () -> Unit,
    onNavigateToScheduleSummary: () -> Unit
) {
    val viewModel: MedicineViewModel = viewModel()
    val patientViewModel: PatientViewModel = viewModel()
    val selectedPatientId by viewModel.selectedPatientId.collectAsState()
    val patients by patientViewModel.patients.collectAsState()
    
    var medicineName by remember { mutableStateOf("") }
    var illnessType by remember { mutableStateOf("") }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var medicineToDelete: Medicine? by remember { mutableStateOf(null) }
    
    // Add these states for medicine suggestions
    var showSuggestions by remember { mutableStateOf(false) }
    var suggestions by remember { mutableStateOf<List<MedicineSuggestion>>(emptyList()) }
    
    val medicines by viewModel.medicines.collectAsState()
    val selectedPatient = patients.find { it.id == selectedPatientId }

    // Add this state
    var isAddSectionExpanded by remember { mutableStateOf(false) }

    // Add these states at the top with other states
    var selectedMedicine by remember { mutableStateOf<Medicine?>(null) }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    if (showConfirmDialog) {
        ConfirmationDialog(
            title = "Clear Everything",
            message = "This will delete all medicines and their schedules. This action cannot be undone. Are you sure?",
            onConfirm = { viewModel.clearEverything() },
            onDismiss = { showConfirmDialog = false }
        )
    }

    // Show delete confirmation dialog
    medicineToDelete?.let { medicine ->
        ConfirmationDialog(
            title = "Delete Medicine",
            message = "This will delete '${medicine.name}' and all its schedules. This action cannot be undone. Are you sure?",
            onConfirm = {
                viewModel.deleteMedicine(medicine)
                medicineToDelete = null
            },
            onDismiss = { medicineToDelete = null }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Medicines") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Go back")
                    }
                },
                actions = {
                    // Add expand/collapse button
                    IconButton(onClick = { isAddSectionExpanded = !isAddSectionExpanded }) {
                        Icon(
                            if (isAddSectionExpanded) Icons.Default.KeyboardArrowUp 
                            else Icons.Default.KeyboardArrowDown,
                            "Toggle add section"
                        )
                    }
                    IconButton(onClick = onNavigateToScheduleSummary) {
                        Icon(Icons.Default.List, "View schedule")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Show selected patient info
            selectedPatient?.let { patient ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Patient: ${patient.name}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Age: ${patient.age} | Sex: ${patient.sex}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                AnimatedVisibility(visible = isAddSectionExpanded) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            // Add a dropdown to select existing medicine
                            if (medicines.isNotEmpty()) {
                                ExposedDropdownMenuBox(
                                    expanded = isDropdownExpanded,
                                    onExpandedChange = { isDropdownExpanded = it },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    TextField(
                                        value = selectedMedicine?.name ?: "Select Medicine",
                                        onValueChange = { },
                                        readOnly = true,
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(isDropdownExpanded) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor()
                                    )

                                    ExposedDropdownMenu(
                                        expanded = isDropdownExpanded,
                                        onDismissRequest = { isDropdownExpanded = false }
                                    ) {
                                        medicines.forEach { medicine ->
                                            DropdownMenuItem(
                                                text = { 
                                                    Column {
                                                        Text(medicine.name)
                                                        Text(
                                                            text = "For: ${medicine.illnessType}",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                },
                                                onClick = {
                                                    selectedMedicine = medicine
                                                    isDropdownExpanded = false
                                                    // Clear the new medicine fields when selecting existing medicine
                                                    medicineName = ""
                                                    illnessType = ""
                                                }
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Add a clear selection button if a medicine is selected
                                if (selectedMedicine != null) {
                                    TextButton(
                                        onClick = { selectedMedicine = null },
                                        modifier = Modifier.align(Alignment.End)
                                    ) {
                                        Text("Clear Selection")
                                    }
                                }
                            }

                            // Update the "Or Add New Medicine" section to be conditional
                            if (selectedMedicine == null) {
                                Text(
                                    "Add New Medicine",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )

                                // Medicine name input with suggestions
                                TextField(
                                    value = medicineName,
                                    onValueChange = { name ->
                                        medicineName = name
                                        suggestions = if (name.length >= 2) {
                                            MedicineSuggestions.searchMedicines(name)
                                        } else {
                                            emptyList()
                                        }
                                        showSuggestions = suggestions.isNotEmpty()
                                    },
                                    label = { Text("Medicine Name") },
                                    modifier = Modifier.fillMaxWidth(),
                                    trailingIcon = {
                                        if (medicineName.isNotEmpty()) {
                                            IconButton(onClick = { medicineName = "" }) {
                                                Icon(Icons.Default.Clear, "Clear text")
                                            }
                                        }
                                    }
                                )

                                // Show suggestions if any
                                if (showSuggestions) {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(max = 200.dp)
                                    ) {
                                        LazyColumn {
                                            items(suggestions) { suggestion ->
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable {
                                                            medicineName = suggestion.name
                                                            illnessType = suggestion.commonUses
                                                            showSuggestions = false
                                                        }
                                                        .padding(16.dp)
                                                ) {
                                                    Text(suggestion.name)
                                                    Text(
                                                        suggestion.commonUses,
                                                        style = MaterialTheme.typography.bodySmall
                                                    )
                                                }
                                                Divider()
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Illness type input
                                TextField(
                                    value = illnessType,
                                    onValueChange = { illnessType = it },
                                    label = { Text("Illness Type") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Add medicine button
                            Button(
                                onClick = {
                                    if (selectedMedicine == null) {
                                        if (medicineName.isNotBlank() && illnessType.isNotBlank()) {
                                            viewModel.addMedicine(medicineName, illnessType)
                                            medicineName = ""
                                            illnessType = ""
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = selectedMedicine == null && (medicineName.isNotBlank() && illnessType.isNotBlank())
                            ) {
                                Text("Add Medicine")
                            }

                            // Update the Add Timing button
                            Button(
                                onClick = {
                                    if (selectedMedicine != null) {
                                        viewModel.setSelectedMedicine(selectedMedicine!!.id)
                                    } else if (medicineName.isNotBlank() && illnessType.isNotBlank()) {
                                        // First add the new medicine
                                        viewModel.addMedicine(medicineName, illnessType)
                                        // The medicine ID will be handled in the ViewModel
                                    }
                                    onNavigateToTimings()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                enabled = selectedMedicine != null || (medicineName.isNotBlank() && illnessType.isNotBlank())
                            ) {
                                Text(if (selectedMedicine != null) "Add Schedule for ${selectedMedicine!!.name}" else "Add Schedule for New Medicine")
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
                Text("Clear All")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyColumn {
                val groupedMedicines = medicines.groupByIllness()
                
                groupedMedicines.forEach { (illnessType, medicinesInGroup) ->
                    item {
                        Text(
                            text = illnessType,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    
                    items(medicinesInGroup) { medicine ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 8.dp, end = 0.dp, bottom = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = medicine.name,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Added for: ${medicine.illnessType}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                
                                IconButton(
                                    onClick = { medicineToDelete = medicine }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete medicine",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                    
                    // Add a divider between groups
                    if (illnessType != groupedMedicines.keys.last()) {
                        item {
                            Divider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                        }
                    }
                }
                
                // Add some bottom padding
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

private fun List<Medicine>.groupByIllness(): Map<String, List<Medicine>> {
    return groupBy { it.illnessType }
} 