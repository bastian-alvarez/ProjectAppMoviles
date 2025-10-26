package com.example.uinavegacion.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
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

class LibraryViewModel(application: Application) : AndroidViewModel(application) {
    private val _games = MutableStateFlow<List<LibraryGame>>(emptyList())
    val games: StateFlow<List<LibraryGame>> = _games

    // Estado para forzar refrescos
    private val _refreshTrigger = MutableStateFlow(0)

    // 🎮 JUEGOS DISPONIBLES EN MEMORIA (SIN BD)
    private val availableGames = listOf(
        LibraryGame(id = "1", name = "Super Mario Bros", price = 29.99, dateAdded = "", genre = "Plataformas"),
        LibraryGame(id = "2", name = "The Legend of Zelda", price = 39.99, dateAdded = "", genre = "Aventura"),
        LibraryGame(id = "3", name = "Pokémon Red", price = 24.99, dateAdded = "", genre = "RPG"),
        LibraryGame(id = "4", name = "Sonic the Hedgehog", price = 19.99, dateAdded = "", genre = "Plataformas"),
        LibraryGame(id = "5", name = "Final Fantasy VII", price = 49.99, dateAdded = "", genre = "RPG"),
        LibraryGame(id = "6", name = "Street Fighter II", price = 14.99, dateAdded = "", genre = "Lucha"),
        LibraryGame(id = "7", name = "Minecraft", price = 26.99, dateAdded = "", genre = "Sandbox"),
        LibraryGame(id = "8", name = "Call of Duty Modern Warfare", price = 59.99, dateAdded = "", genre = "Shooter"),
        LibraryGame(id = "9", name = "FIFA 24", price = 69.99, dateAdded = "", genre = "Deportes"),
        LibraryGame(id = "10", name = "The Witcher 3 Wild Hunt", price = 39.99, dateAdded = "", genre = "RPG"),
        LibraryGame(id = "11", name = "Cyberpunk 2077", price = 59.99, dateAdded = "", genre = "RPG"),
        LibraryGame(id = "12", name = "Red Dead Redemption 2", price = 49.99, dateAdded = "", genre = "Aventura"),
        LibraryGame(id = "13", name = "Dark Souls III", price = 39.99, dateAdded = "", genre = "RPG"),
        LibraryGame(id = "14", name = "Grand Theft Auto V", price = 29.99, dateAdded = "", genre = "Acción"),
        LibraryGame(id = "15", name = "Elden Ring", price = 59.99, dateAdded = "", genre = "RPG"),
        LibraryGame(id = "16", name = "Overwatch 2", price = 39.99, dateAdded = "", genre = "Shooter"),
        LibraryGame(id = "17", name = "Among Us", price = 4.99, dateAdded = "", genre = "Social"),
        LibraryGame(id = "18", name = "Valorant", price = 19.99, dateAdded = "", genre = "Shooter"),
        LibraryGame(id = "19", name = "Assassin's Creed Valhalla", price = 59.99, dateAdded = "", genre = "Aventura"),
        LibraryGame(id = "20", name = "Fortnite", price = 0.0, dateAdded = "", genre = "Battle Royale")
    )

    // 📚 BIBLIOTECA DEL USUARIO (EN MEMORIA)
    private val _userLibrary = MutableStateFlow<List<LibraryGame>>(emptyList())

    // Formato de fecha
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    init {
        loadUserLibrary()
    }

    // ✨ NUEVA VERSIÓN SIN BD - Agregar juegos comprados usando memoria
    fun addPurchasedGames(cartItems: List<com.example.uinavegacion.viewmodel.CartItem>) {
        android.util.Log.d("LibraryViewModel", "🛒 === COMPRA SIN BD (MEMORIA) ===")
        android.util.Log.d("LibraryViewModel", "🛒 Juegos en carrito: ${cartItems.size}")
        
        val currentDate = dateFormat.format(Date())
        val currentLibrary = _userLibrary.value.toMutableList()
        
        // Mostrar juegos que se van a procesar
        cartItems.forEachIndexed { index, item ->
            android.util.Log.d("LibraryViewModel", "  [$index] ${item.name} (ID: ${item.id}) - $${item.price}")
        }
        
        // Procesar cada juego del carrito
        cartItems.forEach { item ->
            android.util.Log.d("LibraryViewModel", "🔄 Procesando compra: ${item.name} (ID: ${item.id})")
            
            // Buscar el juego en nuestros datos de memoria
            val gameData = availableGames.find { it.id == item.id }
            
            if (gameData != null) {
                // Verificar si ya está en la biblioteca
                val alreadyOwned = currentLibrary.any { it.id == item.id }
                
                if (!alreadyOwned) {
                    // Crear nueva entrada en biblioteca
                    val libraryGame = gameData.copy(
                        dateAdded = currentDate,
                        status = "Disponible"
                    )
                    currentLibrary.add(libraryGame)
                    android.util.Log.d("LibraryViewModel", "✅ ${item.name} agregado a biblioteca")
                } else {
                    android.util.Log.w("LibraryViewModel", "⚠️ ${item.name} ya está en biblioteca")
                }
            } else {
                android.util.Log.e("LibraryViewModel", "❌ Juego no encontrado: ${item.name} (ID: ${item.id})")
                // Debug: mostrar todos los IDs disponibles
                android.util.Log.d("LibraryViewModel", "🔍 IDs disponibles: ${availableGames.map { it.id }.joinToString(", ")}")
            }
        }
        
        // Actualizar la biblioteca del usuario
        _userLibrary.value = currentLibrary
        _games.value = currentLibrary
        
        android.util.Log.d("LibraryViewModel", "🎉 COMPRA COMPLETADA - Biblioteca actualizada con ${currentLibrary.size} juegos")
        
        // Mostrar biblioteca actual
        android.util.Log.d("LibraryViewModel", "📚 === BIBLIOTECA ACTUAL ===")
        currentLibrary.forEachIndexed { index, game ->
            android.util.Log.d("LibraryViewModel", "  [$index] ${game.name} - ${game.dateAdded}")
        }
    }

