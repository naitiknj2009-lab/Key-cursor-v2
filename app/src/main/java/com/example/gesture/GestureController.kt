package com.example.gesture

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.graphics.Path
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log

/**
 * Executes system-wide accessibility gestures (click, long-press/right-click, drag).
 * Dispatches real touch events to Android using AccessibilityService.dispatchGesture().
 */
class GestureController(private val service: AccessibilityService) {

    private val vibrator: Vibrator? = try {
        service.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    } catch (_: Exception) {
        null
    }

    /**
     * Dispatches a single tap (Left-Click) at the specified screen coordinates.
     */
    fun performClick(
        x: Float,
        y: Float,
        haptic: Boolean = true,
        onComplete: (() -> Unit)? = null,
        onCancelled: (() -> Unit)? = null
    ) {
        triggerHaptic(haptic, 30)

        val path = Path().apply {
            moveTo(x, y)
        }
        val stroke = GestureDescription.StrokeDescription(path, 0, 40)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()

        service.dispatchGesture(gesture, object : AccessibilityService.GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                super.onCompleted(gestureDescription)
                onComplete?.invoke()
            }

            override fun onCancelled(gestureDescription: GestureDescription?) {
                super.onCancelled(gestureDescription)
                Log.w(TAG, "Left-click gesture cancelled at ($x, $y)")
                onCancelled?.invoke()
            }
        }, null)
    }

    /**
     * Dispatches a long-press (Right-Click equivalent) at the specified screen coordinates.
     * Brings up context menus, text selection handles, or app options.
     */
    fun performRightClick(
        x: Float,
        y: Float,
        haptic: Boolean = true,
        onComplete: (() -> Unit)? = null,
        onCancelled: (() -> Unit)? = null
    ) {
        triggerHaptic(haptic, 75)

        val path = Path().apply {
            moveTo(x, y)
        }
        val stroke = GestureDescription.StrokeDescription(path, 0, 600)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()

        service.dispatchGesture(gesture, object : AccessibilityService.GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                super.onCompleted(gestureDescription)
                onComplete?.invoke()
            }

            override fun onCancelled(gestureDescription: GestureDescription?) {
                super.onCancelled(gestureDescription)
                Log.w(TAG, "Right-click gesture cancelled at ($x, $y)")
                onCancelled?.invoke()
            }
        }, null)
    }

    /**
     * Dispatches a drag gesture from start coordinates to end coordinates.
     */
    fun performDrag(
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float,
        haptic: Boolean = true,
        onComplete: (() -> Unit)? = null,
        onCancelled: (() -> Unit)? = null
    ) {
        triggerHaptic(haptic, 45)

        val path = Path().apply {
            moveTo(startX, startY)
            lineTo(endX, endY)
        }
        val duration = 350L
        val stroke = GestureDescription.StrokeDescription(path, 0, duration)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()

        service.dispatchGesture(gesture, object : AccessibilityService.GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                super.onCompleted(gestureDescription)
                onComplete?.invoke()
            }

            override fun onCancelled(gestureDescription: GestureDescription?) {
                super.onCancelled(gestureDescription)
                Log.w(TAG, "Drag gesture cancelled from ($startX, $startY) to ($endX, $endY)")
                onCancelled?.invoke()
            }
        }, null)
    }

    private fun triggerHaptic(enabled: Boolean, millis: Long) {
        if (!enabled || vibrator == null || !vibrator.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(millis, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(millis)
            }
        } catch (_: Exception) {
            // Safe fallback if vibration permission or device unavailable
        }
    }

    companion object {
        private const val TAG = "GestureController"
    }
}
