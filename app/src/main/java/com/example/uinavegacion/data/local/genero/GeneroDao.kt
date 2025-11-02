package com.example.uinavegacion.data.local.genero

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GeneroDao {
    // Insertar género
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(genero: GeneroEntity): Long

    // Buscar género por id
    @Query("SELECT * FROM generos WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): GeneroEntity?

    // Buscar género por nombre
    @Query("SELECT * FROM generos WHERE nombre = :nombre LIMIT 1")
    suspend fun getByNombre(nombre: String): GeneroEntity?

    // Obtener todos los géneros
    @Query("SELECT * FROM generos")
    suspend fun getAll(): List<GeneroEntity>

    @Query("SELECT * FROM generos")
    fun observeAll(): Flow<List<GeneroEntity>>

    // Contar géneros
    @Query("SELECT COUNT(*) FROM generos")
    suspend fun count(): Int

    // Obtener géneros ordenados por nombre
    @Query("SELECT * FROM generos ORDER BY nombre ASC")
    suspend fun getAllOrderedByNombre(): List<GeneroEntity>

    // Actualizar género
    @Query("UPDATE generos SET nombre = :nombre, descripcion = :descripcion WHERE id = :id")
    suspend fun update(id: Long, nombre: String, descripcion: String)

    // Eliminar género
    @Query("DELETE FROM generos WHERE id = :id")
    suspend fun delete(id: Long)
}
