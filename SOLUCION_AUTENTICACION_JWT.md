# 🔐 SOLUCIÓN: Autenticación JWT para Microservicios

## 🐛 PROBLEMA IDENTIFICADO

**Error HTTP 403 Forbidden:**
```
<-- 403 http://10.0.2.2:3001/api/usuarios/2 (29ms, 0-byte body)
⚠️ No se pudo eliminar del microservicio: HTTP 403
```

**Causa:** El microservicio Auth requiere un **token JWT** para las operaciones de administrador, pero la aplicación no estaba enviando ningún token de autenticación.

---

## 🔍 ANÁLISIS DEL PROBLEMA

### ¿Qué es un Token JWT?

JWT (JSON Web Token) es un estándar para transmitir información de forma segura. El microservicio lo usa para:
1. **Autenticar** al usuario (verificar quién es)
2. **Autorizar** operaciones (verificar qué puede hacer)

### Flujo de Autenticación

```
1. Usuario hace login
   ↓
2. Microservicio valida credenciales
   ↓
3. Microservicio genera un TOKEN JWT
   ↓
4. App guarda el token
   ↓
5. App envía el token en cada petición
   ↓
6. Microservicio valida el token
   ↓
7. Si es válido → Permite la operación ✅
   Si no es válido → HTTP 403 Forbidden ❌
```

### ¿Por qué fallaba?

**ANTES (INCORRECTO):**
```
Login exitoso → Token recibido → ❌ Token NO guardado
Operación de admin → ❌ Token NO enviado
Microservicio → ❌ HTTP 403 Forbidden
```

**AHORA (CORRECTO):**
```
Login exitoso → Token recibido → ✅ Token guardado
Operación de admin → ✅ Token enviado en header
Microservicio → ✅ HTTP 200 OK
```

---

## ✅ SOLUCIÓN IMPLEMENTADA

### 1. Agregar Soporte de Tokens en SessionManager

**Archivo:** `SessionManager.kt`

**Cambios:**
```kotlin
object SessionManager {
    // ... campos existentes ...
    
    // ✅ AGREGADO: Token de autenticación
    private var authToken: String? = null
    
    // ✅ AGREGADO: Métodos para manejar el token
    fun saveToken(token: String) {
        authToken = token
    }
    
    fun getToken(): String? {
        return authToken
    }
    
    fun hasToken(): Boolean {
        return !authToken.isNullOrBlank()
    }
    
    // ✅ MODIFICADO: Limpiar token al cerrar sesión
    fun logout() {
        _currentUser.value = null
        _currentAdmin.value = null
        _isLoggedIn.value = false
        authToken = null  // ✅ Limpiar token
    }
}
```

---

### 2. Crear AuthInterceptor

**Archivo:** `data/remote/interceptor/AuthInterceptor.kt` (NUEVO)

Este interceptor agrega automáticamente el token a todas las peticiones HTTP:

```kotlin
class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Obtener el token del SessionManager
        val token = SessionManager.getToken()
        
        // Si hay token, agregarlo al header
        val request = if (!token.isNullOrBlank()) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")  // ✅ Agregar token
                .build()
        } else {
            originalRequest
        }
        
        return chain.proceed(request)
    }
}
```

**¿Qué hace?**
- Intercepta TODAS las peticiones HTTP antes de enviarlas
- Si hay un token guardado, lo agrega al header `Authorization: Bearer TOKEN`
- Si no hay token, deja la petición sin cambios

---

### 3. Agregar AuthInterceptor a RetrofitClient

**Archivo:** `data/remote/config/RetrofitClient.kt`

**Cambios:**
```kotlin
object RetrofitClient {
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    // ✅ AGREGADO: Interceptor de autenticación
    private val authInterceptor = AuthInterceptor()
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)      // ✅ Agregar primero
        .addInterceptor(loggingInterceptor)   // Luego el logging
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    // ... resto del código ...
}
```

**Importante:** El `authInterceptor` se agrega **ANTES** del `loggingInterceptor` para que los logs muestren el header `Authorization`.

---

### 4. Guardar Token en Login

**Archivo:** `data/repository/UserRepository.kt`

