package com.example.uinavegacion.data.remote.user

class UserRemoteRepository(
    private val service: UserService = UserApi.service
) {

    suspend fun login(email: String, password: String): Result<UserResponse> =
        runCatching { service.login(UserLoginRequest(email = email, password = password)) }

    suspend fun register(
        name: String,
        email: String,
        phone: String,
        password: String,
        gender: String?,
        photoUrl: String?
    ): Result<UserResponse> = runCatching {
        service.register(
            UserRegisterRequest(
                nombre = name,
                email = email,
                telefono = phone,
                password = password,
                genero = gender,
                fotoPerfilUrl = photoUrl
            )
        )
    }

    suspend fun updateProfile(
        remoteId: String,
        name: String,
        email: String,
        phone: String,
        gender: String?,
        photoUrl: String?
    ): Result<UserResponse> = runCatching {
        service.updateProfile(
            remoteId,
            UserUpdateRequest(
                nombre = name,
                email = email,
                telefono = phone,
                genero = gender,
                fotoPerfilUrl = photoUrl
            )
        )
    }

    suspend fun changePassword(
        remoteId: String,
        currentPassword: String,
        newPassword: String
    ): Result<Unit> = runCatching {
        service.changePassword(
            remoteId,
            UserChangePasswordRequest(
                passwordActual = currentPassword,
                passwordNueva = newPassword
            )
        )
    }

    suspend fun listUsers(): Result<List<UserResponse>> = runCatching { service.listUsers() }

    suspend fun getUser(remoteId: String): Result<UserResponse> =
        runCatching { service.getUser(remoteId) }

    suspend fun toggleBlock(remoteId: String, bloquear: Boolean): Result<UserResponse> =
        runCatching { service.toggleBlock(remoteId, bloquear) }
    
    suspend fun deleteUser(remoteId: String): Result<Unit> =
        runCatching { service.deleteUser(remoteId) }
}


