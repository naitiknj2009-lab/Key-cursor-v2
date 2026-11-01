package com.example.manager

import android.content.Context
import android.content.SharedPreferences
import com.example.model.CursorSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages persistent preferences for KeyCursor using SharedPreferences.
 * Provides reactive StateFlow updates for UI and background services.
 */
class SettingsManager private constructor(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _settingsFlow = MutableStateFlow(loadSettings())
    val settingsFlow: StateFlow<CursorSettings> = _settingsFlow.asStateFlow()

    fun getSettings(): CursorSettings = _settingsFlow.value

    fun updateSettings(transform: (CursorSettings) -> CursorSettings) {
        val newSettings = transform(_settingsFlow.value)
        saveSettings(newSettings)
        _settingsFlow.value = newSettings
    }

    fun setEnabled(enabled: Boolean) {
        updateSettings { it.copy(enabled = enabled) }
    }

    fun setActiveMode(active: Boolean) {
        updateSettings { it.copy(activeMode = active) }
    }

    fun setMovementSpeed(speed: Float) {
        updateSettings { it.copy(movementSpeed = speed.coerceIn(4f, 40f)) }
    }

    fun setAcceleration(acceleration: Float) {
        updateSettings { it.copy(acceleration = acceleration.coerceIn(1.0f, 2.5f)) }
    }

    fun setFastMultiplier(multiplier: Float) {
        updateSettings { it.copy(fastMultiplier = multiplier.coerceIn(1.5f, 5.0f)) }
    }

    fun setCursorSizeDp(sizeDp: Int) {
        updateSettings { it.copy(cursorSizeDp = sizeDp.coerceIn(16, 56)) }
    }

    fun setCursorColorIndex(index: Int) {
        val safeIndex = index.coerceIn(0, CursorSettings.CURSOR_COLORS.size - 1)
        updateSettings { it.copy(cursorColorIndex = safeIndex) }
    }

    fun setClickFeedback(enabled: Boolean) {
        updateSettings { it.copy(clickFeedback = enabled) }
    }

    fun setHapticFeedback(enabled: Boolean) {
        updateSettings { it.copy(hapticFeedback = enabled) }
    }

    fun setTrailEnabled(enabled: Boolean) {
        updateSettings { it.copy(trailEnabled = enabled) }
    }

    fun setAutoStart(enabled: Boolean) {
        updateSettings { it.copy(autoStartOnServiceEnabled = enabled) }
    }

    fun resetToDefaults() {
        val defaults = CursorSettings()
        saveSettings(defaults)
        _settingsFlow.value = defaults
    }

    private fun loadSettings(): CursorSettings {
        return CursorSettings(
            enabled = prefs.getBoolean(KEY_ENABLED, true),
            activeMode = prefs.getBoolean(KEY_ACTIVE_MODE, true),
            movementSpeed = prefs.getFloat(KEY_SPEED, 16f),
            acceleration = prefs.getFloat(KEY_ACCELERATION, 1.30f),
            fastMultiplier = prefs.getFloat(KEY_FAST_MULT, 2.5f),
            cursorSizeDp = prefs.getInt(KEY_CURSOR_SIZE, 30),
            cursorColorIndex = prefs.getInt(KEY_COLOR_INDEX, 0),
            clickFeedback = prefs.getBoolean(KEY_CLICK_FEEDBACK, true),
            hapticFeedback = prefs.getBoolean(KEY_HAPTIC, true),
            trailEnabled = prefs.getBoolean(KEY_TRAIL, true),
            autoStartOnServiceEnabled = prefs.getBoolean(KEY_AUTO_START, true)
        )
    }

    private fun saveSettings(s: CursorSettings) {
        prefs.edit()
            .putBoolean(KEY_ENABLED, s.enabled)
            .putBoolean(KEY_ACTIVE_MODE, s.activeMode)
            .putFloat(KEY_SPEED, s.movementSpeed)
            .putFloat(KEY_ACCELERATION, s.acceleration)
            .putFloat(KEY_FAST_MULT, s.fastMultiplier)
            .putInt(KEY_CURSOR_SIZE, s.cursorSizeDp)
            .putInt(KEY_COLOR_INDEX, s.cursorColorIndex)
            .putBoolean(KEY_CLICK_FEEDBACK, s.clickFeedback)
            .putBoolean(KEY_HAPTIC, s.hapticFeedback)
            .putBoolean(KEY_TRAIL, s.trailEnabled)
            .putBoolean(KEY_AUTO_START, s.autoStartOnServiceEnabled)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "keycursor_preferences"
        private const val KEY_ENABLED = "pref_enabled"
        private const val KEY_ACTIVE_MODE = "pref_active_mode"
        private const val KEY_SPEED = "pref_speed"
        private const val KEY_ACCELERATION = "pref_acceleration"
        private const val KEY_FAST_MULT = "pref_fast_mult"
        private const val KEY_CURSOR_SIZE = "pref_cursor_size"
        private const val KEY_COLOR_INDEX = "pref_color_index"
        private const val KEY_CLICK_FEEDBACK = "pref_click_feedback"
        private const val KEY_HAPTIC = "pref_haptic"
        private const val KEY_TRAIL = "pref_trail"
        private const val KEY_AUTO_START = "pref_auto_start"

        @Volatile
        private var instance: SettingsManager? = null

        fun getInstance(context: Context): SettingsManager {
            return instance ?: synchronized(this) {
                instance ?: SettingsManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
