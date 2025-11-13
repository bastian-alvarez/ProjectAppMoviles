package com.example.uinavegacion.data.remote.api

import com.example.uinavegacion.data.remote.dto.CreateOrderRequest
import com.example.uinavegacion.data.remote.dto.OrderResponse
import retrofit2.Response
import retrofit2.http.*

interface OrderApi {
    @POST("orders")
    suspend fun createOrder(@Body request: CreateOrderRequest): Response<OrderResponse>
    
    @GET("orders/user/{userId}")
    suspend fun getOrdersByUserId(@Path("userId") userId: Long): Response<List<OrderResponse>>
    
    @GET("orders/{id}")
    suspend fun getOrderById(@Path("id") id: Long): Response<OrderResponse>
}

