package com.example.tibiagear.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF775000),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDEA8),
    onPrimaryContainer = Color(0xFF281900),
    secondary = Color(0xFF49635B),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD0E8DE),
    onSecondaryContainer = Color(0xFF092018),
    background = Color(0xFFF7F5EF),
    surface = Color(0xFFFFFDF8),
    surfaceVariant = Color(0xFFECE8DE),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFFC56D),
    onPrimary = Color(0xFF402A00),
    primaryContainer = Color(0xFF604000),
    onPrimaryContainer = Color(0xFFFFDEA8),
    secondary = Color(0xFFB4CCBF),
    onSecondary = Color(0xFF20372F),
    secondaryContainer = Color(0xFF354D43),
    onSecondaryContainer = Color(0xFFD0E8DE),
    background = Color(0xFF151714),
    surface = Color(0xFF1D201C),
    surfaceVariant = Color(0xFF45483F),
)

@Composable
fun TibiaTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (darkTheme) DarkColors else LightColors, content = content)
}
