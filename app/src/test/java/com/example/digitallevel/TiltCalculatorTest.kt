package com.example.digitallevel

import com.example.digitallevel.sensor.MeasurementMode
import com.example.digitallevel.sensor.TiltCalculator
import com.example.digitallevel.sensor.TiltData
import com.example.digitallevel.util.Constants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.cos
import kotlin.math.sin

class TiltCalculatorTest {

    @Test
    fun testRestingFlatPosition() {
        val calculator = TiltCalculator(alpha = 0f) // Immediate response
        val result = calculator.process(floatArrayOf(0f, 0f, 9.81f), MeasurementMode.FLAT)

        assertEquals(0f, result.xTilt, 0.1f)
        assertEquals(0f, result.yTilt, 0.1f)
        assertEquals(0f, result.overallTilt, 0.1f)
    }

    @Test
    fun testEdgeModeUprightVertical() {
        val calculator = TiltCalculator(alpha = 0f)
        // Device standing upright vertically (gravity along Y-axis)
        val result = calculator.process(floatArrayOf(0f, 9.81f, 0f), MeasurementMode.EDGE)

        assertEquals(0f, result.xTilt, 0.1f)
        assertEquals(0f, result.yTilt, 0.1f)
        assertEquals(0f, result.overallTilt, 0.1f)
    }

    @Test
    fun testEdgeModeTilted() {
        val calculator = TiltCalculator(alpha = 0f)
        val gravity = 9.81f
        val rad30 = Math.toRadians(30.0)
        val zVal = (gravity * sin(rad30)).toFloat()
        val yVal = (gravity * cos(rad30)).toFloat()

        // Device tilted 30 degrees in EDGE mode
        val result = calculator.process(floatArrayOf(0f, yVal, zVal), MeasurementMode.EDGE)

        assertEquals(0f, result.xTilt, 0.1f)
        assertEquals(30f, result.yTilt, 0.1f)
        assertEquals(30f, result.overallTilt, 0.1f)
    }

    @Test
    fun testLockOffsetMath() {
        val calculator = TiltCalculator(alpha = 0f)
        val (dispX, dispY) = calculator.calculateRelativeTilt(currentX = 15f, currentY = 10f, lockX = 5f, lockY = 2f)

        assertEquals(10f, dispX, 0.001f)
        assertEquals(8f, dispY, 0.001f)

        val currentData = TiltData(xTilt = 15f, yTilt = 10f, overallTilt = 18.02f)
        val lockedData = calculator.applyLockOffset(currentData, lockX = 5f, lockY = 2f)

        assertEquals(10f, lockedData.xTilt, 0.001f)
        assertEquals(8f, lockedData.yTilt, 0.001f)
    }

    @Test
    fun testPureRollXAxis() {
        val calculator = TiltCalculator(alpha = 0f)
        // Device tilted 90 degrees on X axis
        val result = calculator.process(floatArrayOf(9.81f, 0f, 0f))

        assertEquals(90f, result.xTilt, 0.1f)
        assertEquals(0f, result.yTilt, 0.1f)
        assertEquals(90f, result.overallTilt, 0.1f)
    }

    @Test
    fun testPurePitchYAxis() {
        val calculator = TiltCalculator(alpha = 0f)
        // Device tilted 90 degrees on Y axis
        val result = calculator.process(floatArrayOf(0f, 9.81f, 0f))

        assertEquals(0f, result.xTilt, 0.1f)
        assertEquals(90f, result.yTilt, 0.1f)
        assertEquals(90f, result.overallTilt, 0.1f)
    }

    @Test
    fun testNegativeRollAndPitch() {
        val calculator = TiltCalculator(alpha = 0f)

        val rollLeft = calculator.process(floatArrayOf(-9.81f, 0f, 0f))
        assertEquals(-90f, rollLeft.xTilt, 0.1f)
        assertEquals(0f, rollLeft.yTilt, 0.1f)
        assertEquals(90f, rollLeft.overallTilt, 0.1f)

        val pitchBackward = calculator.process(floatArrayOf(0f, -9.81f, 0f))
        assertEquals(0f, pitchBackward.xTilt, 0.1f)
        assertEquals(-90f, pitchBackward.yTilt, 0.1f)
        assertEquals(90f, pitchBackward.overallTilt, 0.1f)
    }

