package com.example.uinavegacion.data.repository

import com.example.uinavegacion.data.local.user.UserDao
import com.example.uinavegacion.data.local.user.UserEntity

//orquesta todas las reglas de negocio para el login/ registro sobre el DAO comun
class UserRepository(
    private val userDao: UserDao //inyectando el DAO

){
    //manipular login (email y pass coincidan)
    suspend fun login(email: String, password: String): Result<UserEntity>{
        val user = userDao.getByEmail(email)
        return if(user != null && user.password == password){
            Result.success(user)
        }else{
            Result.failure(Exception("credenciales invalidas"))

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


}