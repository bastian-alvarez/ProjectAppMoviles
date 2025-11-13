package com.example.uinavegacion.data.remote.juego

/**
 * Repositorio remoto básico para el microservicio de juegos.
 */
class JuegoRemoteRepository(
    private val service: JuegoService = JuegoApi.service
) {

    suspend fun fetchJuegos(
        page: Int = 0,
        size: Int = 50,
        estadoId: String? = null
    ): Result<List<JuegoResponse>> = runCatching {
        service.listJuegos(page = page, size = size, estadoId = estadoId)
    }

    suspend fun fetchJuego(id: String): Result<JuegoResponse> =
        runCatching { service.getJuegoById(id) }
}


