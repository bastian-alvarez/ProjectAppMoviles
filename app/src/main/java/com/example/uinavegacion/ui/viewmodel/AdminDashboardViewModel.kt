package com.example.uinavegacion.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uinavegacion.data.repository.AdminStatsRepository
import com.example.uinavegacion.data.repository.DashboardStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class AdminDashboardViewModel(
    private val adminStatsRepository: AdminStatsRepository
) : ViewModel() {

    private val _dashboardStats = MutableStateFlow(
        DashboardStats(
            totalUsers = 0,
            totalGames = 0,
            totalOrders = 0,
            totalAdmins = 0
        )
    )
    val dashboardStats: StateFlow<DashboardStats> = _dashboardStats.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        observeStats()
    }

    private fun observeStats() {
        viewModelScope.launch {
            adminStatsRepository.observeDashboardStats()
                .onStart {
                    _isLoading.value = true
                }
                .catch { e ->
                    _error.value = "Error al cargar estadísticas: ${e.message}"
                    _isLoading.value = false
                }
                .collect { stats ->
                    _dashboardStats.value = stats
                    _isLoading.value = false
                    _error.value = null
                }
        }
    }

    fun refreshStats() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val stats = adminStatsRepository.getDashboardStats()
                _dashboardStats.value = stats
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error al actualizar estadísticas: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}