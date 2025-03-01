package com.example.remindme

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.remindme.ui.MedicineViewModel
import com.example.remindme.ui.MedicinesTab
import com.example.remindme.ui.TimingsTab
import com.example.remindme.ui.theme.RemindMeTheme
import com.example.remindme.ui.PatientScreen
import com.example.remindme.ui.AddPatientScreen
import com.example.remindme.ui.PatientMedicineScheduleScreen

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != 
                PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
            }
        }

        enableEdgeToEdge()
        setContent {
            RemindMeTheme {
                var currentScreen by remember { mutableStateOf("patients") }
                val viewModel: MedicineViewModel = viewModel()

                when (currentScreen) {
                    "patients" -> PatientScreen(
                        onNavigateToAddPatient = { currentScreen = "addPatient" },
                        onPatientSelected = { patientId ->
                            viewModel.setSelectedPatient(patientId)
                            currentScreen = "medicines"
                        }
                    )
                    "addPatient" -> AddPatientScreen(
                        onNavigateBack = { currentScreen = "patients" }
                    )
                    "medicines" -> MedicinesTab(
                        onNavigateBack = { currentScreen = "patients" },
                        onNavigateToTimings = { currentScreen = "timings" },
                        onNavigateToScheduleSummary = { currentScreen = "schedule_summary" }
                    )
                    "timings" -> TimingsTab(
                        onNavigateBack = { currentScreen = "medicines" }
                    )
                    "schedule_summary" -> PatientMedicineScheduleScreen(
                        onNavigateBack = { currentScreen = "medicines" }
                    )
                }
            }
        }
    }
}