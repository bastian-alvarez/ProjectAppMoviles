package com.example.uinavegacion.data.remote.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.uinavegacion.data.remote.api.AdminGameService
import com.example.uinavegacion.data.remote.api.CreateGameRequest
import com.example.uinavegacion.data.remote.config.RetrofitClient
import com.example.uinavegacion.data.remote.dto.GameResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

/**
 * Repositorio para operaciones de administrador sobre juegos
 */
class AdminGameRepository(private val context: Context) {
    
    // CORREGIDO: Usar Game Catalog Service (puerto 3002) para admin games
    private val service: AdminGameService = RetrofitClient.createGameCatalogService()
        .create(AdminGameService::class.java)
    
    /**
     * Crea un nuevo juego en el catálogo
     */
    suspend fun createGame(request: CreateGameRequest): Result<GameResponse> {
        return try {
            Log.d("AdminGameRepo", "🎮 Creando nuevo juego: ${request.nombre}")
            val response = service.createGame(request)
            if (response.isSuccessful && response.body() != null) {
                Log.d("AdminGameRepo", "✅ Juego creado: ID ${response.body()!!.id}")
                Result.success(response.body()!!)
            } else {
                val error = "Error al crear juego: ${response.code()} - ${response.message()}"
                Log.e("AdminGameRepo", "❌ $error")
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Log.e("AdminGameRepo", "❌ Excepción al crear juego: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Actualiza un juego existente
     */
    suspend fun updateGame(id: Long, request: CreateGameRequest): Result<GameResponse> {
        return try {
            Log.d("AdminGameRepo", "✏️ Actualizando juego ID: $id")
            val response = service.updateGame(id, request)
            if (response.isSuccessful && response.body() != null) {
                Log.d("AdminGameRepo", "✅ Juego actualizado: ${response.body()!!.nombre}")
                Result.success(response.body()!!)
            } else {
                val error = "Error al actualizar juego: ${response.code()} - ${response.message()}"
                Log.e("AdminGameRepo", "❌ $error")
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Log.e("AdminGameRepo", "❌ Excepción al actualizar juego: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Elimina un juego del catálogo
     */
    suspend fun deleteGame(id: Long): Result<Unit> {
        return try {
            Log.d("AdminGameRepo", "🗑️ Eliminando juego ID: $id")
            val response = service.deleteGame(id)
            if (response.isSuccessful) {
                Log.d("AdminGameRepo", "✅ Juego eliminado exitosamente")
                Result.success(Unit)
            } else {
                val error = "Error al eliminar juego: ${response.code()} - ${response.message()}"
                Log.e("AdminGameRepo", "❌ $error")
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Log.e("AdminGameRepo", "❌ Excepción al eliminar juego: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Sube una imagen para un juego (multipart/form-data)
     * 
     * @param gameId ID del juego
     * @param imageUri Uri de la imagen (content:// o file://)
     * @return GameResponse actualizado con la nueva URL de imagen
     */
    suspend fun uploadGameImage(gameId: Long, imageUri: Uri): Result<GameResponse> {
        return try {
            Log.d("AdminGameRepo", "📸 Subiendo imagen para juego ID: $gameId")
            
            // Convertir Uri a File temporal
            val file = uriToFile(imageUri)
            if (file == null || !file.exists()) {
                return Result.failure(Exception("No se pudo crear archivo temporal desde Uri"))
            }
            
            // Crear RequestBody y MultipartBody.Part
            val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
            val multipartBody = MultipartBody.Part.createFormData(
                "file",
                file.name,
                requestBody
            )
            
            // Realizar petición
            val response = service.uploadGameImage(gameId, multipartBody)
            
            // Limpiar archivo temporal
            file.delete()
            
            if (response.isSuccessful && response.body() != null) {
                val gameResponse = response.body()!!
                Log.d("AdminGameRepo", "✅ Imagen subida exitosamente: ${gameResponse.imagenUrl}")
                Result.success(gameResponse)
            } else {
                val error = "Error al subir imagen: ${response.code()} - ${response.message()}"
                Log.e("AdminGameRepo", "❌ $error")
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Log.e("AdminGameRepo", "❌ Excepción al subir imagen: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Actualiza el stock de un juego
     */
    suspend fun updateStock(gameId: Long, newStock: Int): Result<GameResponse> {
        return try {
            Log.d("AdminGameRepo", "📦 Actualizando stock del juego ID: $gameId a $newStock")
            val response = service.updateStock(gameId, mapOf("stock" to newStock))
            if (response.isSuccessful && response.body() != null) {
                Log.d("AdminGameRepo", "✅ Stock actualizado exitosamente")
                Result.success(response.body()!!)
            } else {
                val error = "Error al actualizar stock: ${response.code()} - ${response.message()}"
                Log.e("AdminGameRepo", "❌ $error")
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Log.e("AdminGameRepo", "❌ Excepción al actualizar stock: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Convierte una Uri a un File temporal
     * Maneja tanto content:// como file:// uris
     */
    private fun uriToFile(uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            
            // Crear archivo temporal
            val tempFile = File.createTempFile(
                "game_image_${System.currentTimeMillis()}",
                ".jpg",
                context.cacheDir
            )
            
            // Copiar contenido
            FileOutputStream(tempFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            inputStream.close()
            
            Log.d("AdminGameRepo", "📁 Archivo temporal creado: ${tempFile.absolutePath} (${tempFile.length()} bytes)")
            tempFile
        } catch (e: Exception) {
            Log.e("AdminGameRepo", "❌ Error al convertir Uri a File: ${e.message}", e)
            null
        }
    }
}

