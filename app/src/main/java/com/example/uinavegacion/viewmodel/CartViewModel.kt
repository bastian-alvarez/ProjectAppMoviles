package com.example.uinavegacion.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uinavegacion.data.local.database.AppDatabase
import com.example.uinavegacion.data.repository.GameRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CartItem(
    val id: String,
    val remoteId: String? = null,
    val name: String,
    val price: Double,
    val quantity: Int,
    val imageUrl: String = "",
    val originalPrice: Double? = null,
    val discount: Int = 0,
    val maxStock: Int
) {
    val hasDiscount: Boolean
        get() = discount > 0 && originalPrice != null
}

class CartViewModel : ViewModel() {
    private val _items = MutableStateFlow<List<CartItem>>(emptyList())
    val items: StateFlow<List<CartItem>> = _items

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage

    companion object {
        const val MAX_LICENSES_PER_PURCHASE = 3
    }

    fun addGame(
        id: String,
        remoteId: String? = null,
        name: String,
        price: Double,
        imageUrl: String = "",
        originalPrice: Double? = null,
        discount: Int = 0,
        maxStock: Int
    ): Boolean {
        val currentItems = _items.value.toMutableList()
        val existingIndex = currentItems.indexOfFirst { it.id == id }
        val currentTotalLicenses = currentItems.sumOf { it.quantity }

        if (existingIndex >= 0) {
            val existingItem = currentItems[existingIndex]
            val newQuantity = existingItem.quantity + 1

            if (newQuantity > existingItem.maxStock) {
                _errorMessage.value = "Solo hay ${existingItem.maxStock} unidades disponibles"
                _successMessage.value = null
                return false
            }

            if (currentTotalLicenses >= MAX_LICENSES_PER_PURCHASE) {
                _errorMessage.value = "No puedes comprar más de $MAX_LICENSES_PER_PURCHASE licencias en una sola compra"
                _successMessage.value = null
                return false
            }

            currentItems[existingIndex] = existingItem.copy(quantity = newQuantity)
            _successMessage.value = "✓ Cantidad actualizada en el carrito"
        } else {
            if (maxStock <= 0) {
                _errorMessage.value = "Este juego no tiene stock disponible"
                _successMessage.value = null
                return false
            }

            if (currentTotalLicenses >= MAX_LICENSES_PER_PURCHASE) {
                _errorMessage.value = "No puedes comprar más de $MAX_LICENSES_PER_PURCHASE licencias en una sola compra"
                _successMessage.value = null
                return false
            }

            currentItems.add(
                CartItem(
                    id = id,
                    remoteId = remoteId,
                    name = name,
                    price = price,
                    quantity = 1,
                    imageUrl = imageUrl,
                    originalPrice = originalPrice,
                    discount = discount,
                    maxStock = maxStock
                )
            )
            _successMessage.value = "✓ $name agregado al carrito"
        }

        _items.value = currentItems
        _errorMessage.value = null
        return true
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun clearSuccessMessage() {
        _successMessage.value = null
    }

    fun removeGame(id: String) {
        _items.value = _items.value.filter { it.id != id }
    }

    fun updateQuantity(id: String, newQuantity: Int): Boolean {
        if (newQuantity <= 0) {
            removeGame(id)
            return true
        }

        val item = _items.value.find { it.id == id } ?: return false
        val currentTotalLicenses = _items.value.filter { it.id != id }.sumOf { it.quantity }

        if (newQuantity > item.maxStock) {
            _errorMessage.value = "Solo hay ${item.maxStock} unidades disponibles"
            return false
        }

        if (currentTotalLicenses + newQuantity > MAX_LICENSES_PER_PURCHASE) {
            _errorMessage.value = "No puedes comprar más de $MAX_LICENSES_PER_PURCHASE licencias en una sola compra"
            return false
        }

        _items.value = _items.value.map {
            if (it.id == id) it.copy(quantity = newQuantity) else it
        }
        _errorMessage.value = null
        return true
    }

    fun clearCart() {
        _items.value = emptyList()
    }

    fun getTotalItems(): Int {
        return _items.value.sumOf { it.quantity }
    }

    fun getTotalPrice(): Double {
        return _items.value.sumOf { it.price * it.quantity }
    }

    fun isInCart(gameId: String): Boolean {
        return _items.value.any { it.id == gameId }
    }

    fun getQuantity(gameId: String): Int {
        return _items.value.find { it.id == gameId }?.quantity ?: 0
    }

    fun checkout(context: Context, onResult: (Boolean, String?) -> Unit) {
        val currentItems = _items.value
        if (currentItems.isEmpty()) {
            onResult(false, "El carrito está vacío")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val db = AppDatabase.getInstance(context.applicationContext)
                val repository = GameRepository(db.juegoDao())

                currentItems.forEach { item ->
                    val gameId = item.id.toLongOrNull()
                        ?: throw IllegalArgumentException("ID de juego inválido")
                    val game = repository.getGameById(gameId)
                        ?: throw IllegalStateException("Juego no encontrado")

                    if (!game.activo) {
                        throw IllegalStateException("El juego ${game.nombre} ya no está disponible")
                    }

                    if (game.stock < item.quantity) {
                        throw IllegalStateException("Stock insuficiente para ${game.nombre}")
                    }

                    val updateResult = repository.updateStock(gameId, game.stock - item.quantity)
                    if (updateResult.isFailure) {
                        throw updateResult.exceptionOrNull()
                            ?: IllegalStateException("No se pudo actualizar el stock de ${game.nombre}")
                    }
                }

                launch(Dispatchers.Main) {
                    clearCart()
                    onResult(true, "Compra confirmada")
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    onResult(false, e.message ?: "Error al procesar la compra")
                }
            }
        }
    }
}
