package com.example.remindme

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager

class RemindMeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        try {
            // Try to get the WorkManager instance to check if it's initialized
            WorkManager.getInstance(applicationContext)
        } catch (e: IllegalStateException) {
            // Initialize only if not already initialized
            WorkManager.initialize(
                this,
                Configuration.Builder()
                    .setMinimumLoggingLevel(android.util.Log.INFO)
                    .build()
            )
        }
    }
} 