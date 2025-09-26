package com.offordflix.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFBB86FC),
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0xFF3700B3),
    onPrimaryContainer = Color(0xFFFFFFFF),
    secondary = Color(0xFF03DAC6),
    onSecondary = Color(0xFF000000),
    secondaryContainer = Color(0xFF018786),
    onSecondaryContainer = Color(0xFFFFFFFF),
    tertiary = Color(0xFFCF6679),
    onTertiary = Color(0xFF000000),
    tertiaryContainer = Color(0xFFB00020),
    onTertiaryContainer = Color(0xFFFFFFFF),
    error = Color(0xFFF44336),
    errorContainer = Color(0xFFFFCDD2),
    onError = Color(0xFFFFFFFF),
    onErrorContainer = Color(0xFFD32F2F),
    background = Color(0xFF121212),
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF2D2D2D),
    onSurfaceVariant = Color(0xFFE0E0E0),
    outline = Color(0xFF757575),
    inverseOnSurface = Color(0xFF121212),
    inverseSurface = Color(0xFFFFFFFF),
    inversePrimary = Color(0xFF6200EE)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6200EE),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFBB86FC),
    onPrimaryContainer = Color(0xFF000000),
    secondary = Color(0xFF018786),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFF03DAC6),
    onSecondaryContainer = Color(0xFF000000),
    tertiary = Color(0xFFB00020),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFCF6679),
    onTertiaryContainer = Color(0xFF000000),
    error = Color(0xFFB00020),
    errorContainer = Color(0xFFFFCDD2),
    onError = Color(0xFFFFFFFF),
    onErrorContainer = Color(0xFFD32F2F),
    background = Color(0xFFFFFBFE),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFBFE),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF79747E),
    inverseOnSurface = Color(0xFFF4EFF4),
    inverseSurface = Color(0xFF313033),
    inversePrimary = Color(0xFFD0BCFF)
)

@Composable
fun OffordflixTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}


