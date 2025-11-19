# Caché Mínima con Room - IMPLEMENTACIÓN COMPLETADA ✅

## Fecha: 19 de noviembre de 2025

## ✅ Cambios Implementados

### 1. Entidades Simplificadas

#### **UserEntity** - Solo caché esencial
```kotlin
- remoteId (ID del microservicio)
- name, email, phone
- profilePhotoUri
- cachedAt (timestamp para TTL de 30 min)
```
**Campos deprecados** pero mantenidos para compatibilidad:
- password, isBlocked, gender, roleId, statusId, createdAt

#### **JuegoEntity** - Solo caché esencial
```kotlin
- remoteId (ID del microservicio)
- nombre, precio, imagenUrl
- cachedAt (timestamp para TTL de 1 hora)
```
**Campos mantenidos** para compatibilidad:
- descripcion, stock, desarrollador, categoriaId, generoId, etc.

#### **LibraryEntity** - Ultra simplificada
```kotlin
- userId, juegoId, remoteGameId
- cachedAt (timestamp para TTL de 15 min)
```
**Campos mantenidos** para compatibilidad:
- name, price, dateAdded, licenseId, etc.

### 2. DAOs Mejorados

Cada DAO ahora incluye métodos de gestión de caché:

```kotlin
// Eliminar registros expirados
suspend fun deleteExpired(expirationTimestamp: Long): Int

// Limpiar toda la caché
suspend fun clearCache(): Int

// Actualizar timestamp
suspend fun updateCachedAt(id: Long, timestamp: Long)
```

### 3. CacheManager Centralizado

**Archivo**: `app/src/main/java/com/example/uinavegacion/data/cache/CacheManager.kt`

**Funcionalidades**:
- ✅ Limpieza automática de caché expirada
- ✅ TTL configurables por tipo de dato
- ✅ Limpieza total al hacer logout
- ✅ Logging detallado

**TTL Configurados**:
- Usuarios: 30 minutos
- Juegos: 1 hora
- Biblioteca: 15 minutos

### 4. Integración en MainActivity

La caché se limpia automáticamente **cada vez que la app inicia**:

```kotlin
LaunchedEffect(Unit) {
    // 1. Limpiar caché expirada
    CacheManager.cleanExpiredCache(db)
    
    // 2. Sincronización inicial (solo primera vez)
    // ...
}
```

## 🏗️ Arquitectura Final

```
┌─────────────────────────────────────────┐
│         MICROSERVICIOS                  │
│   (Fuente de Verdad SIEMPRE)           │
│  - Auth Service (3001)                  │
│  - Game Catalog (3002)                  │
│  - Order Service (3003)                 │
│  - Library Service (3004)               │
└──────────────┬──────────────────────────┘
               │
               │ ↓ Siempre obtener datos
               │ ↑ Siempre enviar cambios
               │
┌──────────────▼──────────────────────────┐
│         REPOSITORIOS                    │
│  - UserRepository                       │
│  - GameRepository                       │
│  - LibraryRepository                    │
│                                         │
│  Lógica:                                │
│  1. Intentar microservicio              │
│  2. Si éxito → guardar en caché         │
│  3. Si falla → usar caché (si válida)   │
└──────────────┬──────────────────────────┘
               │
               │ Solo para lectura offline
               │
┌──────────────▼──────────────────────────┐
│         ROOM (CACHÉ MÍNIMA)             │
│  - UserEntity (3 campos esenciales)     │
│  - JuegoEntity (4 campos esenciales)    │
│  - LibraryEntity (3 campos esenciales)  │
│                                         │
│  TTL: Auto-limpieza cada inicio         │
└─────────────────────────────────────────┘
```

## 📋 Flujo de Operaciones

### Lectura (GET)
1. ✅ Intentar obtener del microservicio
2. ✅ Si éxito → Guardar en caché + Retornar
3. ✅ Si falla → Buscar en caché
4. ✅ Si caché válida (no expirada) → Retornar
5. ❌ Si caché expirada → Error "Sin datos"

### Escritura (POST/PUT/DELETE)
1. ✅ Enviar DIRECTO al microservicio
2. ✅ Si éxito → Actualizar caché
3. ❌ Si falla → Error (NO guardar en local)

### Logout
1. ✅ Limpiar TODA la caché (`CacheManager.clearAllCache()`)
2. ✅ Limpiar token de sesión
3. ✅ Navegar a login

## 🎯 Beneficios Logrados

✅ **Caché ultra ligera** - Solo 3 tablas simplificadas  
✅ **Microservicios como fuente de verdad** - Sin conflictos de sincronización  
✅ **Funcionalidad offline básica** - Lectura de últimos datos cacheados  
✅ **Auto-limpieza** - TTL automático, sin acumulación infinita  
✅ **Fácil mantenimiento** - Arquitectura simple y clara  
✅ **Sin contradicciones** - Los datos siempre vienen del microservicio  

## ⚠️ Limitaciones (Esperadas y Aceptadas)

- Sin conexión: Solo lectura de datos cacheados (máx 30 min antiguos)
- Sin escritura offline (todas las operaciones requieren conexión)
- Carrito se pierde al cerrar app (se puede persistir en microservicio si se necesita)
- Categorías/géneros no se cachean (obtener siempre del microservicio)

## 📝 Notas de Deprecación

Los campos marcados como `@Deprecated` en las entidades:
- **Se mantienen** para compatibilidad con código existente
- **No se recomienda** usarlos en código nuevo
- **Se pueden eliminar** en una refactorización futura cuando se actualice todo el código

## 🚀 Estado

✅ **IMPLEMENTADO Y COMPILANDO**  
✅ **LISTO PARA USAR**

Los warnings de deprecación son **esperados e intencionales** para marcar campos que eventualmente se eliminarán.

## 🔄 Próximos Pasos (Opcionales)

1. Monitorear uso de memoria de caché
2. Ajustar TTLs según necesidades reales
3. Implementar métricas de hit/miss de caché
4. Considerar persistir carrito en microservicio Order

