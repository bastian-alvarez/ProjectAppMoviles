package com.example.uinavegacion.data.repository

import android.util.Log
import com.example.uinavegacion.data.local.juego.JuegoDao
import com.example.uinavegacion.data.local.juego.JuegoEntity
import com.example.uinavegacion.data.remote.api.CreateGameRequest
import com.example.uinavegacion.data.remote.catalogo.CatalogoGameResponse
import com.example.uinavegacion.data.remote.catalogo.CatalogoRemoteRepository
import com.example.uinavegacion.data.remote.repository.GameCatalogRemoteRepository
import kotlinx.coroutines.flow.Flow
import kotlin.math.absoluteValue

/**
 * Repositorio para gestión de juegos con integración de microservicios
 */
class GameRepository(
    private val juegoDao: JuegoDao,
    private val catalogoRepository: CatalogoRemoteRepository = CatalogoRemoteRepository(),
    private val gameCatalogRepository: GameCatalogRemoteRepository = GameCatalogRemoteRepository()
) {
    
    /**
     * Obtiene todos los juegos
     */
    suspend fun getAllGames(includeInactive: Boolean = false): List<JuegoEntity> {
        return if (includeInactive) juegoDao.getAllIncludingInactive() else juegoDao.getAll()
    }

    suspend fun syncWithRemote(includeInactive: Boolean = false): Result<Unit> {
        Log.d("GameRepository", "Sincronizando catálogo con microservicio remoto")
        return catalogoRepository.fetchGames(includeInactive = includeInactive)
            .mapCatching { response ->
                Log.d("GameRepository", "Recibidos ${response.size} juegos del catálogo remoto")
                juegoDao.deleteAll()
                response
                    .map { it.toEntity() }
                    .forEach { juegoDao.insert(it) }
                Log.d("GameRepository", "Sincronización completada exitosamente")
                Unit
            }
            .onFailure { error ->
                Log.e("GameRepository", "Error en sincronización: ${error.message}", error)
            }
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
    /**
     * Agrega un juego en BD local y microservicio
     */
    suspend fun addGame(game: JuegoEntity): Result<Long> {
        return try {
            // 1. Insertar en BD local primero
            Log.d("GameRepository", "Agregando juego en BD LOCAL: ${game.nombre}")
            val localId = juegoDao.insert(game.copy(activo = true))
            Log.d("GameRepository", "✓ Juego agregado en BD local con ID: $localId")
            
            // 2. Intentar crear en microservicio
            try {
                Log.d("GameRepository", "Creando juego en microservicio: ${game.nombre}")
                val request = com.example.uinavegacion.data.remote.api.CreateGameRequest(
                    nombre = game.nombre,
                    descripcion = game.descripcion,
                    precio = game.precio,
                    stock = game.stock,
                    imagenUrl = game.imagenUrl,
                    desarrollador = game.desarrollador,
                    fechaLanzamiento = game.fechaLanzamiento,
                    categoriaId = game.categoriaId,
                    generoId = game.generoId,
                    descuento = game.descuento,
                    activo = true
                )
                
                val remoteResult = gameCatalogRepository.createGame(request)
                if (remoteResult.isSuccess) {
                    val remoteGame = remoteResult.getOrNull()
                    Log.d("GameRepository", "✓ Juego creado en microservicio con ID: ${remoteGame?.id}")
                    
                    // Actualizar el juego local con el remoteId
                    if (remoteGame != null) {
                        juegoDao.updateRemoteId(localId, remoteGame.id.toString())
                        Log.d("GameRepository", "✓ RemoteId actualizado en BD local")
                    }
                } else {
                    Log.w("GameRepository", "⚠️ No se pudo crear en microservicio: ${remoteResult.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                Log.w("GameRepository", "⚠️ Error al crear en microservicio: ${e.message}", e)
            }
            
            Result.success(localId)
        } catch (e: Exception) {
            Log.e("GameRepository", "Error al agregar juego: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Actualiza un juego en BD local y microservicio
     */
    suspend fun updateGame(game: JuegoEntity): Result<Unit> {
        return try {
            // 1. Actualizar en BD local
            Log.d("GameRepository", "Actualizando juego en BD LOCAL: ${game.nombre}")
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
            Log.d("GameRepository", "✓ Juego actualizado en BD local")
            
            // 2. Actualizar en microservicio
            // Usar remoteId si existe, sino usar el ID local
            val remoteIdLong = game.remoteId?.toLongOrNull() ?: game.id
            
            try {
                Log.d("GameRepository", "Actualizando juego en microservicio: ${game.nombre} (ID: $remoteIdLong)")
                val request = com.example.uinavegacion.data.remote.api.CreateGameRequest(
                    nombre = game.nombre,
                    descripcion = game.descripcion,
                    precio = game.precio,
                    stock = game.stock,
                    imagenUrl = game.imagenUrl,
                    desarrollador = game.desarrollador,
                    fechaLanzamiento = game.fechaLanzamiento,
                    categoriaId = game.categoriaId,
                    generoId = game.generoId,
                    descuento = game.descuento,
                    activo = game.activo
                )
                
                val remoteResult = gameCatalogRepository.updateGame(remoteIdLong, request)
                if (remoteResult.isSuccess) {
                    Log.d("GameRepository", "✓ Juego actualizado en microservicio")
                    
                    // Si no tenía remoteId, guardarlo ahora
                    if (game.remoteId.isNullOrBlank()) {
                        juegoDao.updateRemoteId(game.id, remoteIdLong.toString())
                        Log.d("GameRepository", "✓ RemoteId actualizado: $remoteIdLong")
                    }
                } else {
                    Log.w("GameRepository", "⚠️ No se pudo actualizar en microservicio: ${remoteResult.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                Log.w("GameRepository", "⚠️ Error al actualizar en microservicio: ${e.message}", e)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("GameRepository", "Error al actualizar juego: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Desactiva un juego (marca como inactivo)
     */
    suspend fun deactivateGame(id: Long): Result<Unit> {
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
        if (newStock < 0) {
            return Result.failure(IllegalArgumentException("El stock no puede ser negativo"))
        }
        return try {
            juegoDao.updateStock(id, newStock)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun decreaseStock(id: Long, quantity: Int): Result<Int> {
        if (quantity <= 0) {
            return Result.failure(IllegalArgumentException("La cantidad a descontar debe ser mayor a 0"))
        }
        return try {
            val game = juegoDao.getById(id)
                ?: return Result.failure(IllegalStateException("Juego con id=$id no encontrado"))

            if (!game.activo) {
                return Result.failure(IllegalStateException("El juego ${game.nombre} ya no está disponible"))
            }

            if (game.stock < quantity) {
                return Result.failure(IllegalStateException("Stock insuficiente para ${game.nombre}"))
            }

            val newStock = game.stock - quantity
            
            // Intentar actualizar en el microservicio si tiene remoteId
            if (game.remoteId != null) {
                try {
                    val remoteResult = gameCatalogRepository.decreaseStock(id, quantity)
                    if (remoteResult.isSuccess) {
                        Log.d("GameRepository", "Stock actualizado en microservicio para juego $id")
                    } else {
                        Log.w("GameRepository", "No se pudo actualizar stock en microservicio: ${remoteResult.exceptionOrNull()?.message}")
                    }
                } catch (e: Exception) {
                    Log.w("GameRepository", "Error al actualizar stock remoto (continuando con local): ${e.message}")
                }
            }
            
            // Actualizar en BD local
            juegoDao.updateStock(id, newStock)
            Log.d("GameRepository", "Stock local actualizado para juego $id: $newStock unidades")
            Result.success(newStock)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun reactivateGame(id: Long) {
        juegoDao.reactivate(id)
    }
    
    /**
     * Exporta todos los juegos locales al microservicio remoto
     * Útil para sincronizar la BD local con Laragon
     */
    suspend fun exportLocalGamesToRemote(): Result<String> {
        return try {
            val localGames = juegoDao.getAllIncludingInactive()
            Log.d("GameRepository", "Iniciando exportación de ${localGames.size} juegos al microservicio")
            
            var successCount = 0
            var failCount = 0
            val errors = mutableListOf<String>()
            
            localGames.forEach { game ->
                try {
                    val request = CreateGameRequest(
                        nombre = game.nombre,
                        descripcion = game.descripcion,
                        precio = game.precio,
                        stock = game.stock,
                        imagenUrl = game.imagenUrl,
                        desarrollador = game.desarrollador,
                        fechaLanzamiento = game.fechaLanzamiento,
                        categoriaId = game.categoriaId,
                        generoId = game.generoId,
                        descuento = game.descuento,
                        activo = game.activo
                    )
                    
                    val result = gameCatalogRepository.createGame(request)
                    if (result.isSuccess) {
                        successCount++
                        Log.d("GameRepository", "✓ Juego exportado: ${game.nombre}")
                    } else {
                        failCount++
                        val error = "✗ ${game.nombre}: ${result.exceptionOrNull()?.message}"
                        errors.add(error)
                        Log.w("GameRepository", error)
                    }
                } catch (e: Exception) {
                    failCount++
                    val error = "✗ ${game.nombre}: ${e.message}"
                    errors.add(error)
                    Log.e("GameRepository", error, e)
                }
            }
            
            val summary = buildString {
                append("📤 Exportación completada:\n")
                append("✅ Exitosos: $successCount\n")
                append("❌ Fallidos: $failCount\n")
                if (errors.isNotEmpty() && errors.size <= 5) {
                    append("\nErrores:\n")
                    errors.forEach { append("  $it\n") }
                } else if (errors.size > 5) {
                    append("\nPrimeros 5 errores:\n")
                    errors.take(5).forEach { append("  $it\n") }
                    append("  ... y ${errors.size - 5} más\n")
                }
            }
            
            Log.i("GameRepository", summary)
            Result.success(summary)
        } catch (e: Exception) {
            val errorMsg = "Error general en exportación: ${e.message}"
            Log.e("GameRepository", errorMsg, e)
            Result.failure(Exception(errorMsg))
        }
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
    
    /**
     * Elimina un juego del microservicio y BD local
     */
    suspend fun deleteGame(gameId: Long): Result<Unit> {
        return try {
            val game = juegoDao.getById(gameId)
            if (game == null) {
                return Result.failure(Exception("Juego no encontrado"))
            }
            
            // 1. Eliminar del microservicio
            // Usar remoteId si existe, sino usar el ID local
            val remoteIdLong = game.remoteId?.toLongOrNull() ?: game.id
            
            Log.d("GameRepository", "Eliminando juego del microservicio: ${game.nombre} (ID: $remoteIdLong)")
            val remoteResult = gameCatalogRepository.deleteGame(remoteIdLong)
            
            if (remoteResult.isSuccess) {
                Log.d("GameRepository", "✓ Juego eliminado del microservicio")
            } else {
                Log.w("GameRepository", "⚠️ No se pudo eliminar del microservicio: ${remoteResult.exceptionOrNull()?.message}")
                // Continuar con eliminación local de todos modos
            }
            
            // 2. Eliminar de BD local
            juegoDao.delete(game)
            Log.d("GameRepository", "✓ Juego eliminado de BD local")
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("GameRepository", "Error al eliminar juego: ${e.message}", e)
            Result.failure(e)
        }
    }
}

private fun CatalogoGameResponse.toEntity(): JuegoEntity {
    val descripcionFallback = "Información provista por catálogo remoto"
    val fecha = fechaLanzamiento ?: "N/D"
    val categoria = categoriaId?.toLongOrNull() ?: 1L
    val genero = generoId?.toLongOrNull() ?: 1L

    return JuegoEntity(
        id = remoteIdToLong(id),
        nombre = nombreJuego,
        descripcion = descripcionFallback,
        precio = precio,
        stock = 20,
        imagenUrl = fotoJuego,
        desarrollador = "Catálogo Remoto",
        fechaLanzamiento = fecha,
        categoriaId = categoria,
        generoId = genero,
        activo = estadoId?.equals("ACTIVO", ignoreCase = true) ?: true,
        descuento = 0,
        remoteId = id
    )
}

private fun remoteIdToLong(remoteId: String): Long =
    remoteId.hashCode().toLong().absoluteValue + 1_000_000_000L