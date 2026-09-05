package com.example.digitallevel.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import com.example.digitallevel.util.Constants
import kotlin.math.hypot
import kotlin.math.min

class DigitalLevelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var currentXTilt = 0f
    private var currentYTilt = 0f
    private var isHoldActive = false
    private var isLockActive = false

    private val maxTiltDegrees = 45f

    // Dimensions calculated in calculateDimensions
    private val housingRect = RectF()
    private var housingCornerRadius = 0f

    private val topTubeRect = RectF()
    private var topTubeRadius = 0f
    private var topTubeCenterX = 0f
    private var topTubeCenterY = 0f
    private var topTubeBubbleRadius = 0f
    private var topTubeMaxTravel = 0f

    private val leftTubeRect = RectF()
    private var leftTubeRadius = 0f
    private var leftTubeCenterX = 0f
    private var leftTubeCenterY = 0f
    private var leftTubeBubbleRadius = 0f
    private var leftTubeMaxTravel = 0f

    private var bullsEyeCenterX = 0f
    private var bullsEyeCenterY = 0f
    private var bullsEyeOuterRadius = 0f
    private var bullsEyeInnerRadius = 0f
    private var bullsEyeBubbleRadius = 0f
    private var bullsEyeMaxTravel = 0f
    private var bullsEyeTargetRadius = 0f
    private var bullsEyeMiddleRadius = 0f
    private var bullsEyeOuterRefRadius = 0f

    private val badgeRect = RectF()

    // Shaders
    private var housingShader: Shader? = null
    private var housingBorderShader: Shader? = null
    private var topGlassShader: Shader? = null
    private var leftGlassShader: Shader? = null
    private var bullsEyeGlassShader: Shader? = null
    private var bullsEyeRingShader: Shader? = null

    // Paints
    private val housingPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val housingBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }

    private val vialBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val vialBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
        color = Color.parseColor("#33000000")
    }

    private val glassHighlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val bullsEyeRingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val targetLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1C1E20")
        style = Paint.Style.STROKE
        strokeWidth = 2.5f
    }

    private val dashedCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#26000000")
        style = Paint.Style.STROKE
        strokeWidth = 2f
        pathEffect = DashPathEffect(floatArrayOf(8f, 6f), 0f)
    }

    private val bubbleFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val bubbleBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2.5f
        color = Color.parseColor("#003366")
    }

    private val bubbleHighlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.argb(220, 255, 255, 255)
    }

    private val badgeBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val badgeTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 28f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    private val overlayTintPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    fun updateTilt(xAngle: Float, yAngle: Float, isHold: Boolean = false, isLock: Boolean = false) {
        currentXTilt = xAngle.coerceIn(-maxTiltDegrees, maxTiltDegrees)
        currentYTilt = yAngle.coerceIn(-maxTiltDegrees, maxTiltDegrees)
        isHoldActive = isHold
        isLockActive = isLock
        invalidate()
    }

    fun setHoldState(hold: Boolean) {
        isHoldActive = hold
        invalidate()
    }

    fun setLockState(lock: Boolean) {
        isLockActive = lock
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val defaultSize = (280 * resources.displayMetrics.density).toInt()
        val width = resolveSize(defaultSize, widthMeasureSpec)
        val height = resolveSize(defaultSize, heightMeasureSpec)
        val size = min(width, height)
        setMeasuredDimension(size, size)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        calculateDimensions(w, h)
    }

    private fun calculateDimensions(w: Int, h: Int) {
        if (w <= 0 || h <= 0) return

        val minDim = min(w, h).toFloat()
        val pad = minDim * 0.04f

        housingRect.set(pad, pad, w - pad, h - pad)
        housingCornerRadius = minDim * 0.08f

        housingShader = LinearGradient(
            pad, pad, w - pad, h - pad,
            intArrayOf(
                Color.parseColor("#2C2F36"),
                Color.parseColor("#1C1E23"),
                Color.parseColor("#121316")
            ),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        housingPaint.shader = housingShader

        housingBorderShader = LinearGradient(
            pad, pad, w - pad, h - pad,
            intArrayOf(
                Color.parseColor("#5A5F6D"),
                Color.parseColor("#2E3138"),
                Color.parseColor("#6E7484"),
                Color.parseColor("#222429")
            ),
            floatArrayOf(0f, 0.35f, 0.7f, 1f),
            Shader.TileMode.CLAMP
        )
        housingBorderPaint.shader = housingBorderShader

        val tubeThickness = minDim * 0.12f
        val gap = minDim * 0.03f

        // Top Horizontal Tube
        val topTubeLeft = pad + tubeThickness + gap
        val topTubeRight = w - pad - gap
        val topTubeTop = pad + gap
        val topTubeBottom = topTubeTop + tubeThickness

        topTubeRect.set(topTubeLeft, topTubeTop, topTubeRight, topTubeBottom)
        topTubeRadius = tubeThickness / 2f
        topTubeCenterX = (topTubeLeft + topTubeRight) / 2f
        topTubeCenterY = (topTubeTop + topTubeBottom) / 2f
        topTubeBubbleRadius = tubeThickness * 0.38f
        topTubeMaxTravel = (topTubeRight - topTubeLeft) / 2f - topTubeBubbleRadius - 4f

        topGlassShader = LinearGradient(
            0f, topTubeTop, 0f, topTubeBottom,
            intArrayOf(
                Color.argb(180, 255, 255, 255),
                Color.argb(40, 255, 255, 255),
                Color.argb(0, 255, 255, 255),
                Color.argb(80, 255, 255, 255)
            ),
            floatArrayOf(0f, 0.35f, 0.7f, 1f),
            Shader.TileMode.CLAMP
        )

        // Left Vertical Tube
        val leftTubeLeft = pad + gap
        val leftTubeRight = leftTubeLeft + tubeThickness
        val leftTubeTop = pad + tubeThickness + gap
        val leftTubeBottom = h - pad - gap

        leftTubeRect.set(leftTubeLeft, leftTubeTop, leftTubeRight, leftTubeBottom)
        leftTubeRadius = tubeThickness / 2f
        leftTubeCenterX = (leftTubeLeft + leftTubeRight) / 2f
        leftTubeCenterY = (leftTubeTop + leftTubeBottom) / 2f
        leftTubeBubbleRadius = tubeThickness * 0.38f
        leftTubeMaxTravel = (leftTubeBottom - leftTubeTop) / 2f - leftTubeBubbleRadius - 4f

        leftGlassShader = LinearGradient(
            leftTubeLeft, 0f, leftTubeRight, 0f,
            intArrayOf(
                Color.argb(180, 255, 255, 255),
                Color.argb(40, 255, 255, 255),
                Color.argb(0, 255, 255, 255),
                Color.argb(80, 255, 255, 255)
            ),
            floatArrayOf(0f, 0.35f, 0.7f, 1f),
            Shader.TileMode.CLAMP
        )

        // Center Circular Bull's Eye
        val bullsEyeLeft = leftTubeRight + gap
        val bullsEyeTop = topTubeBottom + gap
        val bullsEyeRight = w - pad - gap
        val bullsEyeBottom = h - pad - gap

        bullsEyeCenterX = (bullsEyeLeft + bullsEyeRight) / 2f
        bullsEyeCenterY = (bullsEyeTop + bullsEyeBottom) / 2f
        bullsEyeOuterRadius = min(bullsEyeRight - bullsEyeLeft, bullsEyeBottom - bullsEyeTop) / 2f
        bullsEyeInnerRadius = bullsEyeOuterRadius * 0.86f
        bullsEyeBubbleRadius = bullsEyeInnerRadius * 0.18f
        bullsEyeMaxTravel = bullsEyeInnerRadius - bullsEyeBubbleRadius

        bullsEyeTargetRadius = bullsEyeInnerRadius * 0.22f
        bullsEyeMiddleRadius = bullsEyeInnerRadius * 0.52f
        bullsEyeOuterRefRadius = bullsEyeInnerRadius * 0.82f

        bullsEyeRingShader = RadialGradient(
            bullsEyeCenterX, bullsEyeCenterY, bullsEyeOuterRadius,
            intArrayOf(
                Color.parseColor("#151618"),
                Color.parseColor("#383B42"),
                Color.parseColor("#1E2024"),
                Color.parseColor("#4A4E58")
            ),
            floatArrayOf(0f, 0.82f, 0.92f, 1f),
            Shader.TileMode.CLAMP
        )
        bullsEyeRingPaint.shader = bullsEyeRingShader

        bullsEyeGlassShader = RadialGradient(
            bullsEyeCenterX - bullsEyeInnerRadius * 0.3f,
            bullsEyeCenterY - bullsEyeInnerRadius * 0.3f,
            bullsEyeInnerRadius * 1.2f,
            intArrayOf(
                Color.argb(160, 255, 255, 255),
                Color.argb(30, 255, 255, 255),
                Color.argb(0, 255, 255, 255)
            ),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )

        // Badge placement
        val badgeW = minDim * 0.22f
        val badgeH = minDim * 0.08f
        val badgeX = topTubeRight - badgeW
        val badgeY = topTubeTop + gap
        badgeRect.set(badgeX, badgeY, badgeX + badgeW, badgeY + badgeH)
        badgeTextPaint.textSize = minDim * 0.042f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (housingRect.width() <= 0f) {
            if (width > 0 && height > 0) {
                calculateDimensions(width, height)
            } else {
                return
            }
        }

        val overallTilt = hypot(currentXTilt.toDouble(), currentYTilt.toDouble()).toFloat()
        val isLevel = overallTilt < Constants.LEVEL_THRESHOLD

        // Determine fluid color based on state
        val fluidColor = when {
            isHoldActive -> Color.parseColor("#FF9800")
            isLockActive -> Color.parseColor("#9C27B0")
            isLevel -> Color.parseColor("#00B0FF")
            else -> Color.parseColor("#0088FF")
        }

        // Determine bubble fill color based on state
        val bubbleColor = when {
            isHoldActive -> Color.parseColor("#FFE0B2")
            isLockActive -> Color.parseColor("#E1BEE7")
            isLevel -> Color.parseColor("#B3E5FC")
            else -> Color.parseColor("#80D8FF")
        }

        // 1. Draw metallic housing base plate
        canvas.drawRoundRect(housingRect, housingCornerRadius, housingCornerRadius, housingPaint)
        canvas.drawRoundRect(housingRect, housingCornerRadius, housingCornerRadius, housingBorderPaint)

        // 2. Draw Top Horizontal Tube
        vialBgPaint.color = fluidColor
        canvas.drawRoundRect(topTubeRect, topTubeRadius, topTubeRadius, vialBgPaint)

        // Top tube glass highlight layer
        glassHighlightPaint.shader = topGlassShader
        canvas.drawRoundRect(topTubeRect, topTubeRadius, topTubeRadius, glassHighlightPaint)
        canvas.drawRoundRect(topTubeRect, topTubeRadius, topTubeRadius, vialBorderPaint)

        // Top tube target lines (two fine vertical black tick lines)
        val topTargetOffset = topTubeBubbleRadius * 1.05f
        canvas.drawLine(
            topTubeCenterX - topTargetOffset, topTubeRect.top + 2f,
            topTubeCenterX - topTargetOffset, topTubeRect.bottom - 2f,
            targetLinePaint
        )
        canvas.drawLine(
            topTubeCenterX + topTargetOffset, topTubeRect.top + 2f,
            topTubeCenterX + topTargetOffset, topTubeRect.bottom - 2f,
            targetLinePaint
        )

        // Top tube bubble position (moves horizontally mapped to yAngle)
        val yNorm = (currentYTilt / maxTiltDegrees).coerceIn(-1f, 1f)
        val topBubbleX = topTubeCenterX - yNorm * topTubeMaxTravel
        val topBubbleY = topTubeCenterY

        bubbleFillPaint.color = bubbleColor
        canvas.drawCircle(topBubbleX, topBubbleY, topTubeBubbleRadius, bubbleFillPaint)
        canvas.drawCircle(topBubbleX, topBubbleY, topTubeBubbleRadius, bubbleBorderPaint)
        canvas.drawCircle(
            topBubbleX - topTubeBubbleRadius * 0.3f,
            topBubbleY - topTubeBubbleRadius * 0.3f,
            topTubeBubbleRadius * 0.32f,
            bubbleHighlightPaint
        )

        // 3. Draw Left Vertical Tube
        vialBgPaint.color = fluidColor
        canvas.drawRoundRect(leftTubeRect, leftTubeRadius, leftTubeRadius, vialBgPaint)

        // Left tube glass highlight layer
        glassHighlightPaint.shader = leftGlassShader
        canvas.drawRoundRect(leftTubeRect, leftTubeRadius, leftTubeRadius, glassHighlightPaint)
        canvas.drawRoundRect(leftTubeRect, leftTubeRadius, leftTubeRadius, vialBorderPaint)

        // Left tube target lines (two fine horizontal black tick lines)
        val leftTargetOffset = leftTubeBubbleRadius * 1.05f
        canvas.drawLine(
            leftTubeRect.left + 2f, leftTubeCenterY - leftTargetOffset,
            leftTubeRect.right - 2f, leftTubeCenterY - leftTargetOffset,
            targetLinePaint
        )
        canvas.drawLine(
            leftTubeRect.left + 2f, leftTubeCenterY + leftTargetOffset,
            leftTubeRect.right - 2f, leftTubeCenterY + leftTargetOffset,
            targetLinePaint
        )

        // Left tube bubble position (moves vertically mapped to xAngle)
        val xNorm = (currentXTilt / maxTiltDegrees).coerceIn(-1f, 1f)
        val leftBubbleX = leftTubeCenterX
        val leftBubbleY = leftTubeCenterY - xNorm * leftTubeMaxTravel

        bubbleFillPaint.color = bubbleColor
        canvas.drawCircle(leftBubbleX, leftBubbleY, leftTubeBubbleRadius, bubbleFillPaint)
        canvas.drawCircle(leftBubbleX, leftBubbleY, leftTubeBubbleRadius, bubbleBorderPaint)
        canvas.drawCircle(
            leftBubbleX - leftTubeBubbleRadius * 0.3f,
            leftBubbleY - leftTubeBubbleRadius * 0.3f,
            leftTubeBubbleRadius * 0.32f,
            bubbleHighlightPaint
        )

        // 4. Draw Center Circular Bull's Eye
        // Metallic outer bezel ring
        canvas.drawCircle(bullsEyeCenterX, bullsEyeCenterY, bullsEyeOuterRadius, bullsEyeRingPaint)

        // Inner fluid circle
        vialBgPaint.color = fluidColor
        canvas.drawCircle(bullsEyeCenterX, bullsEyeCenterY, bullsEyeInnerRadius, vialBgPaint)

        // Circular glass highlight layer
        glassHighlightPaint.shader = bullsEyeGlassShader
        canvas.drawCircle(bullsEyeCenterX, bullsEyeCenterY, bullsEyeInnerRadius, glassHighlightPaint)
        canvas.drawCircle(bullsEyeCenterX, bullsEyeCenterY, bullsEyeInnerRadius, vialBorderPaint)

        // Bull's Eye target lines (crosshairs & concentric circles)
        // Crosshairs
        canvas.drawLine(
            bullsEyeCenterX - bullsEyeInnerRadius, bullsEyeCenterY,
            bullsEyeCenterX + bullsEyeInnerRadius, bullsEyeCenterY,
            targetLinePaint
        )
        canvas.drawLine(
            bullsEyeCenterX, bullsEyeCenterY - bullsEyeInnerRadius,
            bullsEyeCenterX, bullsEyeCenterY + bullsEyeInnerRadius,
            targetLinePaint
        )

        // Inner center target circle
        canvas.drawCircle(bullsEyeCenterX, bullsEyeCenterY, bullsEyeTargetRadius, targetLinePaint)

        // Concentric dashed reference circles
        canvas.drawCircle(bullsEyeCenterX, bullsEyeCenterY, bullsEyeMiddleRadius, dashedCirclePaint)
        canvas.drawCircle(bullsEyeCenterX, bullsEyeCenterY, bullsEyeOuterRefRadius, dashedCirclePaint)

        // Bull's eye bubble position (2D mapped to xAngle and yAngle)
        val rawXOffset = -(currentXTilt / maxTiltDegrees) * bullsEyeMaxTravel
        val rawYOffset = (currentYTilt / maxTiltDegrees) * bullsEyeMaxTravel
        val dist = hypot(rawXOffset.toDouble(), rawYOffset.toDouble()).toFloat()

        val finalXOffset: Float
        val finalYOffset: Float
        if (dist > bullsEyeMaxTravel && dist > 0f) {
            val scale = bullsEyeMaxTravel / dist
            finalXOffset = rawXOffset * scale
            finalYOffset = rawYOffset * scale
        } else {
            finalXOffset = rawXOffset
            finalYOffset = rawYOffset
        }

        val bullsEyeBubbleX = bullsEyeCenterX + finalXOffset
        val bullsEyeBubbleY = bullsEyeCenterY + finalYOffset

        bubbleFillPaint.color = bubbleColor
        canvas.drawCircle(bullsEyeBubbleX, bullsEyeBubbleY, bullsEyeBubbleRadius, bubbleFillPaint)
        canvas.drawCircle(bullsEyeBubbleX, bullsEyeBubbleY, bullsEyeBubbleRadius, bubbleBorderPaint)
        canvas.drawCircle(
            bullsEyeBubbleX - bullsEyeBubbleRadius * 0.3f,
            bullsEyeBubbleY - bullsEyeBubbleRadius * 0.3f,
            bullsEyeBubbleRadius * 0.32f,
            bubbleHighlightPaint
        )

        // 5. Visual Indicator Overlays (HOLD / LOCK)
        if (isHoldActive || isLockActive) {
            val badgeColor = if (isHoldActive) Color.parseColor("#FF9800") else Color.parseColor("#9C27B0")
            val badgeText = if (isHoldActive) "HOLD" else "LOCK"

            // Color shift tint overlay on housing
            overlayTintPaint.color = if (isHoldActive) Color.argb(30, 255, 152, 0) else Color.argb(30, 156, 39, 176)
            canvas.drawRoundRect(housingRect, housingCornerRadius, housingCornerRadius, overlayTintPaint)

            // Badge overlay
            badgeBgPaint.color = badgeColor
            canvas.drawRoundRect(badgeRect, 12f, 12f, badgeBgPaint)

            val textY = badgeRect.centerY() - (badgeTextPaint.descent() + badgeTextPaint.ascent()) / 2f
            canvas.drawText(badgeText, badgeRect.centerX(), textY, badgeTextPaint)
        }
    }
}
