package com.example.uinavegacion.data.remote.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.uinavegacion.data.remote.dto.UserResponse
import com.example.uinavegacion.data.remote.api.UserService
import com.example.uinavegacion.data.remote.config.RetrofitClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

/**
 * Repositorio para operaciones de usuario autenticado
 */
class UserRemoteRepository(private val context: Context) {
    
    private val service: UserService = RetrofitClient.createAuthService()
        .create(UserService::class.java)
    
    /**
     * Obtiene el perfil del usuario actual autenticado
     */
    suspend fun getMyProfile(): Result<UserResponse> {
        return try {
            Log.d("UserRemoteRepo", "📋 Obteniendo perfil del usuario...")
            val response = service.getMyProfile()
            if (response.isSuccessful && response.body() != null) {
                Log.d("UserRemoteRepo", "✅ Perfil obtenido: ${response.body()!!.email}")
                Result.success(response.body()!!)
            } else {
                val error = "Error al obtener perfil: ${response.code()} - ${response.message()}"
                Log.e("UserRemoteRepo", "❌ $error")
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Log.e("UserRemoteRepo", "❌ Excepción al obtener perfil: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Sube una foto de perfil al microservicio (multipart/form-data)
     * 
     * @param imageUri Uri de la imagen (content:// o file://)
     * @return UserResponse actualizado con la nueva URL de foto
     */
    suspend fun uploadProfilePhoto(imageUri: Uri): Result<UserResponse> {
        return try {
            Log.d("UserRemoteRepo", "📸 Subiendo foto de perfil...")
            
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
            val response = service.uploadProfilePhoto(multipartBody)
            
            // Limpiar archivo temporal
            file.delete()
            
            if (response.isSuccessful && response.body() != null) {
                val userResponse = response.body()!!
                Log.d("UserRemoteRepo", "✅ Foto subida exitosamente: ${userResponse.profilePhotoUri}")
                Result.success(userResponse)
            } else {
                val error = "Error al subir foto: ${response.code()} - ${response.message()}"
                Log.e("UserRemoteRepo", "❌ $error")
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Log.e("UserRemoteRepo", "❌ Excepción al subir foto: ${e.message}", e)
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
                "profile_photo_${System.currentTimeMillis()}",
                ".jpg",
                context.cacheDir
            )
            
            // Copiar contenido
            FileOutputStream(tempFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            inputStream.close()
            
            Log.d("UserRemoteRepo", "📁 Archivo temporal creado: ${tempFile.absolutePath} (${tempFile.length()} bytes)")
            tempFile
        } catch (e: Exception) {
            Log.e("UserRemoteRepo", "❌ Error al convertir Uri a File: ${e.message}", e)
            null
        }
    }
}

