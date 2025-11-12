package com.example.uinavegacion.data.remote.post

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Contrato mínimo para el microservicio de administradores.
 */
interface AdminService {

    @POST("api/administradores/login")
    suspend fun login(@Body request: LoginRequest): AdminRemoteDto

    @POST("api/administradores")
    suspend fun register(@Body request: AdminRegisterRequest): AdminRemoteDto

    @GET("api/administradores")
    suspend fun listAdmins(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 100
    ): List<AdminRemoteDto>

    @GET("api/administradores/email/{email}")
    suspend fun getByEmail(@Path("email") email: String): AdminRemoteDto

    @PUT("api/administradores/{id}/rol")
    suspend fun updateRole(
        @Path("id") adminId: String,
        @Body request: UpdateRoleRequest
    )

    @DELETE("api/administradores/{id}")
    suspend fun delete(@Path("id") adminId: String)
}

data class UpdateRoleRequest(val rol: String)


