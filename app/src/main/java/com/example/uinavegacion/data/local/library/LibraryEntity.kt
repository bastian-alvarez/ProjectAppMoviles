package com.example.uinavegacion.data.local.library

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "biblioteca")
data class LibraryEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val price: Double,
    val dateAdded: String,
    val status: String = "Disponible",
    val genre: String = "Acción"
)
