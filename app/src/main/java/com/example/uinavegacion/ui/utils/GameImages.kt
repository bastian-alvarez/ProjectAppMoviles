package com.example.uinavegacion.ui.utils

/**
 * Utilidad para manejar las imágenes de los juegos de forma centralizada
 */
object GameImages {
    
    /**
     * Imagen por defecto para todos los juegos
     */
    const val DEFAULT_GAME_IMAGE = "https://via.placeholder.com/300x400/2196F3/FFFFFF?text=GAME"
    
    /**
     * Obtiene la imagen del juego. Por ahora siempre devuelve la imagen por defecto,
     * pero en el futuro se puede extender para manejar imágenes específicas por juego.
     * 
     * @param gameId ID del juego (futuro uso)
     * @return URL de la imagen del juego
     */
    fun getGameImage(gameId: String = ""): String {
        // Por ahora siempre devolvemos la imagen por defecto
        // En el futuro se puede implementar lógica para imágenes específicas
        return DEFAULT_GAME_IMAGE
    }
    
    /**
     * Obtiene la imagen por defecto para los juegos
     * 
     * @return URL de la imagen por defecto
     */
    fun getDefaultImage(): String {
        return DEFAULT_GAME_IMAGE
    }
}