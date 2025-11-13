package com.example.uinavegacion.data.remote.repository

import com.example.uinavegacion.data.remote.api.AuthApi
import com.example.uinavegacion.data.remote.config.RetrofitClient
import com.example.uinavegacion.data.remote.dto.AuthResponse
import com.example.uinavegacion.data.remote.dto.LoginRequest
import com.example.uinavegacion.data.remote.dto.RegisterRequest

class AuthRemoteRepository {
    private val api: AuthApi = RetrofitClient.createAuthService().create(AuthApi::class.java)
    
    suspend fun register(request: RegisterRequest): Result<AuthResponse> {
        return try {
            val response = api.register(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error en registro: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun login(request: LoginRequest): Result<AuthResponse> {
        return try {
            val response = api.login(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Credenciales inválidas"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun adminLogin(request: LoginRequest): Result<AuthResponse> {
        return try {
            val response = api.adminLogin(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Credenciales inválidas"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

