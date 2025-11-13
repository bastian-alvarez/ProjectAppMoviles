package com.example.uinavegacion.data.local.resena

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.uinavegacion.data.local.user.UserEntity
import com.example.uinavegacion.data.local.juego.JuegoEntity

@Entity(
    tableName = "resenas",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = JuegoEntity::class,
            parentColumns = ["id"],
            childColumns = ["juegoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["userId"]),
        Index(value = ["juegoId"])
    ]
)
data class ResenaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val userId: Long,
    val juegoId: Long,
    @ColumnInfo(defaultValue = "1")
    val calificacion: Int = 1, // 1 a 5 estrellas
    val comentario: String,
    val fechaCreacion: String,
    @ColumnInfo(defaultValue = "0")
    val isDeleted: Boolean = false // Para soft delete (moderación)
)