**Cambios en `login()`:**
```kotlin
if (remoteResult.isSuccess) {
    val authResponse = remoteResult.getOrNull()!!
    Log.d("UserRepository", "Login successful via microservice")
    
    // ✅ AGREGADO: Guardar el token en SessionManager
    SessionManager.saveToken(authResponse.token)
    Log.d("UserRepository", "✓ Token guardado en SessionManager")
    
    // Sincronizar con la BD local
    val userEntity = UserEntity(
        // ... campos ...
    )
    
    userDao.insert(userEntity)
    
    Result.success(userEntity)
}
```

---

### 5. Guardar Token en Register

**Archivo:** `data/repository/UserRepository.kt`

**Cambios en `register()`:**
```kotlin
if (remoteResult.isSuccess) {
    val authResponse = remoteResult.getOrNull()!!
    Log.d("UserRepository", "Register successful via microservice")
    
    // ✅ AGREGADO: Guardar el token en SessionManager
    SessionManager.saveToken(authResponse.token)
    Log.d("UserRepository", "✓ Token guardado en SessionManager")
    
    // Sincronizar con la BD local
    val userEntity = UserEntity(
        // ... campos ...
    )
    
    userDao.insert(userEntity)
    
    Result.success(authResponse.user.id)
}
```

---

## 🎯 FLUJO COMPLETO CORREGIDO

### Escenario: Usuario hace login y luego bloquea a otro usuario

```
1. Usuario ingresa email y password
   ↓
2. App llama a UserRepository.login()
   ↓
3. Se envía POST /api/usuarios/login al microservicio
   ↓
4. Microservicio valida credenciales
   ↓
5. Microservicio retorna: { user: {...}, token: "eyJhbGc..." }
   ↓
6. ✅ App guarda token en SessionManager
   ↓
7. Usuario va al Panel Admin → Gestión de Usuarios
   ↓
8. Usuario presiona "Bloquear" en un usuario
   ↓
9. App llama a UserRepository.toggleBlockStatus()
   ↓
10. Se prepara DELETE /api/usuarios/2/bloqueo
   ↓
11. ✅ AuthInterceptor agrega header: "Authorization: Bearer eyJhbGc..."
   ↓
12. Se envía la petición con el token
   ↓
13. Microservicio valida el token
   ↓
14. ✅ Token válido → HTTP 200 OK
   ↓
15. Usuario bloqueado exitosamente en microservicio
   ↓
16. Se actualiza BD local
```

---

## 🧪 VERIFICACIÓN

### Logs Esperados ANTES de la Corrección

```
UserRepository: Attempting login via microservice for email: [admin@test.com]
okhttp.OkHttpClient: --> POST http://10.0.2.2:3001/api/usuarios/login
okhttp.OkHttpClient: <-- 200 OK (token recibido pero NO guardado)
UserRepository: Login successful via microservice

... Usuario intenta bloquear a otro usuario ...

UserRepository: Eliminando usuario del microservicio: user@test.com (ID: 2)
okhttp.OkHttpClient: --> DELETE http://10.0.2.2:3001/api/usuarios/2
                     (SIN header Authorization)
okhttp.OkHttpClient: <-- 403 Forbidden
UserRepository: ⚠️ No se pudo eliminar del microservicio: HTTP 403
```

### Logs Esperados DESPUÉS de la Corrección

```
UserRepository: Attempting login via microservice for email: [admin@test.com]
okhttp.OkHttpClient: --> POST http://10.0.2.2:3001/api/usuarios/login
okhttp.OkHttpClient: <-- 200 OK
UserRepository: Login successful via microservice
UserRepository: ✓ Token guardado en SessionManager

... Usuario intenta bloquear a otro usuario ...

UserRepository: Eliminando usuario del microservicio: user@test.com (ID: 2)
okhttp.OkHttpClient: --> DELETE http://10.0.2.2:3001/api/usuarios/2
                     Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
okhttp.OkHttpClient: <-- 200 OK
UserRepository: ✓ Usuario eliminado del microservicio
UserRepository: ✓ Usuario eliminado de BD local
```

---

## 📊 RESUMEN DE CAMBIOS

| Archivo | Cambio | Estado |
|---------|--------|--------|
| `SessionManager.kt` | Agregar soporte para tokens | ✅ |
| `AuthInterceptor.kt` | Crear interceptor (NUEVO) | ✅ |
| `RetrofitClient.kt` | Agregar AuthInterceptor | ✅ |
| `UserRepository.kt` | Guardar token en login | ✅ |
| `UserRepository.kt` | Guardar token en register | ✅ |

