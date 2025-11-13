package com.example.uinavegacion.data.remote.dto

data class AuthResponse(
    val user: UserResponse,
    val token: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long
)

data class UserResponse(
    val id: Long,
    val name: String,
    val email: String,
    val phone: String,
    val profilePhotoUri: String?,
    val isBlocked: Boolean,
    val gender: String
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val phone: String,
    val password: String,
    val gender: String = ""
)

data class LoginRequest(
    val email: String,
    val password: String
)

