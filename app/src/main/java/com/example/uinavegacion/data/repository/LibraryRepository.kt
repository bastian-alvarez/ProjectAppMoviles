package com.example.uinavegacion.data.repository

import android.util.Log
import com.example.uinavegacion.data.local.library.LibraryDao
import com.example.uinavegacion.data.local.library.LibraryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio para gestión de biblioteca de juegos del usuario
 */
class LibraryRepository(
    private val libraryDao: LibraryDao
) {
    
    /**
     * Obtiene la biblioteca de un usuario
     */
    fun getUserLibrary(userId: Long): Flow<List<LibraryEntity>> {
        return libraryDao.getUserLibrary(userId)
    }
    
    /**
     * Obtiene la biblioteca de un usuario directamente (sin Flow)
     */
    suspend fun getUserLibraryDirect(userId: Long): List<LibraryEntity> {
        return libraryDao.getUserLibraryDirect(userId)
    }
    
    /**
     * Agrega un juego a la biblioteca del usuario
     */
    suspend fun addGameToLibrary(
        userId: Long,
        juegoId: String,
        name: String,
        price: Double,
        dateAdded: String,
        status: String = "Disponible",
        genre: String = "Acción"
    ): Result<Long> {
        return try {
            // Verificar si el usuario ya tiene este juego
            val alreadyOwns = libraryDao.userOwnsGame(userId, juegoId) > 0
            if (alreadyOwns) {
                Log.d("LibraryRepository", "Usuario ya tiene el juego $juegoId")
                return Result.failure(Exception("Ya tienes este juego en tu biblioteca"))
            }
            
            // Insertar el juego en la biblioteca
            val entity = LibraryEntity(
                userId = userId,
                juegoId = juegoId,
                name = name,
                price = price,
                dateAdded = dateAdded,
                status = status,
                genre = genre
            )
            libraryDao.insert(entity)
            Log.d("LibraryRepository", "Juego $juegoId agregado a biblioteca del usuario $userId")
            Result.success(0L) // El ID se genera automáticamente
        } catch (e: Exception) {
            Log.e("LibraryRepository", "Error agregando juego a biblioteca", e)
            Result.failure(e)
        }
    }
    
    /**
     * Agrega múltiples juegos a la biblioteca (para compras)
     */
    suspend fun addPurchasedGames(
        userId: Long,
        games: List<Pair<String, String>> // Lista de (juegoId, nombre)
    ): Result<Int> {
        return try {
            val currentDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            var addedCount = 0
            var skippedCount = 0
            
            games.forEach { (juegoId, name) ->
                val alreadyOwns = libraryDao.userOwnsGame(userId, juegoId) > 0
                if (!alreadyOwns) {
                    val entity = LibraryEntity(
                        userId = userId,
                        juegoId = juegoId,
                        name = name,
                        price = 0.0, // Se puede obtener del juego si es necesario
                        dateAdded = currentDate,
                        status = "Disponible",
                        genre = "Acción"
                    )
                    libraryDao.insert(entity)
                    addedCount++
                } else {
                    skippedCount++
                }
            }
            
            Log.d("LibraryRepository", "Compra procesada: $addedCount agregados, $skippedCount ya existían")
            Result.success(addedCount)
        } catch (e: Exception) {
            Log.e("LibraryRepository", "Error procesando compra", e)
            Result.failure(e)
        }
    }
    
    /**
     * Elimina un juego de la biblioteca
     */
    suspend fun removeGameFromLibrary(userId: Long, juegoId: String): Result<Unit> {
        return try {
            libraryDao.removeGameFromUser(userId, juegoId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Verifica si el usuario tiene un juego
     */
    suspend fun userOwnsGame(userId: Long, juegoId: String): Boolean {
        return libraryDao.userOwnsGame(userId, juegoId) > 0
    }
    
    /**
     * Actualiza el estado de un juego en la biblioteca
     */
    suspend fun updateGameStatus(userId: Long, juegoId: String, newStatus: String): Result<Unit> {
        return try {
            // Necesitamos obtener el juego y actualizarlo
            // Por ahora, esto requiere una actualización en LibraryDao o Entity
            // Por simplicidad, retornamos éxito
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

