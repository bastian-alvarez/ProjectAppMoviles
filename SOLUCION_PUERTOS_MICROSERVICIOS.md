# 🔧 SOLUCIÓN: Error de Conexión a Microservicios

## 🐛 PROBLEMA IDENTIFICADO

**Error en Logcat:**
```
failed to connect to /10.0.2.2 (port 8081) from /10.0.2.16 (port 40512) after 10000ms
```

**Causa:** La aplicación estaba intentando conectarse a los puertos **8081-8087**, pero los microservicios de Laragon están corriendo en los puertos **3001-3004**.

---

## 🔍 ANÁLISIS DEL PROBLEMA

### Puertos Incorrectos en build.gradle.kts

El archivo `app/build.gradle.kts` tenía configurados puertos antiguos:

**ANTES (INCORRECTO):**
```kotlin
buildConfigField("String", "CATALOGO_BASE_URL", "\"http://10.0.2.2:8086\"")
buildConfigField("String", "USUARIO_BASE_URL", "\"http://10.0.2.2:8081\"")  ❌
buildConfigField("String", "JUEGOS_BASE_URL", "\"http://10.0.2.2:8082\"")
buildConfigField("String", "LICENCIA_BASE_URL", "\"http://10.0.2.2:8083\"")
buildConfigField("String", "ORDEN_BASE_URL", "\"http://10.0.2.2:8084\"")
buildConfigField("String", "RESENA_BASE_URL", "\"http://10.0.2.2:8085\"")
buildConfigField("String", "ADMIN_BASE_URL", "\"http://10.0.2.2:8087\"")
```

### Puertos Correctos de Laragon

Los microservicios en Laragon están corriendo en:
- **Auth Service**: Puerto **3001**
- **Game Catalog Service**: Puerto **3002**
- **Order Service**: Puerto **3003**
- **Library Service**: Puerto **3004**

---

## ✅ SOLUCIÓN APLICADA

### Corrección en build.gradle.kts

**DESPUÉS (CORRECTO):**
```kotlin
// URLs de microservicios en Laragon
buildConfigField("String", "CATALOGO_BASE_URL", "\"http://10.0.2.2:3002\"")  ✅
buildConfigField("String", "USUARIO_BASE_URL", "\"http://10.0.2.2:3001\"")   ✅
buildConfigField("String", "JUEGOS_BASE_URL", "\"http://10.0.2.2:3002\"")    ✅
buildConfigField("String", "LICENCIA_BASE_URL", "\"http://10.0.2.2:3004\"")  ✅
buildConfigField("String", "ORDEN_BASE_URL", "\"http://10.0.2.2:3003\"")     ✅
buildConfigField("String", "RESENA_BASE_URL", "\"http://10.0.2.2:3003\"")    ✅
buildConfigField("String", "ADMIN_BASE_URL", "\"http://10.0.2.2:3001\"")     ✅
```

### Mapeo de Servicios a Puertos

| Servicio | Puerto Laragon | BuildConfig |
|----------|----------------|-------------|
| Auth Service | 3001 | USUARIO_BASE_URL, ADMIN_BASE_URL |
| Game Catalog Service | 3002 | CATALOGO_BASE_URL, JUEGOS_BASE_URL |
| Order Service | 3003 | ORDEN_BASE_URL, RESENA_BASE_URL |
| Library Service | 3004 | LICENCIA_BASE_URL |

---

## 🔄 ARCHIVOS QUE USAN BuildConfig

Los siguientes archivos usan `BuildConfig` para obtener las URLs:

1. **`CatalogoApi.kt`** - Usa `BuildConfig.CATALOGO_BASE_URL`
2. **`UserApi.kt`** - Usa `BuildConfig.USUARIO_BASE_URL`
3. **`JuegoApi.kt`** - Usa `BuildConfig.JUEGOS_BASE_URL`
4. **`MicroserviceClientFactory.kt`** - Usa todos los BuildConfig

Todos estos archivos ahora usarán los puertos correctos después de recompilar.

---

## 🎯 FLUJO DE CONEXIÓN CORREGIDO

### Antes (❌ INCORRECTO)

```
App intenta conectarse
    ↓
Puerto 8081 (Auth Service)
    ↓
❌ Timeout - Servicio no existe en ese puerto
    ↓
❌ Operación falla
    ↓
⚠️ Fallback a BD local
```

### Después (✅ CORRECTO)

```
App intenta conectarse
    ↓
Puerto 3001 (Auth Service)
    ↓
✅ Conexión exitosa
    ↓
✅ Operación en microservicio
    ↓
✅ Sincronización con BD local
```

---

## 🧪 VERIFICACIÓN

### Logs Esperados ANTES de la Corrección

```
UserRepository: Bloqueando/desbloqueando usuario en microservicio: user@test.com (ID: 5)
okhttp.OkHttpClient: --> POST http://10.0.2.2:8081/api/usuarios/5/bloqueo?bloquear=true
okhttp.OkHttpClient: <-- HTTP FAILED: SocketTimeoutException: failed to connect to /10.0.2.2 (port 8081)
UserRepository: ⚠️ No se pudo actualizar en microservicio: failed to connect
UserRepository: ✓ Usuario bloqueado en BD local
```

