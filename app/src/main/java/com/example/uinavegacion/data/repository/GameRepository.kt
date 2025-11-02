package com.example.uinavegacion.data.repository

import com.example.uinavegacion.data.local.juego.JuegoDao
import com.example.uinavegacion.data.local.juego.JuegoEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio para gestión de juegos
 */
class GameRepository(
    private val juegoDao: JuegoDao
) {
    
    /**
     * Obtiene todos los juegos
     */
    suspend fun getAllGames(includeInactive: Boolean = false): List<JuegoEntity> {
        return if (includeInactive) juegoDao.getAllIncludingInactive() else juegoDao.getAll()
    }

    fun observeAllGames(includeInactive: Boolean = false): Flow<List<JuegoEntity>> {
        return if (includeInactive) juegoDao.observeAll() else juegoDao.observeActive()
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
            val id = juegoDao.insert(game.copy(activo = true))
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
            juegoDao.updateFull(
                id = game.id,
                nombre = game.nombre,
                descripcion = game.descripcion,
                precio = game.precio,
                stock = game.stock,
                imagenUrl = game.imagenUrl,
                desarrollador = game.desarrollador,
                fechaLanzamiento = game.fechaLanzamiento,
                categoriaId = game.categoriaId,
                generoId = game.generoId,
                activo = game.activo
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
            val rows = juegoDao.deactivate(id)
            if (rows > 0) {
                Result.success(Unit)
            } else {
                Result.failure(IllegalStateException("Juego con id=$id no encontrado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Busca juegos por nombre
     */
    suspend fun searchGamesByName(name: String, includeInactive: Boolean = false): List<JuegoEntity> {
        return if (includeInactive) {
            juegoDao.getByNombreIncludingInactive(name)
        } else {
            juegoDao.getByNombre(name)
        }
    }
    
    /**
     * Obtiene el conteo total de juegos
     */
    suspend fun getTotalGamesCount(): Int {
        return juegoDao.count()
    }

    fun observeTotalGamesCount(): Flow<Int> {
        return juegoDao.observeCount()
    }

    suspend fun updateStock(id: Long, newStock: Int): Result<Unit> {
        return try {
            juegoDao.updateStock(id, newStock)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun reactivateGame(id: Long) {
        juegoDao.reactivate(id)
    }
    
    /**
     * Diagnostica y corrige datos incompletos en la base de datos
     */
    suspend fun diagnosticAndFixIncompleteData(): Result<String> {
        return try {
            val currentCountActive = juegoDao.count()
            val currentCountAll = juegoDao.countAll()
            val message = StringBuilder()
            message.append("🔍 Diagnóstico de BD:\n")
            message.append("- Juegos activos: $currentCountActive\n")
            message.append("- Juegos totales: $currentCountAll\n")
            
            if (currentCountAll < 20) {
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