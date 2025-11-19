package com.example.uinavegacion.data.local.user
import androidx.room.PrimaryKey
import androidx.room.Entity

/**
 * Entidad simplificada de Usuario - SOLO PARA CACHÉ
 * NO guardar datos sensibles como contraseñas
 * La fuente de verdad siempre es el microservicio
 */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    
    // Campos ESENCIALES para caché
    val remoteId: String? = null,           // ID del microservicio
    val name: String,
    val email: String,
    val phone: String = "",                 // Mantener para compatibilidad
    val profilePhotoUri: String? = null,
    val cachedAt: Long = System.currentTimeMillis(), // Para TTL
    
    // Campos DEPRECADOS pero mantenidos para compatibilidad
    @Deprecated("No cachear contraseñas - usar solo microservicio")
    val password: String = "",
    @Deprecated("Obtener del microservicio en cada login")
    val isBlocked: Boolean = false,
    @Deprecated("No esencial para caché")
    val gender: String = "",
    @Deprecated("Usar remoteId en su lugar")
    val roleId: String? = null,
    @Deprecated("No usado")
    val statusId: String? = null,
    @Deprecated("Usar cachedAt en su lugar")
    val createdAt: String? = null
) {
    /**
     * Verifica si la caché ha expirado (30 minutos)
     */
    fun isExpired(ttlMs: Long = 30 * 60 * 1000L): Boolean {
        return System.currentTimeMillis() - cachedAt > ttlMs
    }
}