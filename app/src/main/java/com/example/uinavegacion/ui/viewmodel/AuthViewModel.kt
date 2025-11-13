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



data class LoginUiState(
    val email: String = "", //campo email del formulario
    val pass: String = "", //campo clave del formulario
    val emailError: String? = null, //campo error de correo
    val passError: String? = null, //campo error de clave
    val isSubmitting: Boolean = false, //flag de carga
    val canSubmit: Boolean = false, //visibilidad del botón
    val success: Boolean = false, //resultado ok del formulario
    val errorMsg: String? = null, // error general (credenciales son incorrectas)
    val isAdmin: Boolean = false, // indica si el usuario logueado es administrador
    val adminRole: String? = null // rol del administrador (MODERATOR, SUPER_ADMIN, etc.)
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

    fun onLoginEmailChange(value: String) {                 // Handler cuando cambia el email
        _login.update { it.copy(email = value, emailError = validateEmail(value)) } // Guardamos + validamos
        recomputeLoginCanSubmit()                           // Recalculamos habilitado
    }

    fun onLoginPassChange(value: String) {                  // Handler cuando cambia la contraseña
        _login.update { it.copy(pass = value) }             // Guardamos (sin validar fuerza aquí)
        recomputeLoginCanSubmit()                           // Recalculamos habilitado
    }

    private fun recomputeLoginCanSubmit() {                 // Regla para habilitar botón "Entrar"
        val s = _login.value                                // Tomamos el estado actual
        val can = s.emailError == null &&                   // Email válido
                s.email.isNotBlank() &&                   // Email no vacío
                s.pass.isNotBlank()                       // Password no vacía
        _login.update { it.copy(canSubmit = can) }          // Actualizamos el flag
    }

    fun submitLogin() {                                     // Acción de login (simulación async)
        val s = _login.value                                // Snapshot del estado
        if (!s.canSubmit || s.isSubmitting) return          // Si no se puede o ya está cargando, salimos
        viewModelScope.launch {                             // Lanzamos corrutina
            try {
                Log.d("AuthViewModel", "=== INICIO LOGIN ===")
                _login.update { it.copy(isSubmitting = true, errorMsg = null, success = false, isAdmin = false) } // Seteamos loading

                val email = s.email.trim()
                val pass = s.pass.trim()
                Log.d("AuthViewModel", "Attempting login with email: [$email], pass length: [${pass.length}]")

                // Verificar que la BD esté inicializada y crear admins faltantes
                val allAdmins = adminRepository.getAllAdmins()
                val adminCount = allAdmins.size
                Log.d("AuthViewModel", "🔍 Total admins in DB: $adminCount")
                
                // Lista de admins que deben existir
                val requiredAdmins = listOf(
                    Triple("admin@steamish.com", "Admin123!", "SUPER_ADMIN"),
                    Triple("moderador@steamish.com", "Moderador123!", "MODERATOR"),
                    Triple("manager@steamish.com", "Manager456@", "GAME_MANAGER"),
                    Triple("support@steamish.com", "Support789#", "SUPPORT")
                )
                
                // Verificar y crear admins faltantes
                var adminsCreated = false
                requiredAdmins.forEach { (adminEmail, adminPassword, role) ->
                    val exists = allAdmins.any { it.email == adminEmail }
                    if (!exists) {
                        Log.w("AuthViewModel", "⚠️ Admin $adminEmail no existe, creando...")
                        adminsCreated = true
                        try {
                            val name = when (role) {
                                "SUPER_ADMIN" -> "Administrador Principal"
                                "MODERATOR" -> "Moderador"
                                "GAME_MANAGER" -> "Gerente de Juegos"
                                "SUPPORT" -> "Soporte Técnico"
                                else -> "Administrador"
                            }
                            val phone = when (role) {
                                "SUPER_ADMIN" -> "+56 9 8877 6655"
                                "MODERATOR" -> "+56 9 5544 3322"
                                "GAME_MANAGER" -> "+56 9 7766 5544"
                                "SUPPORT" -> "+56 9 6655 4433"
                                else -> "+56 9 0000 0000"
                            }
                            val result = adminRepository.registerAdmin(
                                name = name,
                                email = adminEmail,
                                phone = phone,
                                password = adminPassword,
                                role = role
                            )
                            if (result.isSuccess) {
                                Log.d("AuthViewModel", "✅ Admin $adminEmail creado exitosamente")
                            } else {
                                Log.e("AuthViewModel", "❌ Error creando admin $adminEmail: ${result.exceptionOrNull()?.message}")
                            }
                        } catch (e: Exception) {
                            Log.e("AuthViewModel", "💥 Excepción creando admin $adminEmail", e)
                        }
                    }
                }
                
                // Si se crearon admins, recargar la lista
                val finalAdmins = if (adminsCreated) {
                    Log.d("AuthViewModel", "🔄 Recargando lista de admins después de crear nuevos...")
                    adminRepository.getAllAdmins()
                } else {
                    allAdmins
                }

                // Validar credenciales de admin primero
                Log.d("AuthViewModel", "🔐 Validando credenciales de admin...")
                Log.d("AuthViewModel", "📧 Email ingresado: '$email'")
                Log.d("AuthViewModel", "🔑 Password ingresado: '${pass.take(3)}***' (longitud: ${pass.length})")
                
                // Listar todos los admins para debug
                Log.d("AuthViewModel", "📋 Total admins en BD: ${finalAdmins.size}")
                finalAdmins.forEach { a ->
                    Log.d("AuthViewModel", "  - Admin: ${a.email}, Rol: ${a.role}, Password: '${a.password.take(3)}***' (longitud: ${a.password.length})")
                }
                
                val admin = adminRepository.validateAdmin(email, pass)
                val isAdmin = admin != null
                Log.d("AuthViewModel", "🔍 ¿Es admin? $isAdmin")
                if (admin != null) {
                    Log.d("AuthViewModel", "✅ Admin encontrado: ${admin.email}, Rol: ${admin.role}")
                } else {
                    Log.w("AuthViewModel", "❌ Admin NO encontrado con esas credenciales")
                }
                
                // Si no es admin, validar usuario normal
                val userResult = if (!isAdmin) {
                    Log.d("AuthViewModel", "👤 Validando usuario normal...")
                    userRepository.login(email, pass)
                } else null

                var ok = isAdmin || (userResult?.isSuccess == true)
                var errorMessage: String? = userResult?.exceptionOrNull()?.message
                Log.d("AuthViewModel", "🎯 Login exitoso: $ok")

                // Verificar si el usuario está bloqueado (solo para usuarios normales)
                if (ok && !isAdmin) {
                    val user = userResult?.getOrNull() ?: userRepository.getUserByEmail(email)
                    if (user != null && user.isBlocked) {
                        ok = false
                        errorMessage = "Tu cuenta ha sido bloqueada. Contacta al administrador."
                        Log.d("AuthViewModel", "🚫 User is blocked: $email")
                    } else if (user != null) {
                        SessionManager.loginUser(user)
                        Log.d("AuthViewModel", "👤 Usuario logueado en sesión")
                    }
                } else if (ok && isAdmin && admin != null) {
                    SessionManager.loginAdmin(admin)
                    Log.d("AuthViewModel", "👨‍💼 Admin logueado en sesión: ${admin.email}, Rol: ${admin.role}")
                }

                // Si no es válido y no hay mensaje de bloqueo, mostrar mensaje genérico
                if (!ok && errorMessage == null) {
                    errorMessage = userResult?.exceptionOrNull()?.message ?: "Credenciales inválidas"
                    Log.w("AuthViewModel", "❌ Credenciales inválidas para: $email")
                }

                Log.d("AuthViewModel", "🏁 Finalizando login - Success: $ok, IsAdmin: $isAdmin, Rol: ${admin?.role}")
                _login.update {                                 // Actualizamos con el resultado
                    it.copy(
                        isSubmitting = false,                   // Fin carga
                        success = ok,                           // true si credenciales correctas
                        errorMsg = errorMessage,                // Mensaje si falla
                        isAdmin = isAdmin,                      // Guardamos si es admin
                        adminRole = admin?.role                 // Guardamos el rol del admin
                    )
                }
                Log.d("AuthViewModel", "=== FIN LOGIN ===")
            } catch (e: Exception) {
                Log.e("AuthViewModel", "💥 EXCEPCIÓN CRÍTICA EN LOGIN", e)
                _login.update {
                    it.copy(
                        isSubmitting = false,
                        success = false,
                        errorMsg = "Error interno: ${e.message}",
                        isAdmin = false,
                        adminRole = null
                    )
                }
            }
        }
    }

    fun clearLoginResult() {                                // Limpia banderas tras navegar
        _login.update { it.copy(success = false, errorMsg = null, isAdmin = false, adminRole = null) }
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
                password = s.pass,
                gender = "",
                photoUrl = null
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
