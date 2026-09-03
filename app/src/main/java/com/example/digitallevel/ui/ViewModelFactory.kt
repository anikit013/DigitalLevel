package com.example.digitallevel.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.digitallevel.data.MeasurementRepository
import com.example.digitallevel.data.PreferencesManager

class ViewModelFactory(
    private val repository: MeasurementRepository,
    private val preferencesManager: PreferencesManager? = null
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(LevelViewModel::class.java) -> {
                LevelViewModel(repository, preferencesManager) as T
            }
            modelClass.isAssignableFrom(HistoryViewModel::class.java) -> {
                HistoryViewModel(repository) as T
            }
            modelClass.isAssignableFrom(MeasurementDetailsViewModel::class.java) -> {
                MeasurementDetailsViewModel(repository, preferencesManager!!) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