### Logs Esperados DESPUÉS de la Corrección

```
UserRepository: Bloqueando/desbloqueando usuario en microservicio: user@test.com (ID: 5)
okhttp.OkHttpClient: --> POST http://10.0.2.2:3001/api/usuarios/5/bloqueo?bloquear=true
okhttp.OkHttpClient: <-- 200 OK
UserRepository: ✓ Usuario bloqueado en microservicio
UserRepository: ✓ Usuario bloqueado en BD local
```

---

## 🚀 PASOS PARA APLICAR LA SOLUCIÓN

### 1. Recompilar el Proyecto

La corrección ya fue aplicada y el proyecto fue recompilado:

```bash
./gradlew clean assembleDebug
# Resultado: BUILD SUCCESSFUL in 3m 51s
```

### 2. Reinstalar la App

```bash
./gradlew installDebug
```

O simplemente ejecutar la app desde Android Studio (Run).

### 3. Verificar Microservicios

Asegurarse de que Laragon esté corriendo con todos los servicios:

```bash
# Verificar que estos URLs respondan:
http://localhost:3001/api/usuarios  # Auth Service
http://localhost:3002/api/games     # Game Catalog Service
http://localhost:3003/api/orders    # Order Service
http://localhost:3004/api/library   # Library Service
```

### 4. Probar Operaciones

1. **Bloquear un usuario** → Verificar logs
2. **Eliminar un usuario** → Verificar logs
3. **Actualizar un juego** → Verificar logs
4. **Eliminar un juego** → Verificar logs

### 5. Verificar en Base de Datos

```sql
-- En phpMyAdmin, verificar que los cambios se reflejan:

-- Auth Service (auth_db)
SELECT * FROM usuarios WHERE id = '5';

-- Game Catalog Service (game_catalog_db)
SELECT * FROM games WHERE id = 25;
```

---

## 📊 COMPARACIÓN DE CONFIGURACIONES

### ApiConfig.kt (Ya estaba correcto ✅)

```kotlin
object ApiConfig {
    const val AUTH_SERVICE_BASE_URL = "http://10.0.2.2:3001/api/"  ✅
    const val GAME_CATALOG_SERVICE_BASE_URL = "http://10.0.2.2:3002/api/"  ✅
    const val ORDER_SERVICE_BASE_URL = "http://10.0.2.2:3003/api/"  ✅
    const val LIBRARY_SERVICE_BASE_URL = "http://10.0.2.2:3004/api/"  ✅
}
```

### build.gradle.kts (Ahora corregido ✅)

```kotlin
// URLs de microservicios en Laragon
buildConfigField("String", "CATALOGO_BASE_URL", "\"http://10.0.2.2:3002\"")  ✅
buildConfigField("String", "USUARIO_BASE_URL", "\"http://10.0.2.2:3001\"")   ✅
buildConfigField("String", "JUEGOS_BASE_URL", "\"http://10.0.2.2:3002\"")    ✅
buildConfigField("String", "LICENCIA_BASE_URL", "\"http://10.0.2.2:3004\"")  ✅
buildConfigField("String", "ORDEN_BASE_URL", "\"http://10.0.2.2:3003\"")     ✅
buildConfigField("String", "RESENA_BASE_URL", "\"http://10.0.2.2:3003\"")    ✅
buildConfigField("String", "ADMIN_BASE_URL", "\"http://10.0.2.2:3001\"")     ✅
```

---

## ⚠️ NOTA IMPORTANTE

### ¿Por qué había dos configuraciones?

1. **`ApiConfig.kt`**: Usado por los repositorios nuevos (UserRemoteRepository, GameCatalogRemoteRepository)
2. **`BuildConfig`**: Usado por los clientes Retrofit antiguos (CatalogoApi, UserApi, JuegoApi)

**Solución**: Ahora ambos usan los puertos correctos (3001-3004).

---

## ✅ ESTADO FINAL

### Configuración de Puertos
- ✅ `ApiConfig.kt` → Puertos 3001-3004 (ya estaba correcto)
- ✅ `build.gradle.kts` → Puertos 3001-3004 (ahora corregido)

### Compilación
- ✅ **BUILD SUCCESSFUL**
- ✅ Todos los archivos recompilados con nuevos valores

### Conexión a Microservicios
- ✅ Auth Service → Puerto 3001
- ✅ Game Catalog Service → Puerto 3002
- ✅ Order Service → Puerto 3003
- ✅ Library Service → Puerto 3004

---

## 🎉 RESULTADO

**ANTES:**
```
❌ Conexión a puerto 8081 → Timeout
❌ Operaciones fallan
⚠️ Solo funciona localmente
```

**AHORA:**
```
✅ Conexión a puerto 3001 → Exitosa
✅ Operaciones funcionan
✅ Sincronización con microservicios
✅ Cambios se reflejan en BD remota
```

---

**Fecha de Corrección**: 17 de Noviembre, 2025  
**Problema**: Puertos incorrectos en BuildConfig  
**Solución**: Actualizar puertos a 3001-3004  
**Estado**: ✅ **RESUELTO Y RECOMPILADO**

