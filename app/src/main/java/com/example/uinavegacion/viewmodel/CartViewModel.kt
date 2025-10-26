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
    val imageUrl: String = "",
    val originalPrice: Double? = null,  // Precio original si hay descuento
    val discount: Int = 0  // Porcentaje de descuento
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

    // Agregar juego al carrito
    fun addGame(id: String, name: String, price: Double, imageUrl: String = "", originalPrice: Double? = null, discount: Int = 0): Boolean {
        val currentItems = _items.value.toMutableList()
        val existingIndex = currentItems.indexOfFirst { it.id == id }
        
        // Calcular el total de licencias actual
        val currentTotalLicenses = currentItems.sumOf { it.quantity }
        
        if (existingIndex >= 0) {
            // Si ya existe, verificar si se puede aumentar cantidad
            val newQuantity = currentItems[existingIndex].quantity + 1
            
            if (currentTotalLicenses >= MAX_LICENSES_PER_PURCHASE) {
                _errorMessage.value = "No puedes comprar más de $MAX_LICENSES_PER_PURCHASE licencias en una sola compra"
                _successMessage.value = null
                return false
            }
            
            currentItems[existingIndex] = currentItems[existingIndex].copy(
                quantity = newQuantity
            )
            _successMessage.value = "✓ Cantidad actualizada en el carrito"
        } else {
            // Si no existe, verificar si hay espacio para agregarlo
            if (currentTotalLicenses >= MAX_LICENSES_PER_PURCHASE) {
                _errorMessage.value = "No puedes comprar más de $MAX_LICENSES_PER_PURCHASE licencias en una sola compra"
                _successMessage.value = null
                return false
            }
            
            // Agregarlo
            currentItems.add(CartItem(id, name, price, 1, imageUrl, originalPrice, discount))
            _successMessage.value = "✓ $name agregado al carrito"
        }
        _items.value = currentItems
        _errorMessage.value = null
        return true
    }
    
    // Limpiar mensaje de error
    fun clearErrorMessage() {
        _errorMessage.value = null
    }
    
    // Limpiar mensaje de éxito
    fun clearSuccessMessage() {
        _successMessage.value = null
    }

    // Remover juego del carrito
    fun removeGame(id: String) {
        _items.value = _items.value.filter { it.id != id }
    }

    // Cambiar cantidad
    fun updateQuantity(id: String, newQuantity: Int): Boolean {
        if (newQuantity <= 0) {
            removeGame(id)
            return true
        }
        
        // Calcular el total de licencias sin contar el item actual
        val currentTotalLicenses = _items.value.filter { it.id != id }.sumOf { it.quantity }
        
        // Verificar si el nuevo total excede el límite
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
