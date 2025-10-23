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
    primary = Color4, // Azul claro
    secondary = Color3, // Azul medio
    tertiary = Color5, // Azul muy claro
    background = Color1, // Azul muy oscuro
    surface = Color2, // Azul oscuro
    surfaceVariant = Color3, // Azul medio
    primaryContainer = Color3, // Azul medio para contenedores
    secondaryContainer = Color5, // Azul muy claro para contenedores secundarios
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color1, // Texto oscuro sobre azul claro
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color.White,
    onPrimaryContainer = Color1, // Texto azul oscuro sobre contenedor primario
    onSecondaryContainer = Color1 // Texto azul oscuro sobre contenedor secundario
)

private val LightColorScheme = lightColorScheme(
    primary = Color3, // Azul medio para mejor contraste
    secondary = Color4, // Azul claro
    tertiary = Color5, // Azul muy claro
    background = Color.White,
    surface = Color.White,
    surfaceVariant = Color5, // Azul muy claro
    primaryContainer = Color5, // Azul muy claro para contenedores primarios
    secondaryContainer = Color5, // Azul muy claro para contenedores secundarios
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color1, // Texto oscuro sobre azul claro
    onBackground = Color1, // Texto azul oscuro
    onSurface = Color1, // Texto azul oscuro
    onSurfaceVariant = Color1, // Texto azul oscuro
    onPrimaryContainer = Color1, // Texto azul oscuro sobre contenedor primario
    onSecondaryContainer = Color1 // Texto azul oscuro sobre contenedor secundario
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