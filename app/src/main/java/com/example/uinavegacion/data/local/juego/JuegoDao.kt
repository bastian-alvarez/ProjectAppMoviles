package com.example.uinavegacion.data.local.juego

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface JuegoDao {
    // Insertar juego
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(juego: JuegoEntity): Long

    // Buscar juego por id
    @Query("SELECT * FROM juegos WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): JuegoEntity?

    // Buscar juego por nombre
    @Query("SELECT * FROM juegos WHERE nombre LIKE '%' || :nombre || '%'")
    suspend fun getByNombre(nombre: String): List<JuegoEntity>

    // Obtener todos los juegos
    @Query("SELECT * FROM juegos")
    suspend fun getAll(): List<JuegoEntity>

    // Obtener juegos por categoría
    @Query("SELECT * FROM juegos WHERE categoriaId = :categoriaId")
    suspend fun getByCategoriaId(categoriaId: Long): List<JuegoEntity>

    // Obtener juegos por género
    @Query("SELECT * FROM juegos WHERE generoId = :generoId")
    suspend fun getByGeneroId(generoId: Long): List<JuegoEntity>

    // Contar juegos
    @Query("SELECT COUNT(*) FROM juegos")
    suspend fun count(): Int

    // Obtener juegos ordenados por nombre
    @Query("SELECT * FROM juegos ORDER BY nombre ASC")
    suspend fun getAllOrderedByNombre(): List<JuegoEntity>

    // Obtener juegos ordenados por precio
    @Query("SELECT * FROM juegos ORDER BY precio ASC")
    suspend fun getAllOrderedByPrecio(): List<JuegoEntity>

    // Obtener juegos con stock disponible
    @Query("SELECT * FROM juegos WHERE stock > 0")
    suspend fun getJuegosDisponibles(): List<JuegoEntity>

    // Actualizar juego
    @Query("UPDATE juegos SET nombre = :nombre, descripcion = :descripcion, precio = :precio, stock = :stock, imagenUrl = :imagenUrl, desarrollador = :desarrollador, fechaLanzamiento = :fechaLanzamiento, categoriaId = :categoriaId, generoId = :generoId WHERE id = :id")
    suspend fun update(id: Long, nombre: String, descripcion: String, precio: Double, stock: Int, imagenUrl: String?, desarrollador: String, fechaLanzamiento: String, categoriaId: Long, generoId: Long)

    // Actualizar solo el stock
    @Query("UPDATE juegos SET stock = :stock WHERE id = :id")
    suspend fun updateStock(id: Long, stock: Int)

    // Eliminar juego
    @Query("DELETE FROM juegos WHERE id = :id")
    suspend fun delete(id: Long)
    
    // Eliminar todos los juegos
    @Query("DELETE FROM juegos")
    suspend fun deleteAll()
}
