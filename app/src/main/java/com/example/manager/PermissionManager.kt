package com.example.manager

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import android.view.InputDevice
import com.example.service.CursorAccessibilityService

/**
 * Manages system permissions and hardware detection (Accessibility, Overlay, Physical Keyboard).
 */
object PermissionManager {

    /**
     * Checks if KeyCursor's Accessibility Service is currently enabled in Android Settings.
     */
    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val expectedComponentName = ComponentName(context, CursorAccessibilityService::class.java)
        val enabledServicesSetting = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServicesSetting)

        while (colonSplitter.hasNext()) {
            val componentNameString = colonSplitter.next()
            val enabledComponent = ComponentName.unflattenFromString(componentNameString)
            if (enabledComponent != null && enabledComponent == expectedComponentName) {
                return true
            }
        }
        return false
    }

    /**
     * Checks if the app has permission to draw system overlays (optional for accessibility overlays,
     * but recommended for broader compatibility).
     */
    fun isOverlayPermissionGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    /**
     * Detects if any physical hardware keyboard (USB, Bluetooth, or dock) is connected.
     */
    fun isPhysicalKeyboardConnected(context: Context): Boolean {
        // Method 1: Configuration check
        val config = context.resources.configuration
        if (config.keyboard == Configuration.KEYBOARD_QWERTY) {
            return true
        }

        // Method 2: InputDevice scan
        val deviceIds = InputDevice.getDeviceIds()
        for (id in deviceIds) {
            val device = InputDevice.getDevice(id) ?: continue
            val sources = device.sources
            val isKeyboard = (sources and InputDevice.SOURCE_KEYBOARD) == InputDevice.SOURCE_KEYBOARD
            val isAlphabetic = device.keyboardType == InputDevice.KEYBOARD_TYPE_ALPHABETIC
            val isNotVirtual = !device.isVirtual

            if (isKeyboard && isAlphabetic && isNotVirtual) {
                return true
            }
        }
        return false
    }

    /**
     * Returns a list of connected physical keyboard device names.
     */
    fun getConnectedKeyboardNames(context: Context): List<String> {
        val keyboards = mutableListOf<String>()
        val deviceIds = InputDevice.getDeviceIds()
        for (id in deviceIds) {
            val device = InputDevice.getDevice(id) ?: continue
            val sources = device.sources
            val isKeyboard = (sources and InputDevice.SOURCE_KEYBOARD) == InputDevice.SOURCE_KEYBOARD
            val isAlphabetic = device.keyboardType == InputDevice.KEYBOARD_TYPE_ALPHABETIC
            val isNotVirtual = !device.isVirtual

            if (isKeyboard && isAlphabetic && isNotVirtual) {
                keyboards.add(device.name ?: "Generic Hardware Keyboard (ID $id)")
            }
        }
        return keyboards
    }

    /**
     * Opens the Android Accessibility Settings screen for user enablement.
     */
    fun openAccessibilitySettings(context: Context) {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            // Fallback to general settings
            val fallback = Intent(Settings.ACTION_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(fallback)
        }
    }

    /**
     * Opens the Overlay Permission Settings screen.
     */
    fun openOverlaySettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            ).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            try {
                context.startActivity(intent)
            } catch (_: Exception) {
                openAccessibilitySettings(context)
            }
        }
    }
}
