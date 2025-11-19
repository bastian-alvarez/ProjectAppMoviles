# Plan de Migración: Eliminar SQLite y Usar Solo Microservicios

## Fecha: 19 de Noviembre de 2025

---

## Objetivo

Eliminar completamente Room/SQLite de la aplicación Kotlin y utilizar ÚNICAMENTE los microservicios como fuente de datos.

---

## Análisis de Situación Actual

### Componentes que usan SQLite:

1. **AppDatabase.kt** - Base de datos Room principal
2. **DAOs** (13 archivos):
   - UserDao
   - AdminDao
   - JuegoDao
   - CategoriaDao
   - GeneroDao
   - EstadoDao
   - RolDao
   - LicenciaDao
   - OrdenCompraDao
   - DetalleDao
   - ReservaDao
   - ResenaDao
   - LibraryDao

3. **Entities** (13 archivos):
   - UserEntity
   - AdminEntity
   - JuegoEntity
   - CategoriaEntity
   - GeneroEntity
   - EstadoEntity
   - RolEntity
   - LicenciaEntity
   - OrdenCompraEntity
   - DetalleEntity
   - ReservaEntity
   - ResenaEntity
   - LibraryEntity

4. **Repositorios que mezclan local + remoto**:
   - UserRepository
   - GameRepository
   - LibraryRepository
   - AdminStatsRepository
   - AdminRepository

---

## Riesgos y Consideraciones

### ⚠️ CRÍTICO:
1. **Conectividad**: Sin SQLite, la app NO funcionará offline
2. **Rendimiento**: Todas las operaciones serán por red
3. **Dependencia total**: Si los microservicios caen, la app no funciona
4. **Testing**: Más difícil hacer testing sin caché local

### 💡 Recomendación:
**Mantener una caché mínima en memoria (StateFlow/LiveData)** para:
- Datos del usuario actual (sesión)
- Lista de juegos cargados recientemente
- Carrito de compras temporal

---

## Estrategia de Migración

### Fase 1: Preparación (NO ELIMINAR AÚN)
1. ✅ Verificar que TODOS los microservicios tienen endpoints necesarios
2. ✅ Crear modelos de datos compartidos (DTOs)
3. ✅ Implementar caché en memoria para datos críticos
4. ✅ Actualizar SessionManager para manejar más estado

### Fase 2: Refactorización de Repositorios
1. ✅ Eliminar dependencias de DAOs
2. ✅ Usar solo RemoteRepositories
3. ✅ Implementar caché en memoria donde sea necesario
4. ✅ Manejar errores de red apropiadamente

### Fase 3: Actualizar ViewModels
1. ✅ Eliminar referencias a AppDatabase
2. ✅ Usar solo repositorios remotos
3. ✅ Actualizar manejo de estado

### Fase 4: Actualizar Screens
1. ✅ Eliminar inicialización de AppDatabase
2. ✅ Usar solo repositorios remotos
3. ✅ Agregar indicadores de carga

### Fase 5: Limpieza
1. ✅ Eliminar archivos de DAOs
2. ✅ Eliminar archivos de Entities
3. ✅ Eliminar AppDatabase.kt
4. ✅ Actualizar build.gradle (quitar Room)
5. ✅ Eliminar migraciones

---

## Orden de Implementación

### 1. Crear Sistema de Caché en Memoria
```kotlin
object DataCache {
    private val _currentUser = MutableStateFlow<UserResponse?>(null)
    val currentUser: StateFlow<UserResponse?> = _currentUser.asStateFlow()
    
    private val _games = MutableStateFlow<List<GameResponse>>(emptyList())
    val games: StateFlow<List<GameResponse>> = _games.asStateFlow()
    
    private val _cart = MutableStateFlow<Map<Long, Int>>(emptyMap())
    val cart: StateFlow<Map<Long, Int>> = _cart.asStateFlow()
    
    // ... más cachés
}
```

### 2. Refactorizar Repositorios (Orden):
1. **UserRepository** → Solo AuthRemoteRepository + UserRemoteRepository
2. **GameRepository** → Solo GameCatalogRemoteRepository
3. **LibraryRepository** → Solo LibraryRemoteRepository
4. **OrderRepository** (crear) → Solo OrderRemoteRepository
5. **AdminRepository** → Solo AdminUserRemoteRepository

### 3. Actualizar ViewModels (Orden):
1. AuthViewModel
2. GameCatalogViewModel
3. CartViewModel
4. LibraryViewModel
5. AdminDashboardViewModel
6. UserManagementViewModel
7. GameManagementViewModel

### 4. Actualizar Screens (Orden):
1. LoginScreen / RegisterScreen
2. HomeScreen
3. GameDetailScreen
4. CartScreen / CheckoutScreen
5. LibraryScreen
6. ProfileScreen / ProfileEditScreen
7. AdminDashboardScreen
8. UserManagementScreen
9. GameManagementScreen

### 5. MainActivity
- Eliminar inicialización de AppDatabase
- Eliminar SyncPreferences
- Simplificar AppRoot

---

## Archivos a Eliminar

