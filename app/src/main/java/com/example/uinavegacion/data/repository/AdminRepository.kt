package com.example.uinavegacion.data.repository

import android.util.Log
import com.example.uinavegacion.data.local.admin.AdminDao
import com.example.uinavegacion.data.local.admin.AdminEntity

/**
 * Repositorio para manejar administradores usando Room Database
 */
class AdminRepository(
    private val adminDao: AdminDao
) {
    
    /**
     * Valida las credenciales de un administrador
     */
    suspend fun validateAdmin(email: String, password: String): AdminEntity? {
        Log.d("AdminRepository", "Attempting to validate admin with email: [$email]")
        val admin = adminDao.validateAdmin(email, password)
        if (admin != null) {
            Log.d("AdminRepository", "Admin found in DB for email: [$email]")
        } else {
            Log.d("AdminRepository", "Admin NOT found in DB for email: [$email] with provided password.")
        }
        return admin
    }
    
    /**
     * Registra un nuevo administrador
     */
    suspend fun registerAdmin(
        name: String,
        email: String,
        phone: String,
        password: String,
        role: String
    ): Result<Long> {
        // Verificar si el email ya existe
        val exists = adminDao.getByEmail(email) != null
        if (exists) {
            return Result.failure(Exception("El email ya está registrado como administrador"))
        }
        
        // Insertar nuevo administrador
        val id = adminDao.insert(
            AdminEntity(
                name = name,
                email = email,
                phone = phone,
                password = password,
                role = role
            )
        )
        return Result.success(id)
    }
    
    /**
     * Obtiene un administrador por email
     */
    suspend fun getAdminByEmail(email: String): AdminEntity? {
        return adminDao.getByEmail(email)
    }
    
    /**
     * Obtiene todos los administradores
     */
    suspend fun getAllAdmins(): List<AdminEntity> {
        return adminDao.getAll()
    }
    
    /**
     * Verifica si un administrador tiene un rol específico
     */
    suspend fun hasRole(email: String, role: String): Boolean {
        return adminDao.hasRole(email, role) > 0
    }
    
    /**
     * Actualiza el rol de un administrador
     */
    suspend fun updateAdminRole(id: Long, newRole: String) {
        adminDao.updateRole(id, newRole)
    }
    
    /**
     * Elimina un administrador
     */
    suspend fun removeAdmin(id: Long) {
        adminDao.delete(id)
    }
}
