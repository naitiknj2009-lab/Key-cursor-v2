package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CanvasCardDark
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassBorderHighlight
import com.example.ui.theme.GlassSurface

/**
 * Signature Glassmorphic container with 1px specular light rim and ambient depth.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    borderBrush: Brush = Brush.linearGradient(
        listOf(
            GlassBorderHighlight,
            GlassBorder,
            Color(0x0AFFFFFF)
        )
    ),
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .shadow(elevation = 12.dp, shape = shape, spotColor = Color.Black.copy(alpha = 0.5f))
            .clip(shape)
            .background(CanvasCardDark.copy(alpha = 0.85f))
            .border(BorderStroke(1.dp, borderBrush), shape = shape)
            .padding(18.dp),
        content = content
    )
}

/**
 * Physical keyboard keycap visual indicator for shortcuts.
 */
@Composable
fun KeycapBadge(
    keyLabel: String,
    modifier: Modifier = Modifier,
    accentColor: Color = Color.White
) {
    val shape = RoundedCornerShape(6.dp)

    Box(
        modifier = modifier
            .clip(shape)
            .background(Color(0xFF1E293B))
            .border(
                BorderStroke(
                    1.dp,
                    Brush.verticalGradient(
                        listOf(Color(0x40FFFFFF), Color(0x10000000))
                    )
                ),
                shape
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = keyLabel,
            color = accentColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
