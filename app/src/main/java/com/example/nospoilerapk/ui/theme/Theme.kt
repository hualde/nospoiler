package com.example.nospoilerapk.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nospoilerapk.ui.viewmodels.ThemeViewModel

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark80,
    secondary = SecondaryDark,
    background = BackgroundDark,
    surface = SurfaceDark,
    error = Error,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    secondary = Secondary,
    background = Background,
    surface = Surface,
    error = Error,
    tertiary = Pink40
)

@Composable
fun NoSpoilerApkTheme(
    themeViewModel: ThemeViewModel,
    content: @Composable () -> Unit
) {
    val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()
    val systemInDarkTheme = isSystemInDarkTheme()
    val effectiveDarkTheme = isDarkTheme || systemInDarkTheme

    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (effectiveDarkTheme) {
                dynamicDarkColorScheme(LocalContext.current)
            } else {
                dynamicLightColorScheme(LocalContext.current)
            }
        }
        effectiveDarkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
            
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !effectiveDarkTheme
            insetsController.isAppearanceLightNavigationBars = !effectiveDarkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}