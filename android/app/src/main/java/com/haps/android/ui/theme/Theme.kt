package com.haps.android.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Teal40,
    onPrimary = Color.White,
    secondary = MedBlue40,
    onSecondary = Color.White,
    tertiary = Amber40,
    background = Neutral99,
    surface = Neutral95,
    onBackground = Neutral10,
    onSurface = Neutral10,
    error = Red40,
    outline = Neutral20,
    surfaceVariant = Neutral90,
    onSurfaceVariant = Neutral20,
    inverseSurface = Neutral20,
    inverseOnSurface = Neutral95,
    inversePrimary = Teal80,
    surfaceTint = Teal40,
    outlineVariant = Neutral20,
    scrim = Color.Black
)

private val DarkColors = darkColorScheme(
    primary = Teal80,
    onPrimary = Neutral10,
    secondary = MedBlue80,
    onSecondary = Neutral10,
    tertiary = Amber80,
    background = Neutral10,
    surface = Neutral20,
    onBackground = Neutral95,
    onSurface = Neutral95,
    error = Red80,
    outline = Neutral95,
    surfaceVariant = Neutral20,
    onSurfaceVariant = Neutral95,
    inverseSurface = Neutral95,
    inverseOnSurface = Neutral10,
    inversePrimary = Teal40,
    surfaceTint = Teal80,
    outlineVariant = Neutral95,
    scrim = Color.Black
)

@Composable
fun HapsTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = HapsTypography,
        shapes = HapsShapes,
        content = content
    )
}
