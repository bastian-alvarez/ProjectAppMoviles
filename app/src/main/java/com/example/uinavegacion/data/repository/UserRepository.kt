package com.example.uinavegacion.data.repository

import android.util.Log
import com.example.uinavegacion.data.local.user.UserDao
import com.example.uinavegacion.data.local.user.UserEntity
import com.example.uinavegacion.data.remote.repository.AuthRemoteRepository
import com.example.uinavegacion.data.remote.dto.LoginRequest
import com.example.uinavegacion.data.remote.dto.RegisterRequest

//orquesta todas las reglas de negocio para el login/ registro sobre el DAO comun
class UserRepository(
    private val userDao: UserDao, //inyectando el DAO
    private val authRemoteRepository: AuthRemoteRepository = AuthRemoteRepository()
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
                
                // Sincronizar con la BD local
                val userEntity = UserEntity(
                    id = authResponse.user.id,
                    name = authResponse.user.name,
                    email = authResponse.user.email,
                    phone = authResponse.user.phone,
                    password = password, // Guardamos el password localmente para cache
                    profilePhotoUri = authResponse.user.profilePhotoUri,
                    gender = authResponse.user.gender,
                    isBlocked = authResponse.user.isBlocked
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
                
                // Sincronizar con la BD local
                val userEntity = UserEntity(
                    id = authResponse.user.id,
                    name = authResponse.user.name,
                    email = authResponse.user.email,
                    phone = authResponse.user.phone,
                    password = password,
                    profilePhotoUri = authResponse.user.profilePhotoUri,
                    gender = authResponse.user.gender,
                    isBlocked = authResponse.user.isBlocked
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
    suspend fun getAllUsers(): List<UserEntity> {
        return userDao.getAll()
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
    suspend fun toggleBlockStatus(userId: Long, isBlocked: Boolean): Result<Unit> {
        return try {
            userDao.updateBlockStatus(userId, isBlocked)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    //verificar si un usuario está bloqueado
    suspend fun isUserBlocked(userId: Long): Boolean {
        return userDao.isUserBlocked(userId) ?: false
    }
}