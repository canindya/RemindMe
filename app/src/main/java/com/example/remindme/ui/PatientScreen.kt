package com.example.remindme.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.remindme.data.Patient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientScreen(
    onNavigateToAddPatient: () -> Unit,
    onPatientSelected: (Int) -> Unit,
    viewModel: MedicineViewModel = viewModel()
) {
    val patients by viewModel.patients.collectAsState(initial = emptyList())

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Patients") },
            actions = {
                IconButton(onClick = onNavigateToAddPatient) {
                    Icon(Icons.Default.Add, contentDescription = "Add Patient")
                }
            }
        )

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(patients) { patient ->
                PatientItem(
                    patient = patient,
                    onPatientSelected = onPatientSelected
                )
            }
        }
    }
}

@Composable
fun PatientItem(
    patient: Patient,
    onPatientSelected: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onPatientSelected(patient.id) },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                    text = patient.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Age: ${patient.age}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Button(
                onClick = { onPatientSelected(patient.id) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("View Timings")
            }
        }
    }
} 