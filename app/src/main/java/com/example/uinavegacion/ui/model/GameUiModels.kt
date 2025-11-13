package com.example.uinavegacion.ui.model

import com.example.uinavegacion.ui.viewmodel.CatalogGameUi

data class Game(
    val id: String,
    val remoteId: String?,
    val name: String,
    val price: Double,
    val category: String,
    val stock: Int,
    val description: String = "Descripción del juego",
    val imageUrl: String = "",
    val discount: Int = 0,
    val isActive: Boolean = true
) {
    val discountedPrice: Double
        get() = if (discount > 0) price * (1 - discount / 100.0) else price

    val hasDiscount: Boolean
        get() = discount > 0
}

fun CatalogGameUi.toGame(): Game {
    return Game(
        id = id.toString(),
        remoteId = remoteId,
        name = nombre,
        price = precio,
        category = categoriaNombre,
        stock = stock,
        description = descripcion,
        imageUrl = imagenUrl,
        discount = descuento,
        isActive = activo
    )
}

