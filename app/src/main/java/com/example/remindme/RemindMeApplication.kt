package com.example.remindme

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager

class RemindMeApplication : Application(), Configuration.Provider {
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

    override fun onCreate() {
        super.onCreate()
        // Initialize WorkManager
        WorkManager.getInstance(this)
    }
} 