package com.example.uinavegacion.data.repository

import android.util.Log
import com.example.uinavegacion.data.local.user.UserDao
import com.example.uinavegacion.data.local.user.UserEntity
import com.example.uinavegacion.data.remote.user.UserRemoteRepository
import com.example.uinavegacion.data.remote.user.UserResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository(
    private val userDao: UserDao,
    private val remoteRepository: UserRemoteRepository = UserRemoteRepository()
) {
    suspend fun login(email: String, password: String): Result<UserEntity> {
        return withContext(Dispatchers.IO) {
            remoteRepository.login(email, password)
                .mapCatching { remote ->
                    upsertRemoteUser(remote)
                }
        }
    }

    suspend fun register(
        name: String,
        email: String,
        phone: String,
        password: String,
        gender: String = "",
        photoUrl: String? = null
    ): Result<UserEntity> {
        return withContext(Dispatchers.IO) {
            remoteRepository.register(
                name = name,
                email = email,
                phone = phone,
                password = password,
                gender = gender.ifBlank { null },
                photoUrl = photoUrl
            )
                .mapCatching { remote ->
                    upsertRemoteUser(remote)
                }
        }
    }

    suspend fun updateProfile(
        userId: Long,
        name: String,
        email: String,
        phone: String,
        gender: String,
        photoUrl: String?
    ): Result<UserEntity> = withContext(Dispatchers.IO) {
        val localUser = userDao.getById(userId)
            ?: return@withContext Result.failure(Exception("Usuario no encontrado localmente"))

        val remoteId = localUser.remoteId
            ?: return@withContext Result.failure(Exception("Usuario no sincronizado con el servidor"))

        remoteRepository.updateProfile(
            remoteId = remoteId,
            name = name,
            email = email,
            phone = phone,
            gender = gender.ifBlank { null },
            photoUrl = photoUrl
        ).mapCatching { remote ->
            upsertRemoteUser(remote)
        }
    }

    suspend fun updateProfilePhoto(userId: Long, photoUri: String?): Result<UserEntity> = withContext(Dispatchers.IO) {
        val localUser = userDao.getById(userId)
            ?: return@withContext Result.failure(Exception("Usuario no encontrado localmente"))

        return@withContext updateProfile(
            userId = userId,
            name = localUser.name,
            email = localUser.email,
            phone = localUser.phone,
            gender = localUser.gender,
            photoUrl = photoUri
        )
    }

    suspend fun getAllUsers(): List<UserEntity> {
        return withContext(Dispatchers.IO) {
            remoteRepository.listUsers()
                .onSuccess { remotes ->
                    remotes.forEach { remote ->
                        try {
                            upsertRemoteUser(remote)
                        } catch (e: Exception) {
                            Log.e("UserRepository", "Error sincronizando usuario ${remote.email}", e)
                        }
                    }
                }
                .onFailure { throwable ->
                    Log.e("UserRepository", "No se pudo sincronizar usuarios remotos", throwable)
                }
            userDao.getAll()
        }
    }

    suspend fun changePassword(email: String, currentPassword: String, newPassword: String): Result<Unit> = withContext(Dispatchers.IO) {
        val localUser = userDao.getByEmail(email)
            ?: return@withContext Result.failure(Exception("Usuario no encontrado"))

        val remoteId = localUser.remoteId
            ?: return@withContext Result.failure(Exception("Usuario no sincronizado con el servidor"))

        remoteRepository.changePassword(remoteId, currentPassword, newPassword)
            .onSuccess {
                userDao.updatePasswordByEmail(email, "")
            }
    }

    suspend fun getUserByEmail(email: String): UserEntity? {
        return userDao.getByEmail(email)
    }

    suspend fun toggleBlockStatus(userId: Long, isBlocked: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        val localUser = userDao.getById(userId)
            ?: return@withContext Result.failure(Exception("Usuario no encontrado"))

        val remoteId = localUser.remoteId
            ?: return@withContext Result.failure(Exception("Usuario no sincronizado con el servidor"))

        remoteRepository.toggleBlock(remoteId, bloquear = isBlocked)
            .mapCatching { remote ->
                upsertRemoteUser(remote)
            }
            .map { }
    }

    suspend fun isUserBlocked(userId: Long): Boolean {
        return userDao.getById(userId)?.isBlocked ?: false
    }

    private suspend fun upsertRemoteUser(remote: UserResponse): UserEntity {
        Log.d("UserRepository", "Syncing remote user ${remote.email}")
        val existing = userDao.getByEmail(remote.email)
        val isBlocked = remote.estadoId?.equals("BLOQUEADO", ignoreCase = true) == true

        val entity = UserEntity(
            id = existing?.id ?: 0L,
            name = remote.nombre,
            email = remote.email,
            phone = remote.telefono ?: "",
            password = existing?.password ?: "",
            profilePhotoUri = remote.fotoPerfilUrl,
            gender = remote.genero ?: existing?.gender ?: "",
            isBlocked = isBlocked,
            remoteId = remote.id,
            roleId = remote.rolId ?: existing?.roleId,
            statusId = remote.estadoId ?: existing?.statusId,
            createdAt = remote.creadoEn ?: existing?.createdAt
        )

        userDao.insert(entity)
        return userDao.getByEmail(remote.email) ?: entity
    }
}