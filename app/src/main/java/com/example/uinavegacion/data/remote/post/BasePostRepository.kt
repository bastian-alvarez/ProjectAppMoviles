package com.example.uinavegacion.data.remote.post

import retrofit2.HttpException
import java.io.IOException

/**
 * Base class offering common Result wrapping for microservice calls.
 */
open class BasePostRepository(
    private val serviceName: String
) {
    protected suspend fun <T> safeCall(operation: String, block: suspend () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (http: HttpException) {
            Result.failure(RuntimeException(errorMessage(operation, http.code(), http.message()), http))
        } catch (io: IOException) {
            Result.failure(RuntimeException("[$serviceName::$operation] Falló la comunicación con el microservicio (${io.message})", io))
        } catch (e: Exception) {
            Result.failure(RuntimeException("[$serviceName::$operation] Error inesperado: ${e.message}", e))
        }
    }

    private fun errorMessage(operation: String, code: Int, message: String?): String =
        "[$serviceName::$operation] Error HTTP $code${message?.let { ": $it" } ?: ""}"

    protected fun <T> notImplemented(operation: String): Result<T> =
        Result.failure(NotImplementedError("[$serviceName::$operation] aún no está implementado"))
}

// --- Shared request/response models that will later be mapped to DTOs ---

data class LoginRequest(val email: String, val password: String)

data class RegisterUserRequest(
    val name: String,
    val email: String,
    val phone: String,
    val password: String
)

data class ChangePasswordRequest(
    val email: String,
    val currentPassword: String,
    val newPassword: String
)

data class UpdateProfilePhotoRequest(
    val id: Long,
    val photoUrl: String?
)

data class AdminRegisterRequest(
    val name: String,
    val email: String,
    val phone: String,
    val password: String,
    val role: String
)

data class GameRemoteDto(
    val id: String,
    val nombre: String,
    val descripcion: String,
    val precio: Double,
    val stock: Int,
    val imagenUrl: String?,
    val categoriaId: String?,
    val generoId: String?,
    val activo: Boolean = true,
    val descuento: Int = 0,
    val fechaLanzamiento: String = ""
)

data class AdminRemoteDto(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val role: String
)

data class DashboardStatsRemoteDto(
    val totalUsers: Int,
    val totalGames: Int,
    val totalOrders: Int,
    val totalAdmins: Int
)

data class LibraryEntryRemoteDto(
    val id: Long? = null,
    val userId: Long,
    val gameId: String,
    val name: String,
    val price: Double,
    val genre: String,
    val status: String
)

data class LicenciaRemoteDto(
    val id: String,
    val clave: String,
    val fechaVencimiento: String,
    val estadoId: String,
    val juegoId: String,
    val usuarioId: String?,
    val asignadaEn: String?
)

