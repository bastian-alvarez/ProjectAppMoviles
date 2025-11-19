# Eliminación de SQLite - Progreso

## Estado: EN PROGRESO ⏳

### Fecha: 19 de noviembre de 2025

## Objetivo
Eliminar completamente Room/SQLite de la aplicación, manteniendo solo los microservicios y una caché en memoria para funcionalidad básica offline.

## Cambios Completados ✅

### 1. Sistema de Caché en Memoria
- **Archivo**: `app/src/main/java/com/example/uinavegacion/data/cache/DataCache.kt`
- **Descripción**: Sistema centralizado de caché en memoria usando `StateFlow`
- **Funcionalidades**:
  - Caché de usuario actual
  - Caché de juegos del catálogo
  - Caché de carrito de compras
  - Caché de biblioteca de juegos
  - Caché de órdenes
  - Caché de categorías y géneros
  - Caché de usuarios (para admin)

### 2. Gestor de Estado de Red
- **Archivo**: `app/src/main/java/com/example/uinavegacion/data/network/NetworkStateManager.kt`
- **Descripción**: Monitorea la conectividad de red en tiempo real
- **Funcionalidades**:
  - Detección automática de conexión/desconexión
  - Estado reactivo con `StateFlow`
  - Helper `withNetwork` para operaciones de red

### 3. Refactorización de Repositorios

#### UserRepository
- **Eliminado**: Dependencia de `UserDao`
- **Nuevo comportamiento**:
  - Login/Register: Llama al microservicio Auth y guarda en `DataCache`
  - Perfil: Obtiene desde microservicio y actualiza caché
  - Admin: Gestiona usuarios usando endpoints de admin

#### GameRepository
- **Eliminado**: Dependencia de `JuegoDao`
- **Nuevo comportamiento**:
  - Obtiene juegos desde microservicio de catálogo
  - Guarda en `DataCache` para acceso rápido
  - CRUD completo mediante microservicio

#### LibraryRepository
- **Eliminado**: Dependencia de `LibraryDao`
- **Nuevo comportamiento**:
  - Biblioteca desde microservicio
  - IDs de juegos guardados en `DataCache`

### 4. Actualización de SessionManager
- **Archivo**: `app/src/main/java/com/example/uinavegacion/data/SessionManager.kt`
- **Cambios**:
  - Ahora usa `UserResponse` (del microservicio) en lugar de `UserEntity`
  - Integrado con `DataCache` para usuario actual
  - Mantiene `AdminEntity` para administradores (se migrará después)

### 5. Mappers de UserResponse
- **Archivo**: `app/src/main/java/com/example/uinavegacion/data/remote/UserResponseMapper.kt`
- **Descripción**: Funciones de conversión entre `AuthUserResponse` y `ApiUserResponse`

### 6. Actualización de Modelos
- `UserResponse` (user service) actualizado con campo `isBlocked`
- `CatalogoGameResponse` ampliado con campos de compatibilidad

## Problemas Identificados 🔧

### En Progreso
1. **LibraryRepository** - Error en `getMyLibrary()` (método no existe)
2. **GameRepository** - Conversiones de tipo entre `GameResponse` y `CatalogoGameResponse`
3. **ViewModels** - Pendiente actualización para usar nuevos tipos
4. **Screens** - Pendiente eliminación de referencias a `AppDatabase`
5. **MainActivity** - Pendiente eliminación de inicialización de Room

## Próximos Pasos 📋

1. ✅ Arreglar errores de compilación en repositorios
2. ⏳ Crear/actualizar DTOs faltantes en microservicios
3. ⏳ Actualizar ViewModels principales
4. ⏳ Actualizar Screens (eliminar AppDatabase)
5. ⏳ Actualizar MainActivity
6. ⏳ Eliminar archivos de Room/SQLite
7. ⏳ Actualizar build.gradle (remover dependencias Room)
8. ⏳ Compilación final y pruebas

## Notas Importantes ⚠️

- **Compatibilidad**: Se mantienen interfaces similares en repositorios para minimizar cambios en ViewModels
- **Caché**: Los datos en caché se pierden al cerrar la app (comportamiento esperado)
- **Offline**: Funcionalidad básica offline limitada a datos en caché
- **Admin**: `AdminEntity` aún usa Room (pendiente migrar a microservicio)

## Errores de Compilación Actuales

```
e: LibraryRepository.kt:32:50 Unresolved reference 'getMyLibrary'
e: LibraryRepository.kt:36:67 Unresolved reference 'size'
e: GameRepository.kt:118:35 Argument type mismatch GameResponse vs CatalogoGameResponse
e: GameRepository.kt:148:35 Argument type mismatch GameResponse vs CatalogoGameResponse
```

Estos errores se están resolviendo uno por uno.

