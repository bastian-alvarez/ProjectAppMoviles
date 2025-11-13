package com.example.uinavegacion.data.remote.api

import com.example.uinavegacion.data.remote.dto.AuthResponse
import com.example.uinavegacion.data.remote.dto.LoginRequest
import com.example.uinavegacion.data.remote.dto.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>
    
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
    
    @POST("auth/admin/login")
    suspend fun adminLogin(@Body request: LoginRequest): Response<AuthResponse>
}

