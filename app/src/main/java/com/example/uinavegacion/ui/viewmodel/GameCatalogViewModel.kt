package com.example.uinavegacion.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uinavegacion.data.local.categoria.CategoriaDao
import com.example.uinavegacion.data.local.genero.GeneroDao
import com.example.uinavegacion.data.repository.GameRepository
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
    val activo: Boolean
)

class GameCatalogViewModel(
    private val gameRepository: GameRepository,
    private val categoriaDao: CategoriaDao,
    private val generoDao: GeneroDao
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val categoriesMapFlow = categoriaDao.observeAll()
        .map { list -> list.associate { it.id to it.nombre } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyMap()
        )

    private val genresMapFlow = generoDao.observeAll()
        .map { list -> list.associate { it.id to it.nombre } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyMap()
        )

    val games: StateFlow<List<CatalogGameUi>> = combine(
        gameRepository.observeAllGames(),
        categoriesMapFlow,
        genresMapFlow
    ) { games, categoriesMap, genresMap ->
        games.map { entity ->
            CatalogGameUi(
                id = entity.id,
                nombre = entity.nombre,
                descripcion = entity.descripcion,
                precio = entity.precio,
                stock = entity.stock,
                imagenUrl = entity.imageUrl,
                categoriaNombre = categoriesMap[entity.categoriaId] ?: "Categoría ${entity.categoriaId}",
                generoNombre = genresMap[entity.generoId] ?: "Género ${entity.generoId}",
                activo = entity.activo
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

    val categories: StateFlow<List<String>> = categoriesMapFlow
        .map { map -> map.values.filter { it.isNotBlank() }.sorted() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun refresh() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                gameRepository.getAllGames()
            } catch (e: Exception) {
                _error.value = "Error al refrescar juegos: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}

