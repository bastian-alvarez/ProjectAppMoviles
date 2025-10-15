package com.example.uinavegacion.data.local.licencia

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.uinavegacion.data.local.juego.JuegoEntity
import com.example.uinavegacion.data.local.user.UserEntity

@Entity(
    tableName = "licencias",
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
    indices = [Index(value = ["userId"]), Index(value = ["juegoId"])]
)
data class LicenciaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val userId: Long,
    val juegoId: Long,
    val codigoLicencia: String,
    val fechaCompra: String,
    val fechaExpiracion: String? = null,
    val activa: Boolean = true
)
