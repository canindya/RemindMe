package com.example.remindme

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager

class RemindMeApp : Application(), Configuration.Provider {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize WorkManager with custom configuration
        WorkManager.getInstance(this)
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .setDefaultProcessName("com.example.remindme")
            .build()
} 