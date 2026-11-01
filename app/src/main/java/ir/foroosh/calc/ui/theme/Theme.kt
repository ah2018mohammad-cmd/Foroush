package ir.foroosh.calc.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import ir.foroosh.calc.data.ThemeMode

private fun darkScheme() = darkColorScheme(
    primary = Red600, onPrimary = Color.White,
    secondary = Yellow500, onSecondary = Ink900,
    tertiary = Red800,
    background = Ink900, onBackground = OnDark,
    surface = Ink800, onSurface = OnDark,
    surfaceVariant = Ink700, onSurfaceVariant = OnDarkDim,
    surfaceContainer = Ink800,
    surfaceContainerLow = Color(0xFF1D1F23),
    surfaceContainerHigh = Ink700,
    outline = Color(0xFF4A4E57), outlineVariant = Color(0xFF3A3E46),
    primaryContainer = Red800, onPrimaryContainer = Color.White,
    secondaryContainer = Color(0xFF4A3D1E), onSecondaryContainer = Yellow500,
    error = Color(0xFFFF7B72),
)

/** حالت «سفید» */
private fun whiteScheme() = lightColorScheme(
    primary = Red600, onPrimary = Color.White,
    secondary = Yellow600, onSecondary = Color.White,
    tertiary = Red700,
    background = Color.White, onBackground = Color(0xFF1F2126),
    surface = Mist50, onSurface = Color(0xFF1F2126),
    surfaceVariant = Mist100, onSurfaceVariant = Color(0xFF565B64),
    surfaceContainer = Mist50,
    surfaceContainerLow = Color(0xFFF7F8FA),
    surfaceContainerHigh = Mist100,
    outline = Color(0xFFD5D8DE), outlineVariant = Color(0xFFE7E9ED),
    primaryContainer = Color(0xFFFFE2E2), onPrimaryContainer = Color(0xFF7A1414),
    secondaryContainer = Color(0xFFFFF3D6), onSecondaryContainer = Color(0xFF6E5200),
    error = Color(0xFFB3261E),
)

/** حالت «مشکی تیره» — تم شبانه واقعی با پس‌زمینه نزدیک به مشکی */
private fun grayScheme() = darkColorScheme(
    primary = Red600, onPrimary = Color.White,
    secondary = Yellow500, onSecondary = Ink900,
    tertiary = Red800,
    background = Color(0xFF0B0C0E), onBackground = OnDark,
    surface = Color(0xFF141518), onSurface = OnDark,
    surfaceVariant = Color(0xFF1C1E22), onSurfaceVariant = OnDarkDim,
    surfaceContainer = Color(0xFF141518),
    surfaceContainerLow = Color(0xFF101114),
    surfaceContainerHigh = Color(0xFF1C1E22),
    outline = Color(0xFF3A3E46), outlineVariant = Color(0xFF26292E),
    primaryContainer = Red800, onPrimaryContainer = Color.White,
    secondaryContainer = Color(0xFF4A3D1E), onSecondaryContainer = Yellow500,
    error = Color(0xFFFF7B72),
)

@Composable
fun ForooshTheme(themeMode: ThemeMode, content: @Composable () -> Unit) {
    val systemDark = isSystemInDarkTheme()
    val scheme = when {
        themeMode == ThemeMode.WHITE -> whiteScheme()
        themeMode == ThemeMode.GRAY -> grayScheme()
        systemDark -> darkScheme()
        else -> whiteScheme()
    }
    MaterialTheme(colorScheme = scheme, typography = AppTypography, content = content)
}
