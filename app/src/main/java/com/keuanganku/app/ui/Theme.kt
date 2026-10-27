package com.keuanganku.app.ui

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = Color(0xFF00695C),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB2DFDB),
    onPrimaryContainer = Color(0xFF00201C),
    secondaryContainer = Color(0xFFD7EAE6),
    onSecondaryContainer = Color(0xFF0B1F1C),
    background = Color(0xFFF6FAF9),
    surface = Color(0xFFF6FAF9),
    surfaceContainer = Color(0xFFECF2F0),
    surfaceContainerHighest = Color(0xFFE1EAE7)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF80CBC4),
    onPrimary = Color(0xFF00382F),
    primaryContainer = Color(0xFF004D40),
    onPrimaryContainer = Color(0xFFB2DFDB),
    secondaryContainer = Color(0xFF2B3D3A),
    onSecondaryContainer = Color(0xFFD7EAE6),
    background = Color(0xFF0F1413),
    surface = Color(0xFF0F1413),
    surfaceContainer = Color(0xFF181F1D),
    surfaceContainerHighest = Color(0xFF252E2C)
)

@Composable
fun KeuanganTheme(
    darkTheme: Boolean,
    dynamicColor: Boolean,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colors = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> DarkColors
        else -> LightColors
    }
    MaterialTheme(colorScheme = colors, content = content)
}

@Composable
fun incomeColor(): Color =
    if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0xFF81C784) else Color(0xFF2E7D32)

@Composable
fun expenseColor(): Color =
    if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0xFFEF9A9A) else Color(0xFFC62828)
