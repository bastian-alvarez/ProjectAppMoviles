package com.example.uinavegacion.data.repository

import android.util.Log
import com.example.uinavegacion.data.local.user.UserDao
import com.example.uinavegacion.data.local.user.UserEntity

//orquesta todas las reglas de negocio para el login/ registro sobre el DAO comun
class UserRepository(
    private val userDao: UserDao //inyectando el DAO

){
    //manipular login (email y pass coincidan)
    suspend fun login(email: String, password: String): Result<UserEntity>{
        Log.d("UserRepository", "Attempting login for email: [$email]")
        val user = userDao.getByEmail(email)
        if (user != null) {
            Log.d("UserRepository", "User found in DB. Stored pass: [${user.password}], Provided pass: [$password]")
            if (user.password == password) {
                Log.d("UserRepository", "Password matches. Login success.")
                return Result.success(user)
            } else {
                Log.d("UserRepository", "Password does NOT match. Login failed.")
                return Result.failure(Exception("credenciales invalidas"))
            }
        } else {
            Log.d("UserRepository", "User with email [$email] not found in DB.")
            return Result.failure(Exception("credenciales invalidas"))
        }

    }
    //manipular registro (email duplicado)
    suspend fun register(name: String, email: String, phone: String, password: String): Result<Long>{
        val exists = userDao.getByEmail(email) != null
        if(exists){
            return Result.failure(Exception("el correo ya esta registrado"))

        }else{
            val id = userDao.insert(
                UserEntity
                    (name = name,
                    email = email,
                    phone = phone,
                    password = password))
            return Result.success(id)

        }
    }

    //actualizar foto de perfil
    suspend fun updateProfilePhoto(userId: Long, photoUri: String?): Result<Unit> {
        return try {
            userDao.updateProfilePhoto(userId, photoUri)
            Result.success(Unit)
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
}