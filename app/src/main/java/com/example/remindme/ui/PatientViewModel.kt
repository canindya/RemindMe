package com.example.remindme.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.remindme.data.MedicineDatabase
import com.example.remindme.data.Patient
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PatientViewModel(application: Application) : AndroidViewModel(application) {
    private val database = MedicineDatabase.getDatabase(application)
    private val dao = database.patientDao()

    val patients = dao.getAllPatients()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    fun addPatient(name: String, age: Int, sex: String) {
        viewModelScope.launch {
            dao.insertPatient(Patient(name = name, age = age, sex = sex))
        }
    }

    fun deletePatient(patient: Patient) {
        viewModelScope.launch {
            dao.deletePatient(patient)
        }
    }
} 