package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

@Composable
fun CharacterImage(
    drawableName: String,
    characterName: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    val resId = try {
        val id = context.resources.getIdentifier(drawableName, "drawable", context.packageName)
        if (id != 0) id else null
    } catch (_: Throwable) {
        null
    }

    if (resId != null) {
        Image(
            painter = painterResource(id = resId),
            contentDescription = characterName,
            modifier = modifier,
            contentScale = contentScale
        )
    } else {
        // Fallback stylized gradient avatar
        val colors = when (characterName.firstOrNull()?.uppercaseChar() ?: 'A') {
            in 'A'..'G' -> listOf(Color(0xFFFF3355), Color(0xFF7C4DFF))
            in 'H'..'N' -> listOf(Color(0xFF7C4DFF), Color(0xFF2979FF))
            in 'O'..'T' -> listOf(Color(0xFF00E676), Color(0xFF00B0FF))
            else -> listOf(Color(0xFFFF9100), Color(0xFFFF3355))
        }

        Box(
            modifier = modifier.background(Brush.linearGradient(colors)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = characterName.take(1).uppercase(),
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
