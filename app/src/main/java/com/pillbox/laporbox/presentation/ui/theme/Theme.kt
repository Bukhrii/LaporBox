package com.pillbox.laporbox.presentation.ui.theme

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = PrimaryCyan,
    onPrimary = TextBlack,
    primaryContainer = LightCyan,
    onPrimaryContainer = TextBlack,
    secondary = MidCyan,
    onSecondary = TextBlack,
    secondaryContainer = LightCyan,
    onSecondaryContainer = TextBlack,
    tertiary = DarkCyan,
    onTertiary = AlmostWhite,
    tertiaryContainer = MidCyan,
    onTertiaryContainer = TextBlack,
    background = AlmostWhite,
    onBackground = TextBlack,
    surface = AlmostWhite,
    onSurface = TextBlack,
    surfaceVariant = LightCyan,
    onSurfaceVariant = TextBlack,
    outline = MidCyan,
    error = DarkError,
    onError = Color.White,
    errorContainer = LightErrorContainer,
    onErrorContainer = OnDarkErrorContainer
)

private val DarkColorScheme = darkColorScheme(
    primary = MidCyan,
    onPrimary = TextBlack,
    primaryContainer = DarkerCyan,
    onPrimaryContainer = LightCyan,
    secondary = LightCyan,
    onSecondary = TextBlack,
    secondaryContainer = DarkerCyan,
    onSecondaryContainer = LightCyan,
    tertiary = DarkCyan,
    onTertiary = TextBlack, // Diubah agar kontras lebih baik
    tertiaryContainer = DarkerCyan,
    onTertiaryContainer = LightCyan,
    background = TextBlack,
    onBackground = AlmostWhite,
    surface = Color(0xFF1F1F1F), // Sedikit lebih terang dari background
    onSurface = AlmostWhite,
    surfaceVariant = Color(0xFF3C3C3C),
    onSurfaceVariant = AlmostWhite,
    outline = MidCyan,
    error = Color(0xFFCF6679),
    onError = TextBlack,
    errorContainer = OnDarkErrorContainer,
    onErrorContainer = LightErrorContainer
)

@Composable
fun LaporBoxTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
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

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            // Mengatur warna ikon status bar (jam, baterai) menjadi gelap saat tema terang, dan sebaliknya.
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}