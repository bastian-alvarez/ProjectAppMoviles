package com.example.uinavegacion.data.remote.admin

import com.example.uinavegacion.data.remote.user.UserResponse
import retrofit2.Response
import retrofit2.http.*

/**
 * Servicio para operaciones de administrador sobre usuarios
 * Usa los endpoints protegidos de /api/admin/users
 */
interface AdminUserService {
    
    /**
     * Listar todos los usuarios (solo administradores)
     */
    @GET("api/admin/users")
    suspend fun listAllUsers(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 200
    ): Response<List<UserResponse>>
    
    /**
     * Obtener un usuario por ID (solo administradores)
     */
    @GET("api/admin/users/{id}")
    suspend fun getUserById(@Path("id") id: String): Response<UserResponse>
    
    /**
     * Actualizar un usuario (solo administradores)
     */
    @PUT("api/admin/users/{id}")
    suspend fun updateUser(
        @Path("id") id: String,
        @Body request: UpdateUserRequest
    ): Response<UserResponse>
    
    /**
     * Eliminar un usuario (solo administradores)
     */
    @DELETE("api/admin/users/{id}")
    suspend fun deleteUser(@Path("id") id: String): Response<Unit>
    
    /**
     * Bloquear un usuario (solo administradores)
     */
    @POST("api/admin/users/{id}/block")
    suspend fun blockUser(@Path("id") id: String): Response<UserResponse>
    
    /**
     * Desbloquear un usuario (solo administradores)
     */
    @POST("api/admin/users/{id}/unblock")
    suspend fun unblockUser(@Path("id") id: String): Response<UserResponse>
}

/**
 * DTO para actualizar usuario desde panel de administrador
 */
data class UpdateUserRequest(
    val nombre: String,
    val email: String,
    val telefono: String? = null,
    val genero: String? = null,
    val fotoPerfilUrl: String? = null
)

