package com.example

import com.example.model.CursorSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleRobolectricTest {

    @Test
    fun verifySettingsDefaults() {
        val settings = CursorSettings()
        assertEquals(true, settings.enabled)
        assertEquals(true, settings.activeMode)
        assertEquals(16f, settings.movementSpeed, 0.01f)
        assertEquals(30, settings.cursorSizeDp)
    }

    @Test
    fun verifySpeedCoercion() {
        val minSpeed = 2f.coerceIn(4f, 40f)
        val maxSpeed = 50f.coerceIn(4f, 40f)
        assertEquals(4f, minSpeed, 0.01f)
        assertEquals(40f, maxSpeed, 0.01f)
    }
}
