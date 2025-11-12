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

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: LibraryEntity)

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
}
