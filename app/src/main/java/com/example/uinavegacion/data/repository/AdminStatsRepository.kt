package com.example.uinavegacion.data.repository

import android.util.Log
import com.example.uinavegacion.data.local.user.UserDao
import com.example.uinavegacion.data.local.juego.JuegoDao
import com.example.uinavegacion.data.local.ordenCompra.OrdenCompraDao
import com.example.uinavegacion.data.local.admin.AdminDao
import com.example.uinavegacion.data.remote.repository.OrderRemoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Repositorio para obtener estadísticas del panel de administrador
 * Sincroniza con microservicios cuando es posible
 */
class AdminStatsRepository(
    private val userDao: UserDao,
    private val juegoDao: JuegoDao,
    private val ordenCompraDao: OrdenCompraDao,
    private val adminDao: AdminDao,
    private val orderRemoteRepository: OrderRemoteRepository = OrderRemoteRepository()
) {
    
    /**
     * Obtiene el número total de usuarios registrados
     */
    suspend fun getTotalUsers(): Int {
        return userDao.count()
    }
    
    /**
     * Obtiene el número total de juegos en el catálogo
     */
    suspend fun getTotalGames(): Int {
        return juegoDao.count()
    }
    
    /**
     * Obtiene el número total de órdenes de compra
     * Intenta sincronizar con el microservicio primero
     */
    suspend fun getTotalOrders(): Int {
        return try {
            // Intentar obtener del microservicio
            Log.d("AdminStatsRepo", "📦 Sincronizando órdenes con microservicio...")
            val remoteResult = orderRemoteRepository.getAllOrders()
            
            if (remoteResult.isSuccess) {
                val remoteOrders = remoteResult.getOrNull()!!
                Log.d("AdminStatsRepo", "✅ Órdenes sincronizadas: ${remoteOrders.size}")
                // TODO: Aquí se podrían sincronizar las órdenes con la BD local si es necesario
                remoteOrders.size
            } else {
                // Fallback a BD local
                Log.w("AdminStatsRepo", "⚠️ Usando BD local para órdenes")
                ordenCompraDao.count()
            }
        } catch (e: Exception) {
            Log.e("AdminStatsRepo", "❌ Error al obtener órdenes: ${e.message}")
            // Fallback a BD local
            ordenCompraDao.count()
        }
    }
    
    /**
     * Obtiene el número total de administradores
     */
    suspend fun getTotalAdmins(): Int {
        return adminDao.count()
    }
    
    /**
     * Obtiene estadísticas completas del dashboard
     */
    suspend fun getDashboardStats(): DashboardStats {
        return DashboardStats(
            totalUsers = getTotalUsers(),
            totalGames = getTotalGames(), 
            totalOrders = getTotalOrders(),
            totalAdmins = getTotalAdmins()
        )
    }

    fun observeDashboardStats(): Flow<DashboardStats> {
        return combine(
            userDao.observeCount(),
            juegoDao.observeCount(),
            ordenCompraDao.observeCount(),
            adminDao.observeCount()
        ) { totalUsers, totalGames, totalOrders, totalAdmins ->
            DashboardStats(
                totalUsers = totalUsers,
                totalGames = totalGames,
                totalOrders = totalOrders,
                totalAdmins = totalAdmins
            )
        }
    }
}

/**
 * Data class para las estadísticas del dashboard
 */
data class DashboardStats(
    val totalUsers: Int,
    val totalGames: Int,
    val totalOrders: Int,
    val totalAdmins: Int
)