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

private val DarkColorScheme =
  darkColorScheme(
    primary = GeometricPrimary,
    onPrimary = GeometricOnPrimary,
    primaryContainer = GeometricPrimaryContainer,
    onPrimaryContainer = GeometricOnPrimaryContainer,
    secondary = GeometricSecondary,
    onSecondary = GeometricOnSecondary,
    secondaryContainer = GeometricSecondaryContainer,
    onSecondaryContainer = GeometricOnSecondaryContainer,
    background = GeometricBackground,
    onBackground = GeometricOnSurface,
    surface = GeometricSurface,
    onSurface = GeometricOnSurface,
    surfaceVariant = GeometricSurfaceVariant,
    onSurfaceVariant = GeometricOnSurfaceVariant,
    outline = GeometricOutline,
    outlineVariant = GeometricOutlineVariant
  )

private val LightColorScheme =
  lightColorScheme(
    primary = GeometricPrimary,
    onPrimary = GeometricOnPrimary,
    primaryContainer = GeometricPrimaryContainer,
    onPrimaryContainer = GeometricOnPrimaryContainer,
    secondary = GeometricSecondary,
    onSecondary = GeometricOnSecondary,
    secondaryContainer = GeometricSecondaryContainer,
    onSecondaryContainer = GeometricOnSecondaryContainer,
    background = GeometricBackground,
    onBackground = GeometricOnSurface,
    surface = GeometricSurface,
    onSurface = GeometricOnSurface,
    surfaceVariant = GeometricSurfaceVariant,
    onSurfaceVariant = GeometricOnSurfaceVariant,
    outline = GeometricOutline,
    outlineVariant = GeometricOutlineVariant
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> DarkColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
