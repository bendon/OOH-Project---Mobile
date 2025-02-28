package com.edgetech.bbscout.ui.theme

import android.app.Activity
import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
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

private val LightColors = lightColorScheme(
    primary = mainBlue,
    onPrimary = Color.White,
    primaryContainer = lightBlue,
    onPrimaryContainer = Color.Black,
    secondary = mainOrange,
    onSecondary = Color.White,
    secondaryContainer = lightOrange,
    onSecondaryContainer = Color.Black,
    background = Color.White,
    onBackground = Color.Black,
    surface = Color.LightGray.copy(alpha = 0.2f),
    onSurface = Color.DarkGray,
    tertiary = darkerBlue
)

private val DarkColors = darkColorScheme(
    primary = lightBlue,
    onPrimary = mainBlue,
    primaryContainer = mainBlue,
    onPrimaryContainer = Color.White,
    secondary = lightOrange,
    onSecondary = Color.White,
    secondaryContainer = mainOrange,
    onSecondaryContainer = Color.White,
    background = Color.Black,
    onBackground = Color.White,
    surface = Color.DarkGray,
    onSurface = Color.Gray,
    tertiary = lightBlue
)

@Composable
fun BBScoutTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
//        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
//            val context = LocalContext.current
//            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
//        }
        darkTheme -> DarkColors
        else -> LightColors
    }
    val view = LocalView.current
    val context = LocalActivity.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = context?.window
            window?.statusBarColor = colorScheme.primary.toArgb()
            if (window != null)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}