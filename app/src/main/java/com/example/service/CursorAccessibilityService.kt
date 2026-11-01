package com.example.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import android.view.KeyEvent
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import com.example.controller.CursorController
import com.example.gesture.GestureController
import com.example.input.KeyboardInputHandler
import com.example.manager.SettingsManager
import com.example.overlay.CursorOverlayView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * System AccessibilityService running in background to capture physical keyboard
 * input events and render the system-wide on-screen mouse pointer overlay.
 */
class CursorAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private var windowManager: WindowManager? = null
    private var overlayView: CursorOverlayView? = null
    private var cursorController: CursorController? = null
    private var keyboardInputHandler: KeyboardInputHandler? = null
    private var gestureController: GestureController? = null
    private lateinit var settingsManager: SettingsManager

    override fun onCreate() {
        super.onCreate()
        settingsManager = SettingsManager.getInstance(this)
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.i(TAG, "KeyCursor AccessibilityService connected")
        currentInstance = this
        _isServiceActiveFlow.value = true

        initializeOverlay()
        observeSettings()
    }

    private fun initializeOverlay() {
        try {
            windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = windowManager?.defaultDisplay

            val screenSize = Point()
            display?.getRealSize(screenSize)

            val overlay = CursorOverlayView(this)
            overlayView = overlay

            val gesture = GestureController(this)
            gestureController = gesture

            val controller = CursorController(overlay, gesture, settingsManager)
            controller.updateScreenDimensions(screenSize.x, screenSize.y)
            cursorController = controller

            keyboardInputHandler = KeyboardInputHandler(controller, settingsManager)

            val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
            }

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                layoutType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
            )

            windowManager?.addView(overlay, params)
            Log.i(TAG, "KeyCursor overlay attached successfully: ${screenSize.x}x${screenSize.y}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to attach KeyCursor overlay", e)
        }
    }

    private fun observeSettings() {
        serviceScope.launch {
            settingsManager.settingsFlow.collect { settings ->
                cursorController?.updateSettings(settings)
            }
        }
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        return keyboardInputHandler?.onKeyEvent(event) ?: false
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // No heavy processing needed; keeps CPU usage near 0%
    }

    override fun onInterrupt() {
        Log.w(TAG, "KeyCursor AccessibilityService interrupted")
        keyboardInputHandler?.clearHeldKeys()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Handle tablet screen rotation between portrait and landscape
        try {
            val screenSize = Point()
            windowManager?.defaultDisplay?.getRealSize(screenSize)
            cursorController?.updateScreenDimensions(screenSize.x, screenSize.y)
            Log.d(TAG, "Screen dimensions updated on config change: ${screenSize.x}x${screenSize.y}")
        } catch (e: Exception) {
            Log.w(TAG, "Could not update dimensions on config change", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "KeyCursor AccessibilityService destroyed")
        keyboardInputHandler?.clearHeldKeys()

        overlayView?.let { view ->
            try {
                windowManager?.removeView(view)
            } catch (e: Exception) {
                Log.w(TAG, "Error removing overlay view", e)
            }
        }
        overlayView = null
        cursorController = null
        keyboardInputHandler = null
        gestureController = null
        currentInstance = null
        _isServiceActiveFlow.value = false
        serviceScope.cancel()
    }

    companion object {
        private const val TAG = "KeyCursorService"

        private val _isServiceActiveFlow = MutableStateFlow(false)
        val isServiceActiveFlow: StateFlow<Boolean> = _isServiceActiveFlow.asStateFlow()

        @Volatile
        var currentInstance: CursorAccessibilityService? = null
            private set

        fun getActiveCursorController(): CursorController? {
            return currentInstance?.cursorController
        }
    }
}
