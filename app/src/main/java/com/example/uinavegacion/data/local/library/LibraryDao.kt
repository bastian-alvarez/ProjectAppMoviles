package com.example.uinavegacion.data.local.library

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LibraryDao {
    @Query("SELECT * FROM biblioteca WHERE userId = :userId")
    fun getUserLibrary(userId: Long): Flow<List<LibraryEntity>>

    @Query("SELECT * FROM biblioteca WHERE userId = :userId")
    suspend fun getUserLibraryDirect(userId: Long): List<LibraryEntity>

    @Query("SELECT * FROM biblioteca")
    fun getAll(): Flow<List<LibraryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: LibraryEntity): Long

    @Query("SELECT COUNT(*) FROM biblioteca WHERE userId = :userId AND juegoId = :juegoId")
    suspend fun userOwnsGame(userId: Long, juegoId: String): Int

    @Query("SELECT * FROM biblioteca WHERE userId = :userId AND juegoId = :juegoId LIMIT 1")
    suspend fun findEntry(userId: Long, juegoId: String): LibraryEntity?

    @Query("SELECT COUNT(*) FROM biblioteca WHERE id = :id")
    suspend fun exists(id: Long): Int

    @Query("DELETE FROM biblioteca WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM biblioteca WHERE userId = :userId AND juegoId = :juegoId")
    suspend fun removeGameFromUser(userId: Long, juegoId: String)
    
    // ==================== MÉTODOS DE CACHÉ ====================
    
    /**
     * Elimina entradas de biblioteca cuya caché ha expirado
     * @param expirationTimestamp Timestamp de expiración (ahora - TTL)
     */
    @Query("DELETE FROM biblioteca WHERE cachedAt < :expirationTimestamp")
    suspend fun deleteExpired(expirationTimestamp: Long): Int
    
    /**
     * Limpia toda la caché de biblioteca
     */
    @Query("DELETE FROM biblioteca")
    suspend fun clearCache(): Int
    
    /**
     * Actualiza el timestamp de caché de una entrada
     */
    @Query("UPDATE biblioteca SET cachedAt = :timestamp WHERE id = :id")
    suspend fun updateCachedAt(id: Long, timestamp: Long = System.currentTimeMillis())
}
