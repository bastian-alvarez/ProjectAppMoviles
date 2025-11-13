package com.example.uinavegacion.data.remote.dto

data class GameResponse(
    val id: Long,
    val nombre: String,
    val descripcion: String,
    val precio: Double,
    val stock: Int,
    val imagenUrl: String,
    val desarrollador: String,
    val fechaLanzamiento: String,
    val categoriaId: Long,
    val generoId: Long,
    val activo: Boolean,
    val descuento: Int,
    val discountedPrice: Double,
    val hasDiscount: Boolean,
    val categoriaNombre: String? = null,
    val generoNombre: String? = null
)

