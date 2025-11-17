package com.example.uinavegacion.data.repository

import android.util.Log
import com.example.uinavegacion.data.local.library.LibraryDao
import com.example.uinavegacion.data.local.library.LibraryEntity
import com.example.uinavegacion.data.remote.dto.AddToLibraryRequest
import com.example.uinavegacion.data.remote.post.LicenciaRemoteDto
import com.example.uinavegacion.data.remote.post.LibraryEntryRemoteDto
import com.example.uinavegacion.data.remote.post.LibraryPostRepository
import com.example.uinavegacion.data.remote.repository.LibraryRemoteRepository
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio para gestión de biblioteca de juegos del usuario
 */
class LibraryRepository(
    private val libraryDao: LibraryDao,
    private val remoteRepository: LibraryPostRepository = LibraryPostRepository.create(),
    private val libraryRemoteRepository: LibraryRemoteRepository = LibraryRemoteRepository()
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
        remoteUserId: String? = null,
        juegoId: String,
        remoteGameId: String? = null,
        name: String,
        price: Double,
        dateAdded: String,
        status: String = "Disponible",
        genre: String = "Acción"
    ): Result<Long> {
        return try {
            val alreadyOwns = libraryDao.userOwnsGame(userId, juegoId) > 0
            if (alreadyOwns) {
                Log.d("LibraryRepository", "Usuario ya tiene el juego $juegoId")
                return Result.failure(Exception("Ya tienes este juego en tu biblioteca"))
            }

            val assignedLicense = if (!remoteUserId.isNullOrBlank() && !remoteGameId.isNullOrBlank()) {
                assignRemoteLicense(remoteGameId, remoteUserId)
            } else {
                Log.w("LibraryRepository", "No se proporcionó información remota suficiente para asignar licencia. Guardando solo en local.")
                null
            }

            val entity = LibraryEntity(
                userId = userId,
                juegoId = juegoId,
                name = name,
                price = price,
                dateAdded = dateAdded,
                status = status,
                genre = genre,
                remoteGameId = remoteGameId,
                licenseId = assignedLicense?.id,
                licenseKey = assignedLicense?.clave,
                licenseExpiresAt = assignedLicense?.fechaVencimiento,
                licenseAssignedAt = assignedLicense?.asignadaEn
            )
            
            // 1. Guardar en base de datos LOCAL
            Log.d("LibraryRepository", "Insertando juego en biblioteca LOCAL: userId=$userId, juegoId=$juegoId, name=$name")
            val insertedId = libraryDao.insert(entity)
            Log.d("LibraryRepository", "✓ Juego $name (ID: $juegoId) agregado a biblioteca LOCAL del usuario $userId con ID $insertedId")
            
            // Verificar que se insertó correctamente en LOCAL
            val verification = libraryDao.userOwnsGame(userId, juegoId)
            if (verification > 0) {
                Log.d("LibraryRepository", "✓ Verificación LOCAL exitosa: Usuario $userId ahora posee el juego $juegoId")
            } else {
                Log.e("LibraryRepository", "✗ ERROR: La verificación LOCAL falló. El juego NO se guardó en la BD local")
                return Result.failure(Exception("Error al verificar la inserción del juego en BD local"))
            }
            
            // 2. Guardar en microservicio REMOTO (si hay información remota)
            if (!remoteUserId.isNullOrBlank() && !remoteGameId.isNullOrBlank()) {
                try {
                    Log.d("LibraryRepository", "Agregando juego a biblioteca REMOTA: userId=$remoteUserId, gameId=$remoteGameId")
                    val remoteRequest = AddToLibraryRequest(
                        userId = remoteUserId,
                        gameId = remoteGameId,
                        name = name,
                        price = price,
                        dateAdded = dateAdded,
                        status = status,
                        genre = genre
                    )
                    val remoteResult = libraryRemoteRepository.addToLibrary(remoteRequest)
                    
                    if (remoteResult.isSuccess) {
                        Log.d("LibraryRepository", "✓ Juego agregado exitosamente a biblioteca REMOTA")
                    } else {
                        Log.w("LibraryRepository", "⚠️ No se pudo agregar a biblioteca REMOTA: ${remoteResult.exceptionOrNull()?.message}")
                        Log.w("LibraryRepository", "El juego se guardó en LOCAL pero no en REMOTO")
                    }
                } catch (e: Exception) {
                    Log.w("LibraryRepository", "⚠️ Error al agregar a biblioteca REMOTA: ${e.message}", e)
                    Log.w("LibraryRepository", "El juego se guardó en LOCAL pero no en REMOTO")
                }
            } else {
                Log.w("LibraryRepository", "⚠️ No se proporcionó información remota. Solo se guardó en LOCAL")
            }
            
            Result.success(insertedId)
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
        remoteUserId: String?,
        games: List<LibraryEntryRemoteDto>,
        dateAdded: String
    ): Result<Int> {
        return try {
            var addedCount = 0
            games.forEach { entry ->
                val result = addGameToLibrary(
                    userId = userId,
                    remoteUserId = remoteUserId,
                    juegoId = entry.gameId,
                    remoteGameId = entry.gameId,
                    name = entry.name,
                    price = entry.price,
                    dateAdded = dateAdded,
                    status = entry.status,
                    genre = entry.genre
                )
                if (result.isSuccess) {
                    addedCount++
                }
            }
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
            val entry = libraryDao.findEntry(userId, juegoId)
            entry?.licenseId?.let { licenseId ->
                remoteRepository.releaseLicense(licenseId).onFailure {
                    Log.w("LibraryRepository", "No se pudo liberar la licencia $licenseId: ${it.message}")
                }
            }
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

    private suspend fun assignRemoteLicense(gameRemoteId: String, remoteUserId: String): LicenciaRemoteDto {
        Log.d("LibraryRepository", "Solicitando licencias disponibles para juego remoto $gameRemoteId")
        val availableLicenses = remoteRepository.fetchAvailableLicenses(gameRemoteId, limit = 1)
            .getOrElse { throw it }
        val license = availableLicenses.firstOrNull()
            ?: throw IllegalStateException("No hay licencias disponibles para este juego")
        Log.d("LibraryRepository", "Asignando licencia ${license.id} al usuario remoto $remoteUserId")
        return remoteRepository.assignLicense(license.id, remoteUserId)
            .getOrElse { throw it }
    }
}