    @Test
    fun test45DegreeTilt() {
        val calculator = TiltCalculator(alpha = 0f)
        val gravity = 9.81f
        val rad45 = Math.toRadians(45.0)
        val yVal = (gravity * sin(rad45)).toFloat()
        val zVal = (gravity * cos(rad45)).toFloat()

        val result = calculator.process(floatArrayOf(0f, yVal, zVal))

        assertEquals(0f, result.xTilt, 0.1f)
        assertEquals(45f, result.yTilt, 0.1f)
        assertEquals(45f, result.overallTilt, 0.1f)
    }

    @Test
    fun testZeroAccelerationHandling() {
        val calculator = TiltCalculator(alpha = 0f)
        val result = calculator.process(floatArrayOf(0f, 0f, 0f))

        assertFalse("xTilt should not be NaN", result.xTilt.isNaN())
        assertFalse("yTilt should not be NaN", result.yTilt.isNaN())
        assertFalse("overallTilt should not be NaN", result.overallTilt.isNaN())
        assertEquals(0f, result.overallTilt, 0.001f)
    }

    @Test
    fun testLowPassFilterSmoothing() {
        val calculator = TiltCalculator(alpha = 0.8f)

        val step1 = calculator.process(floatArrayOf(10.0f, 0f, 9.81f))
        assertTrue("Step 1 xTilt should smooth response", step1.xTilt > 0f && step1.xTilt < 90f)

        val step2 = calculator.process(floatArrayOf(10.0f, 0f, 9.81f))
        assertTrue("Step 2 xTilt should increase towards target", step2.xTilt > step1.xTilt)

        val step3 = calculator.process(floatArrayOf(10.0f, 0f, 9.81f))
        assertTrue("Step 3 xTilt should continue increasing", step3.xTilt > step2.xTilt)
    }

    @Test
    fun testLowPassFilterAlphaZeroNoSmoothing() {
        val calculator = TiltCalculator(alpha = 0f)
        val result = calculator.process(floatArrayOf(9.81f, 0f, 0f))
        assertEquals(90f, result.xTilt, 0.1f)
    }

    @Test
    fun testLowPassFilterAlphaOneFullRetention() {
        val calculator = TiltCalculator(alpha = 1.0f)
        val result = calculator.process(floatArrayOf(9.81f, 9.81f, 0f))
        assertEquals(0f, result.xTilt, 0.1f)
        assertEquals(0f, result.yTilt, 0.1f)
        assertEquals(0f, result.overallTilt, 0.1f)
    }

    @Test
    fun testStatusCalculations() {
        val calculator = TiltCalculator()

        assertEquals("LEVEL", calculator.calculateStatus(0.0f))
        assertEquals("LEVEL", calculator.calculateStatus(0.5f))
        assertEquals("LEVEL", calculator.calculateStatus(0.99f))

        assertEquals("SLIGHTLY TILTED", calculator.calculateStatus(1.0f))
        assertEquals("SLIGHTLY TILTED", calculator.calculateStatus(2.5f))
        assertEquals("SLIGHTLY TILTED", calculator.calculateStatus(4.99f))

        assertEquals("TILTED", calculator.calculateStatus(5.0f))
        assertEquals("TILTED", calculator.calculateStatus(10.0f))
    }

    @Test
    fun testStatusCalculationsWithTiltData() {
        val calculator = TiltCalculator()

        val levelData = TiltData(xTilt = 0.2f, yTilt = 0.2f, overallTilt = 0.28f)
        assertEquals("LEVEL", calculator.calculateStatus(levelData))

        val slightlyTiltedData = TiltData(xTilt = 2.0f, yTilt = 1.0f, overallTilt = 2.23f)
        assertEquals("SLIGHTLY TILTED", calculator.calculateStatus(slightlyTiltedData))

        val tiltedData = TiltData(xTilt = 5.0f, yTilt = 3.0f, overallTilt = 5.83f)
        assertEquals("TILTED", calculator.calculateStatus(tiltedData))
    }

    @Test
    fun testConstantsThresholds() {
        assertEquals(1.0f, Constants.LEVEL_THRESHOLD, 0.001f)
        assertEquals(5.0f, Constants.SLIGHT_TILT_THRESHOLD, 0.001f)
    }
}
