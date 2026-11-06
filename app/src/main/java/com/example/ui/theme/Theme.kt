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

private val GuardViewDarkColorScheme = darkColorScheme(
    primary = SecurityCyan,
    onPrimary = Navy950,
    primaryContainer = Navy800,
    onPrimaryContainer = SecurityCyanLight,
    secondary = GuardGreen,
    onSecondary = Navy950,
    secondaryContainer = Navy700,
    onSecondaryContainer = GuardGreenLight,
    tertiary = AlertAmber,
    onTertiary = Navy950,
    background = Navy950,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = CardDark,
    onSurfaceVariant = TextSecondary,
    outline = CardBorder,
    error = CriticalRed,
    onError = Color.White
)

private val GuardViewLightColorScheme = lightColorScheme(
    primary = SecurityCyanDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF0369A1),
    secondary = GuardGreenDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDCFCE7),
    onSecondaryContainer = Color(0xFF15803D),
    tertiary = AlertAmber,
    onTertiary = Color.White,
    background = Slate50,
    onBackground = Slate900,
    surface = Color.White,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate800,
    outline = Slate200,
    error = CriticalRed,
    onError = Color.White
)

@Composable
fun GuardViewTheme(
    darkTheme: Boolean = true, // Default to sleek security dark theme
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) GuardViewDarkColorScheme else GuardViewLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
