package com.example.uinavegacion.data.remote.api

import com.example.uinavegacion.data.remote.dto.GameResponse
import com.example.uinavegacion.data.remote.api.CreateGameRequest
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

/**
 * Servicio para operaciones de administrador sobre juegos
 * Incluye endpoints para gestión de juegos e imágenes
 */
interface AdminGameService {
    
    /**
     * Crear un nuevo juego
     * POST /api/admin/games
     */
    @POST("admin/games")
    suspend fun createGame(@Body request: CreateGameRequest): Response<GameResponse>
    
    /**
     * Actualizar un juego existente
     * PUT /api/admin/games/{id}
     */
    @PUT("admin/games/{id}")
    suspend fun updateGame(
        @Path("id") id: Long,
        @Body request: CreateGameRequest
    ): Response<GameResponse>
    
    /**
     * Eliminar un juego
     * DELETE /api/admin/games/{id}
     */
    @DELETE("admin/games/{id}")
    suspend fun deleteGame(@Path("id") id: Long): Response<Unit>
    
    /**
     * Subir imagen de juego (multipart/form-data)
     * POST /api/admin/games/{id}/image/upload
     * 
     * @param id ID del juego
     * @param file Archivo de imagen (JPG, PNG, GIF) - máximo 10MB
     * @return Juego actualizado con nueva URL de imagen
     */
    @Multipart
    @POST("admin/games/{id}/image/upload")
    suspend fun uploadGameImage(
        @Path("id") id: Long,
        @Part file: MultipartBody.Part
    ): Response<GameResponse>
    
    /**
     * Actualizar stock de un juego
     * PUT /api/admin/games/{id}/stock
     */
    @PUT("admin/games/{id}/stock")
    suspend fun updateStock(
        @Path("id") id: Long,
        @Body request: Map<String, Int>
    ): Response<GameResponse>
}

