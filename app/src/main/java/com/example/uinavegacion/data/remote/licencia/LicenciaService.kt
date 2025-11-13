package com.example.uinavegacion.data.remote.licencia

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface LicenciaService {

    @GET("/internal/licencias/juego/{juegoId}/disponibles")
    suspend fun getLicenciasDisponibles(
        @Path("juegoId") juegoId: String,
        @Query("size") size: Int = 20
    ): List<LicenciaResponse>

    @POST("/internal/licencias/{id}/assign")
    suspend fun assignLicencia(
        @Path("id") licenciaId: String,
        @Body request: AssignLicenciaRequest
    ): LicenciaResponse

    @POST("/internal/licencias/{id}/release")
    suspend fun releaseLicencia(
        @Path("id") licenciaId: String
    ): LicenciaResponse

    @GET("/internal/licencias/{id}")
    suspend fun getLicencia(
        @Path("id") licenciaId: String
    ): LicenciaResponse?
}

@Serializable
data class LicenciaResponse(
    val id: String,
    val clave: String,
    @SerialName("fechaVencimiento")
    val fechaVencimiento: String,
    val estadoId: String,
    val juegoId: String,
    val usuarioId: String? = null,
    val asignadaEn: String? = null
)

@Serializable
data class AssignLicenciaRequest(
    val usuarioId: String
)


