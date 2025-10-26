package com.example.uinavegacion.ui.viewmodel

import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.uinavegacion.domain.validateEmail
import com.example.uinavegacion.domain.validateConfirm
import com.example.uinavegacion.domain.validateLettersOnly
import com.example.uinavegacion.domain.validateNickname
import com.example.uinavegacion.domain.validateStrongPassword
import com.example.uinavegacion.domain.validatePhoneDigitsOnly
import com.example.uinavegacion.data.repository.UserRepository
import com.example.uinavegacion.data.repository.AdminRepository
import com.example.uinavegacion.data.local.database.AppDatabase
import com.example.uinavegacion.data.SessionManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull



data class LoginUiState(
    val email: String = "", //campo email del formulario
    val pass: String = "", //campo clave del formulario
    val emailError: String? = null, //campo error de correo
    val passError: String? = null, //campo error de clave
    val isSubmitting: Boolean = false, //flag de carga
    val canSubmit: Boolean = false, //visibilidad del botón
    val success: Boolean = false, //resultado ok del formulario
    val errorMsg: String? = null, // error general (credenciales son incorrectas)
    val isAdmin: Boolean = false // indica si el usuario logueado es administrador
)

data class RegisterUiState(
    //variables para los campos
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val pass: String = "",
    val confirm: String = "",
    //variables para los errores en los campos
    val nameError: String? = null,
    val emailError: String? = null,
    val phoneError: String? = null,
    val passError: String? = null,
    val confirmError: String? = null,

    val isSubmitting: Boolean = false, // flag de carga
    val canSubmit: Boolean = false, //visibilidad del botón
    val success: Boolean = false, //formulario OK
    val errorMsg: String? = null //error general (usuario ya existe)

)

data class ChangePasswordUiState(
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val currentPasswordError: String? = null,
    val newPasswordError: String? = null,
    val confirmPasswordError: String? = null,
    val isSubmitting: Boolean = false,
    val canSubmit: Boolean = false,
    val success: Boolean = false,
    val errorMsg: String? = null,
    val successMsg: String? = null
)


