package com.example.uinavegacion.data.local.juego

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface JuegoDao {
    // Insertar juego
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(juego: JuegoEntity): Long

    // Buscar juego por id
    @Query("SELECT * FROM juegos WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): JuegoEntity?

    // Buscar juego por nombre (solo activos)
    @Query("SELECT * FROM juegos WHERE activo = 1 AND nombre LIKE '%' || :nombre || '%'")
    suspend fun getByNombre(nombre: String): List<JuegoEntity>
    
    // Buscar juego por nombre (incluyendo inactivos)
    @Query("SELECT * FROM juegos WHERE nombre LIKE '%' || :nombre || '%'")
    suspend fun getByNombreIncludingInactive(nombre: String): List<JuegoEntity>

    // Obtener todos los juegos
    @Query("SELECT * FROM juegos WHERE activo = 1")
    suspend fun getAll(): List<JuegoEntity>

    @Query("SELECT * FROM juegos")
    suspend fun getAllIncludingInactive(): List<JuegoEntity>

    // Observar todos los juegos en tiempo real
    @Query("SELECT * FROM juegos WHERE activo = 1")
    fun observeActive(): Flow<List<JuegoEntity>>

    @Query("SELECT * FROM juegos")
    fun observeAll(): Flow<List<JuegoEntity>>

    // Obtener juegos por categoría
    @Query("SELECT * FROM juegos WHERE activo = 1 AND categoriaId = :categoriaId")
    suspend fun getByCategoriaId(categoriaId: Long): List<JuegoEntity>

    // Obtener juegos por género
    @Query("SELECT * FROM juegos WHERE activo = 1 AND generoId = :generoId")
    suspend fun getByGeneroId(generoId: Long): List<JuegoEntity>

    // Contar juegos (solo activos)
    @Query("SELECT COUNT(*) FROM juegos WHERE activo = 1")
    suspend fun count(): Int
    
    // Contar todos los juegos (activos e inactivos)
    @Query("SELECT COUNT(*) FROM juegos")
    suspend fun countAll(): Int

    // Observar conteo de juegos en tiempo real
    @Query("SELECT COUNT(*) FROM juegos WHERE activo = 1")
    fun observeCount(): Flow<Int>

    // Obtener juegos ordenados por nombre
    @Query("SELECT * FROM juegos WHERE activo = 1 ORDER BY nombre ASC")
    suspend fun getAllOrderedByNombre(): List<JuegoEntity>

    // Obtener juegos ordenados por precio
    @Query("SELECT * FROM juegos WHERE activo = 1 ORDER BY precio ASC")
    suspend fun getAllOrderedByPrecio(): List<JuegoEntity>

    // Obtener juegos con stock disponible
    @Query("SELECT * FROM juegos WHERE activo = 1 AND stock > 0")
    suspend fun getJuegosDisponibles(): List<JuegoEntity>

    // Actualizar juego (sin activo - para compatibilidad)
    @Query("UPDATE juegos SET nombre = :nombre, descripcion = :descripcion, precio = :precio, stock = :stock, imagenUrl = :imagenUrl, desarrollador = :desarrollador, fechaLanzamiento = :fechaLanzamiento, categoriaId = :categoriaId, generoId = :generoId WHERE id = :id")
    suspend fun update(id: Long, nombre: String, descripcion: String, precio: Double, stock: Int, imagenUrl: String?, desarrollador: String, fechaLanzamiento: String, categoriaId: Long, generoId: Long)
    
    // Actualizar juego completo (incluyendo activo)
    @Query("UPDATE juegos SET nombre = :nombre, descripcion = :descripcion, precio = :precio, stock = :stock, imagenUrl = :imagenUrl, desarrollador = :desarrollador, fechaLanzamiento = :fechaLanzamiento, categoriaId = :categoriaId, generoId = :generoId, activo = :activo WHERE id = :id")
    suspend fun updateFull(id: Long, nombre: String, descripcion: String, precio: Double, stock: Int, imagenUrl: String?, desarrollador: String, fechaLanzamiento: String, categoriaId: Long, generoId: Long, activo: Boolean)

    // Actualizar solo el stock
    @Query("UPDATE juegos SET stock = :stock WHERE id = :id")
    suspend fun updateStock(id: Long, stock: Int)

    // Desactivar juego
    @Query("UPDATE juegos SET activo = 0 WHERE id = :id")
    suspend fun deactivate(id: Long): Int

    @Query("UPDATE juegos SET activo = 1 WHERE id = :id")
    suspend fun reactivate(id: Long)
    
    @Query("UPDATE juegos SET remoteId = :remoteId WHERE id = :id")
    suspend fun updateRemoteId(id: Long, remoteId: String)
    
    // Eliminar todos los juegos
    @Query("DELETE FROM juegos")
    suspend fun deleteAll()
}
