package com.example.uinavegacion.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uinavegacion.data.repository.AdminStatsRepository
import com.example.uinavegacion.data.repository.DashboardStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para el panel de administrador
 */
class AdminDashboardViewModel(
    private val adminStatsRepository: AdminStatsRepository
): ViewModel() {
    
    // Estado del dashboard
    private val _dashboardStats = MutableStateFlow(
        DashboardStats(
            totalUsers = 0,
            totalGames = 0,
            totalOrders = 0,
            totalAdmins = 0
        )
    )
    val dashboardStats: StateFlow<DashboardStats> = _dashboardStats.asStateFlow()
    
    // Estado de carga
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // Estado de error
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        loadDashboardStats()
    }
    
    /**
     * Carga las estadísticas del dashboard desde la base de datos
     */
    fun loadDashboardStats() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val stats = adminStatsRepository.getDashboardStats()
                _dashboardStats.value = stats
                
            } catch (e: Exception) {
                _error.value = "Error al cargar estadísticas: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Recargar estadísticas
     */
    fun refreshStats() {
        loadDashboardStats()
    }
    
    /**
     * Limpiar error
     */
    fun clearError() {
        _error.value = null
    }
}