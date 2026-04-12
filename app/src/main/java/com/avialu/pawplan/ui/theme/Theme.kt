package com.avialu.pawplan.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,

    secondary = Secondary,
    onSecondary = OnSecondary,

    background = Background,
    onBackground = OnBackground,

    surface = Surface,
    onSurface = OnSurface,

    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant
)

private val DarkColors = darkColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,

    secondary = Secondary,
    onSecondary = OnSecondary,

    background = Background,
    onBackground = OnBackground,

    surface = Surface,
    onSurface = OnSurface,

    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant
)

@Composable
fun PawPlanTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content
    )
}