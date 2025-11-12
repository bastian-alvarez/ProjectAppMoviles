package com.example.uinavegacion.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.catch
import com.example.uinavegacion.data.repository.LibraryRepository
import com.example.uinavegacion.data.SessionManager
import com.example.uinavegacion.data.local.library.LibraryEntity
import java.text.SimpleDateFormat
import java.util.*

// Data class para estadísticas de biblioteca
data class LibraryStats(
    val totalGames: Int,
    val installedGames: Int,
    val availableGames: Int,
    val downloadingGames: Int = 0
)

// Modelo para juegos en la biblioteca
data class LibraryGame(
    val id: String,
    val name: String,
    val price: Double,
    val dateAdded: String,
    val status: String = "Disponible", // Disponible, Descargando, Instalado
    val genre: String = "Acción" // Género por defecto
)

class LibraryViewModel(
    application: Application,
    private val libraryRepository: LibraryRepository
) : AndroidViewModel(application) {
    
    private val _games = MutableStateFlow<List<LibraryGame>>(emptyList())
    val games: StateFlow<List<LibraryGame>> = _games.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    // Formato de fecha
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    // Obtener ID del usuario actual
    private fun getCurrentUserId(): Long? {
        val currentUser = SessionManager.currentUser.value
        return currentUser?.id
    }

    private fun getCurrentUserRemoteId(): String? = SessionManager.currentUser.value?.remoteId
    
    init {
        loadUserLibrary()
    }
    
    // Cargar biblioteca del usuario desde BD
    private fun loadUserLibrary() {
        val userId = getCurrentUserId()
        if (userId == null) {
            android.util.Log.w("LibraryViewModel", "⚠️ No hay usuario logueado")
            _games.value = emptyList()
            return
        }
        
        android.util.Log.d("LibraryViewModel", "📚 Cargando biblioteca desde BD para usuario $userId...")
        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                libraryRepository.getUserLibrary(userId)
                    .catch { e ->
                        android.util.Log.e("LibraryViewModel", "Error cargando biblioteca", e)
                        _error.value = "Error cargando biblioteca: ${e.message}"
                        _isLoading.value = false
                    }
                    .collect { libraryEntities ->
                        val libraryGames = libraryEntities.map { entity ->
                            LibraryGame(
                                id = entity.juegoId,
                                name = entity.name,
                                price = entity.price,
                                dateAdded = entity.dateAdded,
                                status = entity.status,
                                genre = entity.genre
                            )
                        }
                        _games.value = libraryGames
                        _isLoading.value = false
                        android.util.Log.d("LibraryViewModel", "✅ Biblioteca cargada: ${libraryGames.size} juegos")
                    }
            } catch (e: Exception) {
                android.util.Log.e("LibraryViewModel", "❌ Error en loadUserLibrary", e)
                _error.value = "Error: ${e.message}"
                _isLoading.value = false
            }
        }
    }
    
    // Agregar juegos comprados a la biblioteca en BD
    fun addPurchasedGames(cartItems: List<CartItem>) {
        val userId = getCurrentUserId()
        val remoteUserId = getCurrentUserRemoteId()
        if (userId == null) {
            android.util.Log.e("LibraryViewModel", "❌ No hay usuario logueado para agregar compras")
            _error.value = "No hay usuario logueado"
            return
        }
        
        android.util.Log.d("LibraryViewModel", "🛒 === COMPRA CON BD ===")
        android.util.Log.d("LibraryViewModel", "🛒 Juegos en carrito: ${cartItems.size}")
        
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val currentDate = dateFormat.format(Date())
                
                cartItems.forEach { item ->
                    android.util.Log.d("LibraryViewModel", "🔄 Procesando: ${item.name} (ID: ${item.id})")
                    
                    val result = libraryRepository.addGameToLibrary(
                        userId = userId,
                        remoteUserId = remoteUserId,
                        juegoId = item.id,
                        remoteGameId = item.remoteId,
                        name = item.name,
                        price = item.price,
                        dateAdded = currentDate,
                        status = "Disponible",
                        genre = "Acción" // Se puede obtener del juego si es necesario
                    )
                    
                    if (result.isSuccess) {
                        android.util.Log.d("LibraryViewModel", "✅ ${item.name} agregado a biblioteca")
                    } else {
                        val errorMsg = result.exceptionOrNull()?.message ?: "Error desconocido"
                        android.util.Log.w("LibraryViewModel", "⚠️ ${item.name}: $errorMsg")
                        _error.value = errorMsg
                    }
                }
                
                // Recargar la biblioteca después de agregar los juegos
                loadUserLibrary()
                android.util.Log.d("LibraryViewModel", "🎉 COMPRA COMPLETADA")
            } catch (e: Exception) {
                android.util.Log.e("LibraryViewModel", "❌ Error procesando compra", e)
                _error.value = "Error procesando compra: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // Método público para forzar recarga
    fun forceRefresh() {
        android.util.Log.d("LibraryViewModel", "🔄 === FORZANDO RECARGA ===")
        loadUserLibrary()
    }
    
    // Cargar inmediatamente
    fun loadUserLibraryImmediate() {
        viewModelScope.launch {
            val userId = getCurrentUserId()
            if (userId != null) {
                try {
                    val libraryEntities = libraryRepository.getUserLibraryDirect(userId)
                    val libraryGames = libraryEntities.map { entity ->
                        LibraryGame(
                            id = entity.juegoId,
                            name = entity.name,
                            price = entity.price,
                            dateAdded = entity.dateAdded,
                            status = entity.status,
                            genre = entity.genre
                        )
                    }
                    _games.value = libraryGames
                    android.util.Log.d("LibraryViewModel", "⚡ Biblioteca cargada inmediatamente: ${libraryGames.size} juegos")
                } catch (e: Exception) {
                    android.util.Log.e("LibraryViewModel", "❌ Error en carga inmediata", e)
                    _error.value = "Error: ${e.message}"
                }
            }
        }
    }
    
    // Eliminar juego de la biblioteca
    fun removeGameFromLibrary(gameId: String) {
        val userId = getCurrentUserId()
        if (userId == null) {
            android.util.Log.w("LibraryViewModel", "⚠️ No hay usuario logueado")
            return
        }
        
        viewModelScope.launch {
            try {
                val result = libraryRepository.removeGameFromLibrary(userId, gameId)
                if (result.isSuccess) {
                    android.util.Log.d("LibraryViewModel", "🗑️ Juego $gameId eliminado de biblioteca")
                    loadUserLibrary() // Recargar para reflejar cambios
                } else {
                    android.util.Log.e("LibraryViewModel", "❌ Error eliminando juego", result.exceptionOrNull())
                }
            } catch (e: Exception) {
                android.util.Log.e("LibraryViewModel", "❌ Error en removeGameFromLibrary", e)
            }
        }
    }
    
    // Limpiar biblioteca completa (útil para testing)
    fun clearLibrary() {
        val userId = getCurrentUserId()
        if (userId == null) return
        
        viewModelScope.launch {
            // Esto requeriría un método en el repositorio para limpiar toda la biblioteca
            // Por ahora, solo limpiamos el estado local
            _games.value = emptyList()
            android.util.Log.d("LibraryViewModel", "🧹 Biblioteca limpiada (solo local)")
        }
    }
    
    // Método de compatibilidad
    fun getUserLibrary() = games
    
    // Método para obtener un juego específico
    fun getGameById(gameId: String): LibraryGame? {
        return _games.value.find { it.id == gameId }
    }
    
    // Métodos requeridos por LibraryScreen
    fun getLibraryStats(): LibraryStats {
        val games = _games.value
        return LibraryStats(
            totalGames = games.size,
            installedGames = games.count { it.status == "Instalado" },
            availableGames = games.count { it.status == "Disponible" },
            downloadingGames = games.count { it.status == "Descargando" }
        )
    }
    
    fun getGamesByStatus(status: String): List<LibraryGame> {
        return when (status) {
            "Todos" -> _games.value
            "Instalados" -> _games.value.filter { it.status == "Instalado" }
            "Disponibles" -> _games.value.filter { it.status == "Disponible" }
            "Descargando" -> _games.value.filter { it.status == "Descargando" }
            else -> _games.value.filter { it.status == status }
        }
    }
    
    fun installGame(gameId: String) {
        // Actualizar estado local (la persistencia en BD se puede agregar si es necesario)
        val currentGames = _games.value.toMutableList()
        val gameIndex = currentGames.indexOfFirst { it.id == gameId }
        
        if (gameIndex != -1) {
            currentGames[gameIndex] = currentGames[gameIndex].copy(status = "Instalado")
            _games.value = currentGames
            android.util.Log.d("LibraryViewModel", "🎮 Juego instalado: ${currentGames[gameIndex].name}")
        }
    }
    
    fun updateGameStatus(gameId: String, newStatus: String) {
        val currentGames = _games.value.toMutableList()
        val gameIndex = currentGames.indexOfFirst { it.id == gameId }
        
        if (gameIndex != -1) {
            currentGames[gameIndex] = currentGames[gameIndex].copy(status = newStatus)
            _games.value = currentGames
            android.util.Log.d("LibraryViewModel", "📝 Status actualizado: ${currentGames[gameIndex].name} -> $newStatus")
        }
    }
}
