package com.example.uinavegacion.data

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import com.example.uinavegacion.data.local.user.UserEntity
import com.example.uinavegacion.data.local.admin.AdminEntity

/**
 * SessionManager simple para mantener la información del usuario logueado
 */
object SessionManager {
    private val _currentUser = MutableLiveData<UserEntity?>()
    val currentUser: LiveData<UserEntity?> = _currentUser
    
    private val _currentAdmin = MutableLiveData<AdminEntity?>()
    val currentAdmin: LiveData<AdminEntity?> = _currentAdmin
    
    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> = _isLoggedIn
    
    // Token de autenticación
    private var authToken: String? = null
    
    fun loginUser(user: UserEntity) {
        _currentUser.value = user
        _currentAdmin.value = null
        _isLoggedIn.value = true
    }
    
    fun loginAdmin(admin: AdminEntity) {
        _currentAdmin.value = admin
        _currentUser.value = null
        _isLoggedIn.value = true
    }
    
    fun logout() {
        _currentUser.value = null
        _currentAdmin.value = null
        _isLoggedIn.value = false
        authToken = null
    }
    
    // Métodos para manejar el token
    fun saveToken(token: String) {
        authToken = token
    }
    
    fun getToken(): String? {
        return authToken
    }
    
    fun hasToken(): Boolean {
        return !authToken.isNullOrBlank()
    }
    
    fun isAdmin(): Boolean {
        return _currentAdmin.value != null
    }
    
    fun getCurrentUserEmail(): String? {
        return _currentUser.value?.email ?: _currentAdmin.value?.email
    }
    
    fun getCurrentUserName(): String? {
        return _currentUser.value?.name ?: _currentAdmin.value?.name
    }
    
    fun getCurrentUserPhotoUri(): String? {
        return _currentUser.value?.profilePhotoUri ?: _currentAdmin.value?.profilePhotoUri
    }
    
    fun getCurrentUserId(): Long? {
        return _currentUser.value?.id
    }
    
    fun getCurrentUserRemoteId(): String? {
        return _currentUser.value?.remoteId
    }
    
    fun getCurrentUser(): UserEntity? {
        return _currentUser.value
    }
    
    fun getCurrentAdmin(): AdminEntity? {
        return _currentAdmin.value
    }
    
    fun isModerator(): Boolean {
        return _currentAdmin.value?.role == "MODERATOR"
    }
}

