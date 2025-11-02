package com.example.uinavegacion.data.local.categoria

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoriaDao {
    // Insertar categoría
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(categoria: CategoriaEntity): Long

    // Buscar categoría por id
    @Query("SELECT * FROM categorias WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): CategoriaEntity?

    // Buscar categoría por nombre
    @Query("SELECT * FROM categorias WHERE nombre = :nombre LIMIT 1")
    suspend fun getByNombre(nombre: String): CategoriaEntity?

    // Obtener todas las categorías
    @Query("SELECT * FROM categorias")
    suspend fun getAll(): List<CategoriaEntity>

    @Query("SELECT * FROM categorias")
    fun observeAll(): Flow<List<CategoriaEntity>>

    // Contar categorías
    @Query("SELECT COUNT(*) FROM categorias")
    suspend fun count(): Int

    // Obtener categorías ordenadas por nombre
    @Query("SELECT * FROM categorias ORDER BY nombre ASC")
    suspend fun getAllOrderedByNombre(): List<CategoriaEntity>

    // Actualizar categoría
    @Query("UPDATE categorias SET nombre = :nombre, descripcion = :descripcion WHERE id = :id")
    suspend fun update(id: Long, nombre: String, descripcion: String)

    // Eliminar categoría
    @Query("DELETE FROM categorias WHERE id = :id")
    suspend fun delete(id: Long)
}