### Directorio `data/local/`:
```
app/src/main/java/com/example/uinavegacion/data/local/
├── admin/
│   ├── AdminDao.kt ❌
│   └── AdminEntity.kt ❌
├── categoria/
│   ├── CategoriaDao.kt ❌
│   └── CategoriaEntity.kt ❌
├── database/
│   └── AppDatabase.kt ❌
├── detalle/
│   ├── DetalleDao.kt ❌
│   └── DetalleEntity.kt ❌
├── estado/
│   ├── EstadoDao.kt ❌
│   └── EstadoEntity.kt ❌
├── genero/
│   ├── GeneroDao.kt ❌
│   └── GeneroEntity.kt ❌
├── juego/
│   ├── JuegoDao.kt ❌
│   └── JuegoEntity.kt ❌
├── library/
│   ├── LibraryDao.kt ❌
│   └── LibraryEntity.kt ❌
├── licencia/
│   ├── LicenciaDao.kt ❌
│   └── LicenciaEntity.kt ❌
├── ordenCompra/
│   ├── OrdenCompraDao.kt ❌
│   └── OrdenCompraEntity.kt ❌
├── reserva/
│   ├── ReservaDao.kt ❌
│   └── ReservaEntity.kt ❌
├── resena/
│   ├── ResenaDao.kt ❌
│   └── ResenaEntity.kt ❌
├── rol/
│   ├── RolDao.kt ❌
│   └── RolEntity.kt ❌
└── user/
    ├── UserDao.kt ❌
    └── UserEntity.kt ❌
```

### Archivos de Documentación:
```
DATABASE_SQLITE_GUIDE.md ❌
DATABASE_STRUCTURE.md ❌
```

### build.gradle.kts:
```kotlin
// ELIMINAR:
implementation("androidx.room:room-runtime:2.6.1")
implementation("androidx.room:room-ktx:2.6.1")
ksp("androidx.room:room-compiler:2.6.1")
```

---

## Nuevos Archivos a Crear

### 1. DataCache.kt
Sistema de caché en memoria para datos críticos

### 2. NetworkStateManager.kt
Monitoreo de conectividad y manejo de errores

### 3. Repositorios simplificados
Versiones sin DAOs de todos los repositorios

---

## Endpoints Necesarios en Microservicios

### Auth Service (Puerto 3001)
- ✅ POST /api/auth/login
- ✅ POST /api/auth/register
- ✅ GET /api/users/me
- ✅ PUT /api/users/me
- ✅ PUT /api/users/me/photo

### Game Catalog Service (Puerto 3002)
- ✅ GET /api/games
- ✅ GET /api/games/{id}
- ✅ POST /api/games
- ✅ PUT /api/games/{id}
- ✅ DELETE /api/games/{id}
- ❓ GET /api/categories
- ❓ GET /api/genres

### Order Service (Puerto 3003)
- ✅ POST /api/orders
- ✅ GET /api/orders
- ❓ GET /api/orders/{id}
- ❓ GET /api/orders/user/{userId}

### Library Service (Puerto 3004)
- ✅ POST /api/library
- ✅ GET /api/library/user/{userId}
- ❓ GET /api/library/{id}
- ❓ DELETE /api/library/{id}

### Admin Endpoints (Auth Service)
- ✅ GET /admin/users
- ✅ GET /admin/users/{id}
- ✅ PUT /admin/users/{id}
- ✅ DELETE /admin/users/{id}
- ✅ POST /admin/users/{id}/block
- ✅ POST /admin/users/{id}/unblock

---

## Impacto en Funcionalidades

### ✅ Funcionará Normal:
- Login/Register
- Ver catálogo de juegos
- Ver detalle de juego
- Agregar a carrito (en memoria)
- Realizar compra
- Ver biblioteca
- Editar perfil
- Panel de admin

### ⚠️ Requiere Cambios:
- **Búsqueda de juegos**: Debe ser por API
- **Filtros**: Deben ser por API
- **Categorías/Géneros**: Deben venir de API
- **Carrito**: Solo en memoria (se pierde al cerrar app)
- **Favoritos**: No implementado aún en microservicios

### ❌ NO Funcionará:
- **Modo offline**: Nada funcionará sin internet
- **Caché de imágenes**: Solo lo que Android cachee
- **Datos persistentes locales**: Solo sesión actual

---

## Testing

### Antes de Eliminar SQLite:
1. ✅ Verificar todos los endpoints funcionan
2. ✅ Probar flujo completo de compra
3. ✅ Probar panel de admin
4. ✅ Probar edición de perfil
5. ✅ Probar login/register
6. ✅ Probar manejo de errores de red

### Después de Eliminar SQLite:
1. ✅ Testing manual completo
2. ✅ Testing de conectividad
3. ✅ Testing de errores
4. ✅ Testing de rendimiento

---

## Rollback Plan

Si algo sale mal:
1. ✅ Git revert al commit anterior
2. ✅ Restaurar dependencias de Room en build.gradle
3. ✅ Restaurar AppDatabase y DAOs desde backup

---

## Estimación de Tiempo

- **Preparación**: 2 horas
- **Refactorización de Repositorios**: 4 horas
- **Actualización de ViewModels**: 3 horas
- **Actualización de Screens**: 3 horas
- **Limpieza y Testing**: 2 horas

**Total**: ~14 horas de trabajo

---

## Estado Actual

❌ **NO INICIADO**

---

## Próximos Pasos

1. ¿El usuario confirma que quiere proceder?
2. ¿Los microservicios tienen TODOS los endpoints necesarios?
3. ¿Se acepta que la app NO funcione offline?
4. ¿Se acepta la pérdida de rendimiento por llamadas de red?

---

**IMPORTANTE**: Esta es una decisión arquitectónica crítica que cambiará fundamentalmente cómo funciona la aplicación.

**Ventajas**:
- ✅ Código más simple
- ✅ Una sola fuente de verdad (microservicios)
- ✅ Sin problemas de sincronización
- ✅ Más fácil de mantener

**Desventajas**:
- ❌ Sin modo offline
- ❌ Dependencia total de red
- ❌ Menor rendimiento
- ❌ Más difícil de testear


