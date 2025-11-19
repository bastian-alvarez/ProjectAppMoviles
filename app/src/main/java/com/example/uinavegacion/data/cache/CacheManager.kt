package com.example.uinavegacion.data.cache

import android.util.Log
import com.example.uinavegacion.data.local.database.AppDatabase

/**
 * Gestor centralizado de caché
 * Limpia datos expirados automáticamente
 */
object CacheManager {
    
    // TTL (Time To Live) para cada tipo de caché
    object TTL {
        const val USER_MS = 30 * 60 * 1000L        // 30 minutos
        const val GAME_MS = 60 * 60 * 1000L        // 1 hora
        const val LIBRARY_MS = 15 * 60 * 1000L     // 15 minutos
    }
    
    /**
     * Limpia TODA la caché expirada
     */
    suspend fun cleanExpiredCache(database: AppDatabase) {
        val now = System.currentTimeMillis()
        
        try {
            // Limpiar usuarios expirados
            val usersDeleted = database.userDao().deleteExpired(now - TTL.USER_MS)
            if (usersDeleted > 0) {
                Log.d("CacheManager", "🧹 Eliminados $usersDeleted usuarios expirados")
            }
            
            // Limpiar juegos expirados
            val gamesDeleted = database.juegoDao().deleteExpired(now - TTL.GAME_MS)
            if (gamesDeleted > 0) {
                Log.d("CacheManager", "🧹 Eliminados $gamesDeleted juegos expirados")
            }
            
            // Limpiar biblioteca expirada
            val libraryDeleted = database.libraryDao().deleteExpired(now - TTL.LIBRARY_MS)
            if (libraryDeleted > 0) {
                Log.d("CacheManager", "🧹 Eliminadas $libraryDeleted entradas de biblioteca expiradas")
            }
            
            val total = usersDeleted + gamesDeleted + libraryDeleted
            if (total > 0) {
                Log.d("CacheManager", "✅ Limpieza de caché completada: $total registros eliminados")
            } else {
                Log.d("CacheManager", "✨ Caché limpia, sin registros expirados")
            }
        } catch (e: Exception) {
            Log.e("CacheManager", "❌ Error al limpiar caché", e)
        }
    }
    
    /**
     * Limpia TODA la caché (sin importar si está expirada)
     * Útil al hacer logout
     */
    suspend fun clearAllCache(database: AppDatabase) {
        try {
            Log.d("CacheManager", "🗑️ Limpiando TODA la caché...")
            
            val users = database.userDao().clearCache()
            val games = database.juegoDao().clearCache()
            val library = database.libraryDao().clearCache()
            
            val total = users + games + library
            Log.d("CacheManager", "✅ Caché completamente limpiada: $total registros eliminados")
        } catch (e: Exception) {
            Log.e("CacheManager", "❌ Error al limpiar toda la caché", e)
        }
    }
    
    /**
     * Verifica si un timestamp de caché ha expirado
     */
    fun isExpired(cachedAt: Long, ttlMs: Long): Boolean {
        return System.currentTimeMillis() - cachedAt > ttlMs
    }
    
    /**
     * Obtiene un timestamp de expiración basado en el TTL
     */
    fun getExpirationTimestamp(ttlMs: Long): Long {
        return System.currentTimeMillis() - ttlMs
    }
}

