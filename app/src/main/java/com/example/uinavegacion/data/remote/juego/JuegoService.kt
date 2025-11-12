package com.example.uinavegacion.data.remote.juego

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Contrato mínimo para interactuar con el microservicio de juegos.
 */
interface JuegoService {

    @GET("internal/juegos")
    suspend fun listJuegos(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50,
        @Query("estadoId") estadoId: String? = null
    ): List<JuegoResponse>

    @GET("internal/juegos/{id}")
    suspend fun getJuegoById(@Path("id") id: String): JuegoResponse
}

data class JuegoResponse(
    val id: String,
    val nombre: String,
    val descripcion: String? = null,
    val precio: Double,
    val stock: Int,
    val imagenUrl: String? = null,
    val categoriaId: String? = null,
    val generoId: String? = null,
    val estadoId: String? = null
)


