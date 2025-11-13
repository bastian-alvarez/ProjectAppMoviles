package com.example.uinavegacion.data.local.juego

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.uinavegacion.data.local.categoria.CategoriaEntity
import com.example.uinavegacion.data.local.genero.GeneroEntity

@Entity(
    tableName = "juegos",
    foreignKeys = [
        ForeignKey(
            entity = CategoriaEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoriaId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = GeneroEntity::class,
            parentColumns = ["id"],
            childColumns = ["generoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["categoriaId"]), Index(value = ["generoId"])]
)
data class JuegoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val nombre: String,
    val descripcion: String,
    val precio: Double,
    val stock: Int,
    val imagenUrl: String? = null,
    val desarrollador: String = "Desarrollador",
    val fechaLanzamiento: String = "2024",
    val categoriaId: Long = 1L,
    val generoId: Long = 1L,
    @ColumnInfo(defaultValue = "1")
    val activo: Boolean = true,
    @ColumnInfo(defaultValue = "0")
    val descuento: Int = 0,
    @ColumnInfo(name = "remoteId")
    val remoteId: String? = null
) {
    // Propiedad computada para compatibilidad
    val imageUrl: String
        get() = imagenUrl ?: ""
}
