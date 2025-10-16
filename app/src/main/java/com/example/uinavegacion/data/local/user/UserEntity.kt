package com.example.uinavegacion.data.local.user
import androidx.room.PrimaryKey
import androidx.room.Entity

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val email:String,
    val phone: String,
    val password: String,
    val profilePhotoUri: String? = null
)