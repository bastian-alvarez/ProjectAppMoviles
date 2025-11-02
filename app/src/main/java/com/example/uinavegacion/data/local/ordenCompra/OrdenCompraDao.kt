package com.example.uinavegacion.data.local.ordenCompra

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface OrdenCompraDao {
    // Insertar orden de compra
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(orden: OrdenCompraEntity): Long

    // Buscar orden por id
    @Query("SELECT * FROM ordenes_compra WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): OrdenCompraEntity?

    // Obtener todas las órdenes
    @Query("SELECT * FROM ordenes_compra")
    suspend fun getAll(): List<OrdenCompraEntity>

    // Obtener órdenes de un usuario
    @Query("SELECT * FROM ordenes_compra WHERE userId = :userId")
    suspend fun getByUserId(userId: Long): List<OrdenCompraEntity>

    // Obtener órdenes por estado
    @Query("SELECT * FROM ordenes_compra WHERE estadoId = :estadoId")
    suspend fun getByEstadoId(estadoId: Long): List<OrdenCompraEntity>

    // Obtener órdenes de un usuario ordenadas por fecha
    @Query("SELECT * FROM ordenes_compra WHERE userId = :userId ORDER BY fechaOrden DESC")
    suspend fun getByUserIdOrderedByFecha(userId: Long): List<OrdenCompraEntity>

    // Contar órdenes
    @Query("SELECT COUNT(*) FROM ordenes_compra")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM ordenes_compra")
    fun observeCount(): Flow<Int>

    // Calcular total de ventas
    @Query("SELECT SUM(total) FROM ordenes_compra")
    suspend fun getTotalVentas(): Double?

    // Actualizar orden
    @Query("UPDATE ordenes_compra SET fechaOrden = :fecha, total = :total, estadoId = :estadoId, metodoPago = :metodoPago, direccionEnvio = :direccion WHERE id = :id")
    suspend fun update(id: Long, fecha: String, total: Double, estadoId: Long, metodoPago: String, direccion: String?)

    // Actualizar solo el estado
    @Query("UPDATE ordenes_compra SET estadoId = :estadoId WHERE id = :id")
    suspend fun updateEstado(id: Long, estadoId: Long)

    // Eliminar orden
    @Query("DELETE FROM ordenes_compra WHERE id = :id")
    suspend fun delete(id: Long)
}
