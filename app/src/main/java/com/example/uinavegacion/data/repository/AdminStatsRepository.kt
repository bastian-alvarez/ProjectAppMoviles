package com.example.uinavegacion.data.repository

import android.util.Log
import com.example.uinavegacion.data.local.user.UserDao
import com.example.uinavegacion.data.local.juego.JuegoDao
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
     * Obtiene el número total de órdenes de compra desde el microservicio
     */
    suspend fun getTotalOrders(): Int {
        return try {
            Log.d("AdminStatsRepo", "📦 Obteniendo órdenes desde microservicio...")
            val remoteResult = orderRemoteRepository.getAllOrders()
            
            if (remoteResult.isSuccess) {
                val remoteOrders = remoteResult.getOrNull()!!
                Log.d("AdminStatsRepo", "✅ Órdenes obtenidas: ${remoteOrders.size}")
                remoteOrders.size
            } else {
                Log.w("AdminStatsRepo", "⚠️ No se pudieron obtener órdenes")
                0
            }
        } catch (e: Exception) {
            Log.e("AdminStatsRepo", "❌ Error al obtener órdenes: ${e.message}")
            0
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
            adminDao.observeCount()
        ) { totalUsers, totalGames, totalAdmins ->
            DashboardStats(
                totalUsers = totalUsers,
                totalGames = totalGames,
                totalOrders = 0, // Las órdenes se obtienen solo via suspend function
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