package com.example.uinavegacion.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uinavegacion.data.local.database.AppDatabase
import com.example.uinavegacion.data.repository.GameRepository
import com.example.uinavegacion.data.repository.LibraryRepository
import com.example.uinavegacion.data.remote.repository.OrderRemoteRepository
import com.example.uinavegacion.data.remote.dto.CreateOrderRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

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

        if (existingIndex >= 0) {
            val existingItem = currentItems[existingIndex]
            val newQuantity = existingItem.quantity + 1

            if (newQuantity > existingItem.maxStock) {
                _errorMessage.value = "Solo hay ${existingItem.maxStock} unidades disponibles"
                _successMessage.value = null
                return false
            }

            currentItems[existingIndex] = existingItem.copy(quantity = newQuantity)
            _successMessage.value = "✓ Cantidad actualizada en el carrito"
        } else {
            if (maxStock <= 0) {
                _errorMessage.value = "Este producto está agotado"
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

        if (newQuantity > item.maxStock) {
            _errorMessage.value = "Solo hay ${item.maxStock} unidades disponibles"
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

    fun checkout(
        context: Context,
        userId: Long,
        remoteUserId: String?,
        metodoPago: String = "Tarjeta",
        direccionEnvio: String? = null,
        onResult: (Boolean, String?) -> Unit
    ) {
        val currentItems = _items.value
        if (currentItems.isEmpty()) {
            onResult(false, "El carrito está vacío")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val db = AppDatabase.getInstance(context.applicationContext)
                val gameRepository = GameRepository(db.juegoDao())
                val libraryRepository = LibraryRepository(db.libraryDao())
                val orderRemoteRepository = OrderRemoteRepository()

                Log.d("CartViewModel", "Iniciando checkout para usuario $userId")

                // 1. Crear orden en el microservicio
                val orderItems = currentItems.map { item ->
                    CreateOrderRequest.OrderItem(
                        juegoId = item.id.toLongOrNull() ?: 0L,
                        cantidad = item.quantity
                    )
                }

                val createOrderRequest = CreateOrderRequest(
                    userId = userId,
                    items = orderItems,
                    metodoPago = metodoPago,
                    direccionEnvio = direccionEnvio
                )

                val orderResult = orderRemoteRepository.createOrder(createOrderRequest)
                
                if (orderResult.isFailure) {
                    throw orderResult.exceptionOrNull() 
                        ?: Exception("Error al crear la orden en el servidor")
                }

                val orderResponse = orderResult.getOrNull()!!
                Log.d("CartViewModel", "Orden creada exitosamente: ID=${orderResponse.id}, Total=${orderResponse.total}")

                // 2. Actualizar stock local
                val updatedStocks = mutableListOf<Pair<String, Int>>()
                currentItems.forEach { item ->
                    val gameId = item.id.toLongOrNull()
                        ?: throw IllegalArgumentException("ID de juego inválido")
                    val decreaseResult = gameRepository.decreaseStock(gameId, item.quantity)
                    if (decreaseResult.isFailure) {
                        Log.w("CartViewModel", "No se pudo actualizar stock local: ${decreaseResult.exceptionOrNull()?.message}")
                    } else {
                        updatedStocks += item.name to (decreaseResult.getOrNull() ?: 0)
                    }
                }

                // 3. Agregar juegos a la biblioteca del usuario
                val dateAdded = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                currentItems.forEach { item ->
                    val addResult = libraryRepository.addGameToLibrary(
                        userId = userId,
                        remoteUserId = remoteUserId,
                        juegoId = item.id,
                        remoteGameId = item.remoteId,
                        name = item.name,
                        price = item.price,
                        dateAdded = dateAdded,
                        status = "Disponible",
                        genre = "General"
                    )
                    if (addResult.isFailure) {
                        Log.w("CartViewModel", "No se pudo agregar ${item.name} a biblioteca: ${addResult.exceptionOrNull()?.message}")
                    }
                }

                launch(Dispatchers.Main) {
                    clearCart()
                    onResult(
                        true,
                        buildString {
                            append("✓ Compra confirmada\n")
                            append("Orden #${orderResponse.id}\n")
                            append("Total: $${String.format("%.2f", orderResponse.total)}\n")
                            append("${currentItems.size} juego(s) agregado(s) a tu biblioteca")
                        }
                    )
                }
            } catch (e: Exception) {
                Log.e("CartViewModel", "Error en checkout", e)
                launch(Dispatchers.Main) {
                    onResult(false, "Error al procesar la compra: ${e.message}")
                }
            }
        }
    }
    
    // Método legacy para compatibilidad (sin microservicio)
    @Deprecated("Usar checkout con parámetros de usuario")
    fun checkoutLegacy(context: Context, onResult: (Boolean, String?) -> Unit) {
        val currentItems = _items.value
        if (currentItems.isEmpty()) {
            onResult(false, "El carrito está vacío")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val db = AppDatabase.getInstance(context.applicationContext)
                val repository = GameRepository(db.juegoDao())

                val updatedStocks = mutableListOf<Pair<String, Int>>()

                currentItems.forEach { item ->
                    val gameId = item.id.toLongOrNull()
                        ?: throw IllegalArgumentException("ID de juego inválido")
                    val decreaseResult = repository.decreaseStock(gameId, item.quantity)
                    if (decreaseResult.isFailure) {
                        throw decreaseResult.exceptionOrNull()
                            ?: IllegalStateException("No se pudo actualizar el stock del juego con id $gameId")
                    }
                    updatedStocks += item.name to (decreaseResult.getOrNull() ?: 0)
                }

                launch(Dispatchers.Main) {
                    clearCart()
                    onResult(
                        true,
                        if (updatedStocks.isEmpty()) {
                            "Compra confirmada"
                        } else {
                            buildString {
                                append("Compra confirmada. Stock actualizado:")
                                updatedStocks.forEach { (name, stockRestante) ->
                                    append("\n• $name → $stockRestante unidades restantes")
                                }
                            }
                        }
                    )
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    onResult(false, e.message ?: "Error al procesar la compra")
                }
            }
        }
    }
}
