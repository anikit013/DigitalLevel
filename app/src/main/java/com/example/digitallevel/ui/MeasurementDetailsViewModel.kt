package com.example.digitallevel.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.digitallevel.data.MeasurementEntity
import com.example.digitallevel.data.MeasurementRepository
import com.example.digitallevel.data.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MeasurementDetailsViewModel(
    private val repository: MeasurementRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _measurement = MutableStateFlow<MeasurementEntity?>(null)
    val measurement: StateFlow<MeasurementEntity?> = _measurement.asStateFlow()

    fun loadMeasurement(id: Int) {
        viewModelScope.launch {
            _measurement.value = repository.getMeasurementById(id)
        }
    }

    fun deleteCurrentMeasurement(onDeleted: () -> Unit) {
        val m = _measurement.value ?: return
        viewModelScope.launch {
            repository.delete(m)
            onDeleted()
        }
    }

    fun setAsReference(m: MeasurementEntity) {
        preferencesManager.saveCalibration(m.angleX, m.angleY)
    }
}
