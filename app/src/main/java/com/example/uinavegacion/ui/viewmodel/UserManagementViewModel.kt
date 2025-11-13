package com.example.uinavegacion.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uinavegacion.data.local.user.UserEntity
import com.example.uinavegacion.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para la gestión de usuarios en el panel de administrador
 */
class UserManagementViewModel(
    private val userRepository: UserRepository
): ViewModel() {
    
    // Estado de la lista de usuarios
    private val _users = MutableStateFlow<List<UserEntity>>(emptyList())
    val users: StateFlow<List<UserEntity>> = _users.asStateFlow()
    
    // Estado de carga
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // Estado de error
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    // Estado de mensaje de éxito
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()
    
    init {
        android.util.Log.d("UserManagementVM", "🚀 INIT - Cargando usuarios desde BD")
        loadUsers()
    }
    
    /**
     * Función para recargar cuando se regresa a la pantalla
     */
    fun onScreenResumed() {
        android.util.Log.d("UserManagementVM", "👁️ PANTALLA RESUMIDA - Recargando usuarios")
        loadUsers()
    }
    
    /**
     * Carga todos los usuarios desde la base de datos
     */
    fun loadUsers() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val usersList = userRepository.getAllUsers()
                _users.value = usersList
                
            } catch (e: Exception) {
                _error.value = "Error al cargar usuarios: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Busca usuarios por nombre o email
     */
    fun searchUsers(query: String) {
        if (query.isEmpty()) {
            loadUsers()
            return
        }
        
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val allUsers = userRepository.getAllUsers()
                val filteredUsers = allUsers.filter { user ->
                    user.name.contains(query, ignoreCase = true) ||
                    user.email.contains(query, ignoreCase = true)
                }
                _users.value = filteredUsers
                
            } catch (e: Exception) {
                _error.value = "Error en la búsqueda: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Recargar usuarios
     */
    fun refreshUsers() {
        loadUsers()
    }
    
    /**
     * Limpiar mensajes
     */
    fun clearMessages() {
        _error.value = null
        _successMessage.value = null
    }
    
    /**
     * Bloquear/Desbloquear un usuario
     */
    fun toggleUserBlockStatus(userId: Long, currentlyBlocked: Boolean) {
        viewModelScope.launch {
            try {
                val newStatus = !currentlyBlocked
                val result = userRepository.toggleBlockStatus(userId, newStatus)
                
                if (result.isSuccess) {
                    _successMessage.value = if (newStatus) {
                        "Usuario bloqueado exitosamente"
                    } else {
                        "Usuario desbloqueado exitosamente"
                    }
                    // Recargar la lista de usuarios
                    loadUsers()
                } else {
                    _error.value = "Error al ${if (newStatus) "bloquear" else "desbloquear"} usuario"
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            }
        }
    }
}