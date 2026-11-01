package com.example.ui.components

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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CursorSettings
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

/**
 * Sliders and selectors for fine-tuning pointer velocity, acceleration,
 * sizes, and color themes.
 */
@Composable
fun TuningSliders(
    settings: CursorSettings,
    onSpeedChange: (Float) -> Unit,
    onAccelerationChange: (Float) -> Unit,
    onFastMultChange: (Float) -> Unit,
    onCursorSizeChange: (Int) -> Unit,
    onColorSelect: (Int) -> Unit,
    onClickFeedbackToggle: (Boolean) -> Unit,
    onHapticToggle: (Boolean) -> Unit,
    onTrailToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "SPEED & MOTION ENGINE",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 1. Movement Speed
            SliderControlItem(
                label = "Base Movement Speed",
                valueDisplay = "${settings.movementSpeed.toInt()} px/tick",
                value = settings.movementSpeed,
                valueRange = 4f..40f,
                steps = 35,
                testTag = "speed_slider",
                onValueChange = onSpeedChange
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Acceleration
            SliderControlItem(
                label = "Hold Acceleration Rate",
                valueDisplay = String.format(Locale.US, "%.2fx", settings.acceleration),
                value = settings.acceleration,
                valueRange = 1.0f..2.5f,
                steps = 15,
                testTag = "acceleration_slider",
                onValueChange = onAccelerationChange
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Shift Multiplier
            SliderControlItem(
                label = "Fast Speed (Shift + Arrow)",
                valueDisplay = String.format(Locale.US, "%.1fx", settings.fastMultiplier),
                value = settings.fastMultiplier,
                valueRange = 1.5f..5.0f,
                steps = 14,
                testTag = "multiplier_slider",
                onValueChange = onFastMultChange
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Cursor Size
            SliderControlItem(
                label = "Cursor Arrow Size",
                valueDisplay = "${settings.cursorSizeDp} dp",
                value = settings.cursorSizeDp.toFloat(),
                valueRange = 16f..56f,
                steps = 19,
                testTag = "size_slider",
                onValueChange = { onCursorSizeChange(it.toInt()) }
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 5. Cursor Color Palette
            Text(
                text = "Pointer Accent Color",
                color = TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CursorSettings.CURSOR_COLORS.forEachIndexed { index, colorLong ->
                    val isSelected = settings.cursorColorIndex == index
                    val color = Color(colorLong)
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) Color.White else Color(0x30FFFFFF),
                                shape = CircleShape
                            )
                            .clickable { onColorSelect(index) }
                            .testTag("color_choice_$index"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 6. Visual Toggles
            ToggleItem(
                label = "Click Ripple Animation",
                checked = settings.clickFeedback,
                onCheckedChange = onClickFeedbackToggle
            )

            Spacer(modifier = Modifier.height(10.dp))

            ToggleItem(
                label = "Haptic Vibration on Tap",
                checked = settings.hapticFeedback,
                onCheckedChange = onHapticToggle
            )

            Spacer(modifier = Modifier.height(10.dp))

            ToggleItem(
                label = "Motion Trail Effect",
                checked = settings.trailEnabled,
                onCheckedChange = onTrailToggle
            )
        }
    }
}

@Composable
private fun SliderControlItem(
    label: String,
    valueDisplay: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    testTag: String,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = valueDisplay,
                color = NeonCyan,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = NeonCyan,
                activeTrackColor = NeonCyan,
                inactiveTrackColor = Color(0xFF1E293B)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag(testTag)
        )
    }
}

@Composable
private fun ToggleItem(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 13.sp
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = NeonCyan,
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = Color(0xFF1E293B)
            )
        )
    }
}
