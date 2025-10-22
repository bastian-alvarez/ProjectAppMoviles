package com.example.uinavegacion.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

/**
 * Enum para definir los diferentes tipos de dispositivos
 */
enum class DeviceType {
    PHONE_PORTRAIT,
    PHONE_LANDSCAPE, 
    TABLET_PORTRAIT,
    TABLET_LANDSCAPE,
    DESKTOP
}

/**
 * Enum para definir los tipos de navegación según el dispositivo
 */
enum class NavigationType {
    BOTTOM_NAVIGATION,      // Teléfonos en vertical
    NAVIGATION_RAIL,        // Tablets en vertical o teléfonos horizontales  
    PERMANENT_NAVIGATION_DRAWER // Tablets grandes y desktop
}

/**
 * Data class que contiene información sobre el tamaño de ventana
 */
data class WindowInfo(
    val deviceType: DeviceType,
    val navigationType: NavigationType,
    val isTablet: Boolean,
    val isLandscape: Boolean,
    val screenWidthDp: Int,
    val screenHeightDp: Int
)

/**
 * Composable que proporciona información detallada sobre el tamaño de ventana
 */
@Composable
fun rememberWindowInfo(): WindowInfo {
    val configuration = LocalConfiguration.current
    
    val screenWidthDp = configuration.screenWidthDp
    val screenHeightDp = configuration.screenHeightDp
    val isLandscape = screenWidthDp > screenHeightDp
    
    // Determinar tipo de dispositivo basado en breakpoints estándar
    val deviceType = when {
        screenWidthDp >= 1200 -> DeviceType.DESKTOP
        screenWidthDp >= 840 && isLandscape -> DeviceType.TABLET_LANDSCAPE
        screenWidthDp >= 600 && !isLandscape -> DeviceType.TABLET_PORTRAIT
        screenWidthDp >= 600 && isLandscape -> DeviceType.TABLET_LANDSCAPE
        isLandscape -> DeviceType.PHONE_LANDSCAPE
        else -> DeviceType.PHONE_PORTRAIT
    }
    
    // Determinar tipo de navegación
    val navigationType = when {
        screenWidthDp >= 1200 -> NavigationType.PERMANENT_NAVIGATION_DRAWER
        screenWidthDp >= 840 -> NavigationType.PERMANENT_NAVIGATION_DRAWER
        screenWidthDp >= 600 && isLandscape -> NavigationType.NAVIGATION_RAIL
        screenWidthDp >= 600 -> NavigationType.BOTTOM_NAVIGATION
        isLandscape -> NavigationType.NAVIGATION_RAIL
        else -> NavigationType.BOTTOM_NAVIGATION
    }
    
    val isTablet = deviceType in listOf(DeviceType.TABLET_PORTRAIT, DeviceType.TABLET_LANDSCAPE, DeviceType.DESKTOP)
    
    return WindowInfo(
        deviceType = deviceType,
        navigationType = navigationType,
        isTablet = isTablet,
        isLandscape = isLandscape,
        screenWidthDp = screenWidthDp,
        screenHeightDp = screenHeightDp
    )
}

/**
 * Extensiones útiles para cálculos adaptativos
 */
object AdaptiveUtils {
    
    /**
     * Obtiene el número de columnas para una grilla según el tamaño de pantalla
     */
    fun getGridColumns(windowInfo: WindowInfo): Int = when (windowInfo.deviceType) {
        DeviceType.PHONE_PORTRAIT -> 2
        DeviceType.PHONE_LANDSCAPE -> 3
        DeviceType.TABLET_PORTRAIT -> 3
        DeviceType.TABLET_LANDSCAPE -> 4
        DeviceType.DESKTOP -> 5
    }
    
    /**
     * Obtiene el padding horizontal adaptativo
     */
    fun getHorizontalPadding(windowInfo: WindowInfo) = when (windowInfo.deviceType) {
        DeviceType.PHONE_PORTRAIT, DeviceType.PHONE_LANDSCAPE -> 16.dp
        DeviceType.TABLET_PORTRAIT -> 24.dp
        DeviceType.TABLET_LANDSCAPE, DeviceType.DESKTOP -> 32.dp
    }
    
    /**
     * Obtiene el ancho máximo del contenido para evitar líneas de texto muy largas
     */
    fun getMaxContentWidth(windowInfo: WindowInfo) = when (windowInfo.deviceType) {
        DeviceType.PHONE_PORTRAIT, DeviceType.PHONE_LANDSCAPE -> Int.MAX_VALUE.dp
        DeviceType.TABLET_PORTRAIT -> 600.dp
        DeviceType.TABLET_LANDSCAPE, DeviceType.DESKTOP -> 800.dp
    }
    
    /**
     * Indica si se debe usar un diseño de dos paneles
     */
    fun shouldUseTwoPaneLayout(windowInfo: WindowInfo): Boolean = 
        windowInfo.deviceType in listOf(DeviceType.TABLET_LANDSCAPE, DeviceType.DESKTOP)
    
    /**
     * Obtiene el espaciado entre elementos según el tamaño de pantalla
     */
    fun getItemSpacing(windowInfo: WindowInfo) = when (windowInfo.deviceType) {
        DeviceType.PHONE_PORTRAIT, DeviceType.PHONE_LANDSCAPE -> 8.dp
        DeviceType.TABLET_PORTRAIT -> 12.dp
        DeviceType.TABLET_LANDSCAPE, DeviceType.DESKTOP -> 16.dp
    }
}