package com.example.model

/**
 * Data model representing all user-configurable parameters for KeyCursor.
 */
data class CursorSettings(
    val enabled: Boolean = true,
    val activeMode: Boolean = true,
    val movementSpeed: Float = 16f,
    val acceleration: Float = 1.30f,
    val fastMultiplier: Float = 2.5f,
    val cursorSizeDp: Int = 30,
    val cursorColorIndex: Int = 0,
    val clickFeedback: Boolean = true,
    val hapticFeedback: Boolean = true,
    val trailEnabled: Boolean = true,
    val autoStartOnServiceEnabled: Boolean = true
) {
    companion object {
        val CURSOR_COLORS = listOf(
            0xFF00E5FF, // Neon Cyan
            0xFFA855F7, // Electric Violet
            0xFF10B981, // Emerald Glow
            0xFFF59E0B, // Cyber Amber
            0xFFF8FAFC  // Arctic White
        )

        val COLOR_NAMES = listOf(
            "Neon Cyan",
            "Electric Violet",
            "Emerald Glow",
            "Cyber Amber",
            "Arctic White"
        )
    }

    val currentHexColor: Long
        get() = CURSOR_COLORS.getOrElse(cursorColorIndex) { 0xFF00E5FF }

    val currentColorName: String
        get() = COLOR_NAMES.getOrElse(cursorColorIndex) { "Neon Cyan" }
}
