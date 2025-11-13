package com.example.uinavegacion.data.remote.catalogo

/**
 * Repositorio ultra simple que encapsula las llamadas al microservicio de catálogo.
 */
class CatalogoRemoteRepository(
    private val service: CatalogoService = CatalogoApi.service
) {

    suspend fun fetchGames(
        query: String? = null,
        includeInactive: Boolean = false
    ): Result<List<CatalogoGameResponse>> = runCatching {
        service.listGames(
            query = query,
            estadoId = if (includeInactive) null else "ACTIVO"
        )
    }

    suspend fun fetchGameById(id: String): Result<CatalogoGameResponse> =
        runCatching { service.getGameById(id) }
}


