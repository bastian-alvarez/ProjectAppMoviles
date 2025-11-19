package com.example.uinavegacion.data.local.library

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad ultra-simplificada de Biblioteca - SOLO PARA CACHÉ
 * Solo guarda qué juegos tiene el usuario (IDs)
 * Detalles del juego se obtienen del microservicio
 */
@Entity(tableName = "biblioteca")
data class LibraryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    
    // Campos ESENCIALES
    val userId: Long,                   // Usuario propietario
    val juegoId: String,                // ID local del juego
    val remoteGameId: String? = null,   // ID del juego en microservicio
    val cachedAt: Long = System.currentTimeMillis(), // Para TTL
    
    // Campos MANTENIDOS solo para compatibilidad
    val name: String = "",              // Obtener del microservicio idealmente
    val price: Double = 0.0,
    val dateAdded: String = "",
    val status: String = "Disponible",
    val genre: String = "Acción",
    val licenseId: String? = null,
    val licenseKey: String? = null,
    val licenseExpiresAt: String? = null,
    val licenseAssignedAt: String? = null
) {
    /**
     * Verifica si la caché ha expirado (15 minutos)
     */
    fun isExpired(ttlMs: Long = 15 * 60 * 1000L): Boolean {
        return System.currentTimeMillis() - cachedAt > ttlMs
    }
}
