package com.example.uinavegacion.data.remote.user

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface UserService {

    @POST("api/usuarios/login")
    suspend fun login(@Body request: UserLoginRequest): UserResponse

    @POST("api/usuarios/register")
    suspend fun register(@Body request: UserRegisterRequest): UserResponse

    @GET("api/usuarios")
    suspend fun listUsers(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 200
    ): List<UserResponse>

    @GET("api/usuarios/{id}")
    suspend fun getUser(@Path("id") id: String): UserResponse

    @PUT("api/usuarios/{id}/perfil")
    suspend fun updateProfile(
        @Path("id") id: String,
        @Body request: UserUpdateRequest
    ): UserResponse

    @POST("api/usuarios/{id}/password/cambiar")
    suspend fun changePassword(
        @Path("id") id: String,
        @Body request: UserChangePasswordRequest
    )

    @POST("api/usuarios/{id}/bloqueo")
    suspend fun toggleBlock(
        @Path("id") id: String,
        @Query("bloquear") bloquear: Boolean
    ): UserResponse
    
    @DELETE("api/usuarios/{id}")
    suspend fun deleteUser(@Path("id") id: String)
}

data class UserLoginRequest(
    val email: String,
    val password: String
)

data class UserRegisterRequest(
    val nombre: String,
    val email: String,
    val telefono: String,
    val password: String,
    val genero: String? = null,
    val fotoPerfilUrl: String? = null
)

data class UserUpdateRequest(
    val nombre: String,
    val email: String,
    val telefono: String,
    val genero: String? = null,
    val fotoPerfilUrl: String? = null
)

data class UserChangePasswordRequest(
    val passwordActual: String,
    val passwordNueva: String
)

data class UserResponse(
    val id: String,
    val nombre: String,
    val email: String,
    val telefono: String? = null,
    val genero: String? = null,
    val fotoPerfilUrl: String? = null,
    val rolId: String? = null,
    val estadoId: String? = null,
    val creadoEn: String? = null
)

