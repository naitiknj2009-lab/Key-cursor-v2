package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CursorSettings
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Diagnostics and readiness status for Accessibility Service,
 * Physical Keyboard connection, and Tablet display parameters.
 */
@Composable
fun StatusDiagnosticsCard(
    settings: CursorSettings,
    isServiceActive: Boolean,
    isKeyboardConnected: Boolean,
    connectedKeyboards: List<String>,
    onToggleMaster: (Boolean) -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val masterGlowColor by animateColorAsState(
        if (settings.enabled && isServiceActive) NeonCyan else TextMuted,
        label = "masterGlow"
    )

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        borderBrush = Brush.linearGradient(
            listOf(masterGlowColor.copy(alpha = 0.5f), Color(0x10FFFFFF))
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Master Switch Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "KeyCursor Controller",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (settings.enabled) "System pointer active & intercepting" else "Cursor suspended",
                        color = if (settings.enabled) NeonCyan else TextSecondary,
                        fontSize = 12.sp
                    )
                }

                Switch(
                    checked = settings.enabled,
                    onCheckedChange = onToggleMaster,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = NeonCyan,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = Color(0xFF1E293B)
                    ),
                    modifier = Modifier.testTag("master_cursor_switch")
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Diagnostic Items
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. Accessibility Service Status
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0F172A))
                        .border(
                            1.dp,
                            if (isServiceActive) EmeraldGlow.copy(alpha = 0.4f) else CyberAmber.copy(alpha = 0.4f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = if (isServiceActive) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (isServiceActive) EmeraldGlow else CyberAmber,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Accessibility Service",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (isServiceActive) "Connected & listening" else "Permission required to intercept keys",
                                    color = if (isServiceActive) EmeraldGlow else CyberAmber,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        if (!isServiceActive) {
                            ElevatedButton(
                                onClick = onOpenAccessibilitySettings,
                                colors = ButtonDefaults.elevatedButtonColors(
                                    containerColor = CyberAmber,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("enable_accessibility_button")
                            ) {
                                Text("Enable", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.OpenInNew,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                // 2. Physical Keyboard Detector
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0F172A))
                        .border(
                            1.dp,
                            if (isKeyboardConnected) EmeraldGlow.copy(alpha = 0.4f) else Color(0x20FFFFFF),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Keyboard,
                            contentDescription = null,
                            tint = if (isKeyboardConnected) EmeraldGlow else TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Physical Keyboard",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(if (isKeyboardConnected) EmeraldGlow else TextMuted)
                                )
                            }
                            Text(
                                text = if (isKeyboardConnected) {
                                    if (connectedKeyboards.isNotEmpty()) connectedKeyboards.joinToString(", ") else "Hardware keyboard attached"
                                } else {
                                    "No physical keyboard detected (connect USB-OTG or Bluetooth)"
                                },
                                color = if (isKeyboardConnected) TextSecondary else TextMuted,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // 3. Quick Mode Toggle indicator (Escape key state)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x0AFFFFFF))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Current Key State:",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        KeycapBadge(keyLabel = "Esc", accentColor = NeonCyan)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (settings.activeMode) "Cursor Control (Active)" else "Normal Typing (Paused)",
                            color = if (settings.activeMode) NeonCyan else CyberAmber,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
