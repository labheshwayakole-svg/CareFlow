package com.example.careflow.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val CalmHospitalLightColorScheme = lightColorScheme(
    primary = CarePrimary,
    onPrimary = CareOnPrimary,
    primaryContainer = CarePrimaryContainer,
    onPrimaryContainer = CareOnPrimaryContainer,
    secondary = CareSecondary,
    onSecondary = CareOnSecondary,
    secondaryContainer = CareSecondaryContainer,
    onSecondaryContainer = CareOnSecondaryContainer,
    tertiary = CareTertiary,
    onTertiary = CareOnTertiary,
    tertiaryContainer = CareTertiaryContainer,
    onTertiaryContainer = CareOnTertiaryContainer,
    surface = CareSurface,
    onSurface = CareOnSurface,
    surfaceVariant = CareSurfaceVariant,
    onSurfaceVariant = CareOnSurfaceVariant,
    background = CareBackground,
    onBackground = CareOnBackground,
    outline = CareOutline,
    outlineVariant = CareOutlineVariant,
    error = QueueCancelledRed
)

@Composable
fun CareFlowTheme(
    darkTheme: Boolean = false, // Enforce calm hospital light theme for clear medical readability
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CalmHospitalLightColorScheme,
        typography = Typography,
        content = content
    )
}