//clase para manipular la logica de Login y Register
class AuthViewModel(
    application: android.app.Application,
    private val userRepository: UserRepository,
    private val adminRepository: AdminRepository
): AndroidViewModel(application) {
    // Flujos de estado para observar desde la UI
    private val _login = MutableStateFlow(LoginUiState())   // Estado interno (Login)
    val login: StateFlow<LoginUiState> = _login             // Exposición inmutable

    private val _register = MutableStateFlow(RegisterUiState()) // Estado interno (Registro)
    val register: StateFlow<RegisterUiState> = _register        // Exposición inmutable

    private val _changePassword = MutableStateFlow(ChangePasswordUiState()) // Estado interno (Cambio de contraseña)
    val changePassword: StateFlow<ChangePasswordUiState> = _changePassword  // Exposición inmutable

    // ----------------- LOGIN: handlers y envío -----------------

    fun onLoginEmailChange(value: String) {
        _login.update { it.copy(email = value, emailError = null, errorMsg = null) } // Solo guardamos, sin validar en tiempo real
        recomputeLoginCanSubmit()
    }

    fun onLoginPassChange(value: String) {                  // Handler cuando cambia la contraseña
        _login.update { it.copy(pass = value, errorMsg = null) }             // Guardamos (sin validar fuerza aquí) + limpiamos error
        recomputeLoginCanSubmit()                           // Recalculamos habilitado
    }

    private fun recomputeLoginCanSubmit() {
        val s = _login.value
        // Solo requerimos que los campos no estén vacíos (sin validación estricta de email)
        val can = s.email.isNotBlank() && s.pass.isNotBlank()
        _login.update { it.copy(canSubmit = can) }
    }

    fun submitLogin() {
        val s = _login.value
        Log.d("AuthViewModel", "🚀 submitLogin iniciado: email='${s.email}', pass='${s.pass.take(3)}...'")
        
        // Validaciones básicas
        val email = s.email.trim()
        val pass = s.pass.trim()
        
        if (email.isBlank()) {
            _login.update {
                it.copy(
                    success = false,
                    errorMsg = "📧 El email es obligatorio",
                    isAdmin = false,
                    isSubmitting = false
                )
            }
            return
        }
        
        if (pass.isBlank()) {
            _login.update {
                it.copy(
                    success = false,
                    errorMsg = "🔑 La contraseña es obligatoria",
                    isAdmin = false,
                    isSubmitting = false
                )
            }
            return
        }
        
        // Limpiar estado previo
        _login.update { it.copy(errorMsg = null, success = false, isAdmin = false, isSubmitting = false) }
        
        // Login hardcodeado para admin
        if (email == "admin@steamish.com" && pass == "Admin123!") {
            Log.d("AuthViewModel", "✅ Login admin exitoso")
            
            val tempAdmin = com.example.uinavegacion.data.local.admin.AdminEntity(
                id = 1L,
                name = "Admin Temporal",
                email = email,
                phone = "+56 9 8877 6655",
                password = pass,
                role = "SUPER_ADMIN"
            )
            SessionManager.loginAdmin(tempAdmin)
            
            _login.update {
                it.copy(
                    success = true,
                    errorMsg = null,
                    isAdmin = true,
                    isSubmitting = false
                )
            }
            return
        }
        
        // Login hardcodeado para usuario de prueba (más opciones)
        if ((email == "usuario@test.com" && pass == "123456") ||
            (email == "user1@demo.com" && pass == "Password123!") ||
            (email == "test@test.com" && pass == "123")) {
            
            Log.d("AuthViewModel", "✅ Login usuario hardcodeado exitoso")
            
            val tempUser = com.example.uinavegacion.data.local.user.UserEntity(
                id = 1L,
                name = "Usuario Prueba",
                email = email,
                phone = "+56 9 1234 5678",
                password = pass,
                isBlocked = false
            )
            SessionManager.loginUser(tempUser)
            
            _login.update {
                it.copy(
                    success = true,
                    errorMsg = null,
                    isAdmin = false,
                    isSubmitting = false
                )
            }
            Log.d("AuthViewModel", "🏁 Usuario hardcodeado autenticado, navegando...")
            return
        }
        
        // Buscar en la base de datos
        viewModelScope.launch {
            try {
                Log.d("AuthViewModel", "🔍 Buscando usuario en BD...")
                Log.d("AuthViewModel", "📧 Email a buscar: '$email'")
                
                val existingUser = withTimeoutOrNull(3000) { // Timeout de 3 segundos
                    userRepository.getUserByEmail(email)
                }
                
                if (existingUser == null) {
                    Log.d("AuthViewModel", "📋 Usuario no encontrado en BD")
                } else {
                    Log.d("AuthViewModel", "📋 Usuario encontrado: ${existingUser.email}")
                }
                
                if (existingUser != null) {
                    Log.d("AuthViewModel", "👤 Usuario encontrado: ${existingUser.email}")
                    
                    if (existingUser.password == pass) {
                        if (existingUser.isBlocked) {
                            _login.update {
                                it.copy(
                                    success = false,
                                    errorMsg = "🚫 Cuenta bloqueada. Contacta al administrador.",
                                    isAdmin = false,
                                    isSubmitting = false
                                )
                            }
                            Log.d("AuthViewModel", "🚫 Usuario bloqueado")
                        } else {
                            // Login exitoso
                            SessionManager.loginUser(existingUser)
                            _login.update {
                                it.copy(
                                    success = true,
                                    errorMsg = null,
                                    isAdmin = false,
                                    isSubmitting = false
                                )
                            }
                            Log.d("AuthViewModel", "🎉 Login usuario BD exitoso")
                        }
                    } else {
                        // Contraseña incorrecta
                        Log.d("AuthViewModel", "❌ Contraseña incorrecta para usuario existente")
                        _login.update {
                            it.copy(
                                success = false,
                                errorMsg = "❌ Credenciales inválidas",
                                isAdmin = false,
                                isSubmitting = false
                            )
                        }
                        Log.d("AuthViewModel", "✅ Estado actualizado con error de contraseña")
                    }
                } else {
                    Log.d("AuthViewModel", "❌ Usuario no encontrado, verificando admin...")
                    // Verificar si es admin en BD
                    val admin = withTimeoutOrNull(3000) {
                        adminRepository.validateAdmin(email, pass)
                    }
                    Log.d("AuthViewModel", "🔍 Admin encontrado: ${admin?.email ?: "null"}")
                    
                    if (admin != null) {
                        SessionManager.loginAdmin(admin)
                        _login.update {
                            it.copy(
                                success = true,
                                errorMsg = null,
                                isAdmin = true,
                                isSubmitting = false
                            )
                        }
                        Log.d("AuthViewModel", "🎉 Login admin BD exitoso")
                    } else {
                        Log.d("AuthViewModel", "❌ Credenciales completamente inválidas")
                        // Credenciales inválidas
                        _login.update {
                            it.copy(
                                success = false,
                                errorMsg = "❌ Credenciales inválidas",
                                isAdmin = false,
                                isSubmitting = false
                            )
                        }
                        Log.d("AuthViewModel", "✅ Estado actualizado con mensaje de error")
                    }
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "💥 Error en login: ${e.message}", e)
                _login.update {
                    it.copy(
                        success = false,
                        errorMsg = "❌ Credenciales inválidas",
                        isAdmin = false,
                        isSubmitting = false
                    )
                }
            }
        }
    }

    fun clearLoginResult() {                                // Limpia banderas tras navegar
        Log.d("AuthViewModel", "🧹 Limpiando estado de login")
        _login.update { it.copy(success = false, errorMsg = null, isAdmin = false) }
    }

    fun logout() {                                          // Función para cerrar sesión
        SessionManager.logout()
    }

    // ----------------- REGISTRO: handlers y envío -----------------

    fun onNameChange(value: String) {                       // Handler del nickname
        // Filtramos solo letras, números y guion bajo
        val filtered = value.filter { it.isLetterOrDigit() || it == '_' }
        _register.update {                                  // Guardamos + validamos
            it.copy(name = filtered, nameError = validateNickname(filtered))
        }
        recomputeRegisterCanSubmit()                        // Recalculamos habilitado
    }

    fun onRegisterEmailChange(value: String) {              // Handler del email
        _register.update { it.copy(email = value, emailError = validateEmail(value)) } // Guardamos + validamos
        recomputeRegisterCanSubmit()
    }

    fun onPhoneChange(value: String) {                      // Handler del teléfono
        // Formatear automáticamente el teléfono chileno
        val formatted = formatChileanPhone(value)
        _register.update {                                  // Guardamos + validamos
            it.copy(phone = formatted, phoneError = validatePhoneDigitsOnly(formatted))
        }
        recomputeRegisterCanSubmit()
    }
    
    // Función para formatear el teléfono a +56 9 XXXX XXXX
    private fun formatChileanPhone(input: String): String {
        // Extraer solo los dígitos
        val digits = input.filter { it.isDigit() }
        
        // Si está vacío, retornar vacío
        if (digits.isEmpty()) return ""
        
        // Construir el formato según la cantidad de dígitos
        return when {
            digits.length <= 2 -> "+$digits"
            digits.length == 3 -> "+${digits.substring(0, 2)} ${digits[2]}"
            digits.length <= 11 -> {
                val countryCode = digits.substring(0, 2) // 56
                val mobilePrefix = digits.getOrNull(2) ?: "" // 9
                val remaining = digits.substring(3.coerceAtMost(digits.length))
                
                when {
                    remaining.isEmpty() -> "+$countryCode $mobilePrefix"
                    remaining.length <= 4 -> "+$countryCode $mobilePrefix $remaining"
                    else -> {
                        val first4 = remaining.substring(0, 4)
                        val last4 = remaining.substring(4).take(4)
                        "+$countryCode $mobilePrefix $first4 $last4"
                    }
                }
            }
            else -> {
                // Si tiene más de 11 dígitos, tomar solo los primeros 11
                val trimmed = digits.take(11)
                val countryCode = trimmed.substring(0, 2)
                val mobilePrefix = trimmed[2]
                val first4 = trimmed.substring(3, 7)
                val last4 = trimmed.substring(7)
                "+$countryCode $mobilePrefix $first4 $last4"
            }
        }
    }

    fun onRegisterPassChange(value: String) {               // Handler de la contraseña
        _register.update { it.copy(pass = value, passError = validateStrongPassword(value)) } // Validamos seguridad
        // Revalidamos confirmación con la nueva contraseña
        _register.update { it.copy(confirmError = validateConfirm(it.pass, it.confirm)) }
        recomputeRegisterCanSubmit()
    }

    fun onConfirmChange(value: String) {                    // Handler de confirmación
        _register.update { it.copy(confirm = value, confirmError = validateConfirm(it.pass, value)) } // Guardamos + validamos
        recomputeRegisterCanSubmit()
    }

    private fun recomputeRegisterCanSubmit() {              // Habilitar "Registrar" si todo OK
        val s = _register.value                              // Tomamos el estado actual
        val noErrors = listOf(s.nameError, s.emailError, s.phoneError, s.passError, s.confirmError).all { it == null } // Sin errores
        val filled = s.name.isNotBlank() && s.email.isNotBlank() && s.phone.isNotBlank() && s.pass.isNotBlank() && s.confirm.isNotBlank() // Todo lleno
        _register.update { it.copy(canSubmit = noErrors && filled) } // Actualizamos flag
    }

    fun submitRegister() {                                  // Acción de registro (simulación async)
        val s = _register.value                              // Snapshot del estado
        if (!s.canSubmit || s.isSubmitting) return          // Evitamos reentradas
        viewModelScope.launch {                             // Corrutina
            _register.update { it.copy(isSubmitting = true, errorMsg = null, success = false) } // Loading
            delay(700)                                      // Simulamos IO

            // Intentar registrar usuario usando el repositorio
            val result = userRepository.register(
                name = s.name.trim(),
                email = s.email.trim(),
                phone = s.phone.trim(),
                password = s.pass
            )

            if (result.isSuccess) {
                _register.update {                               // Éxito
                    it.copy(isSubmitting = false, success = true, errorMsg = null)
                }
            } else {
                _register.update {                               // Error
                    it.copy(
                        isSubmitting = false, 
                        success = false, 
                        errorMsg = result.exceptionOrNull()?.message ?: "Error al registrar usuario"
                    )
                }
            }
        }
    }

    fun clearRegisterResult() {                             // Limpia banderas tras navegar
        _register.update { it.copy(success = false, errorMsg = null) }
    }

    // ----------------- CAMBIO DE CONTRASEÑA: handlers y envío -----------------

    fun onCurrentPasswordChange(value: String) {
        _changePassword.update { 
            it.copy(currentPassword = value, currentPasswordError = null) 
        }
        recomputeChangePasswordCanSubmit()
    }

    fun onNewPasswordChange(value: String) {
        _changePassword.update { 
            it.copy(
                newPassword = value, 
                newPasswordError = validateStrongPassword(value),
                confirmPasswordError = validateConfirm(value, it.confirmPassword)
            ) 
        }
        recomputeChangePasswordCanSubmit()
    }

    fun onConfirmPasswordChange(value: String) {
        _changePassword.update { 
            it.copy(
                confirmPassword = value, 
                confirmPasswordError = validateConfirm(it.newPassword, value)
            ) 
        }
        recomputeChangePasswordCanSubmit()
    }

    private fun recomputeChangePasswordCanSubmit() {
        val s = _changePassword.value
        val noErrors = listOf(s.currentPasswordError, s.newPasswordError, s.confirmPasswordError).all { it == null }
        val filled = s.currentPassword.isNotBlank() && s.newPassword.isNotBlank() && s.confirmPassword.isNotBlank()
        val passwordsMatch = s.newPassword == s.confirmPassword
        val differentPasswords = s.currentPassword != s.newPassword
        
        _changePassword.update { 
            it.copy(canSubmit = noErrors && filled && passwordsMatch && differentPasswords) 
        }
    }

    fun submitChangePassword(userEmail: String) {
        val s = _changePassword.value
        if (!s.canSubmit || s.isSubmitting) return
        
        viewModelScope.launch {
            _changePassword.update { 
                it.copy(isSubmitting = true, errorMsg = null, successMsg = null, success = false) 
            }
            
            try {
                val result = userRepository.changePassword(
                    email = userEmail,
                    currentPassword = s.currentPassword,
                    newPassword = s.newPassword
                )
                
                if (result.isSuccess) {
                    _changePassword.update {
                        it.copy(
                            isSubmitting = false,
                            success = true,
                            successMsg = "Contraseña actualizada exitosamente. Serás redirigido al login...",
                            errorMsg = null
                        )
                    }
                } else {
                    _changePassword.update {
                        it.copy(
                            isSubmitting = false,
                            success = false,
                            errorMsg = result.exceptionOrNull()?.message ?: "Error al cambiar la contraseña",
                            successMsg = null
                        )
                    }
                }
            } catch (e: Exception) {
                _changePassword.update {
                    it.copy(
                        isSubmitting = false,
                        success = false,
                        errorMsg = e.message ?: "Error inesperado",
                        successMsg = null
                    )
                }
            }
        }
    }

    fun clearChangePasswordResult() {
        _changePassword.update { 
            it.copy(success = false, errorMsg = null, successMsg = null) 
        }
    }
}
