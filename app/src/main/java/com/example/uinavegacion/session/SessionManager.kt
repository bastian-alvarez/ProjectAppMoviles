package com.example.uinavegacion.session

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Gestor simple de sesión en memoria.
 * Guarda el email del usuario autenticado para usarlo en pantallas (Profile/ProfileEdit).
 */
object SessionManager {
    private val _currentUserEmail = MutableStateFlow<String?>(null)
    val currentUserEmail: StateFlow<String?> = _currentUserEmail

    fun login(email: String) { _currentUserEmail.value = email }
    fun logout() { _currentUserEmail.value = null }
}
