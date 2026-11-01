package com.example

import com.example.model.CursorSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KeyCursorLogicTest {

    @Test
    fun testScreenBoundaryClamping() {
        val screenWidth = 1920f
        val screenHeight = 1200f

        var x = 960f
        var y = 600f

        // Move far beyond right and bottom boundaries
        val dx = 2000f
        val dy = 2000f

        x = (x + dx).coerceIn(0f, screenWidth - 1f)
        y = (y + dy).coerceIn(0f, screenHeight - 1f)

        assertEquals(1919f, x, 0.001f)
        assertEquals(1199f, y, 0.001f)

        // Move far beyond left and top boundaries
        x = (x - 5000f).coerceIn(0f, screenWidth - 1f)
        y = (y - 5000f).coerceIn(0f, screenHeight - 1f)

        assertEquals(0f, x, 0.001f)
        assertEquals(0f, y, 0.001f)
    }

    @Test
    fun testDiagonalNormalization() {
        var vx = 1f
        var vy = 1f
        val length = 1.41421356f
        vx /= length
        vy /= length

        val totalSpeed = kotlin.math.sqrt(vx * vx + vy * vy)
        assertEquals(1.0f, totalSpeed, 0.01f)
    }

    @Test
    fun testAccelerationCurve() {
        val baseSpeed = 16f
        val maxAccel = 1.5f
        val durationMs = 650L

        val progress = (durationMs / 650f).coerceIn(0f, 1f)
        val accel = 1f + (maxAccel - 1f) * progress
        val step = baseSpeed * accel

        assertEquals(1.5f, accel, 0.01f)
        assertEquals(24f, step, 0.01f)
    }

    @Test
    fun testCursorSettingsColorMapping() {
        val settings = CursorSettings(cursorColorIndex = 0)
        assertEquals(0xFF00E5FF, settings.currentHexColor)
        assertEquals("Neon Cyan", settings.currentColorName)

        val settingsViolet = CursorSettings(cursorColorIndex = 1)
        assertEquals(0xFFA855F7, settingsViolet.currentHexColor)
        assertEquals("Electric Violet", settingsViolet.currentColorName)
    }
}
