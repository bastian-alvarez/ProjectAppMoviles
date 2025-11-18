package com.example.uinavegacion.data.repository

import android.util.Log
import com.example.uinavegacion.data.SessionManager
import com.example.uinavegacion.data.local.user.UserDao
import com.example.uinavegacion.data.local.user.UserEntity
import com.example.uinavegacion.data.remote.repository.AuthRemoteRepository
import com.example.uinavegacion.data.remote.dto.LoginRequest
import com.example.uinavegacion.data.remote.dto.RegisterRequest
import com.example.uinavegacion.data.remote.admin.AdminUserRemoteRepository

//orquesta todas las reglas de negocio para el login/ registro sobre el DAO comun
class UserRepository(
    private val userDao: UserDao, //inyectando el DAO
    private val authRemoteRepository: AuthRemoteRepository = AuthRemoteRepository(),
    private val userRemoteRepository: com.example.uinavegacion.data.remote.user.UserRemoteRepository = com.example.uinavegacion.data.remote.user.UserRemoteRepository(),
    private val adminUserRemoteRepository: AdminUserRemoteRepository = AdminUserRemoteRepository()
){
    //manipular login (email y pass coincidan) - ahora usa microservicio
    suspend fun login(email: String, password: String): Result<UserEntity>{
        Log.d("UserRepository", "Attempting login via microservice for email: [$email]")
        
        return try {
            // Intentar login con el microservicio
            val remoteResult = authRemoteRepository.login(LoginRequest(email, password))
            
            if (remoteResult.isSuccess) {
                val authResponse = remoteResult.getOrNull()!!
                Log.d("UserRepository", "Login successful via microservice")
                
                // Guardar el token en SessionManager
                SessionManager.saveToken(authResponse.token)
                Log.d("UserRepository", "✓ Token guardado en SessionManager")
                
                // Sincronizar con la BD local
                val userEntity = UserEntity(
                    id = authResponse.user.id,
                    name = authResponse.user.name,
                    email = authResponse.user.email,
                    phone = authResponse.user.phone,
                    password = password, // Guardamos el password localmente para cache
                    profilePhotoUri = authResponse.user.profilePhotoUri,
                    gender = authResponse.user.gender,
                    isBlocked = authResponse.user.isBlocked,
                    remoteId = authResponse.user.id.toString() // Guardar el remoteId
                )
                
                // Guardar/actualizar en BD local
                userDao.insert(userEntity)
                
                Result.success(userEntity)
            } else {
                Log.d("UserRepository", "Login failed via microservice: ${remoteResult.exceptionOrNull()?.message}")
                Result.failure(remoteResult.exceptionOrNull() ?: Exception("credenciales invalidas"))
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error connecting to microservice, trying local DB", e)
            
            // Fallback a BD local si falla el microservicio
            val user = userDao.getByEmail(email)
            if (user != null && user.password == password) {
                Log.d("UserRepository", "Login success via local DB (fallback)")
                Result.success(user)
            } else {
                Result.failure(Exception("credenciales invalidas"))
            }
        }
    }
    
    //manipular registro (email duplicado) - ahora usa microservicio
    suspend fun register(name: String, email: String, phone: String, password: String): Result<Long>{
        Log.d("UserRepository", "Attempting register via microservice for email: [$email]")
        
        return try {
            // Intentar registro con el microservicio
            val remoteResult = authRemoteRepository.register(
                RegisterRequest(
                    name = name,
                    email = email,
                    phone = phone,
                    password = password,
                    gender = ""
                )
            )
            
            if (remoteResult.isSuccess) {
                val authResponse = remoteResult.getOrNull()!!
                Log.d("UserRepository", "Register successful via microservice")
                
                // Guardar el token en SessionManager
                SessionManager.saveToken(authResponse.token)
                Log.d("UserRepository", "✓ Token guardado en SessionManager")
                
                // Sincronizar con la BD local
                val userEntity = UserEntity(
                    id = authResponse.user.id,
                    name = authResponse.user.name,
                    email = authResponse.user.email,
                    phone = authResponse.user.phone,
                    password = password,
                    profilePhotoUri = authResponse.user.profilePhotoUri,
                    gender = authResponse.user.gender,
                    isBlocked = authResponse.user.isBlocked,
                    remoteId = authResponse.user.id.toString() // Guardar el remoteId
                )
                
                userDao.insert(userEntity)
                
                Result.success(authResponse.user.id)
            } else {
                Log.d("UserRepository", "Register failed via microservice: ${remoteResult.exceptionOrNull()?.message}")
                Result.failure(remoteResult.exceptionOrNull() ?: Exception("Error en el registro"))
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error connecting to microservice for register", e)
            Result.failure(Exception("No se pudo conectar con el servidor: ${e.message}"))
        }
    }

    //actualizar perfil completo
    suspend fun updateProfile(
        userId: Long,
        name: String,
        email: String,
        phone: String,
        gender: String,
        photoUrl: String?
    ): Result<UserEntity> {
        return try {
            val existingUser = userDao.getById(userId)
                ?: return Result.failure(Exception("Usuario no encontrado"))
            
            val updatedUser = existingUser.copy(
                name = name,
                email = email,
                phone = phone,
                gender = gender,
                profilePhotoUri = photoUrl
            )
            userDao.insert(updatedUser)
            
            val savedUser = userDao.getById(userId)
                ?: return Result.failure(Exception("Error al guardar usuario"))
            Result.success(savedUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //actualizar foto de perfil
    suspend fun updateProfilePhoto(userId: Long, photoUri: String?): Result<UserEntity> {
        return try {
            userDao.updateProfilePhoto(userId, photoUri)
            val updatedUser = userDao.getById(userId)
                ?: return Result.failure(Exception("Usuario no encontrado después de actualizar"))
            Result.success(updatedUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //obtener todos los usuarios (para administradores)
    /**
     * Obtiene todos los usuarios sincronizando con el microservicio
     */
    suspend fun getAllUsers(): List<UserEntity> {
        return try {
            // 1. Intentar obtener usuarios del microservicio usando endpoint de admin
            Log.d("UserRepository", "📋 Obteniendo usuarios del microservicio (admin endpoint)...")
            val remoteResult = adminUserRemoteRepository.listAllUsers()
            
            if (remoteResult.isSuccess) {
                val remoteUsers = remoteResult.getOrNull() ?: emptyList()
                Log.d("UserRepository", "✓ Obtenidos ${remoteUsers.size} usuarios del microservicio")
                
                // 2. Sincronizar con BD local
                remoteUsers.forEach { remote ->
                    try {
                        upsertRemoteUser(remote)
                    } catch (e: Exception) {
                        Log.w("UserRepository", "No se pudo sincronizar usuario ${remote.email}: ${e.message}")
                    }
                }
                
                // 3. Retornar usuarios de BD local (ya sincronizados)
                userDao.getAll()
            } else {
                // Fallback a BD local si falla el microservicio
                Log.w("UserRepository", "⚠️ No se pudo obtener usuarios del microservicio, usando BD local")
                userDao.getAll()
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error al obtener usuarios: ${e.message}", e)
            // Fallback a BD local
            userDao.getAll()
        }
    }

    //cambiar contraseña de usuario
    suspend fun changePassword(email: String, currentPassword: String, newPassword: String): Result<Unit> {
        return try {
            val user = userDao.getByEmail(email)
            if (user == null) {
                return Result.failure(Exception("Usuario no encontrado"))
            }
            
            if (user.password != currentPassword) {
                return Result.failure(Exception("La contraseña actual es incorrecta"))
            }
            
            userDao.updatePassword(user.id, newPassword)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //obtener usuario por email (para SessionManager)
    suspend fun getUserByEmail(email: String): UserEntity? {
        return userDao.getByEmail(email)
    }
    
    //obtener usuario por id
    suspend fun getUserById(id: Long): UserEntity? {
        return userDao.getById(id)
    }
    
    //bloquear/desbloquear usuario
    /**
     * Bloquea/desbloquea un usuario en el microservicio y BD local
     * Usa los endpoints de administrador /api/admin/users/{id}/block o /unblock
     */
    suspend fun toggleBlockStatus(userId: Long, isBlocked: Boolean): Result<Unit> {
        return try {
            val user = userDao.getById(userId)
            if (user == null) {
                return Result.failure(Exception("Usuario no encontrado"))
            }
            
            // 1. Actualizar en microservicio usando endpoint de admin
            // Usar remoteId si existe, sino usar el ID local
            val idToUse = if (!user.remoteId.isNullOrBlank()) {
                user.remoteId
            } else {
                user.id.toString()
            }
            
            Log.d("UserRepository", "${if (isBlocked) "🚫 Bloqueando" else "✅ Desbloqueando"} usuario en microservicio: ${user.email} (ID: $idToUse)")
            
            val remoteResult = if (isBlocked) {
                adminUserRemoteRepository.blockUser(idToUse)
            } else {
                adminUserRemoteRepository.unblockUser(idToUse)
            }
            
            if (remoteResult.isSuccess) {
                Log.d("UserRepository", "✓ Usuario ${if (isBlocked) "bloqueado" else "desbloqueado"} en microservicio")
                
                // Si no tenía remoteId, guardarlo ahora
                if (user.remoteId.isNullOrBlank()) {
                    userDao.updateRemoteId(userId, idToUse)
                    Log.d("UserRepository", "✓ RemoteId actualizado: $idToUse")
                }
            } else {
                Log.w("UserRepository", "⚠️ No se pudo actualizar en microservicio: ${remoteResult.exceptionOrNull()?.message}")
            }
            
            // 2. Actualizar en BD local
            userDao.updateBlockStatus(userId, isBlocked)
            Log.d("UserRepository", "✓ Usuario ${if (isBlocked) "bloqueado" else "desbloqueado"} en BD local")
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error al bloquear/desbloquear usuario: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    //verificar si un usuario está bloqueado
    suspend fun isUserBlocked(userId: Long): Boolean {
        return userDao.isUserBlocked(userId) ?: false
    }
    
    /**
     * Elimina un usuario del microservicio y BD local
     */
    suspend fun deleteUser(userId: Long): Result<Unit> {
        return try {
            val user = userDao.getById(userId)
            if (user == null) {
                return Result.failure(Exception("Usuario no encontrado"))
            }
            
            // 1. Eliminar del microservicio
            // Usar remoteId si existe, sino usar el ID local
            val idToUse = if (!user.remoteId.isNullOrBlank()) {
                user.remoteId
            } else {
                user.id.toString()
            }
            
            Log.d("UserRepository", "🗑️ Eliminando usuario del microservicio (admin endpoint): ${user.email} (ID: $idToUse)")
            val remoteResult = adminUserRemoteRepository.deleteUser(idToUse)
            
            if (remoteResult.isSuccess) {
                Log.d("UserRepository", "✓ Usuario eliminado del microservicio")
            } else {
                Log.w("UserRepository", "⚠️ No se pudo eliminar del microservicio: ${remoteResult.exceptionOrNull()?.message}")
                // Continuar con eliminación local de todos modos
            }
            
            // 2. Eliminar de BD local
            userDao.delete(user.id)
            Log.d("UserRepository", "✓ Usuario eliminado de BD local")
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error al eliminar usuario: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Sincroniza un usuario del microservicio con la BD local
     */
    private suspend fun upsertRemoteUser(remote: com.example.uinavegacion.data.remote.user.UserResponse) {
        try {
            // Buscar si ya existe en BD local por email
            val existingUser = userDao.getByEmail(remote.email)
            
            if (existingUser != null) {
                // Actualizar usuario existente
                val updated = existingUser.copy(
                    name = remote.nombre,
                    email = remote.email,
                    phone = remote.telefono ?: existingUser.phone,
                    profilePhotoUri = remote.fotoPerfilUrl,
                    gender = remote.genero ?: existingUser.gender,
                    isBlocked = existingUser.isBlocked, // Mantener el estado de bloqueo local
                    remoteId = remote.id
                )
                userDao.insert(updated)
                Log.d("UserRepository", "Usuario actualizado en BD local: ${remote.email}")
            } else {
                // Crear nuevo usuario
                val newUser = UserEntity(
                    id = 0, // Room generará el ID local
                    name = remote.nombre,
                    email = remote.email,
                    phone = remote.telefono ?: "",
                    password = "", // No tenemos el password del microservicio
                    profilePhotoUri = remote.fotoPerfilUrl,
                    gender = remote.genero ?: "",
                    isBlocked = false, // Por defecto no bloqueado
                    remoteId = remote.id
                )
                userDao.insert(newUser)
                Log.d("UserRepository", "Usuario creado en BD local: ${remote.email}")
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error sincronizando usuario ${remote.email}: ${e.message}", e)
            throw e
        }
    }
}