package com.example.uinavegacion.data.remote.repository

import com.example.uinavegacion.data.remote.api.OrderApi
import com.example.uinavegacion.data.remote.config.RetrofitClient
import com.example.uinavegacion.data.remote.dto.CreateOrderRequest
import com.example.uinavegacion.data.remote.dto.OrderResponse

class OrderRemoteRepository {
    private val api: OrderApi = RetrofitClient.createOrderService().create(OrderApi::class.java)
    
    suspend fun createOrder(request: CreateOrderRequest): Result<OrderResponse> {
        return try {
            val response = api.createOrder(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al crear orden: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getOrdersByUserId(userId: Long): Result<List<OrderResponse>> {
        return try {
            val response = api.getOrdersByUserId(userId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al obtener órdenes: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

