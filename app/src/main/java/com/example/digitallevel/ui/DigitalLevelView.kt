package com.example.digitallevel.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

class DigitalLevelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var currentXTilt = 0f
    private var currentYTilt = 0f

    // Define the maximum angle (in degrees) represented by the edge of the level
    private val maxTiltDegrees = 45f

    private val outerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.DKGRAY
        style = Paint.Style.STROKE
        strokeWidth = 10f
    }

    private val centerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }

    private val bubblePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GREEN
        style = Paint.Style.FILL
    }

    fun updateTilt(xAngle: Float, yAngle: Float) {
        currentXTilt = xAngle.coerceIn(-maxTiltDegrees, maxTiltDegrees)
        currentYTilt = yAngle.coerceIn(-maxTiltDegrees, maxTiltDegrees)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cx = width / 2f
        val cy = height / 2f

        // Radius for the outer boundary
        val radius = min(cx, cy) * 0.9f
        val bubbleRadius = radius * 0.15f
        // Max distance the center of the bubble can travel
        val innerBoundaryRadius = radius - bubbleRadius

        // Draw outer boundary
        canvas.drawCircle(cx, cy, radius, outerPaint)
        
        // Draw center reference crosshair / target circle
        canvas.drawCircle(cx, cy, bubbleRadius, centerPaint)
        canvas.drawLine(cx - bubbleRadius - 20f, cy, cx + bubbleRadius + 20f, cy, centerPaint)
        canvas.drawLine(cx, cy - bubbleRadius - 20f, cx, cy + bubbleRadius + 20f, centerPaint)

        // Calculate bubble position based on tilt.
        // Tilting left (-xTilt) usually moves bubble right (+x), so we invert it for realistic bubble level behavior.
        // Let's assume standard bubble level: tilting right (positive roll) moves bubble left (negative x).
        // For simplicity: offset is directly proportional to tilt angle.
        val xOffset = -(currentXTilt / maxTiltDegrees) * innerBoundaryRadius
        val yOffset = (currentYTilt / maxTiltDegrees) * innerBoundaryRadius
        
        // Constrain bubble within the circular boundary
        val distance = Math.hypot(xOffset.toDouble(), yOffset.toDouble()).toFloat()
        val finalXOffset: Float
        val finalYOffset: Float
        if (distance > innerBoundaryRadius) {
            val scale = innerBoundaryRadius / distance
            finalXOffset = xOffset * scale
            finalYOffset = yOffset * scale
        } else {
            finalXOffset = xOffset
            finalYOffset = yOffset
        }

        // Draw the bubble
        canvas.drawCircle(cx + finalXOffset, cy + finalYOffset, bubbleRadius, bubblePaint)
    }
}
