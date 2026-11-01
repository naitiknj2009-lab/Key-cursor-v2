package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Visual cheat sheet mapping physical keyboard keys to mouse actions.
 */
@Composable
fun ShortcutsReferenceCard(modifier: Modifier = Modifier) {
    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Keyboard,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "KEYBOARD CONTROLS REFERENCE",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ShortcutRow(
                    keys = listOf("↑", "↓", "←", "→"),
                    description = "Move mouse cursor (supports diagonal steering)",
                    color = NeonCyan
                )

                ShortcutRow(
                    keys = listOf("Shift", "Arrows"),
                    description = "Sprint / Fast movement (2.5x multiplier)",
                    color = NeonCyan
                )

                ShortcutRow(
                    keys = listOf("Enter"),
                    description = "Left-Click (tap at pointer coordinates)",
                    color = EmeraldGlow
                )

                ShortcutRow(
                    keys = listOf("Shift", "Enter"),
                    description = "Right-Click (context menu / long-press)",
                    color = ElectricViolet
                )

                ShortcutRow(
                    keys = listOf("Space"),
                    description = "Drag Mode (start drag, steer, release/tap again to drop)",
                    color = CyberAmber
                )

                ShortcutRow(
                    keys = listOf("Esc"),
                    description = "Pause / Resume cursor (switches to normal typing)",
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun ShortcutRow(
    keys: List<String>,
    description: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0F172A))
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            keys.forEach { key ->
                KeycapBadge(keyLabel = key, accentColor = color)
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = description,
            color = TextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Technical guidance and troubleshooting for Android 10 and Lenovo Tab M10.
 */
@Composable
fun TroubleshootingCard(modifier: Modifier = Modifier) {
    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = ElectricViolet,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "HARDWARE & ANDROID 10 SETUP",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TroubleItem(
                    stepNumber = "1",
                    title = "Connecting Physical Keyboard",
                    body = "Plug a USB keyboard into your Lenovo Tab M10 using a USB-C or Micro-USB OTG adapter, or connect via Bluetooth in Android Settings -> Connected Devices."
                )

                TroubleItem(
                    stepNumber = "2",
                    title = "System Accessibility Service",
                    body = "Go to Settings -> Accessibility -> KeyCursor and switch it ON. This allows KeyCursor to capture arrow keys and dispatch system-wide touch taps."
                )

                TroubleItem(
                    stepNumber = "3",
                    title = "Typing in Text Fields",
                    body = "When you need to type in a search box, browser URL bar, or document, simply tap the [Escape] key to pause KeyCursor. Tap [Escape] again to resume pointer navigation."
                )

                TroubleItem(
                    stepNumber = "4",
                    title = "Android Accessibility Boundary",
                    body = "Android security policy prevents accessibility gestures from interacting with secure password entry fields or system permission dialogs. Normal apps and home screens are fully supported."
                )
            }
        }
    }
}

@Composable
private fun TroubleItem(
    stepNumber: String,
    title: String,
    body: String
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF1E293B))
                .border(1.dp, Color(0x30FFFFFF), RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber,
                color = ElectricViolet,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = body,
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}
