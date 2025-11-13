package com.example.uinavegacion.data.remote.repository

import com.example.uinavegacion.data.remote.api.LibraryApi
import com.example.uinavegacion.data.remote.config.RetrofitClient
import com.example.uinavegacion.data.remote.dto.AddToLibraryRequest
import com.example.uinavegacion.data.remote.dto.LibraryItemResponse

class LibraryRemoteRepository {
    private val api: LibraryApi = RetrofitClient.createLibraryService().create(LibraryApi::class.java)
    
    suspend fun addToLibrary(request: AddToLibraryRequest): Result<LibraryItemResponse> {
        return try {
            val response = api.addToLibrary(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al agregar a biblioteca: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getUserLibrary(userId: Long): Result<List<LibraryItemResponse>> {
        return try {
            val response = api.getUserLibrary(userId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al obtener biblioteca: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun userOwnsGame(userId: Long, juegoId: String): Result<Boolean> {
        return try {
            val response = api.userOwnsGame(userId, juegoId)
            if (response.isSuccessful && response.body() != null) {
                val owns = response.body()!!["owns"] ?: false
                Result.success(owns)
            } else {
                Result.failure(Exception("Error al verificar juego: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

