package com.example.digitallevel.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.digitallevel.data.MeasurementEntity
import com.example.digitallevel.data.MeasurementRepository
import com.example.digitallevel.data.PreferencesManager
import com.example.digitallevel.sensor.MeasurementMode
import com.example.digitallevel.util.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.sqrt

data class LevelUiState(
    val rawX: Float = 0f,
    val rawY: Float = 0f,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val isCalibrated: Boolean = false,
    val calibratedX: Float = 0f,
    val calibratedY: Float = 0f,
    val overallTilt: Float = 0f,
    val status: String = "LEVEL",
    val lightLevel: Float = 0f,
    val isAccelerometerAvailable: Boolean = true,
    val isLightSensorAvailable: Boolean = true,
    val currentMode: MeasurementMode = MeasurementMode.FLAT
)

class LevelViewModel(
    private val repository: MeasurementRepository,
    private val preferencesManager: PreferencesManager? = null
) : ViewModel() {

    companion object {
        const val LEVEL_THRESHOLD = Constants.LEVEL_THRESHOLD
        const val SLIGHT_TILT_THRESHOLD = Constants.SLIGHT_TILT_THRESHOLD
    }

    private val _uiState = MutableStateFlow(LevelUiState())
    val uiState: StateFlow<LevelUiState> = _uiState.asStateFlow()

    init {
        preferencesManager?.let { prefs ->
            val mode = _uiState.value.currentMode
            val offX = prefs.getOffsetX(mode)
            val offY = prefs.getOffsetY(mode)
            val isCal = prefs.isCalibratedForMode(mode)
            _uiState.value = _uiState.value.copy(
                offsetX = offX,
                offsetY = offY,
                isCalibrated = isCal
            )
            recalculateState()
        }
    }

    fun setMeasurementMode(mode: MeasurementMode) {
        _uiState.value = _uiState.value.copy(currentMode = mode)
        preferencesManager?.let { prefs ->
            val offX = prefs.getOffsetX(mode)
            val offY = prefs.getOffsetY(mode)
            val isCal = prefs.isCalibratedForMode(mode)
            _uiState.value = _uiState.value.copy(
                offsetX = offX,
                offsetY = offY,
                isCalibrated = isCal
            )
        }
        recalculateState()
    }

    fun updateSensorAvailability(accelAvailable: Boolean, lightAvailable: Boolean) {
        _uiState.value = _uiState.value.copy(
            isAccelerometerAvailable = accelAvailable,
            isLightSensorAvailable = lightAvailable
        )
    }

    fun processTilt(rawX: Float, rawY: Float) {
        _uiState.value = _uiState.value.copy(
            rawX = rawX,
            rawY = rawY
        )
        recalculateState()
    }

    fun processLight(light: Float) {
        _uiState.value = _uiState.value.copy(lightLevel = light)
    }

    fun setCalibrationOffset(x: Float, y: Float) {
        val mode = _uiState.value.currentMode
        _uiState.value = _uiState.value.copy(
            offsetX = x,
            offsetY = y,
            isCalibrated = true
        )
        preferencesManager?.saveCalibrationForMode(x, y, mode)
        recalculateState()
    }

    fun calibrateCurrentPosition() {
        val currentRawX = _uiState.value.rawX
        val currentRawY = _uiState.value.rawY
        setCalibrationOffset(currentRawX, currentRawY)
    }

    fun resetCalibration() {
        val mode = _uiState.value.currentMode
        _uiState.value = _uiState.value.copy(
            offsetX = 0f,
            offsetY = 0f,
            isCalibrated = false
        )
        preferencesManager?.resetCalibrationForMode(mode)
        recalculateState()
    }

    private fun recalculateState() {
        val currentState = _uiState.value
        val calX = currentState.rawX - currentState.offsetX
        val calY = currentState.rawY - currentState.offsetY
        val overall = sqrt((calX * calX + calY * calY).toDouble()).toFloat()
        val status = calculateStatus(overall)

        _uiState.value = currentState.copy(
            calibratedX = calX,
            calibratedY = calY,
            overallTilt = overall,
            status = status
        )
    }

    fun calculateStatus(overallTilt: Float): String {
        return when {
            overallTilt < LEVEL_THRESHOLD -> "LEVEL"
            overallTilt < SLIGHT_TILT_THRESHOLD -> "SLIGHTLY TILTED"
            else -> "TILTED"
        }
    }

    fun saveMeasurement(onSaved: (Long) -> Unit) {
        val state = _uiState.value
        val entity = MeasurementEntity(
            timestamp = System.currentTimeMillis(),
            angleX = state.calibratedX,
            angleY = state.calibratedY,
            overallTilt = state.overallTilt,
            lightLevel = state.lightLevel,
            status = state.status,
            mode = state.currentMode.name
        )

        viewModelScope.launch {
            val id = repository.insert(entity)
            onSaved(id)
        }
    }
}
