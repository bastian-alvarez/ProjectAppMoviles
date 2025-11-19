package com.example.uinavegacion.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
// import com.example.uinavegacion.data.local.categoria.CategoriaDao
// import com.example.uinavegacion.data.local.genero.GeneroDao
import com.example.uinavegacion.data.repository.GameRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Representación de un juego preparado para las pantallas de catálogo.
 */
data class CatalogGameUi(
    val id: Long,
    val nombre: String,
    val descripcion: String,
    val precio: Double,
    val stock: Int,
    val imagenUrl: String,
    val categoriaNombre: String,
    val generoNombre: String,
    val activo: Boolean,
    val descuento: Int = 0,
    val remoteId: String? = null
)

class GameCatalogViewModel(
    private val gameRepository: GameRepository
    // private val categoriaDao: CategoriaDao,
    // private val generoDao: GeneroDao
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Temporalmente sin mapeo de categorías y géneros
    val games: StateFlow<List<CatalogGameUi>> = gameRepository.observeAllGames()
        .map { games ->
        games.map { entity ->
            CatalogGameUi(
                id = entity.id,
                nombre = entity.nombre,
                descripcion = entity.descripcion,
                precio = entity.precio,
                stock = entity.stock,
                    imagenUrl = entity.imagenUrl ?: "",
                    categoriaNombre = "Categoría ${entity.categoriaId}",
                    generoNombre = "Género ${entity.generoId}",
                activo = entity.activo,
                descuento = entity.descuento,
                remoteId = entity.remoteId
            )
        }
    }
        .onStart {
            _isLoading.value = true
        }
        .catch { e ->
            _error.value = "Error al cargar juegos: ${e.message}"
            _isLoading.value = false
            emit(emptyList())
        }
        .onEach {
            _isLoading.value = false
            _error.value = null
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    // Categorías temporalmente deshabilitadas
    val categories: StateFlow<List<String>> = MutableStateFlow<List<String>>(emptyList())
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun refresh() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val result = withContext(Dispatchers.IO) {
                    gameRepository.syncWithRemote()
                }
                result.onFailure { throwable ->
                    _error.value = "Error al sincronizar con catálogo remoto: ${throwable.message}"
                }
            } catch (e: Exception) {
                _error.value = "Error al refrescar juegos: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}

