package com.example.uinavegacion.data.repository

import com.example.uinavegacion.data.local.user.UserDao
import com.example.uinavegacion.data.local.juego.JuegoDao
import com.example.uinavegacion.data.local.ordenCompra.OrdenCompraDao
import com.example.uinavegacion.data.local.admin.AdminDao

/**
 * Repositorio para obtener estadísticas del panel de administrador
 */
class AdminStatsRepository(
    private val userDao: UserDao,
    private val juegoDao: JuegoDao,
    private val ordenCompraDao: OrdenCompraDao,
    private val adminDao: AdminDao
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
     */
    suspend fun getTotalOrders(): Int {
        return ordenCompraDao.count()
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