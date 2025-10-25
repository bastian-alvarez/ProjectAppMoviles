package com.example.uinavegacion.utils

/**
 * Objeto centralizado para manejar todas las URLs de imágenes de juegos
 */
object GameImages {
    
    /**
     * Imagen por defecto que se usa para todos los juegos
     * Puedes cambiar esta URL por cualquier imagen que quieras usar como predeterminada
     */
    const val DEFAULT_GAME_IMAGE = "https://via.placeholder.com/300x400/2196F3/FFFFFF?text=GAME"
    
    /**
     * Función para obtener la imagen de un juego
     * Si el juego no tiene imagen, devuelve la imagen por defecto
     */
    fun getGameImage(imageUrl: String?): String {
        return if (imageUrl.isNullOrEmpty()) {
            DEFAULT_GAME_IMAGE
        } else {
            imageUrl
        }
    }
    
    /**
     * Función alternativa que siempre devuelve la imagen por defecto
     * Útil si quieres forzar que todos los juegos usen la misma imagen
     */
    fun getDefaultImage(): String = DEFAULT_GAME_IMAGE
    
    // Si en el futuro quieres agregar imágenes específicas para cada juego,
    // puedes agregar constantes aquí:
    /*
    const val SUPER_MARIO_BROS = "https://example.com/mario.webp"
    const val ZELDA_BOTW = "https://example.com/zelda.webp"
    // ... más imágenes específicas
    */
}