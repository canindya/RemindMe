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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import com.example.remindme.data.Medicine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicinesTab(viewModel: MedicineViewModel) {
    var medicineName by remember { mutableStateOf("") }
    var illnessType by remember { mutableStateOf("") }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var medicineToDelete: Medicine? by remember { mutableStateOf(null) }
    
    val medicines by viewModel.medicines.collectAsState()

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

    Column(modifier = Modifier.padding(16.dp)) {
        TextField(
            value = medicineName,
            onValueChange = { medicineName = it },
            label = { Text("Medicine Name") },
            modifier = Modifier.fillMaxWidth()
        )
        
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
                }
            },
            modifier = Modifier.fillMaxWidth()
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
            items(medicines) { medicine ->
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
                            Text("Illness: ${medicine.illnessType}")
                        }
                        
                        IconButton(
                            onClick = { medicineToDelete = medicine }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete medicine",
                                tint = Color.Red
                            )
                        }
                    }
                }
            }
        }
    }
} 