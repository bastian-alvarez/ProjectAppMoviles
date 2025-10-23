package com.example.uinavegacion.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// Modelo simple para el carrito
data class CartItem(
    val id: String,
    val name: String,
    val price: Double,
    val quantity: Int,
    val imageUrl: String = ""
)

class CartViewModel : ViewModel() {
    private val _items = MutableStateFlow<List<CartItem>>(emptyList())
    val items: StateFlow<List<CartItem>> = _items

    // Agregar juego al carrito
    fun addGame(id: String, name: String, price: Double, imageUrl: String = "") {
        val currentItems = _items.value.toMutableList()
        val existingIndex = currentItems.indexOfFirst { it.id == id }
        
        if (existingIndex >= 0) {
            // Si ya existe, aumentar cantidad
            currentItems[existingIndex] = currentItems[existingIndex].copy(
                quantity = currentItems[existingIndex].quantity + 1
            )
        } else {
            // Si no existe, agregarlo
            currentItems.add(CartItem(id, name, price, 1, imageUrl))
        }
        _items.value = currentItems
    }

    // Remover juego del carrito
    fun removeGame(id: String) {
        _items.value = _items.value.filter { it.id != id }
    }

    // Cambiar cantidad
    fun updateQuantity(id: String, newQuantity: Int) {
        if (newQuantity <= 0) {
            removeGame(id)
            return
        }
        
        _items.value = _items.value.map {
            if (it.id == id) it.copy(quantity = newQuantity) else it
        }
    }

    // Limpiar carrito
    fun clearCart() {
        _items.value = emptyList()
    }

    // Obtener cantidad total de items
    fun getTotalItems(): Int {
        return _items.value.sumOf { it.quantity }
    }

    // Obtener total del carrito
    fun getTotalPrice(): Double {
        return _items.value.sumOf { it.price * it.quantity }
    }

    // Verificar si un juego está en el carrito
    fun isInCart(gameId: String): Boolean {
        return _items.value.any { it.id == gameId }
    }
}
