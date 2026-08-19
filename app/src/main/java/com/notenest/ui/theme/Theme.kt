package com.notenest.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = SkyBluePrimary,
    onPrimary = NavyDeep,
    primaryContainer = NavyActivePill,
    onPrimaryContainer = SkyBlueLight,
    secondary = SkyBlueSoft,
    onSecondary = NavyDarkSurface,
    secondaryContainer = NavyDarkSurfaceContainerHigh,
    onSecondaryContainer = TextSkyHighContrast,
    tertiary = SkyBlueLight,
    background = NavyDarkBackground,
    onBackground = TextSkyHighContrast,
    surface = NavyDarkSurface,
    onSurface = TextSkyHighContrast,
    surfaceVariant = NavyDarkBorder,
    onSurfaceVariant = TextSkyMediumContrast,
    surfaceContainer = NavyDarkSurfaceContainer,
    surfaceContainerHigh = NavyDarkSurfaceContainerHigh,
    outline = NavyDarkBorder,
    outlineVariant = NavyDarkBorder
)

private val LightColorScheme = lightColorScheme(
    primary = NavyLightPrimary,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = SkyBlueLightContainer,
    onPrimaryContainer = NavyLightPrimary,
    secondary = SkyBlueAccent,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    secondaryContainer = LightSkySurfaceContainer,
    onSecondaryContainer = NavyLightPrimary,
    tertiary = SkyBlueAccent,
    background = LightSkyBackground,
    onBackground = NavyLightPrimary,
    surface = LightSkySurface,
    onSurface = NavyLightPrimary,
    surfaceVariant = LightSkyBorder,
    onSurfaceVariant = Color(0xFF334E68),
    surfaceContainer = LightSkySurfaceContainer,
    surfaceContainerHigh = LightSkySurfaceContainerHigh,
    outline = LightSkyBorder,
    outlineVariant = LightSkyBorder
)

@Composable
fun NoteNestTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.surfaceContainer.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
