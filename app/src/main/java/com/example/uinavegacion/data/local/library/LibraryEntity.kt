package com.example.uinavegacion.data.local.library

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.uinavegacion.data.local.user.UserEntity

@Entity(
    tableName = "biblioteca",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"])]
)
data class LibraryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val userId: Long,           // Usuario propietario del juego
    val juegoId: String,        // ID del juego comprado
    val name: String,           // Nombre del juego
    val price: Double,          // Precio pagado
    val dateAdded: String,      // Fecha de compra
    val status: String = "Disponible",  // Estado del juego
    val genre: String = "Acción"        // Género del juego
)
