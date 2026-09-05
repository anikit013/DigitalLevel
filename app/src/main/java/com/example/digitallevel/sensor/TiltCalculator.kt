package com.example.digitallevel.sensor

import com.example.digitallevel.util.Constants
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.sqrt

enum class MeasurementMode {
    FLAT,
    EDGE
}

data class TiltData(
    val xTilt: Float, // in degrees
    val yTilt: Float, // in degrees
    val overallTilt: Float // in degrees
)

class TiltCalculator(private val alpha: Float = 0.8f) {

    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 9.81f // Assume resting flat initially

    fun process(values: FloatArray, mode: MeasurementMode = MeasurementMode.FLAT): TiltData {
        // Apply low-pass smoothing filter
        // alpha * previous + (1-alpha) * current
        lastX = alpha * lastX + (1 - alpha) * values[0]
        lastY = alpha * lastY + (1 - alpha) * values[1]
        lastZ = alpha * lastZ + (1 - alpha) * values[2]

        val norm = sqrt((lastX * lastX + lastY * lastY + lastZ * lastZ).toDouble())

        return when (mode) {
            MeasurementMode.FLAT -> {
                // FLAT mode: pitch & roll relative to flat horizontal surface
                val xTiltRad = atan2(lastX.toDouble(), sqrt((lastY * lastY + lastZ * lastZ).toDouble()))
                val yTiltRad = atan2(lastY.toDouble(), sqrt((lastX * lastX + lastZ * lastZ).toDouble()))
                val zNormalized = if (norm > 0) (lastZ / norm).coerceIn(-1.0, 1.0) else 1.0
                val overallTiltRad = acos(zNormalized)

                TiltData(
                    xTilt = Math.toDegrees(xTiltRad).toFloat(),
                    yTilt = Math.toDegrees(yTiltRad).toFloat(),
                    overallTilt = Math.toDegrees(overallTiltRad).toFloat()
                )
            }
            MeasurementMode.EDGE -> {
                // EDGE mode: inclination relative to vertical edge reference
                val absX = abs(lastX)
                val absY = abs(lastY)

                val xTiltRad: Double
                val yTiltRad: Double
                val vertNormalized: Double

                if (absY >= absX) {
                    // Portrait vertical edge
                    xTiltRad = atan2(lastX.toDouble(), if (absY > 0) absY.toDouble() else 1.0)
                    yTiltRad = atan2(lastZ.toDouble(), if (absY > 0) absY.toDouble() else 1.0)
                    vertNormalized = if (norm > 0) (absY / norm).coerceIn(-1.0, 1.0) else 1.0
                } else {
                    // Landscape vertical edge
                    xTiltRad = atan2(lastY.toDouble(), if (absX > 0) absX.toDouble() else 1.0)
                    yTiltRad = atan2(lastZ.toDouble(), if (absX > 0) absX.toDouble() else 1.0)
                    vertNormalized = if (norm > 0) (absX / norm).coerceIn(-1.0, 1.0) else 1.0
                }

                val overallTiltRad = acos(vertNormalized)

                TiltData(
                    xTilt = Math.toDegrees(xTiltRad).toFloat(),
                    yTilt = Math.toDegrees(yTiltRad).toFloat(),
                    overallTilt = Math.toDegrees(overallTiltRad).toFloat()
                )
            }
        }
    }

    /**
     * Calculates relative LOCK offset math:
     * displayedX = currentX - lockX
     * displayedY = currentY - lockY
     */
    fun calculateRelativeTilt(
        currentX: Float,
        currentY: Float,
        lockX: Float,
        lockY: Float
    ): Pair<Float, Float> {
        val displayedX = currentX - lockX
        val displayedY = currentY - lockY
        return Pair(displayedX, displayedY)
    }

    fun applyLockOffset(current: TiltData, lockX: Float, lockY: Float): TiltData {
        val (displayedX, displayedY) = calculateRelativeTilt(current.xTilt, current.yTilt, lockX, lockY)
        val overall = sqrt((displayedX * displayedX + displayedY * displayedY).toDouble()).toFloat()
        return TiltData(
            xTilt = displayedX,
            yTilt = displayedY,
            overallTilt = overall
        )
    }

    fun calculateStatus(overallTilt: Float): String {
        val absTilt = abs(overallTilt)
        return when {
            absTilt < Constants.LEVEL_THRESHOLD -> "LEVEL"
            absTilt < Constants.SLIGHT_TILT_THRESHOLD -> "SLIGHTLY TILTED"
            else -> "TILTED"
        }
    }

    fun calculateStatus(tiltData: TiltData): String {
        return calculateStatus(tiltData.overallTilt)
    }
}
