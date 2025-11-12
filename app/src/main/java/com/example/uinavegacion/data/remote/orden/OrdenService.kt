package com.example.uinavegacion.data.remote.orden

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Contrato mínimo para el microservicio de órdenes.
 */
interface OrdenService {

    @GET("internal/ordenes")
    suspend fun listOrdenes(
        @Query("usuarioId") usuarioId: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50
    ): List<OrdenResponse>

    @GET("internal/ordenes/{id}")
    suspend fun getOrden(@Path("id") id: String): OrdenResponse

    @POST("internal/ordenes")
    suspend fun crearOrden(@Body request: CrearOrdenRequest): OrdenResponse
}

data class OrdenResponse(
    val id: String,
    val usuarioId: String,
    val estado: String,
    val total: Double,
    val creadoEn: String,
    val items: List<OrdenItemResponse> = emptyList()
)

data class OrdenItemResponse(
    val juegoId: String,
    val cantidad: Int,
    val precioUnitario: Double
)

data class CrearOrdenRequest(
    val usuarioId: String,
    val items: List<CrearOrdenItemRequest>
)

data class CrearOrdenItemRequest(
    val juegoId: String,
    val cantidad: Int
)


