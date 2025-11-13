package com.example.uinavegacion.data.repository

import android.util.Log
import com.example.uinavegacion.data.local.resena.ResenaDao
import com.example.uinavegacion.data.local.resena.ResenaEntity
import java.text.SimpleDateFormat
import java.util.*

class ResenaRepository(
    private val resenaDao: ResenaDao
) {
    suspend fun addResena(
        userId: Long,
        juegoId: Long,
        calificacion: Int,
        comentario: String
    ): Result<Long> {
        return try {
            // Verificar que el usuario no haya hecho ya una reseña para este juego
            val existingResena = resenaDao.getByUserAndJuego(userId, juegoId)
            if (existingResena != null) {
                return Result.failure(Exception("Ya has hecho una reseña para este juego"))
            }
            
            // Validar calificación
            if (calificacion < 1 || calificacion > 5) {
                return Result.failure(Exception("La calificación debe estar entre 1 y 5 estrellas"))
            }
            
            // Validar comentario
            if (comentario.isBlank()) {
                return Result.failure(Exception("El comentario no puede estar vacío"))
            }
            
            val fechaCreacion = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            val resena = ResenaEntity(
                userId = userId,
                juegoId = juegoId,
                calificacion = calificacion,
                comentario = comentario.trim(),
                fechaCreacion = fechaCreacion
            )
            
            val id = resenaDao.insert(resena)
            Log.d("ResenaRepository", "Reseña agregada: ID=$id")
            Result.success(id)
        } catch (e: Exception) {
            Log.e("ResenaRepository", "Error agregando reseña", e)
            Result.failure(e)
        }
    }
    
    suspend fun getResenasByJuegoId(juegoId: Long): List<ResenaEntity> {
        return resenaDao.getByJuegoId(juegoId)
    }
    
    suspend fun getAllResenas(includeDeleted: Boolean = false): List<ResenaEntity> {
        return if (includeDeleted) {
            resenaDao.getAllIncludingDeleted()
        } else {
            resenaDao.getAll()
        }
    }
    
    suspend fun getAverageRating(juegoId: Long): Double {
        return resenaDao.getAverageRating(juegoId) ?: 0.0
    }
    
    suspend fun getResenaCount(juegoId: Long): Int {
        return resenaDao.countByJuegoId(juegoId)
    }
    
    suspend fun deleteResena(id: Long): Result<Unit> {
        return try {
            resenaDao.delete(id)
            Log.d("ResenaRepository", "Reseña eliminada: ID=$id")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ResenaRepository", "Error eliminando reseña", e)
            Result.failure(e)
        }
    }
    
    suspend fun restoreResena(id: Long): Result<Unit> {
        return try {
            resenaDao.restore(id)
            Log.d("ResenaRepository", "Reseña restaurada: ID=$id")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ResenaRepository", "Error restaurando reseña", e)
            Result.failure(e)
        }
    }
}

