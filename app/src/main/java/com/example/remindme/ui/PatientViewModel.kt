package com.example.remindme.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.remindme.data.Patient
import com.example.remindme.data.PatientDao
import com.example.remindme.data.MedicineDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class PatientViewModel(application: Application) : AndroidViewModel(application) {
    private val database = MedicineDatabase.getDatabase(application)
    private val patientDao: PatientDao = database.patientDao()

    val patients: Flow<List<Patient>> = patientDao.getAllPatients()

    fun addPatient(name: String, age: Int, sex: String) {
        viewModelScope.launch {
            patientDao.insertPatient(Patient(name = name, age = age, sex = sex))
        }
    }

    fun deletePatient(patient: Patient) {
        viewModelScope.launch {
            patientDao.deletePatient(patient)
        }
    }

    fun updatePatient(patient: Patient) {
        viewModelScope.launch {
            patientDao.updatePatient(patient)
        }
    }
} 