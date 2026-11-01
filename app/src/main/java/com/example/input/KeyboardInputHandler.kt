package com.example.input

import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.KeyEvent
import com.example.controller.CursorController
import com.example.manager.SettingsManager
import com.example.model.CursorSettings

/**
 * Handles physical keyboard events intercepted by the AccessibilityService.
 * Implements smooth 60fps tick-based continuous movement, diagonal motion,
 * acceleration curves, shift multipliers, and action triggers.
 */
class KeyboardInputHandler(
    private val cursorController: CursorController,
    private val settingsManager: SettingsManager
) {

    private val mainHandler = Handler(Looper.getMainLooper())
    private val heldKeys = mutableSetOf<Int>()
    private var isShiftHeld = false
    private var holdStartTimeMs: Long = 0L
    private var isTickerRunning = false

    private val tickRunnable = object : Runnable {
        override fun run() {
            if (heldKeys.isEmpty()) {
                isTickerRunning = false
                return
            }

            processTickMovement()
            // Schedule next frame (~60 FPS = 16ms)
            mainHandler.postDelayed(this, 16L)
        }
    }

    /**
     * Intercepts key events from AccessibilityService.onKeyEvent().
     * Returns true to consume event, false to pass through to the foreground application.
     */
    fun onKeyEvent(event: KeyEvent): Boolean {
        val settings = settingsManager.getSettings()

        // If KeyCursor master switch is disabled, pass everything through
        if (!settings.enabled) {
            clearHeldKeys()
            return false
        }

        val keyCode = event.keyCode

        // 1. Handle Escape key to toggle Active / Dormant mode
        if (keyCode == KeyEvent.KEYCODE_ESCAPE) {
            if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
                cursorController.toggleActiveMode()
            }
            return true
        }

        // 2. If dormant/paused, allow all other keys to pass through for normal typing
        if (!settings.activeMode) {
            clearHeldKeys()
            return false
        }

        // Track shift state
        isShiftHeld = event.isShiftPressed

        val isArrowKey = isArrowKeyCode(keyCode)
        val isEnterKey = (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER)
        val isSpaceKey = (keyCode == KeyEvent.KEYCODE_SPACE)

        if (event.action == KeyEvent.ACTION_DOWN) {
            if (isArrowKey) {
                if (!heldKeys.contains(keyCode)) {
                    if (heldKeys.isEmpty()) {
                        holdStartTimeMs = SystemClock.uptimeMillis()
                    }
                    heldKeys.add(keyCode)
                    startTickerIfNeeded()
                }
                return true
            }

            if (isEnterKey) {
                // Avoid repeated clicks on long key press
                if (event.repeatCount == 0) {
                    if (isShiftHeld) {
                        cursorController.triggerRightClick()
                    } else {
                        cursorController.triggerLeftClick()
                    }
                }
                return true
            }

            if (isSpaceKey) {
                if (event.repeatCount == 0) {
                    cursorController.toggleDragMode()
                }
                return true
            }
        } else if (event.action == KeyEvent.ACTION_UP) {
            if (isArrowKey) {
                heldKeys.remove(keyCode)
                if (heldKeys.isEmpty()) {
                    holdStartTimeMs = 0L
                    stopTicker()
                }
                return true
            }

            if (isEnterKey || isSpaceKey) {
                return true
            }
        }

        return false
    }

    private fun processTickMovement() {
        val settings = settingsManager.getSettings()
        var vx = 0f
        var vy = 0f

        if (heldKeys.contains(KeyEvent.KEYCODE_DPAD_UP)) vy -= 1f
        if (heldKeys.contains(KeyEvent.KEYCODE_DPAD_DOWN)) vy += 1f
        if (heldKeys.contains(KeyEvent.KEYCODE_DPAD_LEFT)) vx -= 1f
        if (heldKeys.contains(KeyEvent.KEYCODE_DPAD_RIGHT)) vx += 1f

        if (vx == 0f && vy == 0f) return

        // Normalize diagonal speed
        if (vx != 0f && vy != 0f) {
            val length = 1.41421356f
            vx /= length
            vy /= length
        }

        // Acceleration calculation based on duration held
        val holdDuration = SystemClock.uptimeMillis() - holdStartTimeMs
        val progress = (holdDuration / 650f).coerceIn(0f, 1f)
        val accel = 1f + (settings.acceleration - 1f) * progress

        // Shift multiplier
        val multiplier = if (isShiftHeld) settings.fastMultiplier else 1f

        val step = settings.movementSpeed * accel * multiplier
        cursorController.moveCursor(vx * step, vy * step)
    }

    private fun startTickerIfNeeded() {
        if (!isTickerRunning) {
            isTickerRunning = true
            mainHandler.post(tickRunnable)
        }
    }

    private fun stopTicker() {
        isTickerRunning = false
        mainHandler.removeCallbacks(tickRunnable)
    }

    fun clearHeldKeys() {
        heldKeys.clear()
        stopTicker()
    }

    private fun isArrowKeyCode(keyCode: Int): Boolean {
        return keyCode == KeyEvent.KEYCODE_DPAD_UP ||
                keyCode == KeyEvent.KEYCODE_DPAD_DOWN ||
                keyCode == KeyEvent.KEYCODE_DPAD_LEFT ||
                keyCode == KeyEvent.KEYCODE_DPAD_RIGHT
    }
}
