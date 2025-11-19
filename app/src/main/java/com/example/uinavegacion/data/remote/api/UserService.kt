package com.example.uinavegacion.data.remote.api

import com.example.uinavegacion.data.remote.dto.UserResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

/**
 * Servicio para operaciones de usuario autenticado
 * Incluye endpoints para gestión de perfil y foto de perfil
 */
interface UserService {
    
    /**
     * Obtener perfil del usuario actual
     * GET /api/users/me
     */
    @GET("users/me")
    suspend fun getMyProfile(): Response<UserResponse>
    
    /**
     * Subir foto de perfil (multipart/form-data)
     * POST /api/users/me/photo/upload
     * 
     * @param file Archivo de imagen (JPG, PNG, GIF) - máximo 5MB
     * @return Usuario actualizado con nueva URL de foto
     */
    @Multipart
    @POST("users/me/photo/upload")
    suspend fun uploadProfilePhoto(
        @Part file: MultipartBody.Part
    ): Response<UserResponse>
    
    /**
     * Actualizar URL de foto de perfil (deprecated - usar uploadProfilePhoto)
     * PUT /api/users/me/photo
     */
    @Deprecated("Usar uploadProfilePhoto en su lugar")
    @PUT("users/me/photo")
    suspend fun updatePhotoUrl(@Body request: UpdatePhotoUrlRequest): Response<UserResponse>
}

/**
 * Request para actualizar URL de foto (método antiguo)
 */
data class UpdatePhotoUrlRequest(
    val profilePhotoUri: String
)

