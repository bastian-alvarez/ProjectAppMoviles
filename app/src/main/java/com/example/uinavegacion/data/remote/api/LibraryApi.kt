package com.example.uinavegacion.data.remote.api

import com.example.uinavegacion.data.remote.dto.AddToLibraryRequest
import com.example.uinavegacion.data.remote.dto.LibraryItemResponse
import retrofit2.Response
import retrofit2.http.*

interface LibraryApi {
    @POST("library")
    suspend fun addToLibrary(@Body request: AddToLibraryRequest): Response<LibraryItemResponse>
    
    @GET("library/user/{userId}")
    suspend fun getUserLibrary(@Path("userId") userId: Long): Response<List<LibraryItemResponse>>
    
    @GET("library/user/{userId}/game/{juegoId}")
    suspend fun userOwnsGame(
        @Path("userId") userId: Long,
        @Path("juegoId") juegoId: String
    ): Response<Map<String, Boolean>>
    
    @DELETE("library/user/{userId}/game/{juegoId}")
    suspend fun removeFromLibrary(
        @Path("userId") userId: Long,
        @Path("juegoId") juegoId: String
    ): Response<Map<String, String>>
}