    // Cargar biblioteca del usuario (versión sin BD)
    private fun loadUserLibrary() {
        android.util.Log.d("LibraryViewModel", "📚 Cargando biblioteca desde memoria...")
        _games.value = _userLibrary.value
    }

    // Método público para forzar recarga
    fun forceRefresh() {
        android.util.Log.d("LibraryViewModel", "🔄 === FORZANDO RECARGA (MEMORIA) ===")
        _refreshTrigger.value = _refreshTrigger.value + 1
        
        viewModelScope.launch {
            try {
                delay(50) // Pequeña pausa
                loadUserLibrary()
                
                // Debug: mostrar biblioteca actual
                android.util.Log.d("LibraryViewModel", "📚 === BIBLIOTECA ACTUAL EN REFRESH ===")
                _games.value.forEachIndexed { index, game ->
                    android.util.Log.d("LibraryViewModel", "  [$index] ${game.name} - ${game.dateAdded}")
                }
            } catch (e: Exception) {
                android.util.Log.e("LibraryViewModel", "❌ ERROR en forceRefresh: ${e.message}", e)
            }
        }
    }

    // Cargar inmediatamente (sin BD)
    fun loadUserLibraryImmediate() {
        android.util.Log.d("LibraryViewModel", "⚡ === CARGA INMEDIATA (MEMORIA) ===")
        _games.value = _userLibrary.value
        android.util.Log.d("LibraryViewModel", "⚡ Biblioteca cargada: ${_games.value.size} juegos")
    }

    // Eliminar juego de la biblioteca
    fun removeGameFromLibrary(gameId: String) {
        val currentLibrary = _userLibrary.value.toMutableList()
        val gameToRemove = currentLibrary.find { it.id == gameId }
        
        if (gameToRemove != null) {
            currentLibrary.remove(gameToRemove)
            _userLibrary.value = currentLibrary
            _games.value = currentLibrary
            android.util.Log.d("LibraryViewModel", "🗑️ ${gameToRemove.name} eliminado de biblioteca")
        }
    }

    // Limpiar biblioteca completa
    fun clearLibrary() {
        _userLibrary.value = emptyList()
        _games.value = emptyList()
        android.util.Log.d("LibraryViewModel", "🧹 Biblioteca limpiada")
    }

    // Método de compatibilidad para que no se rompa el resto del código
    fun getUserLibrary() = _games

    // Método para obtener un juego específico
    fun getGameById(gameId: String): LibraryGame? {
        return availableGames.find { it.id == gameId }
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
            "Todos" -> _games.value // Mostrar todos los juegos
            "Instalados" -> _games.value.filter { it.status == "Instalado" }
            "Disponibles" -> _games.value.filter { it.status == "Disponible" }
            "Descargando" -> _games.value.filter { it.status == "Descargando" }
            else -> _games.value.filter { it.status == status }
        }
    }

    fun installGame(gameId: String) {
        val currentLibrary = _userLibrary.value.toMutableList()
        val gameIndex = currentLibrary.indexOfFirst { it.id == gameId }
        
        if (gameIndex != -1) {
            currentLibrary[gameIndex] = currentLibrary[gameIndex].copy(status = "Instalado")
            _userLibrary.value = currentLibrary
            _games.value = currentLibrary
            android.util.Log.d("LibraryViewModel", "🎮 Juego instalado: ${currentLibrary[gameIndex].name}")
        }
    }

    fun updateGameStatus(gameId: String, newStatus: String) {
        val currentLibrary = _userLibrary.value.toMutableList()
        val gameIndex = currentLibrary.indexOfFirst { it.id == gameId }
        
        if (gameIndex != -1) {
            currentLibrary[gameIndex] = currentLibrary[gameIndex].copy(status = newStatus)
            _userLibrary.value = currentLibrary
            _games.value = currentLibrary
            android.util.Log.d("LibraryViewModel", "📝 Status actualizado: ${currentLibrary[gameIndex].name} -> $newStatus")
        }
    }
}