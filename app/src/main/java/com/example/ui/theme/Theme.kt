package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val SlateColorScheme = darkColorScheme(
    primary = ClimatePrimary,
    onPrimary = SlateDark,
    primaryContainer = SlateLight,
    onPrimaryContainer = TextPrimary,
    secondary = ClimateSecondary,
    onSecondary = SlateDark,
    background = BackgroundDark,
    onBackground = TextPrimary,
    surface = CardSlate,
    onSurface = TextPrimary,
    surfaceVariant = SlateMedium,
    onSurfaceVariant = TextSecondary,
    outline = BorderSlate
)

// Simple fallback for Light Slate
private val SimpleLightColorScheme = lightColorScheme(
    primary = ClimatePrimary,
    secondary = ClimateSecondary,
    background = androidx.compose.ui.graphics.Color(0xFFF1F5F9), // Slate 100
    surface = androidx.compose.ui.graphics.Color.White,
    onBackground = SlateDark,
    onSurface = SlateDark,
    outline = BorderSlate
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // We default to darkTheme = true for stunning weather visualization
    dynamicColor: Boolean = false, // Disable to preserve our gorgeous high contrast themed maps
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) SlateColorScheme else SimpleLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
