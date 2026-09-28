package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = CyberGreen,
    onPrimary = Color(0xFF00381B),
    primaryContainer = Color(0xFF00532B),
    onPrimaryContainer = Color(0xFF7BFFAB),
    secondary = CyberCyan,
    onSecondary = Color(0xFF00363D),
    secondaryContainer = Color(0xFF004F58),
    onSecondaryContainer = Color(0xFF97F0FF),
    tertiary = CyberAmber,
    onTertiary = Color(0xFF452B00),
    error = CyberRed,
    background = DarkBackground,
    onBackground = Color(0xFFE2E8F0),
    surface = DarkSurface,
    onSurface = Color(0xFFE2E8F0),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = DarkCardBorder
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFA6F5C9),
    onPrimaryContainer = Color(0xFF002111),
    secondary = LightSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFAFEAFF),
    onSecondaryContainer = Color(0xFF001F26),
    tertiary = CyberAmber,
    background = LightBackground,
    onBackground = Color(0xFF191C1E),
    surface = LightSurface,
    onSurface = Color(0xFF191C1E),
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF41484D),
    outline = Color(0xFFB0B9C2)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to sleek cyber dark theme for IT/Security workbench
    dynamicColor: Boolean = false, // Keep high-contrast cyber theme intact by default
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
