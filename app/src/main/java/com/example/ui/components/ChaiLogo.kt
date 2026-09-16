package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ChaiLogo(
    modifier: Modifier = Modifier,
    iconWidth: Dp = 38.dp,
    iconHeight: Dp = 20.dp,
    fontSize: TextUnit = 24.sp,
    textColor: Color = Color.White
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ChaiIconMark(width = iconWidth, height = iconHeight)
        Text(
            text = "CHAI",
            color = textColor,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            fontFamily = FontFamily.SansSerif
        )
    }
}

@Composable
fun ChaiIconMark(
    width: Dp = 36.dp,
    height: Dp = 20.dp,
    color: Color = Color.White
) {
    Canvas(modifier = Modifier.size(width = width, height = height)) {
        val w = size.width
        val h = size.height
        val pillHeight = h * 0.46f
        val pillRadius = pillHeight / 2f

        // Top pill (offset slightly to left)
        drawRoundRect(
            color = color,
            topLeft = Offset(0f, 0f),
            size = Size(w * 0.72f, pillHeight),
            cornerRadius = CornerRadius(pillRadius, pillRadius)
        )

        // Bottom pill (offset slightly to right)
        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.28f, h - pillHeight),
            size = Size(w * 0.72f, pillHeight),
            cornerRadius = CornerRadius(pillRadius, pillRadius)
        )

        // Small circle accent inside top pill
        drawCircle(
            color = Color(0xFF0C0C0F),
            radius = pillHeight * 0.28f,
            center = Offset(pillRadius * 1.2f, pillHeight / 2f)
        )
    }
}
