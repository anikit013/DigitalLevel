package com.example.digitallevel.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.digitallevel.data.MeasurementEntity
import com.example.digitallevel.data.MeasurementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val repository: MeasurementRepository
) : ViewModel() {

    val measurements: Flow<List<MeasurementEntity>> = repository.getAllMeasurements()

    fun deleteMeasurement(measurement: MeasurementEntity) {
        viewModelScope.launch {
            repository.delete(measurement)
        }
    }

    fun deleteMeasurementById(id: Int) {
        viewModelScope.launch {
            val measurement = repository.getMeasurementById(id)
            if (measurement != null) {
                repository.delete(measurement)
            }
        }
    }

    fun deleteAllMeasurements() {
        viewModelScope.launch {
            repository.deleteAll()
        }
    }
}
