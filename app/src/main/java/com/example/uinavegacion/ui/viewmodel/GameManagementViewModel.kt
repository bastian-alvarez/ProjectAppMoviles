package com.example.uinavegacion.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uinavegacion.data.local.juego.JuegoEntity
import com.example.uinavegacion.data.repository.GameRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
        android.util.Log.d("GameManagementVM", "🚀 INIT - Cargando juegos desde la base de datos")
        refreshGames()
    }
    
    
    
    /**
     * Carga todos los juegos desde la base de datos
     */
    /**
     * Función para llamar cuando se regresa a la pantalla
     */
    fun onScreenResumed() {
        android.util.Log.d("GameManagementVM", "👁️ PANTALLA RESUMIDA - Recargando juegos")
        refreshGames()
    }
    
    fun loadGames() {
        refreshGames()
    }
    
    /**
     * Agrega un nuevo juego
     */
    fun addGame(nombre: String, descripcion: String, precio: Double, stock: Int, imageUrl: String) {
        viewModelScope.launch {
            try {
                Log.d("GameManagementVM", "➕ Intentando agregar juego: $nombre")
                _isLoading.value = true
                _error.value = null

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
                    id = 0L,
                    nombre = nombre,
                    descripcion = descripcion,
                    precio = precio,
                    stock = stock,
                    imagenUrl = imageUrl.ifBlank { null },
                    desarrollador = "Desarrollador",
                    fechaLanzamiento = "2024",
                    categoriaId = 1L,
                    generoId = 1L
                )

                val result = gameRepository.addGame(nuevoJuego)
                if (result.isSuccess) {
                    val newId = result.getOrNull() ?: 0L
                    Log.d("GameManagementVM", "✅ Juego agregado exitosamente con ID: $newId")
                    _successMessage.value = "✅ Juego '$nombre' agregado correctamente"
                    _games.value = gameRepository.getAllGames(includeInactive = true)
                } else {
                    val errorMsg = result.exceptionOrNull()?.message ?: "Error desconocido"
                    Log.e("GameManagementVM", "❌ Error al agregar juego: $errorMsg")
                    _error.value = "❌ Error al agregar juego: $errorMsg"
                }
            } catch (e: Exception) {
                Log.e("GameManagementVM", "💥 Excepción al agregar juego", e)
                _error.value = "❌ Error inesperado: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Actualiza un juego existente
     */
    fun updateGame(game: JuegoEntity) {
        viewModelScope.launch {
            try {
                Log.d("GameManagementVM", "🔄 Actualizando juego: ${game.nombre}")
                _isLoading.value = true
                _error.value = null

                val result = gameRepository.updateGame(game)
                if (result.isSuccess) {
                    Log.d("GameManagementVM", "✅ Juego actualizado en BD: ${game.nombre}")
                    _successMessage.value = "✅ Juego '${game.nombre}' actualizado correctamente"
                    _games.value = gameRepository.getAllGames(includeInactive = true)
                } else {
                    val errorMsg = result.exceptionOrNull()?.message ?: "Error desconocido"
                    Log.e("GameManagementVM", "❌ Error actualizando juego: $errorMsg")
                    _error.value = "❌ Error al actualizar juego: $errorMsg"
                }
            } catch (e: Exception) {
                Log.e("GameManagementVM", "💥 Excepción actualizando juego", e)
                _error.value = "❌ Error inesperado: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Elimina un juego
     */
    fun deleteGame(gameId: Long) {
        viewModelScope.launch {
            val gameName = _games.value.find { it.id == gameId }?.nombre ?: "Juego #$gameId"
            Log.d("GameManagementVM", "🗑️ Eliminando juego: $gameName (ID: $gameId)")
            _isLoading.value = true
            _error.value = null
            try {
                val result = gameRepository.deleteGame(gameId)
                if (result.isSuccess) {
                    Log.d("GameManagementVM", "✅ Juego eliminado de BD: $gameName")
                    _successMessage.value = "🗑️ Juego '$gameName' eliminado correctamente"
                    _games.value = gameRepository.getAllGames(includeInactive = true)
                } else {
                    val errorMsg = result.exceptionOrNull()?.message ?: "Error desconocido"
                    Log.e("GameManagementVM", "❌ Error eliminando juego: $errorMsg")
                    _error.value = "❌ Error al eliminar juego: $errorMsg"
                }
            } catch (e: Exception) {
                Log.e("GameManagementVM", "💥 Excepción eliminando juego", e)
                _error.value = "❌ Error inesperado: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Busca juegos por nombre
     */
    fun searchGames(query: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val results = if (query.isBlank()) {
                    gameRepository.getAllGames(includeInactive = true)
                } else {
                    gameRepository.searchGamesByName(query, includeInactive = true)
                }

                _games.value = results
            } catch (e: Exception) {
                Log.e("GameManagementVM", "💥 Error en la búsqueda", e)
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
        viewModelScope.launch {
            try {
                android.util.Log.d("GameManagementVM", "🔄 REFRESH - Recargando juegos desde BD")
                _isLoading.value = true
                _error.value = null
                val syncResult = withContext(Dispatchers.IO) {
                    gameRepository.syncWithRemote(includeInactive = true)
                }
                syncResult.onFailure { throwable ->
                    android.util.Log.e("GameManagementVM", "⚠️ No se pudo sincronizar con catálogo remoto", throwable)
                    _error.value = "No se pudo sincronizar con el catálogo remoto: ${throwable.message}"
                }
                val gamesList = withContext(Dispatchers.IO) {
                    gameRepository.getAllGames(includeInactive = true)
                } // Incluir inactivos para admin
                android.util.Log.d("GameManagementVM", "🎮 Juegos obtenidos: ${'$'}{gamesList.size}")
                _games.value = gamesList
            } catch (e: Exception) {
                android.util.Log.e("GameManagementVM", "❌ Error al cargar juegos", e)
                _error.value = "Error al cargar juegos: ${'$'}{e.message}"
                _games.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    

    /**
     * Limpiar mensajes
     */
    fun clearMessages() {
        _error.value = null
        _successMessage.value = null
    }
    
    /**
     * Diagnosticar y corregir datos incompletos
     */
    fun diagnosticAndFix() {
        viewModelScope.launch {
            try {
                Log.d("GameManagementVM", "🔍 Iniciando diagnóstico de base de datos...")
                _isLoading.value = true
                
                val result = gameRepository.diagnosticAndFixIncompleteData()
                if (result.isSuccess) {
                    val message = result.getOrNull() ?: "Diagnóstico completado"
                    Log.d("GameManagementVM", "📋 Resultado diagnóstico: $message")
                    _successMessage.value = message
                    
                    // Recargar después del diagnóstico
                    val gamesList = gameRepository.getAllGames(includeInactive = true)
                    _games.value = gamesList
                } else {
                    val errorMsg = result.exceptionOrNull()?.message ?: "Error en diagnóstico"
                    Log.e("GameManagementVM", "❌ Error en diagnóstico: $errorMsg")
                    _error.value = "Error en diagnóstico: $errorMsg"
                }
            } catch (e: Exception) {
                Log.e("GameManagementVM", "💥 Excepción en diagnóstico", e)
                _error.value = "Error inesperado: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}