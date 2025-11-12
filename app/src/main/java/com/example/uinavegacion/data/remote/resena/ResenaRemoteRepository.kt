package com.example.uinavegacion.data.remote.resena

/**
 * Repositorio remoto básico para el microservicio de reseñas.
 */
class ResenaRemoteRepository(
    private val service: ResenaService = ResenaApi.service
) {

    suspend fun fetchResenas(
        juegoId: String? = null,
        usuarioId: String? = null,
        page: Int = 0,
        size: Int = 50
    ): Result<List<ResenaResponse>> = runCatching {
        service.listResenas(juegoId = juegoId, usuarioId = usuarioId, page = page, size = size)
    }

    suspend fun fetchResena(id: String): Result<ResenaResponse> =
        runCatching { service.getResena(id) }

    suspend fun crearResena(request: CrearResenaRequest): Result<ResenaResponse> =
        runCatching { service.crearResena(request) }
}


