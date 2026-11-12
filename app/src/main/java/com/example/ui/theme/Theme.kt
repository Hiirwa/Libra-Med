package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = BrandBlue,
    onPrimary = SurfaceLight,
    primaryContainer = BrandBlueSoft,
    onPrimaryContainer = BrandBlueDark,
    secondary = ClinicalTeal,
    onSecondary = SurfaceLight,
    secondaryContainer = ClinicalTealSoft,
    onSecondaryContainer = ClinicalTeal,
    tertiary = StatusInfo,
    background = BackgroundLight,
    onBackground = TextPrimary,
    surface = SurfaceLight,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextMuted,
    error = StatusDanger,
    onError = SurfaceLight
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkBrandBlue,
    onPrimary = DarkBackground,
    primaryContainer = BrandBlueDark,
    onPrimaryContainer = DarkTextPrimary,
    secondary = DarkClinicalTeal,
    onSecondary = DarkBackground,
    secondaryContainer = DarkSurfaceContainer,
    onSecondaryContainer = DarkClinicalTeal,
    tertiary = StatusInfo,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextMuted,
    error = StatusDanger,
    onError = SurfaceLight
)

@Composable
fun LibraryMedicineTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.primary.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
