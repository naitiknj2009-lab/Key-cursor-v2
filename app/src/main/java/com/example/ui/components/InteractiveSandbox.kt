package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CursorSettings
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Interactive testing playground allowing users to verify left-click,
 * right-click (context menu), and drag-and-drop gestures right on screen.
 */
@Composable
fun InteractiveSandbox(
    settings: CursorSettings,
    isServiceActive: Boolean,
    modifier: Modifier = Modifier
) {
    var leftClickCount by remember { mutableIntStateOf(0) }
    var contextMenuTriggered by remember { mutableStateOf(false) }
    var dragItemInTargetZone by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val clickScale = remember { Animatable(1f) }

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        borderBrush = Brush.linearGradient(
            listOf(NeonCyan.copy(alpha = 0.4f), ElectricViolet.copy(alpha = 0.2f), Color(0x10FFFFFF))
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isServiceActive) EmeraldGlow else CyberAmber)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "LIVE TEST SANDBOX",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Text(
                    text = if (isServiceActive) "Target Ready" else "Enable Service to Test",
                    color = if (isServiceActive) EmeraldGlow else TextMuted,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Use physical keyboard arrow keys to move the cursor here. Press Enter to tap, Shift+Enter to long-press, or Space to drag.",
                color = TextSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Test targets grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Left-Click target
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF0F172A))
                        .border(
                            1.dp,
                            if (leftClickCount > 0) NeonCyan.copy(alpha = 0.6f) else Color(0x20FFFFFF),
                            RoundedCornerShape(14.dp)
                        )
                        .scale(clickScale.value)
                        .clickable {
                            leftClickCount++
                            coroutineScope.launch {
                                clickScale.animateTo(0.92f, tween(80))
                                clickScale.animateTo(1f, tween(120))
                            }
                        }
                        .padding(14.dp)
                        .testTag("sandbox_click_target"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.TouchApp,
                            contentDescription = "Left click target",
                            tint = NeonCyan,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Left-Click (Enter)",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Taps: $leftClickCount",
                            color = NeonCyan,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // 2. Right-Click / Context target
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF0F172A))
                        .border(
                            1.dp,
                            if (contextMenuTriggered) ElectricViolet.copy(alpha = 0.8f) else Color(0x20FFFFFF),
                            RoundedCornerShape(14.dp)
                        )
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = {
                                    contextMenuTriggered = true
                                    coroutineScope.launch {
                                        delay(2500)
                                        contextMenuTriggered = false
                                    }
                                }
                            )
                        }
                        .padding(14.dp)
                        .testTag("sandbox_context_target"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Context menu target",
                            tint = ElectricViolet,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Long-Press (Shift+Enter)",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (contextMenuTriggered) "TRIGGERED!" else "Hold to test",
                            color = if (contextMenuTriggered) ElectricViolet else TextMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Drag and Drop Zone
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF0B1120))
                    .border(
                        1.dp,
                        if (dragItemInTargetZone) EmeraldGlow.copy(alpha = 0.8f) else Color(0x20FFFFFF),
                        RoundedCornerShape(14.dp)
                    )
                    .clickable {
                        dragItemInTargetZone = !dragItemInTargetZone
                    }
                    .padding(14.dp)
                    .testTag("sandbox_drag_target")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Drag-Drop Test Zone (Space)",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (dragItemInTargetZone) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Passed",
                                    tint = EmeraldGlow,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Press Space to initiate drag, steer with arrows, press Space again to drop.",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (dragItemInTargetZone) EmeraldGlow.copy(alpha = 0.2f) else CyberAmber.copy(alpha = 0.2f))
                            .border(
                                1.dp,
                                if (dragItemInTargetZone) EmeraldGlow else CyberAmber,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (dragItemInTargetZone) "DROPPED" else "TAP / DRAG HERE",
                            color = if (dragItemInTargetZone) EmeraldGlow else CyberAmber,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = contextMenuTriggered,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(ElectricViolet.copy(alpha = 0.15f))
                        .border(1.dp, ElectricViolet, RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "ContextMenu / Long-Press Detected Successfully!",
                        color = ElectricViolet,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
