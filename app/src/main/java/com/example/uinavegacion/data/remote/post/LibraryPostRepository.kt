package com.example.uinavegacion.data.remote.post

import com.example.uinavegacion.data.remote.core.MicroserviceClientFactory
import com.example.uinavegacion.data.remote.licencia.AssignLicenciaRequest
import com.example.uinavegacion.data.remote.licencia.LicenciaResponse
import com.example.uinavegacion.data.remote.licencia.LicenciaService

/**
 * Remote contract for the Library microservice that will persist purchases and
 * synchronize the user's owned catalog.
 */
class LibraryPostRepository(
    private val service: LicenciaService
) : BasePostRepository(serviceName = "licencia") {

    suspend fun fetchAvailableLicenses(gameRemoteId: String, limit: Int = 10): Result<List<LicenciaRemoteDto>> =
        safeCall("fetchAvailableLicenses") {
            service.getLicenciasDisponibles(gameRemoteId, size = limit).map { it.toRemoteDto() }
        }

    suspend fun assignLicense(licenciaId: String, usuarioRemoteId: String): Result<LicenciaRemoteDto> =
        safeCall("assignLicense") {
            service.assignLicencia(licenciaId, AssignLicenciaRequest(usuarioRemoteId)).toRemoteDto()
        }

    suspend fun releaseLicense(licenciaId: String): Result<LicenciaRemoteDto> =
        safeCall("releaseLicense") {
            service.releaseLicencia(licenciaId).toRemoteDto()
        }

    suspend fun fetchLicense(licenciaId: String): Result<LicenciaRemoteDto?> =
        safeCall("fetchLicense") {
            service.getLicencia(licenciaId)?.toRemoteDto()
        }

    companion object {
        fun create(): LibraryPostRepository {
            val retrofit = MicroserviceClientFactory.licenciaService()
            val service = retrofit.create(LicenciaService::class.java)
            return LibraryPostRepository(service)
        }
    }
}

private fun LicenciaResponse.toRemoteDto(): LicenciaRemoteDto =
    LicenciaRemoteDto(
        id = id,
        clave = clave,
        fechaVencimiento = fechaVencimiento,
        estadoId = estadoId,
        juegoId = juegoId,
        usuarioId = usuarioId,
        asignadaEn = asignadaEn
    )

