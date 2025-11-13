package com.example.uinavegacion.data.remote.resena

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Contrato mínimo para el microservicio de reseñas.
 */
interface ResenaService {

    @GET("internal/resenas")
    suspend fun listResenas(
        @Query("juegoId") juegoId: String? = null,
        @Query("usuarioId") usuarioId: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50
    ): List<ResenaResponse>

    @GET("internal/resenas/{id}")
    suspend fun getResena(@Path("id") id: String): ResenaResponse

    @POST("internal/resenas")
    suspend fun crearResena(@Body request: CrearResenaRequest): ResenaResponse
}

data class ResenaResponse(
    val id: String,
    val juegoId: String,
    val usuarioId: String,
    val calificacion: Int,
    val comentario: String?,
    val creadoEn: String
)

data class CrearResenaRequest(
    val juegoId: String,
    val usuarioId: String,
    val calificacion: Int,
    val comentario: String?
)


