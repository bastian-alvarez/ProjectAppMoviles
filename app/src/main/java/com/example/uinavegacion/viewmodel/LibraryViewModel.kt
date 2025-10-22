package com.example.uinavegacion.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.*

// Modelo para juegos en la biblioteca
data class LibraryGame(
    val id: String,
    val name: String,
    val price: Double,
    val dateAdded: String,
    val status: String = "Disponible", // Disponible, Descargando, Instalado
    val genre: String = "Acción" // Género por defecto
)

class LibraryViewModel : ViewModel() {
    private val _games = MutableStateFlow<List<LibraryGame>>(emptyList())
    val games: StateFlow<List<LibraryGame>> = _games

    // Formato de fecha
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // Agregar juegos comprados a la biblioteca
    fun addPurchasedGames(cartItems: List<com.example.uinavegacion.viewmodel.CartItem>) {
        val currentGames = _games.value.toMutableList()
        val currentDate = dateFormat.format(Date())
        
        cartItems.forEach { item ->
            // Verificar que el juego no esté ya en la biblioteca
            if (!currentGames.any { it.id == item.id }) {
                val newGame = LibraryGame(
                    id = item.id,
                    name = item.name,
                    price = item.price,
                    dateAdded = currentDate,
                    status = "Disponible",
                    genre = getGenreForGame(item.name)
                )
                currentGames.add(newGame)
            }
        }
        
        _games.value = currentGames
    }

    // Cambiar estado de un juego (para simular descarga/instalación)
    fun updateGameStatus(gameId: String, newStatus: String) {
        _games.value = _games.value.map { game ->
            if (game.id == gameId) game.copy(status = newStatus) else game
        }
    }

    // Obtener juegos filtrados por estado
    fun getGamesByStatus(status: String): List<LibraryGame> {
        return when (status) {
            "Todos" -> _games.value
            "Instalados" -> _games.value.filter { it.status == "Instalado" }
            "Disponibles" -> _games.value.filter { it.status == "Disponible" }
            "Descargando" -> _games.value.filter { it.status == "Descargando" || it.status == "Actualizando" }
            else -> _games.value
        }
    }

    // Obtener estadísticas de la biblioteca
    fun getLibraryStats(): LibraryStats {
        val allGames = _games.value
        return LibraryStats(
            totalGames = allGames.size,
            installedGames = allGames.count { it.status == "Instalado" },
            availableGames = allGames.count { it.status == "Disponible" },
            downloadingGames = allGames.count { it.status == "Descargando" || it.status == "Actualizando" }
        )
    }

    // Función auxiliar para asignar géneros a los juegos
    private fun getGenreForGame(gameName: String): String {
        return when {
            gameName.contains("Cyberpunk", ignoreCase = true) -> "RPG"
            gameName.contains("Witcher", ignoreCase = true) -> "RPG"
            gameName.contains("Minecraft", ignoreCase = true) -> "Sandbox"
            gameName.contains("Among Us", ignoreCase = true) -> "Multijugador"
            gameName.contains("Valorant", ignoreCase = true) -> "FPS"
            gameName.contains("Fortnite", ignoreCase = true) -> "Battle Royale"
            gameName.contains("FIFA", ignoreCase = true) -> "Deportes"
            gameName.contains("Call of Duty", ignoreCase = true) -> "FPS"
            gameName.contains("Grand Theft Auto", ignoreCase = true) -> "Acción"
            gameName.contains("The Sims", ignoreCase = true) -> "Simulación"
            else -> "Acción" // Género por defecto
        }
    }

    // Simular instalación de un juego (versión para String)
    fun installGame(gameId: String) {
        updateGameStatus(gameId, "Descargando")
        // En una aplicación real, aquí iría la lógica de descarga
        // Por ahora, después de 3 segundos simularemos que está instalado
    }

    // Simular instalación de un juego (versión para Int - usada en UI)
    fun installGame(gameId: Int) {
        val game = _games.value.find { it.id == "game_$gameId" }
        if (game != null) {
            updateGameStatus(game.id, "Descargando")
        }
    }

    // Eliminar un juego de la biblioteca
    fun removeGame(gameId: String) {
        _games.value = _games.value.filter { it.id != gameId }
    }

    // Limpiar biblioteca (para testing)
    fun clearLibrary() {
        _games.value = emptyList()
    }
}

// Modelo para estadísticas de la biblioteca
data class LibraryStats(
    val totalGames: Int,
    val installedGames: Int,
    val availableGames: Int,
    val downloadingGames: Int
)