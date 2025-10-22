package com.example.uinavegacion.data.local.library

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LibraryDao {
    @Query("SELECT * FROM biblioteca")
    fun getAll(): Flow<List<LibraryEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: LibraryEntity)

    @Query("SELECT COUNT(*) FROM biblioteca WHERE id = :id")
    suspend fun exists(id: String): Int

    @Query("DELETE FROM biblioteca WHERE id = :id")
    suspend fun deleteById(id: String)
}
