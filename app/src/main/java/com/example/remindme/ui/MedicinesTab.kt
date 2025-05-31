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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import com.example.remindme.data.Medicine
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import com.example.remindme.data.MedicineSuggestion
import com.example.remindme.util.MedicineSuggestions
import androidx.compose.foundation.lazy.items

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicinesTab(
    onNavigateBack: () -> Unit,
    onNavigateToTimings: () -> Unit,
    onNavigateToScheduleSummary: () -> Unit,
    onNavigateToAddSchedule: (Int) -> Unit,
    onNavigateToRefills: () -> Unit
) {
    val viewModel: MedicineViewModel = viewModel()
    val selectedPatientId by viewModel.selectedPatientId.collectAsState()
    val medicines by viewModel.medicines.collectAsState()
    
    var medicineName by remember { mutableStateOf("") }
    var illnessType by remember { mutableStateOf("") }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var medicineToDelete: Medicine? by remember { mutableStateOf(null) }
    
    // Add these states for medicine suggestions
    var showSuggestions by remember { mutableStateOf(false) }
    var suggestions by remember { mutableStateOf<List<MedicineSuggestion>>(emptyList()) }

    if (showConfirmDialog) {
        ConfirmationDialog(
            title = "Clear Everything",
            message = "This will delete all medicines and their schedules. This action cannot be undone. Are you sure?",
            onConfirm = { 
                viewModel.clearEverything()
                showConfirmDialog = false
            },
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
                    IconButton(onClick = onNavigateToTimings) {
                        Icon(Icons.Default.Schedule, "View Timings")
                    }
                    IconButton(onClick = onNavigateToScheduleSummary) {
                        Icon(Icons.Default.List, "View Schedule Summary")
                    }
                    IconButton(onClick = onNavigateToRefills) {
                        Icon(Icons.Default.Refresh, "Medicine Refills")
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
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = medicineName,
                    onValueChange = { 
                        medicineName = it
                        suggestions = MedicineSuggestions.searchMedicines(it)
                        showSuggestions = suggestions.isNotEmpty() && it.isNotEmpty()
                    },
                    label = { Text("Medicine Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    ),
                    trailingIcon = {
                        if (medicineName.isNotEmpty()) {
                            IconButton(onClick = { 
                                medicineName = ""
                                suggestions = emptyList()
                                showSuggestions = false
                            }) {
                                Icon(Icons.Default.Clear, "Clear")
                            }
                        }
                    }
                )
                
                AnimatedVisibility(
                    visible = showSuggestions,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                            .heightIn(max = 200.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(suggestions) { suggestion ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            medicineName = suggestion.name
                                            illnessType = suggestion.commonUses
                                            showSuggestions = false
                                            suggestions = emptyList()
                                        },
                                    color = MaterialTheme.colorScheme.surface
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                    ) {
                                        Text(
                                            text = suggestion.name,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = suggestion.commonUses,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Divider()
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            TextField(
                value = illnessType,
                onValueChange = { illnessType = it },
                label = { Text("Illness Type") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    if (medicineName.isNotBlank() && illnessType.isNotBlank()) {
                        viewModel.addMedicine(medicineName, illnessType)
                        medicineName = ""
                        illnessType = ""
                        showSuggestions = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = medicineName.isNotBlank() && illnessType.isNotBlank() && selectedPatientId != null
            ) {
                Text("Add Medicine")
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
                if (medicines.isEmpty()) {
                    item {
                        Text(
                            text = "No medicines added yet",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }
                } else {
                    val groupedMedicines = medicines.groupBy { it.illnessType }
                    
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
                                    
                                    Row {
                                        Button(
                                            onClick = { onNavigateToAddSchedule(medicine.id) },
                                            modifier = Modifier.padding(end = 8.dp)
                                        ) {
                                            Text("Add Schedule")
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
                        }
                        
                        if (illnessType != groupedMedicines.keys.last()) {
                            item {
                                Divider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                            }
                        }
                    }
                }
                
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