package com.example.uinavegacion.data.remote.repository

import com.example.uinavegacion.data.remote.api.GameCatalogApi
import com.example.uinavegacion.data.remote.config.RetrofitClient
import com.example.uinavegacion.data.remote.dto.GameResponse

class GameCatalogRemoteRepository {
    private val api: GameCatalogApi = RetrofitClient.createGameCatalogService().create(GameCatalogApi::class.java)
    
    suspend fun getAllGames(
        categoria: Long? = null,
        genero: Long? = null,
        descuento: Boolean? = null,
        search: String? = null
    ): Result<List<GameResponse>> {
        return try {
            val response = api.getAllGames(categoria, genero, descuento, search)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al obtener juegos: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getGameById(id: Long): Result<GameResponse> {
        return try {
            val response = api.getGameById(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Juego no encontrado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun decreaseStock(id: Long, quantity: Int): Result<GameResponse> {
        return try {
            val response = api.decreaseStock(id, mapOf("quantity" to quantity))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al actualizar stock: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

