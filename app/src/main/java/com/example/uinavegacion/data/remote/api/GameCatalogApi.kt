package com.example.uinavegacion.data.remote.api

import com.example.uinavegacion.data.remote.dto.GameResponse
import retrofit2.Response
import retrofit2.http.*

interface GameCatalogApi {
    @GET("games")
    suspend fun getAllGames(
        @Query("categoria") categoria: Long? = null,
        @Query("genero") genero: Long? = null,
        @Query("descuento") descuento: Boolean? = null,
        @Query("search") search: String? = null
    ): Response<List<GameResponse>>
    
    @GET("games/{id}")
    suspend fun getGameById(@Path("id") id: Long): Response<GameResponse>
    
    @POST("games")
    suspend fun createGame(@Body request: CreateGameRequest): Response<GameResponse>
    
    @PUT("games/{id}")
    suspend fun updateGame(
        @Path("id") id: Long,
        @Body request: CreateGameRequest
    ): Response<GameResponse>
    
    @PUT("games/{id}/stock")
    suspend fun updateStock(
        @Path("id") id: Long,
        @Body request: Map<String, Int>
    ): Response<GameResponse>
    
    @POST("games/{id}/decrease-stock")
    suspend fun decreaseStock(
        @Path("id") id: Long,
        @Body request: Map<String, Int>
    ): Response<GameResponse>
    
    @DELETE("games/{id}")
    suspend fun deleteGame(@Path("id") id: Long): Response<Unit>
}

data class CreateGameRequest(
    val nombre: String,
    val descripcion: String,
    val precio: Double,
    val stock: Int,
    val imagenUrl: String?,
    val desarrollador: String,
    val fechaLanzamiento: String,
    val categoriaId: Long,
    val generoId: Long,
    val descuento: Int = 0,
    val activo: Boolean = true
)

