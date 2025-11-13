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
    val userId: Long,
    val juegoId: String,
    val name: String,
    val price: Double,
    val status: String = "Disponible",
    val genre: String = "Acción"
)

