package com.example.uinavegacion.data.local.resena

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ResenaDao {
    // Insertar reseña
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(resena: ResenaEntity): Long

    // Obtener todas las reseñas de un juego (solo las no eliminadas)
    @Query("SELECT * FROM resenas WHERE juegoId = :juegoId AND isDeleted = 0 ORDER BY fechaCreacion DESC")
    suspend fun getByJuegoId(juegoId: Long): List<ResenaEntity>

    // Obtener todas las reseñas de un juego (incluyendo eliminadas) - para moderadores
    @Query("SELECT * FROM resenas WHERE juegoId = :juegoId ORDER BY fechaCreacion DESC")
    suspend fun getAllByJuegoId(juegoId: Long): List<ResenaEntity>

    // Obtener todas las reseñas (solo no eliminadas)
    @Query("SELECT * FROM resenas WHERE isDeleted = 0 ORDER BY fechaCreacion DESC")
    suspend fun getAll(): List<ResenaEntity>

    // Obtener todas las reseñas (incluyendo eliminadas) - para moderadores
    @Query("SELECT * FROM resenas ORDER BY fechaCreacion DESC")
    suspend fun getAllIncludingDeleted(): List<ResenaEntity>

    // Obtener reseña por ID
    @Query("SELECT * FROM resenas WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ResenaEntity?

    // Verificar si un usuario ya hizo una reseña para un juego
    @Query("SELECT * FROM resenas WHERE userId = :userId AND juegoId = :juegoId AND isDeleted = 0 LIMIT 1")
    suspend fun getByUserAndJuego(userId: Long, juegoId: Long): ResenaEntity?

    // Soft delete (marcar como eliminada)
    @Query("UPDATE resenas SET isDeleted = 1 WHERE id = :id")
    suspend fun delete(id: Long)

    // Restaurar reseña eliminada
    @Query("UPDATE resenas SET isDeleted = 0 WHERE id = :id")
    suspend fun restore(id: Long)

    // Obtener promedio de calificaciones de un juego
    @Query("SELECT AVG(calificacion) FROM resenas WHERE juegoId = :juegoId AND isDeleted = 0")
    suspend fun getAverageRating(juegoId: Long): Double?

    // Contar reseñas de un juego
    @Query("SELECT COUNT(*) FROM resenas WHERE juegoId = :juegoId AND isDeleted = 0")
    suspend fun countByJuegoId(juegoId: Long): Int

    // Observar reseñas de un juego
    @Query("SELECT * FROM resenas WHERE juegoId = :juegoId AND isDeleted = 0 ORDER BY fechaCreacion DESC")
    fun observeByJuegoId(juegoId: Long): Flow<List<ResenaEntity>>
}

