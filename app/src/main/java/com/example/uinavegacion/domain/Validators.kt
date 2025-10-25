package com.example.uinavegacion.domain
import android.util.Patterns


//funcion para validar el formato del correo
fun validateEmail(email: String): String?{
    if (email.isBlank()) return "El correo es obligatorio"
    val ok = Patterns.EMAIL_ADDRESS.matcher(email).matches()
    return if (!ok) "Formato de correo inválido" else null
}

//valida que el nombre no tenga numeros
fun validateLettersOnly(nombre: String): String?{
    if(nombre.isBlank()) return "El nombre es obligatorio"
    val regex = Regex("^[A-Za-zÁÉÍÓÚÑáéíóúñ ]+$")
    return if(!regex.matches(nombre)) "Solo letras y espacios" else null
}

//valida telefono celular chileno: +56 9 XXXX XXXX o +569XXXXXXXX
fun validatePhoneDigitsOnly(phone: String): String?{
    if(phone.isBlank()) return "El teléfono es obligatorio"
    
    // Aceptar tanto "+56 9" como "+569"
    if(!phone.startsWith("+56")) return "Debe comenzar con +56"
    
    // Extraer todos los dígitos después de +56
    val digitsAfterCountryCode = phone.substring(3).replace(" ", "").trim()
    
    if(digitsAfterCountryCode.isEmpty()) return "Ingrese el número después de +56"
    if(!digitsAfterCountryCode.all { it.isDigit() }) return "Solo números después del +56"
    
    // Debe tener 9 dígitos después del +56 (el 9 inicial + 8 dígitos)
    if(digitsAfterCountryCode.length != 9) return "Debe tener 9 dígitos después del +56"
    if(!digitsAfterCountryCode.startsWith("9")) return "El número debe empezar con 9"
    
    return null
}

//validar robustez de la contraseña
fun validateStrongPassword(clave: String): String?{
    if (clave.isBlank()) return "La Contraseña es Obligatoria"
    if(clave.length < 8) return "Mínimo 8 caracteres"
    if(!clave.any { it.isUpperCase()}) return "Debe incluir una Mayúscula"
    if(!clave.any { it.isLowerCase() }) return "Debe incluir una Minúscula"
    if(!clave.any { it.isDigit() }) return "Debe incluir un número"
    if(!clave.any { it.isLetterOrDigit() }) return "Debe incluir un símbolo"
    if(clave.contains(' ')) return "No debe contener espacios"
    return null
}

//validar claves coincidan
fun validateConfirm(clave: String, confirm: String): String?{
    if(confirm.isBlank()) return "Debe confirmar su contraseña"
    return if(clave != confirm) "Las contraseñas no coinciden" else null
}
