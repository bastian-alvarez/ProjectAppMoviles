package com.example.uinavegacion.data.remote.catalogo

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Interfaz mínima para el microservicio de catálogo.
 * Las propiedades coinciden con los nombres JSON para evitar anotaciones extra.
 */
interface CatalogoService {

    @GET("internal/catalogo/juegos")
    suspend fun listGames(
        @Query("q") query: String? = null,
        @Query("categoriaId") categoriaId: String? = null,
        @Query("generoId") generoId: String? = null,
        @Query("estadoId") estadoId: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50
    ): List<CatalogoGameResponse>

    @GET("internal/catalogo/juegos/{id}")
    suspend fun getGameById(@Path("id") id: String): CatalogoGameResponse
}

data class CatalogoGameResponse(
    val id: String,
    val nombreJuego: String,
    val precio: Double,
    val fotoJuego: String? = null,
    val fechaLanzamiento: String? = null,
    val categoriaId: String? = null,
    val generoId: String? = null,
    val estadoId: String? = null
)

