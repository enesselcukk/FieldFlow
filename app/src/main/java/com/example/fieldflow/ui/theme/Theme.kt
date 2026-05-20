package com.example.fieldflow.ui.theme

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
    primary = FieldFlowBlueDark,
    onPrimary = Color(0xFF001D36),
    primaryContainer = Color(0xFF004A77),
    onPrimaryContainer = Color(0xFFD4E3FF),
    secondary = FieldFlowSlateDark,
    onSecondary = Color(0xFF1A2329),
    secondaryContainer = Color(0xFF546E7A),
    onSecondaryContainer = Color(0xFFECEFF1),
    tertiary = FieldFlowAlertDark,
    onTertiary = Color(0xFF442B00),
    background = Color(0xFF0D1117),
    onBackground = Color(0xFFE2E4E9),
    surface = Color(0xFF0D1117),
    onSurface = Color(0xFFE2E4E9),
    surfaceVariant = Color(0xFF3D4450),
    onSurfaceVariant = Color(0xFFC2C7D3),
    surfaceContainerLowest = Color(0xFF080B10),
    surfaceContainerLow = Color(0xFF141920),
    surfaceContainer = Color(0xFF1A2028),
    surfaceContainerHigh = Color(0xFF242B35),
    surfaceContainerHighest = Color(0xFF2F3743),
    outline = Color(0xFF9AA0AE),
    outlineVariant = Color(0xFF464C59),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
)

private val LightColorScheme = lightColorScheme(
    primary = FieldFlowBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD3E4FD),
    onPrimaryContainer = Color(0xFF041E49),
    secondary = FieldFlowSlate,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFB0BEC5),
    onSecondaryContainer = Color(0xFF1C2830),
    tertiary = FieldFlowAlert,
    onTertiary = Color.White,
    background = Color(0xFFF8F9FC),
    onBackground = Color(0xFF1A1C1E),
    surface = Color(0xFFF8F9FC),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFE0E3EB),
    onSurfaceVariant = Color(0xFF44474E),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF0F2F7),
    surfaceContainer = Color(0xFFEAEEF5),
    surfaceContainerHigh = Color(0xFFE4E8F0),
    surfaceContainerHighest = Color(0xFFDEE3EC),
    outline = Color(0xFF74777F),
    outlineVariant = Color(0xFFC4C6CF),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
)

@Composable
internal fun FieldFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
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
