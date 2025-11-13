package com.example.uinavegacion.data.remote.orden

/**
 * Repositorio remoto sencillo para el microservicio de órdenes.
 */
class OrdenRemoteRepository(
    private val service: OrdenService = OrdenApi.service
) {

    suspend fun fetchOrdenes(
        usuarioId: String? = null,
        page: Int = 0,
        size: Int = 50
    ): Result<List<OrdenResponse>> = runCatching {
        service.listOrdenes(usuarioId = usuarioId, page = page, size = size)
    }

    suspend fun fetchOrden(id: String): Result<OrdenResponse> =
        runCatching { service.getOrden(id) }

    suspend fun crearOrden(request: CrearOrdenRequest): Result<OrdenResponse> =
        runCatching { service.crearOrden(request) }
}


