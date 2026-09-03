package com.example.digitallevel

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.example.digitallevel.data.AppDatabase
import com.example.digitallevel.data.MeasurementRepository
import com.example.digitallevel.data.PreferencesManager

class DigitalLevelApplication : Application() {

    lateinit var preferencesManager: PreferencesManager
        private set

    lateinit var repository: MeasurementRepository
        private set

    override fun onCreate() {
        super.onCreate()
        preferencesManager = PreferencesManager(this)
        
        val database = AppDatabase.getDatabase(this)
        repository = MeasurementRepository(database.measurementDao())

        if (preferencesManager.isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
}
