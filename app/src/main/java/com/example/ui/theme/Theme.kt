package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = SafetyAmber500,
    onPrimary = Slate950,
    primaryContainer = SafetyAmber900,
    onPrimaryContainer = SafetyAmber100,
    secondary = IndustrialCyan500,
    onSecondary = Slate950,
    secondaryContainer = IndustrialCyan900,
    onSecondaryContainer = IndustrialCyan100,
    tertiary = RiskLow,
    onTertiary = Slate950,
    tertiaryContainer = RiskLowContainer,
    onTertiaryContainer = RiskLowLightContainer,
    background = Slate950,
    onBackground = Slate100,
    surface = Slate900,
    onSurface = Slate100,
    surfaceVariant = Slate800,
    onSurfaceVariant = Slate400,
    outline = Slate700,
    error = RiskCritical,
    onError = Color.White,
    errorContainer = RiskCriticalContainer,
    onErrorContainer = RiskCriticalLightContainer
)

private val LightColorScheme = lightColorScheme(
    primary = SafetyAmber600,
    onPrimary = Color.White,
    primaryContainer = SafetyAmber100,
    onPrimaryContainer = SafetyAmber900,
    secondary = IndustrialCyan600,
    onSecondary = Color.White,
    secondaryContainer = IndustrialCyan100,
    onSecondaryContainer = IndustrialCyan900,
    tertiary = RiskLow,
    onTertiary = Color.White,
    tertiaryContainer = RiskLowLightContainer,
    onTertiaryContainer = RiskLowContainer,
    background = Slate50,
    onBackground = Slate900,
    surface = Color.White,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate600,
    outline = Slate200,
    error = RiskCritical,
    onError = Color.White,
    errorContainer = RiskCriticalLightContainer,
    onErrorContainer = RiskCriticalContainer
)

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // We use our intentional Industrial Safety color scheme for consistent high-contrast readability
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
