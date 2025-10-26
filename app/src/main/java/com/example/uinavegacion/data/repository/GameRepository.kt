package com.example.uinavegacion.data.repository

import com.example.uinavegacion.data.local.juego.JuegoDao
import com.example.uinavegacion.data.local.juego.JuegoEntity

/**
 * Repositorio para gestión de juegos
 */
class GameRepository(
    private val juegoDao: JuegoDao
) {
    
    /**
     * Obtiene todos los juegos
     */
    suspend fun getAllGames(): List<JuegoEntity> {
        return juegoDao.getAll()
    }
    
    /**
     * Obtiene un juego por ID
     */
    suspend fun getGameById(id: Long): JuegoEntity? {
        return juegoDao.getById(id)
    }
    
    /**
     * Agrega un nuevo juego
     */
    suspend fun addGame(game: JuegoEntity): Result<Long> {
        return try {
            val id = juegoDao.insert(game)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Actualiza un juego existente
     */
    suspend fun updateGame(game: JuegoEntity): Result<Unit> {
        return try {
            juegoDao.update(
                id = game.id,
                nombre = game.nombre,
                descripcion = game.descripcion,
                precio = game.precio,
                stock = game.stock,
                imagenUrl = game.imagenUrl,
                desarrollador = game.desarrollador,
                fechaLanzamiento = game.fechaLanzamiento,
                categoriaId = game.categoriaId,
                generoId = game.generoId
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Elimina un juego
     */
    suspend fun deleteGame(id: Long): Result<Unit> {
        return try {
            juegoDao.delete(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Busca juegos por nombre
     */
    suspend fun searchGamesByName(name: String): List<JuegoEntity> {
        return juegoDao.getByNombre(name)
    }
    
    /**
     * Obtiene el conteo total de juegos
     */
    suspend fun getTotalGamesCount(): Int {
        return juegoDao.count()
    }
    
    /**
     * Diagnostica y corrige datos incompletos en la base de datos
     */
    suspend fun diagnosticAndFixIncompleteData(): Result<String> {
        return try {
            val currentCount = juegoDao.count()
            val message = StringBuilder()
            message.append("🔍 Diagnóstico de BD:\n")
            message.append("- Juegos actuales: $currentCount\n")
            
            if (currentCount < 20) {
                message.append("⚠️ Datos incompletos detectados\n")
                message.append("🧹 Limpiando datos parciales...\n")
                
                // Limpiar juegos incompletos
                juegoDao.deleteAll()
                message.append("✅ Juegos eliminados\n")
                
                message.append("🔄 Los datos se reinicializarán automáticamente\n")
                message.append("📱 Reinicia la aplicación para completar el proceso\n")
            } else {
                message.append("✅ Base de datos completa\n")
            }
            
            Result.success(message.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}