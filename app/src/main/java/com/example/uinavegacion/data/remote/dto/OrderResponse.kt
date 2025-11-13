package com.example.uinavegacion.data.remote.dto

data class CreateOrderRequest(
    val userId: Long,
    val items: List<OrderItem>,
    val metodoPago: String = "Tarjeta",
    val direccionEnvio: String? = null
) {
    data class OrderItem(
        val juegoId: Long,
        val cantidad: Int
    )
}

data class OrderResponse(
    val id: Long,
    val userId: Long,
    val fechaOrden: String,
    val total: Double,
    val estadoId: Long,
    val estadoNombre: String?,
    val metodoPago: String,
    val direccionEnvio: String?,
    val detalles: List<OrderDetailResponse>?
) {
    data class OrderDetailResponse(
        val id: Long,
        val juegoId: Long,
        val juegoNombre: String?,
        val cantidad: Int,
        val precioUnitario: Double,
        val subtotal: Double
    )
}

