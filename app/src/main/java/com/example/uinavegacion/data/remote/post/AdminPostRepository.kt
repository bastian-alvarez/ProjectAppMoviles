package com.example.uinavegacion.data.remote.post

/**
 * Remote contract for the Administrator microservice.
 */
class AdminPostRepository(
    private val service: AdminService = AdminApi.service
) : BasePostRepository(serviceName = "administradores") {

    suspend fun validateAdmin(request: LoginRequest): Result<AdminRemoteDto> =
        safeCall("validateAdmin") { service.login(request) }

    suspend fun registerAdmin(request: AdminRegisterRequest): Result<AdminRemoteDto> =
        safeCall("registerAdmin") { service.register(request) }

    suspend fun fetchAdminByEmail(email: String): Result<AdminRemoteDto> =
        safeCall("fetchAdminByEmail") { service.getByEmail(email) }

    suspend fun fetchAllAdmins(): Result<List<AdminRemoteDto>> =
        safeCall("fetchAllAdmins") { service.listAdmins() }

    suspend fun updateRole(adminId: Long, role: String): Result<Unit> =
        safeCall("updateRole") {
            service.updateRole(
                adminId = adminId.toString(),
                request = UpdateRoleRequest(rol = role)
            )
        }

    suspend fun deleteAdmin(adminId: Long): Result<Unit> =
        safeCall("deleteAdmin") { service.delete(adminId.toString()) }
}

