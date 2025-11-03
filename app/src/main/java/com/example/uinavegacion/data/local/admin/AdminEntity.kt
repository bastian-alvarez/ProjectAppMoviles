package com.example.uinavegacion.data.local.admin

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "admins")
data class AdminEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val email: String,
    val phone: String,
    val password: String,
    val role: String,
    val profilePhotoUri: String? = null
)
