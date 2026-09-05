package com.example.digitallevel.data

import android.content.Context
import android.content.SharedPreferences
import com.example.digitallevel.sensor.MeasurementMode

class PreferencesManager(private val prefs: SharedPreferences) {

    constructor(context: Context) : this(
        context.getSharedPreferences("digital_level_prefs", Context.MODE_PRIVATE)
    )

    companion object {
        private const val KEY_DARK_MODE = "key_dark_mode"
        private const val KEY_KEEP_SCREEN_AWAKE = "key_keep_screen_awake"
        private const val KEY_LEVEL_FEEDBACK_ENABLED = "key_level_feedback_enabled"

        // FLAT mode calibration
        private const val KEY_OFFSET_X = "key_offset_x"
        private const val KEY_OFFSET_Y = "key_offset_y"
        private const val KEY_IS_CALIBRATED = "key_is_calibrated"

        // EDGE mode calibration
        private const val KEY_EDGE_OFFSET_X = "key_edge_offset_x"
        private const val KEY_EDGE_OFFSET_Y = "key_edge_offset_y"
        private const val KEY_IS_EDGE_CALIBRATED = "key_is_edge_calibrated"
    }

    var isDarkMode: Boolean
        get() = prefs.getBoolean(KEY_DARK_MODE, false)
        set(value) {
            prefs.edit().putBoolean(KEY_DARK_MODE, value).apply()
        }

    var keepScreenAwake: Boolean
        get() = prefs.getBoolean(KEY_KEEP_SCREEN_AWAKE, true)
        set(value) {
            prefs.edit().putBoolean(KEY_KEEP_SCREEN_AWAKE, value).apply()
        }

    var levelFeedbackEnabled: Boolean
        get() = prefs.getBoolean(KEY_LEVEL_FEEDBACK_ENABLED, true)
        set(value) {
            prefs.edit().putBoolean(KEY_LEVEL_FEEDBACK_ENABLED, value).apply()
        }

    // Default / FLAT mode offsets for backwards compatibility
    var offsetX: Float
        get() = prefs.getFloat(KEY_OFFSET_X, 0f)
        set(value) {
            prefs.edit().putFloat(KEY_OFFSET_X, value).apply()
        }

    var offsetY: Float
        get() = prefs.getFloat(KEY_OFFSET_Y, 0f)
        set(value) {
            prefs.edit().putFloat(KEY_OFFSET_Y, value).apply()
        }

    var isCalibrated: Boolean
        get() = prefs.getBoolean(KEY_IS_CALIBRATED, false)
        set(value) {
            prefs.edit().putBoolean(KEY_IS_CALIBRATED, value).apply()
        }

    // Mode-specific getters / setters for FLAT and EDGE modes
    var flatOffsetX: Float
        get() = prefs.getFloat(KEY_OFFSET_X, 0f)
        set(value) {
            prefs.edit().putFloat(KEY_OFFSET_X, value).apply()
        }

    var flatOffsetY: Float
        get() = prefs.getFloat(KEY_OFFSET_Y, 0f)
        set(value) {
            prefs.edit().putFloat(KEY_OFFSET_Y, value).apply()
        }

    var isFlatCalibrated: Boolean
        get() = prefs.getBoolean(KEY_IS_CALIBRATED, false)
        set(value) {
            prefs.edit().putBoolean(KEY_IS_CALIBRATED, value).apply()
        }

    var edgeOffsetX: Float
        get() = prefs.getFloat(KEY_EDGE_OFFSET_X, 0f)
        set(value) {
            prefs.edit().putFloat(KEY_EDGE_OFFSET_X, value).apply()
        }

    var edgeOffsetY: Float
        get() = prefs.getFloat(KEY_EDGE_OFFSET_Y, 0f)
        set(value) {
            prefs.edit().putFloat(KEY_EDGE_OFFSET_Y, value).apply()
        }

    var isEdgeCalibrated: Boolean
        get() = prefs.getBoolean(KEY_IS_EDGE_CALIBRATED, false)
        set(value) {
            prefs.edit().putBoolean(KEY_IS_EDGE_CALIBRATED, value).apply()
        }

    fun getOffsetX(mode: MeasurementMode): Float {
        return when (mode) {
            MeasurementMode.FLAT -> flatOffsetX
            MeasurementMode.EDGE -> edgeOffsetX
        }
    }

    fun getOffsetY(mode: MeasurementMode): Float {
        return when (mode) {
            MeasurementMode.FLAT -> flatOffsetY
            MeasurementMode.EDGE -> edgeOffsetY
        }
    }

    fun isCalibratedForMode(mode: MeasurementMode): Boolean {
        return when (mode) {
            MeasurementMode.FLAT -> isFlatCalibrated
            MeasurementMode.EDGE -> isEdgeCalibrated
        }
    }

    fun saveCalibration(x: Float, y: Float) {
        saveCalibrationForMode(x, y, MeasurementMode.FLAT)
    }

    fun saveCalibrationForMode(x: Float, y: Float, mode: MeasurementMode) {
        when (mode) {
            MeasurementMode.FLAT -> {
                prefs.edit()
                    .putFloat(KEY_OFFSET_X, x)
                    .putFloat(KEY_OFFSET_Y, y)
                    .putBoolean(KEY_IS_CALIBRATED, true)
                    .apply()
            }
            MeasurementMode.EDGE -> {
                prefs.edit()
                    .putFloat(KEY_EDGE_OFFSET_X, x)
                    .putFloat(KEY_EDGE_OFFSET_Y, y)
                    .putBoolean(KEY_IS_EDGE_CALIBRATED, true)
                    .apply()
            }
        }
    }

    fun resetCalibration() {
        resetCalibrationForMode(MeasurementMode.FLAT)
    }

    fun resetCalibrationForMode(mode: MeasurementMode) {
        when (mode) {
            MeasurementMode.FLAT -> {
                prefs.edit()
                    .putFloat(KEY_OFFSET_X, 0f)
                    .putFloat(KEY_OFFSET_Y, 0f)
                    .putBoolean(KEY_IS_CALIBRATED, false)
                    .apply()
            }
            MeasurementMode.EDGE -> {
                prefs.edit()
                    .putFloat(KEY_EDGE_OFFSET_X, 0f)
                    .putFloat(KEY_EDGE_OFFSET_Y, 0f)
                    .putBoolean(KEY_IS_EDGE_CALIBRATED, false)
                    .apply()
            }
        }
    }
}
