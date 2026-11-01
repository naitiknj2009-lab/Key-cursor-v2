package com.example.overlay

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.view.View
import android.view.animation.DecelerateInterpolator
import com.example.model.CursorSettings
import java.util.ArrayDeque

/**
 * High-performance, hardware-accelerated custom overlay View for rendering
 * the system mouse cursor, animations, ripples, and drag telemetry.
 */
class CursorOverlayView(context: Context) : View(context) {

    var cursorX: Float = 300f
        private set
    var cursorY: Float = 300f
        private set

    var isCursorActive: Boolean = true
        private set
    var isDragging: Boolean = false
        private set
    var dragStartX: Float = 0f
        private set
    var dragStartY: Float = 0f
        private set

    private var settings: CursorSettings = CursorSettings()

    // Paints
    private val arrowFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#00E5FF")
    }

    private val arrowOutlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
        color = Color.parseColor("#E0000000")
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }

    private val arrowShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#50000000")
    }

    private val clickRipplePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
        color = Color.parseColor("#00E5FF")
    }

    private val dragLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
        color = Color.parseColor("#80FFAB00")
    }

    private val dragAnchorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#FFAB00")
    }

    private val badgeBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#CC0D1322")
    }

    private val badgeBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
        color = Color.parseColor("#8000E5FF")
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 28f
        isFakeBoldText = true
    }

    private val trailPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    // Ripple animation state
    private var rippleRadius = 0f
    private var rippleAlpha = 0
    private var isRightClickRipple = false
    private var rippleAnimator: ValueAnimator? = null

    // Motion trail queue (holds up to 4 previous positions)
    private val trailQueue = ArrayDeque<Pair<Float, Float>>(5)

    init {
        // Hardware acceleration enabled by default on custom View
        setLayerType(LAYER_TYPE_HARDWARE, null)
    }

    fun updateSettings(newSettings: CursorSettings) {
        this.settings = newSettings
        val colorInt = newSettings.currentHexColor.toInt()
        arrowFillPaint.color = colorInt
        clickRipplePaint.color = colorInt
        badgeBorderPaint.color = colorInt
        invalidate()
    }

    fun setPosition(x: Float, y: Float) {
        if (settings.trailEnabled && (x != cursorX || y != cursorY)) {
            if (trailQueue.size >= 4) {
                trailQueue.pollFirst()
            }
            trailQueue.offerLast(Pair(cursorX, cursorY))
        }
        cursorX = x
        cursorY = y
        invalidate()
    }

    fun setActiveState(active: Boolean) {
        isCursorActive = active
        invalidate()
    }

    fun startDrag(startX: Float, startY: Float) {
        isDragging = true
        dragStartX = startX
        dragStartY = startY
        invalidate()
    }

    fun endDrag() {
        isDragging = false
        invalidate()
    }

    fun triggerClickAnimation(isRightClick: Boolean = false) {
        if (!settings.clickFeedback) return

        rippleAnimator?.cancel()
        isRightClickRipple = isRightClick
        val maxRadius = (settings.cursorSizeDp * 2.2f).coerceAtLeast(60f)

        rippleAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = if (isRightClick) 400 else 250
            interpolator = DecelerateInterpolator()
            addUpdateListener { animator ->
                val progress = animator.animatedValue as Float
                rippleRadius = progress * maxRadius
                rippleAlpha = ((1f - progress) * 220).toInt()
                invalidate()
            }
            start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (!settings.enabled) return

        // 1. Draw Drag connection line and anchor if dragging
        if (isDragging) {
            canvas.drawLine(dragStartX, dragStartY, cursorX, cursorY, dragLinePaint)
            canvas.drawCircle(dragStartX, dragStartY, 8f, dragAnchorPaint)
        }

        // 2. Draw Motion trail
        if (settings.trailEnabled && trailQueue.isNotEmpty()) {
            val baseColor = settings.currentHexColor.toInt()
            var step = 1
            for ((tx, ty) in trailQueue) {
                val alpha = (step * 35).coerceIn(10, 140)
                trailPaint.color = (baseColor and 0x00FFFFFF) or (alpha shl 24)
                val dotRadius = (settings.cursorSizeDp * 0.12f * step).coerceAtLeast(3f)
                canvas.drawCircle(tx + 4f, ty + 4f, dotRadius, trailPaint)
                step++
            }
        }

        // 3. Draw Click Ripple
        if (rippleAlpha > 0) {
            clickRipplePaint.alpha = rippleAlpha
            if (isRightClickRipple) {
                clickRipplePaint.color = Color.parseColor("#FFAB00")
                canvas.drawCircle(cursorX, cursorY, rippleRadius, clickRipplePaint)
                clickRipplePaint.color = Color.parseColor("#A855F7")
                canvas.drawCircle(cursorX, cursorY, (rippleRadius * 0.65f), clickRipplePaint)
            } else {
                clickRipplePaint.color = settings.currentHexColor.toInt()
                canvas.drawCircle(cursorX, cursorY, rippleRadius, clickRipplePaint)
            }
        }

        // 4. Render Mouse Arrow Cursor
        val scale = settings.cursorSizeDp / 24f
        val opacity = if (isCursorActive) 1f else 0.45f
        arrowFillPaint.alpha = (opacity * 255).toInt()
        arrowOutlinePaint.alpha = (opacity * 240).toInt()
        arrowShadowPaint.alpha = (opacity * 70).toInt()

        canvas.save()
        canvas.translate(cursorX, cursorY)

        // Draw drop shadow
        val shadowPath = buildArrowPath(scale)
        canvas.save()
        canvas.translate(3f, 3f)
        canvas.drawPath(shadowPath, arrowShadowPaint)
        canvas.restore()

        // Draw arrow fill and crisp outline
        val arrowPath = buildArrowPath(scale)
        canvas.drawPath(arrowPath, arrowFillPaint)
        canvas.drawPath(arrowPath, arrowOutlinePaint)

        canvas.restore()

        // 5. Draw status badge if dragging or dormant
        if (isDragging) {
            drawStatusBadge(canvas, cursorX + (settings.cursorSizeDp * 0.9f), cursorY + 10f, "DRAG", Color.parseColor("#FFAB00"))
        } else if (!isCursorActive) {
            drawStatusBadge(canvas, cursorX + (settings.cursorSizeDp * 0.9f), cursorY + 10f, "PAUSED", Color.parseColor("#94A3B8"))
        }
    }

    private fun drawStatusBadge(canvas: Canvas, x: Float, y: Float, text: String, accentColor: Int) {
        val textWidth = textPaint.measureText(text)
        val padding = 14f
        val rect = RectF(x, y, x + textWidth + (padding * 2), y + 42f)

        badgeBorderPaint.color = accentColor
        canvas.drawRoundRect(rect, 10f, 10f, badgeBgPaint)
        canvas.drawRoundRect(rect, 10f, 10f, badgeBorderPaint)
        textPaint.color = accentColor
        canvas.drawText(text, x + padding, y + 30f, textPaint)
    }

    /**
     * Builds standard angled mouse arrow cursor path scaled from base (24x24).
     */
    private fun buildArrowPath(scale: Float): Path {
        val path = Path()
        // Coordinates for classic ergonomic pointer arrow
        path.moveTo(0f * scale, 0f * scale)
        path.lineTo(0f * scale, 21f * scale)
        path.lineTo(5.5f * scale, 16.5f * scale)
        path.lineTo(9.5f * scale, 24.5f * scale)
        path.lineTo(13.5f * scale, 22.5f * scale)
        path.lineTo(9.5f * scale, 14.5f * scale)
        path.lineTo(16.5f * scale, 14.5f * scale)
        path.close()
        return path
    }
}
