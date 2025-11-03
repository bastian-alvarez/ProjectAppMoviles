package com.example.uinavegacion.data.local.admin

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AdminDao {
    // Insertar administrador
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(admin: AdminEntity): Long

    // Buscar admin por email
    @Query("SELECT * FROM admins WHERE email = :email LIMIT 1")
    suspend fun getByEmail(email: String): AdminEntity?

    // Buscar admin por id
    @Query("SELECT * FROM admins WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): AdminEntity?

    // Buscar admin por email y password (para login)
    @Query("SELECT * FROM admins WHERE email = :email AND password = :password LIMIT 1")
    suspend fun validateAdmin(email: String, password: String): AdminEntity?

    // Obtener todos los admins
    @Query("SELECT * FROM admins")
    suspend fun getAll(): List<AdminEntity>

    // Contar admins
    @Query("SELECT COUNT(*) FROM admins")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM admins")
    fun observeCount(): Flow<Int>

    // Verificar si tiene rol específico
    @Query("SELECT COUNT(*) FROM admins WHERE email = :email AND role = :role")
    suspend fun hasRole(email: String, role: String): Int

    // Actualizar admin
    @Query("UPDATE admins SET name = :name, email = :email, phone = :phone, password = :password, role = :role WHERE id = :id")
    suspend fun update(id: Long, name: String, email: String, phone: String, password: String, role: String)
    
    // Actualizar foto de perfil
    @Query("UPDATE admins SET profilePhotoUri = :photoUri WHERE id = :id")
    suspend fun updateProfilePhoto(id: Long, photoUri: String?)

    // Actualizar solo el rol
    @Query("UPDATE admins SET role = :role WHERE id = :id")
    suspend fun updateRole(id: Long, role: String)

    // Eliminar admin
    @Query("DELETE FROM admins WHERE id = :id")
    suspend fun delete(id: Long)
}
