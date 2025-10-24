package com.example.uinavegacion.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uinavegacion.data.local.juego.JuegoEntity
import com.example.uinavegacion.data.repository.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para la gestión de juegos en el panel de administrador
 */
class GameManagementViewModel(
    private val gameRepository: GameRepository
): ViewModel() {
    
    // Estado de la lista de juegos
    private val _games = MutableStateFlow<List<JuegoEntity>>(emptyList())
    val games: StateFlow<List<JuegoEntity>> = _games.asStateFlow()
    
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
        loadGames()
    }
    
    /**
     * Carga todos los juegos desde la base de datos
     */
    fun loadGames() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val gamesList = gameRepository.getAllGames()
                _games.value = gamesList
                
            } catch (e: Exception) {
                _error.value = "Error al cargar juegos: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Agrega un nuevo juego
     */
    fun addGame(nombre: String, descripcion: String, precio: Double, stock: Int, imageUrl: String) {
        viewModelScope.launch {
            try {
                Log.d("GameManagementVM", "Intentando agregar juego: $nombre")
                
                // Validar datos
                if (nombre.isBlank()) {
                    _error.value = "El nombre del juego es obligatorio"
                    return@launch
                }
                
                if (precio <= 0) {
                    _error.value = "El precio debe ser mayor a 0"
                    return@launch
                }
                
                if (stock < 0) {
                    _error.value = "El stock no puede ser negativo"
                    return@launch
                }
                
                val nuevoJuego = JuegoEntity(
                    id = 0L, // Room auto-generará el ID
                    nombre = nombre,
                    descripcion = descripcion,
                    precio = precio,
                    stock = stock,
                    imagenUrl = imageUrl.ifEmpty { null },
                    desarrollador = "Desarrollador",
                    fechaLanzamiento = "2024",
                    categoriaId = 1L, // Categoría por defecto
                    generoId = 1L // Género por defecto
                )
                
                Log.d("GameManagementVM", "JuegoEntity creado: $nuevoJuego")
                
                val result = gameRepository.addGame(nuevoJuego)
                if (result.isSuccess) {
                    Log.d("GameManagementVM", "Juego agregado exitosamente con ID: ${result.getOrNull()}")
                    _successMessage.value = "✅ Juego '$nombre' agregado correctamente"
                    loadGames() // Recargar la lista
                } else {
                    val errorMsg = result.exceptionOrNull()?.message ?: "Error desconocido"
                    Log.e("GameManagementVM", "Error al agregar juego: $errorMsg")
                    _error.value = "❌ Error al agregar juego: $errorMsg"
                }
            } catch (e: Exception) {
                Log.e("GameManagementVM", "Excepción al agregar juego", e)
                _error.value = "❌ Error inesperado: ${e.message}"
            }
        }
    }
    
    /**
     * Actualiza un juego existente
     */
    fun updateGame(game: JuegoEntity) {
        viewModelScope.launch {
            try {
                val result = gameRepository.updateGame(game)
                if (result.isSuccess) {
                    _successMessage.value = "Juego actualizado correctamente"
                    loadGames() // Recargar la lista
                } else {
                    _error.value = "Error al actualizar juego: ${result.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _error.value = "Error al actualizar juego: ${e.message}"
            }
        }
    }
    
    /**
     * Elimina un juego
     */
    fun deleteGame(gameId: Long) {
        viewModelScope.launch {
            try {
                val result = gameRepository.deleteGame(gameId)
                if (result.isSuccess) {
                    _successMessage.value = "Juego eliminado correctamente"
                    loadGames() // Recargar la lista
                } else {
                    _error.value = "Error al eliminar juego: ${result.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _error.value = "Error al eliminar juego: ${e.message}"
            }
        }
    }
    
    /**
     * Busca juegos por nombre
     */
    fun searchGames(query: String) {
        if (query.isEmpty()) {
            loadGames()
            return
        }
        
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val searchResults = gameRepository.searchGamesByName(query)
                _games.value = searchResults
                
            } catch (e: Exception) {
                _error.value = "Error en la búsqueda: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Recargar juegos
     */
    fun refreshGames() {
        loadGames()
    }
    
    /**
     * Limpiar mensajes
     */
    fun clearMessages() {
        _error.value = null
        _successMessage.value = null
    }
}