package com.example.uinavegacion.data.remote.dto

data class LibraryItemResponse(
    val id: Long,
    val userId: Long,
    val juegoId: String,
    val name: String,
    val price: Double,
    val dateAdded: String,
    val status: String,
    val genre: String
)

data class AddToLibraryRequest(
    val userId: String,  // Cambiado a String para soportar IDs remotos
    val gameId: String,  // Nombre consistente con el backend
    val name: String,
    val price: Double,
    val dateAdded: String,  // Agregado para registrar fecha de compra
    val status: String = "Disponible",
    val genre: String = "General"
)

