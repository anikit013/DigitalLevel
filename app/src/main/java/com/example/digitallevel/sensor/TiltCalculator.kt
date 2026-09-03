package com.example.digitallevel.sensor

import com.example.digitallevel.util.Constants
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.sqrt

data class TiltData(
    val xTilt: Float, // in degrees
    val yTilt: Float, // in degrees
    val overallTilt: Float // in degrees
)

class TiltCalculator(private val alpha: Float = 0.8f) {

    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 9.81f // Assume resting flat initially

    fun process(values: FloatArray): TiltData {
        // Apply low-pass smoothing filter
        // alpha * previous + (1-alpha) * current
        lastX = alpha * lastX + (1 - alpha) * values[0]
        lastY = alpha * lastY + (1 - alpha) * values[1]
        lastZ = alpha * lastZ + (1 - alpha) * values[2]

        // Calculate X and Y tilt in radians
        // Roll (X-axis tilt)
        val xTiltRad = atan2(lastX.toDouble(), sqrt((lastY * lastY + lastZ * lastZ).toDouble()))
        // Pitch (Y-axis tilt)
        val yTiltRad = atan2(lastY.toDouble(), sqrt((lastX * lastX + lastZ * lastZ).toDouble()))
        
        // Overall tilt from Z-axis
        val norm = sqrt((lastX * lastX + lastY * lastY + lastZ * lastZ).toDouble())
        val zNormalized = if (norm > 0) (lastZ / norm).coerceIn(-1.0, 1.0) else 1.0
        val overallTiltRad = acos(zNormalized)

        // Convert radians to degrees
        val xTiltDeg = Math.toDegrees(xTiltRad).toFloat()
        val yTiltDeg = Math.toDegrees(yTiltRad).toFloat()
        val overallTiltDeg = Math.toDegrees(overallTiltRad).toFloat()

        return TiltData(
            xTilt = xTiltDeg,
            yTilt = yTiltDeg,
            overallTilt = overallTiltDeg
        )
    }

    fun calculateStatus(overallTilt: Float): String {
        return when {
            overallTilt < Constants.LEVEL_THRESHOLD -> "LEVEL"
            overallTilt < Constants.SLIGHT_TILT_THRESHOLD -> "SLIGHTLY TILTED"
            else -> "TILTED"
        }
    }

    fun calculateStatus(tiltData: TiltData): String {
        return calculateStatus(tiltData.overallTilt)
    }
}
