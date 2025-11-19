package com.example.uinavegacion.data.remote.admin

import android.util.Log
import com.example.uinavegacion.data.remote.config.RetrofitClient
import com.example.uinavegacion.data.remote.dto.UserResponse

/**
 * Repositorio para operaciones de administrador sobre usuarios
 */
class AdminUserRemoteRepository {
    private val api: AdminUserService = RetrofitClient.createAuthService()
        .create(AdminUserService::class.java)
    
    /**
     * Listar todos los usuarios (solo administradores)
     */
    suspend fun listAllUsers(): Result<List<UserResponse>> {
        return try {
            Log.d("AdminUserRepo", "📋 Obteniendo todos los usuarios (admin)...")
            val response = api.listAllUsers()
            if (response.isSuccessful && response.body() != null) {
                Log.d("AdminUserRepo", "✅ Usuarios obtenidos: ${response.body()!!.size}")
                Result.success(response.body()!!)
            } else {
                val error = "Error al obtener usuarios: ${response.code()} - ${response.message()}"
                Log.e("AdminUserRepo", "❌ $error")
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Log.e("AdminUserRepo", "❌ Excepción al obtener usuarios: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Obtener un usuario por ID (solo administradores)
     */
    suspend fun getUserById(userId: String): Result<UserResponse> {
        return try {
            Log.d("AdminUserRepo", "🔍 Obteniendo usuario ID: $userId")
            val response = api.getUserById(userId)
            if (response.isSuccessful && response.body() != null) {
                Log.d("AdminUserRepo", "✅ Usuario obtenido: ${response.body()!!.email}")
                Result.success(response.body()!!)
            } else {
                val error = "Usuario no encontrado: ${response.code()}"
                Log.e("AdminUserRepo", "❌ $error")
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Log.e("AdminUserRepo", "❌ Excepción al obtener usuario: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Actualizar un usuario (solo administradores)
     */
    suspend fun updateUser(
        userId: String,
        nombre: String,
        email: String,
        telefono: String?,
        genero: String?,
        fotoPerfilUrl: String?
    ): Result<UserResponse> {
        return try {
            Log.d("AdminUserRepo", "✏️ Actualizando usuario ID: $userId")
            val request = UpdateUserRequest(
                nombre = nombre,
                email = email,
                telefono = telefono,
                genero = genero,
                fotoPerfilUrl = fotoPerfilUrl
            )
            val response = api.updateUser(userId, request)
            if (response.isSuccessful && response.body() != null) {
                Log.d("AdminUserRepo", "✅ Usuario actualizado: ${response.body()!!.email}")
                Result.success(response.body()!!)
            } else {
                val error = "Error al actualizar usuario: ${response.code()}"
                Log.e("AdminUserRepo", "❌ $error")
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Log.e("AdminUserRepo", "❌ Excepción al actualizar usuario: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Eliminar un usuario (solo administradores)
     */
    suspend fun deleteUser(userId: String): Result<Unit> {
        return try {
            Log.d("AdminUserRepo", "🗑️ Eliminando usuario ID: $userId")
            val response = api.deleteUser(userId)
            if (response.isSuccessful) {
                Log.d("AdminUserRepo", "✅ Usuario eliminado exitosamente")
                Result.success(Unit)
            } else {
                val error = "Error al eliminar usuario: ${response.code()} - ${response.message()}"
                Log.e("AdminUserRepo", "❌ $error")
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Log.e("AdminUserRepo", "❌ Excepción al eliminar usuario: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Bloquear un usuario (solo administradores)
     */
    suspend fun blockUser(userId: String): Result<UserResponse> {
        return try {
            Log.d("AdminUserRepo", "🚫 Bloqueando usuario ID: $userId")
            val response = api.blockUser(userId)
            if (response.isSuccessful && response.body() != null) {
                Log.d("AdminUserRepo", "✅ Usuario bloqueado: ${response.body()!!.email}")
                Result.success(response.body()!!)
            } else {
                val error = "Error al bloquear usuario: ${response.code()}"
                Log.e("AdminUserRepo", "❌ $error")
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Log.e("AdminUserRepo", "❌ Excepción al bloquear usuario: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Desbloquear un usuario (solo administradores)
     */
    suspend fun unblockUser(userId: String): Result<UserResponse> {
        return try {
            Log.d("AdminUserRepo", "✅ Desbloqueando usuario ID: $userId")
            val response = api.unblockUser(userId)
            if (response.isSuccessful && response.body() != null) {
                Log.d("AdminUserRepo", "✅ Usuario desbloqueado: ${response.body()!!.email}")
                Result.success(response.body()!!)
            } else {
                val error = "Error al desbloquear usuario: ${response.code()}"
                Log.e("AdminUserRepo", "❌ $error")
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Log.e("AdminUserRepo", "❌ Excepción al desbloquear usuario: ${e.message}", e)
            Result.failure(e)
        }
    }
}

