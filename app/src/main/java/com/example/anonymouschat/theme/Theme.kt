package com.example.anonymouschat.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryNeon,
    secondary = PrimaryVariant,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = DarkBackground,
    onSecondary = DarkBackground,
    onBackground = OnDark,
    onSurface = OnDark
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Forced Dark Mode for privacy aesthetic
  dynamicColor: Boolean = false, // Disable dynamic color to maintain our theme
  content: @Composable () -> Unit,
) {
  MaterialTheme(
      colorScheme = DarkColorScheme, 
      typography = Typography, 
      content = content
  )
}
