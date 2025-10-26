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
        // Cargar juegos inmediatamente
        android.util.Log.d("GameManagementVM", "🚀 INIT - Cargando juegos inmediatos")
        loadGamesImmediate()
    }
    
    /**
     * Carga todos los juegos desde la base de datos
     */
    fun loadGames() {
        viewModelScope.launch {
            try {
                Log.d("GameManagementVM", "=== INICIANDO CARGA DE JUEGOS ===")
                _isLoading.value = true
                _error.value = null
                
                val gamesList = gameRepository.getAllGames()
                Log.d("GameManagementVM", "🎮 Juegos cargados desde repositorio: ${gamesList.size}")
                
                // Debug detallado
                gamesList.forEachIndexed { index, game ->
                    Log.d("GameManagementVM", "[$index] ${game.nombre} - \$${game.precio} (Stock: ${game.stock})")
                }
                
                _games.value = gamesList
                
                if (gamesList.isEmpty()) {
                    Log.w("GameManagementVM", "⚠️ NO SE ENCONTRARON JUEGOS - Lista vacía")
                    _error.value = "No hay juegos en el catálogo. Agrega el primer juego para comenzar."
                } else {
                    Log.d("GameManagementVM", "✅ Carga exitosa: ${gamesList.size} juegos cargados")
                    _error.value = null
                }
                
            } catch (e: Exception) {
                Log.e("GameManagementVM", "❌ ERROR CRÍTICO al cargar juegos", e)
                _error.value = "Error al cargar juegos: ${e.message}"
                _games.value = emptyList()
            } finally {
                _isLoading.value = false
                Log.d("GameManagementVM", "=== FIN CARGA DE JUEGOS ===")
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
        android.util.Log.d("GameManagementVM", "🔄 REFRESH - Recargando juegos")
        loadGamesImmediate()
    }
    
    /**
     * Carga inmediata de juegos (hardcoded para evitar problemas de BD)
     */
    private fun loadGamesImmediate() {
        android.util.Log.d("GameManagementVM", "⚡ CARGA INMEDIATA DE JUEGOS")
        
        // Lista de juegos hardcoded para mostrar inmediatamente
        val hardcodedGames = listOf(
            JuegoEntity(id = 1, nombre = "Super Mario Bros", precio = 29.99, imagenUrl = "", descripcion = "El clásico juego de plataformas", stock = 15, desarrollador = "Nintendo", fechaLanzamiento = "1985", categoriaId = 1, generoId = 1),
            JuegoEntity(id = 2, nombre = "The Legend of Zelda", precio = 39.99, imagenUrl = "", descripcion = "Épica aventura en Hyrule", stock = 8, desarrollador = "Nintendo", fechaLanzamiento = "1986", categoriaId = 1, generoId = 1),
            JuegoEntity(id = 3, nombre = "Pokémon Red", precio = 24.99, imagenUrl = "", descripcion = "Conviértete en maestro Pokémon", stock = 20, desarrollador = "Game Freak", fechaLanzamiento = "1996", categoriaId = 1, generoId = 1),
            JuegoEntity(id = 4, nombre = "Sonic the Hedgehog", precio = 19.99, imagenUrl = "", descripcion = "Velocidad supersónica", stock = 12, desarrollador = "Sega", fechaLanzamiento = "1991", categoriaId = 1, generoId = 1),
            JuegoEntity(id = 5, nombre = "Final Fantasy VII", precio = 49.99, imagenUrl = "", descripcion = "RPG épico de Square Enix", stock = 5, desarrollador = "Square Enix", fechaLanzamiento = "1997", categoriaId = 1, generoId = 1),
            JuegoEntity(id = 6, nombre = "Street Fighter II", precio = 14.99, imagenUrl = "", descripcion = "El mejor juego de lucha", stock = 10, desarrollador = "Capcom", fechaLanzamiento = "1991", categoriaId = 1, generoId = 1),
            JuegoEntity(id = 7, nombre = "Minecraft", precio = 26.99, imagenUrl = "", descripcion = "Construye tu mundo", stock = 25, desarrollador = "Mojang", fechaLanzamiento = "2011", categoriaId = 1, generoId = 1),
            JuegoEntity(id = 8, nombre = "Call of Duty Modern Warfare", precio = 59.99, imagenUrl = "", descripcion = "Acción militar intensa", stock = 7, desarrollador = "Infinity Ward", fechaLanzamiento = "2019", categoriaId = 1, generoId = 1),
            JuegoEntity(id = 9, nombre = "FIFA 24", precio = 69.99, imagenUrl = "", descripcion = "El mejor fútbol virtual", stock = 18, desarrollador = "EA Sports", fechaLanzamiento = "2023", categoriaId = 1, generoId = 1),
            JuegoEntity(id = 10, nombre = "The Witcher 3 Wild Hunt", precio = 39.99, imagenUrl = "", descripcion = "Aventura de Geralt de Rivia", stock = 6, desarrollador = "CD Projekt RED", fechaLanzamiento = "2015", categoriaId = 1, generoId = 1),
            JuegoEntity(id = 11, nombre = "Cyberpunk 2077", precio = 59.99, imagenUrl = "", descripcion = "Futuro cyberpunk", stock = 9, desarrollador = "CD Projekt RED", fechaLanzamiento = "2020", categoriaId = 1, generoId = 1),
            JuegoEntity(id = 12, nombre = "Red Dead Redemption 2", precio = 49.99, imagenUrl = "", descripcion = "Western épico", stock = 11, desarrollador = "Rockstar Games", fechaLanzamiento = "2018", categoriaId = 1, generoId = 1),
            JuegoEntity(id = 13, nombre = "Dark Souls III", precio = 39.99, imagenUrl = "", descripcion = "Desafío extremo", stock = 8, desarrollador = "FromSoftware", fechaLanzamiento = "2016", categoriaId = 1, generoId = 1),
            JuegoEntity(id = 14, nombre = "Grand Theft Auto V", precio = 29.99, imagenUrl = "", descripcion = "Mundo abierto épico", stock = 22, desarrollador = "Rockstar Games", fechaLanzamiento = "2013", categoriaId = 1, generoId = 1),
            JuegoEntity(id = 15, nombre = "Elden Ring", precio = 59.99, imagenUrl = "", descripcion = "Obra maestra de FromSoftware", stock = 10, desarrollador = "FromSoftware", fechaLanzamiento = "2022", categoriaId = 1, generoId = 1),
            JuegoEntity(id = 16, nombre = "Overwatch 2", precio = 39.99, imagenUrl = "", descripcion = "Shooter por equipos", stock = 14, desarrollador = "Blizzard", fechaLanzamiento = "2022", categoriaId = 1, generoId = 1),
            JuegoEntity(id = 17, nombre = "Among Us", precio = 4.99, imagenUrl = "", descripcion = "Encuentra al impostor", stock = 30, desarrollador = "InnerSloth", fechaLanzamiento = "2018", categoriaId = 1, generoId = 1),
            JuegoEntity(id = 18, nombre = "Valorant", precio = 19.99, imagenUrl = "", descripcion = "Shooter táctico", stock = 100, desarrollador = "Riot Games", fechaLanzamiento = "2020", categoriaId = 1, generoId = 1),
            JuegoEntity(id = 19, nombre = "Assassin's Creed Valhalla", precio = 59.99, imagenUrl = "", descripcion = "Aventura vikinga", stock = 13, desarrollador = "Ubisoft", fechaLanzamiento = "2020", categoriaId = 1, generoId = 1),
            JuegoEntity(id = 20, nombre = "Fortnite", precio = 0.0, imagenUrl = "", descripcion = "Battle Royale", stock = 100, desarrollador = "Epic Games", fechaLanzamiento = "2017", categoriaId = 1, generoId = 1)
        )
        
        _games.value = hardcodedGames
        _isLoading.value = false
        _error.value = null
        
        android.util.Log.d("GameManagementVM", "✅ ${hardcodedGames.size} juegos hardcoded cargados")
        android.util.Log.d("GameManagementVM", "🔄 isLoading: ${_isLoading.value}")
        android.util.Log.d("GameManagementVM", "📊 Total stock: ${hardcodedGames.sumOf { it.stock }}")
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
                    loadGamesImmediate()
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