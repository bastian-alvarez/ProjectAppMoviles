package com.example.uinavegacion.data.remote.licencia

/**
 * Repositorio remoto básico que encapsula las llamadas al microservicio de licencias.
 */
class LicenciaRemoteRepository(
    private val service: LicenciaService = LicenciaApi.service
) {

    suspend fun fetchDisponibles(juegoId: String, size: Int = 20): Result<List<LicenciaResponse>> =
        runCatching { service.getLicenciasDisponibles(juegoId, size) }

    suspend fun assign(licenciaId: String, usuarioId: String): Result<LicenciaResponse> =
        runCatching { service.assignLicencia(licenciaId, AssignLicenciaRequest(usuarioId)) }

    suspend fun release(licenciaId: String): Result<LicenciaResponse> =
        runCatching { service.releaseLicencia(licenciaId) }

    suspend fun fetchById(licenciaId: String): Result<LicenciaResponse?> =
        runCatching { service.getLicencia(licenciaId) }
}


