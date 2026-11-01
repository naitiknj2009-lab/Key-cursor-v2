package com.example.controller

import com.example.gesture.GestureController
import com.example.manager.SettingsManager
import com.example.model.CursorSettings
import com.example.overlay.CursorOverlayView

/**
 * High-level coordinator managing cursor coordinates, bounds clamping,
 * drag cycles, and gesture routing.
 */
class CursorController(
    private val overlayView: CursorOverlayView,
    private val gestureController: GestureController,
    private val settingsManager: SettingsManager
) {

    var screenWidth: Float = 1920f
        private set
    var screenHeight: Float = 1200f
        private set

    var currentX: Float = 960f
        private set
    var currentY: Float = 600f
        private set

    var isDragging: Boolean = false
        private set
    var dragStartX: Float = 0f
        private set
    var dragStartY: Float = 0f
        private set

    init {
        val settings = settingsManager.getSettings()
        overlayView.updateSettings(settings)
        overlayView.setActiveState(settings.activeMode)
    }

    fun updateScreenDimensions(width: Int, height: Int) {
        if (width <= 0 || height <= 0) return
        val oldWidth = screenWidth
        val oldHeight = screenHeight
        screenWidth = width.toFloat()
        screenHeight = height.toFloat()

        // If screen bounds changed (e.g. rotation), keep cursor safely in view
        currentX = if (oldWidth > 0) (currentX / oldWidth * screenWidth).coerceIn(0f, screenWidth - 1f) else screenWidth / 2f
        currentY = if (oldHeight > 0) (currentY / oldHeight * screenHeight).coerceIn(0f, screenHeight - 1f) else screenHeight / 2f
        overlayView.setPosition(currentX, currentY)
    }

    fun updateSettings(settings: CursorSettings) {
        overlayView.updateSettings(settings)
        overlayView.setActiveState(settings.activeMode)
    }

    fun moveCursor(dx: Float, dy: Float) {
        val newX = (currentX + dx).coerceIn(0f, screenWidth - 1f)
        val newY = (currentY + dy).coerceIn(0f, screenHeight - 1f)
        currentX = newX
        currentY = newY
        overlayView.setPosition(currentX, currentY)
    }

    fun setCursorPosition(x: Float, y: Float) {
        currentX = x.coerceIn(0f, screenWidth - 1f)
        currentY = y.coerceIn(0f, screenHeight - 1f)
        overlayView.setPosition(currentX, currentY)
    }

    fun triggerLeftClick() {
        val settings = settingsManager.getSettings()
        if (!settings.enabled || !settings.activeMode) return

        overlayView.triggerClickAnimation(isRightClick = false)
        gestureController.performClick(
            x = currentX,
            y = currentY,
            haptic = settings.hapticFeedback
        )
    }

    fun triggerRightClick() {
        val settings = settingsManager.getSettings()
        if (!settings.enabled || !settings.activeMode) return

        overlayView.triggerClickAnimation(isRightClick = true)
        gestureController.performRightClick(
            x = currentX,
            y = currentY,
            haptic = settings.hapticFeedback
        )
    }

    fun toggleDragMode() {
        val settings = settingsManager.getSettings()
        if (!settings.enabled || !settings.activeMode) return

        if (!isDragging) {
            // Start drag mode
            isDragging = true
            dragStartX = currentX
            dragStartY = currentY
            overlayView.startDrag(dragStartX, dragStartY)
        } else {
            // Complete drag gesture
            isDragging = false
            overlayView.endDrag()
            gestureController.performDrag(
                startX = dragStartX,
                startY = dragStartY,
                endX = currentX,
                endY = currentY,
                haptic = settings.hapticFeedback
            )
        }
    }

    fun cancelDragIfActive() {
        if (isDragging) {
            isDragging = false
            overlayView.endDrag()
        }
    }

    fun toggleActiveMode(): Boolean {
        val current = settingsManager.getSettings().activeMode
        val next = !current
        settingsManager.setActiveMode(next)
        overlayView.setActiveState(next)
        cancelDragIfActive()
        return next
    }
}
