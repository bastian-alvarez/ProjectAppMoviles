package com.example.uinavegacion.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF415A77), // Azul medio
    secondary = Color(0xFF778DA9), // Azul claro
    tertiary = Color(0xFFE0E1DD), // Azul muy claro
    background = Color(0xFF0D1B2A), // Azul muy oscuro
    surface = Color(0xFF1B263B), // Azul oscuro
    surfaceVariant = Color(0xFF415A77), // Azul medio
    primaryContainer = Color(0xFF415A77), // Azul medio para contenedores
    secondaryContainer = Color(0xFFE0E1DD), // Azul muy claro para contenedores secundarios
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color(0xFF0D1B2A), // Texto oscuro sobre azul claro
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color.White,
    onPrimaryContainer = Color(0xFF0D1B2A), // Texto azul oscuro sobre contenedor primario
    onSecondaryContainer = Color(0xFF0D1B2A) // Texto azul oscuro sobre contenedor secundario
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF415A77), // Azul medio para mejor contraste
    secondary = Color(0xFF778DA9), // Azul claro
    tertiary = Color(0xFFE0E1DD), // Azul muy claro
    background = Color.White,
    surface = Color.White,
    surfaceVariant = Color(0xFFE0E1DD), // Azul muy claro
    primaryContainer = Color(0xFFE0E1DD), // Azul muy claro para contenedores primarios
    secondaryContainer = Color(0xFFE0E1DD), // Azul muy claro para contenedores secundarios
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color(0xFF0D1B2A), // Texto oscuro sobre azul claro
    onBackground = Color(0xFF0D1B2A), // Texto azul oscuro
    onSurface = Color(0xFF0D1B2A), // Texto azul oscuro
    onSurfaceVariant = Color(0xFF0D1B2A), // Texto azul oscuro
    onPrimaryContainer = Color(0xFF0D1B2A), // Texto azul oscuro sobre contenedor primario
    onSecondaryContainer = Color(0xFF0D1B2A) // Texto azul oscuro sobre contenedor secundario
)

@Composable
fun UINavegacionTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color disabled to use our custom blue colors
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}