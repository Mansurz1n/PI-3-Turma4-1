package br.edu.puc.superid.ui.theme

import android.app.Activity
import android.os.Build
import android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary      = LightPrimary,
    onPrimary    = LightOnPrimary,
    secondary    = LightSecondary,
    onSecondary  = LightOnSecondary,
    tertiary     = LightTertiary,
    background   = LightBackground,
    onBackground = LightOnBackground,
    surface      = LightSurface,
    onSurface    = LightOnSurface,
    error        = Color(0xFFB00020),
    onError      = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary      = DarkPrimary,
    onPrimary    = DarkOnPrimary,
    secondary    = DarkSecondary,
    onSecondary  = DarkOnSecondary,
    tertiary     = DarkTertiary,
    background   = DarkBackground,     // ← aplica o preto puro
    onBackground = DarkOnBackground,
    surface      = DarkSurface,
    onSurface    = DarkOnSurface,
    error        = Color(0xFFCF6679),
    onError      = Color.Black
)

@Composable
fun SuperIDTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColorScheme else LightColorScheme

    val window = (LocalContext.current as Activity).window
    window.statusBarColor = colors.primary.toArgb()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        window.insetsController?.setSystemBarsAppearance(
            if (!darkTheme) APPEARANCE_LIGHT_STATUS_BARS else 0,
            APPEARANCE_LIGHT_STATUS_BARS
        )
    }

    MaterialTheme(
        colorScheme = colors,
        typography  = Typography,
        content     = content
    )
}
