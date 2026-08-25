package com.maaz.befit.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.maaz.befit.ui.theme.LightColorScheme

/**
 * here we are making a custom theme for our app using Material3.
 * We have defined two color schemes, one for light mode and one for dark mode.
 * We also have a high contrast color scheme for accessibility.
 * The BeFitTheme composable function takes in parameters for darkTheme, dynamicColor,
 * and highContrast, and applies the appropriate color scheme based on those parameters.
 * It also sets the status bar color and appearance based on the theme.
 */
private val DarkColorScheme = darkColorScheme(
    primary = HealthBlue,
    secondary = HealthGreen,
    tertiary = HealthPurple,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    error = HealthRed
)

private val LightColorScheme = lightColorScheme(
    primary = HealthBlue, // Primary color will be used for app bars, buttons, and other prominent UI elements like FABs (Floating Action
    secondary = HealthGreen, // Secondary color will be used on screens such as cards, lists, and other surfaces that require a secondary accent color.
    tertiary = HealthPurple, // Tertiary color will be used on such as text, icons, and other elements that require a tertiary accent color.
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE), // Surface color will be used for the background of components like cards, sheets, and dialogs.
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    error = HealthRed

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

// High contrast color schemes for accessibility
private val HighContrastLightColorScheme = lightColorScheme(
    primary = Color(0xFF0000FF),
    secondary = Color(0xFF008000),
    tertiary = Color(0xFF800080),
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black,
    error = Color(0xFFCC0000)
)

// High contrast dark color scheme for accessibility
private val HighContrastDarkColorScheme = darkColorScheme(
    primary = Color(0xFF00BFFF),
    secondary = Color(0xFF00FF00),
    tertiary = Color(0xFFFF00FF),
    background = Color.Black,
    surface = Color.Black,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    error = Color(0xFFFF6666)
)



@Composable
fun BeFitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    highContrast: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        highContrast && darkTheme -> HighContrastDarkColorScheme
        highContrast -> HighContrastLightColorScheme
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    // Set the status bar color and appearance based on the theme
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = if (darkTheme) Color.Black.toArgb() else Color(0xFFE3F2FD).toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}