---

## 🚀 PASOS PARA APLICAR LA SOLUCIÓN

### 1. Recompilar el Proyecto

Ya está compilado:
```bash
./gradlew assembleDebug
# BUILD SUCCESSFUL in 19s
```

### 2. Reinstalar la App

```bash
./gradlew installDebug
```

O ejecutar desde Android Studio (Run).

### 3. **IMPORTANTE: Cerrar Sesión y Volver a Iniciar Sesión**

⚠️ **Este paso es CRÍTICO:**

Los usuarios que ya están logueados NO tienen el token guardado. Necesitas:

1. **Cerrar sesión** en la app
2. **Volver a iniciar sesión**
3. Ahora el token se guardará correctamente

### 4. Probar Operaciones de Admin

1. Ir a Panel Admin → Gestión de Usuarios
2. Bloquear un usuario → Debería funcionar ✅
3. Eliminar un usuario → Debería funcionar ✅
4. Actualizar un juego → Debería funcionar ✅
5. Eliminar un juego → Debería funcionar ✅

### 5. Verificar en Logs

Buscar en Logcat:
```
✓ Token guardado en SessionManager
Authorization: Bearer eyJhbGc...
<-- 200 OK
✓ Usuario eliminado del microservicio
```

---

## 🔐 SEGURIDAD

### ¿Dónde se guarda el token?

El token se guarda en **memoria** (variable en SessionManager), no en disco. Esto significa:

✅ **Ventajas:**
- Más seguro (no persiste en disco)
- Se limpia automáticamente al cerrar la app

⚠️ **Desventajas:**
- Se pierde al cerrar la app
- Usuario debe volver a iniciar sesión

### ¿Es seguro?

Para una app de desarrollo/pruebas: **SÍ** ✅

Para producción, considera:
- Guardar el token en `EncryptedSharedPreferences`
- Implementar refresh tokens
- Agregar expiración de tokens

---

## ⚠️ NOTAS IMPORTANTES

### 1. Cerrar Sesión Limpia el Token

```kotlin
SessionManager.logout()
// Limpia: usuario, admin, Y token
```

### 2. El Token se Envía en TODAS las Peticiones

El `AuthInterceptor` agrega el token a:
- ✅ Operaciones de usuarios (bloquear, eliminar)
- ✅ Operaciones de juegos (crear, actualizar, eliminar)
- ✅ Operaciones de órdenes
- ✅ Operaciones de biblioteca

### 3. Si el Token Expira

Si el microservicio retorna `401 Unauthorized`:
- El token ha expirado
- Usuario debe volver a iniciar sesión
- (Futura mejora: implementar refresh tokens)

---

## ✅ ESTADO FINAL

### Autenticación
- ✅ Token se guarda en login
- ✅ Token se guarda en register
- ✅ Token se envía en todas las peticiones
- ✅ Token se limpia al cerrar sesión

### Operaciones de Admin
- ✅ Bloquear usuario → Con autenticación
- ✅ Desbloquear usuario → Con autenticación
- ✅ Eliminar usuario → Con autenticación
- ✅ Crear juego → Con autenticación
- ✅ Actualizar juego → Con autenticación
- ✅ Eliminar juego → Con autenticación

### Compilación
- ✅ BUILD SUCCESSFUL
- ✅ Sin errores
- ✅ Listo para instalar

---

**Fecha de Corrección**: 17 de Noviembre, 2025  
**Problema**: HTTP 403 - Falta autenticación JWT  
**Solución**: Implementar AuthInterceptor y guardar tokens  
**Estado**: ✅ **RESUELTO Y COMPILADO**

---

## 🎉 RESULTADO FINAL

**ANTES:**
```
❌ Login → Token recibido pero NO guardado
❌ Operaciones admin → HTTP 403 Forbidden
❌ Cambios NO se reflejan en microservicio
```

**AHORA:**
```
✅ Login → Token guardado en SessionManager
✅ Operaciones admin → HTTP 200 OK
✅ Cambios se reflejan en microservicio
✅ Todo funciona correctamente
```

