package com.example.uinavegacion.data.local.juego

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad simplificada de Juego - SOLO PARA CACHÉ
 * Sin foreign keys para simplificar
 * La fuente de verdad siempre es el microservicio
 */
@Entity(tableName = "juegos")
data class JuegoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    
    // Campos ESENCIALES para caché
    val remoteId: String? = null,           // ID del microservicio
    val nombre: String,
    val precio: Double,
    val imagenUrl: String? = null,
    val cachedAt: Long = System.currentTimeMillis(), // Para TTL
    
    // Campos MANTENIDOS para compatibilidad (menos críticos)
    val descripcion: String = "",
    val stock: Int = 0,                     // Idealmente obtener del microservicio
    val desarrollador: String = "Desarrollador",
    val fechaLanzamiento: String = "2024",
    val categoriaId: Long = 1L,
    val generoId: Long = 1L,
    @ColumnInfo(defaultValue = "1")
    val activo: Boolean = true,
    @ColumnInfo(defaultValue = "0")
    val descuento: Int = 0
) {
    // Propiedad computada para compatibilidad
    val imageUrl: String
        get() = imagenUrl ?: ""
    
    /**
     * Verifica si la caché ha expirado (1 hora)
     */
    fun isExpired(ttlMs: Long = 60 * 60 * 1000L): Boolean {
        return System.currentTimeMillis() - cachedAt > ttlMs
    }
}
