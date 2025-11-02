package com.example.uinavegacion.data.local.user

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

//usar delete, insert, select, update
@Dao
interface UserDao{
    //Insertar el usuario - abortar si hay errores(pk duplicado)
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: UserEntity): Long

    //buscar usuario por email(si no existe devuelve null)
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getByEmail(email: String): UserEntity?

    //buscar usuario por id
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): UserEntity?
    //buscar todos los usuarios
    @Query("SELECT * FROM users")
    suspend fun getAll(): List<UserEntity>
    //contar el total de usuarios
    @Query("SELECT COUNT(*) FROM users")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM users")
    fun observeCount(): Flow<Int>
    //lista de usuarios ordenada ascendente por su id
    @Query("SELECT * FROM users ORDER BY id ASC")
    suspend fun getAllOrderedById(): List<UserEntity>
    //actualizar el usuario
    @Query("UPDATE users SET name = :name, email = :email, phone = :phone, password = :password, gender = :gender WHERE id = :id")
    suspend fun update(id: Long, name: String, email: String, phone: String, password: String, gender: String = "")
    //actualizar la foto de perfil
    @Query("UPDATE users SET profilePhotoUri = :photoUri WHERE id = :id")
    suspend fun updateProfilePhoto(id: Long, photoUri: String?)
    //actualizar solo la contraseña
    @Query("UPDATE users SET password = :newPassword WHERE id = :id")
    suspend fun updatePassword(id: Long, newPassword: String)
    //eliminar el usuario
    @Query("DELETE FROM users WHERE id = :id")
    suspend fun delete(id: Long)
    
    //bloquear/desbloquear usuario
    @Query("UPDATE users SET isBlocked = :isBlocked WHERE id = :id")
    suspend fun updateBlockStatus(id: Long, isBlocked: Boolean)
    
    //obtener estado de bloqueo del usuario
    @Query("SELECT isBlocked FROM users WHERE id = :id")
    suspend fun isUserBlocked(id: Long): Boolean?

}