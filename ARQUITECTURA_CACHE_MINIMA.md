# Arquitectura de Caché Mínima con Room

## Fecha: 19 de noviembre de 2025

## Filosofía

**Microservicios = Fuente de Verdad**  
**Room = Caché temporal SOLO para offline**

## Tablas de Room a MANTENER (Mínimas)

### 1. `user_cache` (UserEntity) - SIMPLIFICADA
**Propósito**: Caché del último usuario logueado (solo para mostrar perfil offline)

Campos ESENCIALES:
- `id` (PK local)
- `remoteId` (ID del microservicio)
- `name`
- `email`
- `profilePhotoUri`
- `cachedAt` (timestamp para TTL)

Campos ELIMINADOS:
- ~~`password`~~ (NUNCA cachear contraseñas)
- ~~`phone`~~ (no esencial offline)
- ~~`gender`~~ (no esencial offline)
- ~~`isBlocked`~~ (debe venir siempre del microservicio)

### 2. `game_cache` (JuegoEntity) - SIMPLIFICADA
**Propósito**: Caché de juegos recientes para navegación offline

Campos ESENCIALES:
- `id` (PK local)
- `remoteId` (ID del microservicio)
- `nombre`
- `precio`
- `imagenUrl`
- `cachedAt` (timestamp para TTL)

Campos ELIMINADOS:
- ~~`descripcion`~~ (no crítico offline)
- ~~`stock`~~ (debe ser en tiempo real del microservicio)
- ~~`activo`~~ (debe venir del microservicio)
- ~~`categoriaId`, `generoId`~~ (simplificar)

### 3. `library_cache` (LibraryEntity) - SIMPLIFICADA
**Propósito**: Lista de juegos que el usuario posee (solo IDs)

Campos ESENCIALES:
- `id` (PK local)
- `userId`
- `gameRemoteId` (ID del juego en microservicio)
- `cachedAt`

Campos ELIMINADOS:
- ~~`name`, `price`, `genre`, etc.~~ (obtener del microservicio cuando se necesite)

### 4. `admin` - MANTENER IGUAL (por ahora)
**Nota**: Los admins aún no tienen microservicio, mantener como está

## Tablas a ELIMINAR COMPLETAMENTE

- ❌ `carrito` - Usar solo estado en memoria (se pierde al cerrar app)
- ❌ `order` - Obtener siempre del microservicio
- ❌ Cualquier tabla de categorías/géneros - Obtener del microservicio

## Estrategia de TTL (Time To Live)

```kotlin
object CacheConfig {
    const val USER_TTL_MS = 30 * 60 * 1000L      // 30 minutos
    const val GAME_TTL_MS = 60 * 60 * 1000L      // 1 hora
    const val LIBRARY_TTL_MS = 15 * 60 * 1000L   // 15 minutos
}
```

## Lógica de Acceso

### Para CUALQUIER operación:

```kotlin
suspend fun getData(): Result<Data> {
    // 1. Intentar obtener del microservicio
    val remoteResult = microserviceRepository.getData()
    
    if (remoteResult.isSuccess) {
        // 2. Si éxito, guardar en caché
        cacheDao.insert(remoteResult.getOrNull()!!)
        return remoteResult
    }
    
    // 3. Si falla, buscar en caché
    val cachedData = cacheDao.getData()
    
    if (cachedData != null && !cachedData.isExpired()) {
        return Result.success(cachedData)
    }
    
    // 4. Si caché expirada o no existe, retornar error
    return Result.failure(Exception("No hay datos disponibles"))
}
```

## Operaciones de Escritura

### TODAS las escrituras (crear, actualizar, eliminar) van DIRECTO al microservicio

```kotlin
suspend fun createData(data: Data): Result<Long> {
    // NO guardar en local primero
    // Enviar directo al microservicio
    val result = microserviceRepository.create(data)
    
    if (result.isSuccess) {
        // Opcional: guardar en caché para lectura posterior
        cacheDao.insert(result.getOrNull()!!)
    }
    
    return result
}
```

## Limpieza de Caché

### Limpieza automática al iniciar app:

```kotlin
fun cleanExpiredCache() {
    viewModelScope.launch {
        val now = System.currentTimeMillis()
        userDao.deleteExpired(now - CacheConfig.USER_TTL_MS)
        gameDao.deleteExpired(now - CacheConfig.GAME_TTL_MS)
        libraryDao.deleteExpired(now - CacheConfig.LIBRARY_TTL_MS)
    }
}
```

## Beneficios

✅ Caché ultra ligera (solo ~3 tablas)  
✅ Microservicios como única fuente de verdad  
✅ Funcionalidad básica offline  
✅ Sin problemas de sincronización compleja  
✅ Fácil de mantener  

## Desventajas (aceptables)

⚠️ Sin conexión, solo lectura de datos cacheados  
⚠️ Caché se limpia cada X minutos  
⚠️ Carrito se pierde al cerrar app (se puede guardar en microservicio si es necesario)

