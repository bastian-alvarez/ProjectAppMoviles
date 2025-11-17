# 🔍 CÓMO VERIFICAR EN LA BASE DE DATOS

## Guía paso a paso para verificar que las operaciones del administrador se reflejan en la base de datos de los microservicios

---

## 🗄️ ACCESO A LAS BASES DE DATOS

### Opción 1: phpMyAdmin (Laragon)
1. Abrir Laragon
2. Click en "Database" → "phpMyAdmin"
3. URL: `http://localhost/phpmyadmin`
4. Usuario: `root`
5. Contraseña: (vacía por defecto)

### Opción 2: MySQL Workbench
1. Abrir MySQL Workbench
2. Conectar a `localhost:3306`
3. Usuario: `root`
4. Contraseña: (vacía por defecto)

---

## 📊 BASES DE DATOS DE LOS MICROSERVICIOS

### 1. Auth Service - Base de Datos: `auth_db`

#### Tabla: `usuarios`
```sql
-- Ver todos los usuarios
SELECT * FROM usuarios;

-- Ver usuarios bloqueados
SELECT id, name, email, isBlocked FROM usuarios WHERE isBlocked = 1;

-- Ver usuarios activos
SELECT id, name, email, isBlocked FROM usuarios WHERE isBlocked = 0;
```

**Campos importantes:**
- `id`: ID del usuario en el microservicio (este es el `remoteId` en la app)
- `name`: Nombre del usuario
- `email`: Email del usuario
- `isBlocked`: Estado de bloqueo (0 = activo, 1 = bloqueado)

---

### 2. Game Catalog Service - Base de Datos: `game_catalog_db`

#### Tabla: `games`
```sql
-- Ver todos los juegos
SELECT * FROM games;

-- Ver juegos activos
SELECT id, nombre, precio, stock, activo FROM games WHERE activo = 1;

-- Ver juegos inactivos
SELECT id, nombre, precio, stock, activo FROM games WHERE activo = 0;

-- Ver stock de juegos
SELECT id, nombre, stock FROM games ORDER BY stock DESC;
```

**Campos importantes:**
- `id`: ID del juego en el microservicio (este es el `remoteId` en la app)
- `nombre`: Nombre del juego
- `precio`: Precio del juego
- `stock`: Cantidad disponible
- `activo`: Estado (0 = inactivo, 1 = activo)
- `categoriaId`: ID de la categoría
- `generoId`: ID del género

---

### 3. Library Service - Base de Datos: `library_db`

#### Tabla: `biblioteca`
```sql
-- Ver todos los juegos en bibliotecas
SELECT * FROM biblioteca;

-- Ver juegos de un usuario específico
SELECT * FROM biblioteca WHERE userId = 'USER_ID';

-- Contar juegos por usuario
SELECT userId, COUNT(*) as total_juegos 
FROM biblioteca 
GROUP BY userId;
```

**Campos importantes:**
- `id`: ID del registro en la biblioteca
- `userId`: ID del usuario (remoteId)
- `gameId`: ID del juego (remoteId)
- `dateAdded`: Fecha de adquisición

---

### 4. Order Service - Base de Datos: `order_db`

#### Tabla: `ordenes`
```sql
-- Ver todas las órdenes
SELECT * FROM ordenes;

-- Ver órdenes por estado
SELECT id, userId, total, estado, fecha 
FROM ordenes 
WHERE estado = 'completada';

-- Ver total de ventas
SELECT SUM(total) as total_ventas FROM ordenes WHERE estado = 'completada';
```

---

## 🧪 PRUEBAS DE VERIFICACIÓN

### ✅ Prueba 1: Crear Juego

**Pasos:**
1. En la app, ir a Panel Admin → Gestión de Juegos
2. Presionar "Agregar Juego"
3. Llenar los datos:
   - Nombre: "Juego de Prueba"
   - Descripción: "Descripción de prueba"
   - Precio: 59.99
   - Stock: 100
4. Guardar

**Verificar en BD:**
```sql
-- En game_catalog_db
SELECT * FROM games WHERE nombre = 'Juego de Prueba';
```

**Resultado esperado:**
- ✅ Debe aparecer un registro con el juego creado
- ✅ El campo `activo` debe ser 1
- ✅ El `stock` debe ser 100
- ✅ El `precio` debe ser 59.99

---

### ✅ Prueba 2: Actualizar Juego

**Pasos:**
1. En la app, seleccionar un juego existente
2. Presionar "Editar"
3. Cambiar el stock a 50
4. Guardar

**Verificar en BD:**
```sql
-- En game_catalog_db
SELECT id, nombre, stock FROM games WHERE id = [ID_DEL_JUEGO];
```

**Resultado esperado:**
- ✅ El campo `stock` debe mostrar 50

---

### ✅ Prueba 3: Eliminar Juego

**Pasos:**
1. En la app, seleccionar un juego
2. Presionar "Eliminar"
3. Confirmar eliminación

**Verificar en BD:**
```sql
-- En game_catalog_db
SELECT * FROM games WHERE id = [ID_DEL_JUEGO];
```

**Resultado esperado:**
- ✅ El registro NO debe existir (fue eliminado)

---

### ✅ Prueba 4: Bloquear Usuario

**Pasos:**
1. En la app, ir a Panel Admin → Gestión de Usuarios
2. Seleccionar un usuario activo
3. Presionar "Bloquear"
4. Confirmar

**Verificar en BD:**
```sql
-- En auth_db
SELECT id, name, email, isBlocked FROM usuarios WHERE email = '[EMAIL_DEL_USUARIO]';
```

**Resultado esperado:**
- ✅ El campo `isBlocked` debe ser 1

---

### ✅ Prueba 5: Desbloquear Usuario

**Pasos:**
1. En la app, seleccionar un usuario bloqueado
2. Presionar "Desbloquear"
3. Confirmar

**Verificar en BD:**
```sql
-- En auth_db
SELECT id, name, email, isBlocked FROM usuarios WHERE email = '[EMAIL_DEL_USUARIO]';
```

**Resultado esperado:**
- ✅ El campo `isBlocked` debe ser 0

---

### ✅ Prueba 6: Eliminar Usuario

**Pasos:**
1. En la app, seleccionar un usuario
2. Presionar "Eliminar"
3. Confirmar eliminación

**Verificar en BD:**
```sql
-- En auth_db
SELECT * FROM usuarios WHERE email = '[EMAIL_DEL_USUARIO]';
```

**Resultado esperado:**
- ✅ El registro NO debe existir (fue eliminado)

---

## 🔍 VERIFICACIÓN DE SINCRONIZACIÓN

### Verificar que los IDs están sincronizados

**En la app (Room Database):**
```sql
-- Ver juegos con remoteId
SELECT id, nombre, remoteId FROM juegos WHERE remoteId IS NOT NULL;
```

**En el microservicio (MySQL):**
```sql
-- Ver juegos en game_catalog_db
SELECT id, nombre FROM games;
```

**Verificar:**
- ✅ El `remoteId` de la app debe coincidir con el `id` del microservicio
- ✅ Los nombres deben ser iguales

---

## 📊 QUERIES ÚTILES PARA AUDITORÍA

### Ver todos los usuarios y su estado
```sql
SELECT 
    id,
    name,
    email,
    CASE 
        WHEN isBlocked = 1 THEN 'BLOQUEADO'
        ELSE 'ACTIVO'
    END as estado
FROM usuarios
ORDER BY name;
```

### Ver todos los juegos con su stock
```sql
SELECT 
    id,
    nombre,
    precio,
    stock,
    CASE 
        WHEN activo = 1 THEN 'ACTIVO'
        ELSE 'INACTIVO'
    END as estado,
    CASE 
        WHEN stock = 0 THEN 'SIN STOCK'
        WHEN stock < 10 THEN 'STOCK BAJO'
        ELSE 'STOCK OK'
    END as estado_stock
FROM games
ORDER BY nombre;
```

### Ver actividad reciente
```sql
-- Últimos juegos agregados
SELECT id, nombre, createdAt 
FROM games 
ORDER BY createdAt DESC 
LIMIT 10;

-- Últimos usuarios registrados
SELECT id, name, email, createdAt 
FROM usuarios 
ORDER BY createdAt DESC 
LIMIT 10;
```

---

## 🚨 SOLUCIÓN DE PROBLEMAS

### Problema: Los cambios no se reflejan en la BD

**Verificar:**
1. ✅ Los microservicios están corriendo en Laragon
2. ✅ La app puede conectarse a `http://10.0.2.2:300X`
3. ✅ Revisar logs en Android Studio:
   ```
   Filtrar por: "UserRepository" o "GameRepository"
   Buscar: "✓" (operaciones exitosas)
   Buscar: "⚠️" (advertencias)
   Buscar: "❌" (errores)
   ```

### Problema: El usuario/juego tiene remoteId NULL

**Causa:** El objeto fue creado antes de implementar la sincronización

**Solución:**
1. Eliminar el objeto de la app
2. Volver a crearlo
3. Verificar que ahora tenga `remoteId`

---

## 📝 LOGS DE VERIFICACIÓN

### Logs esperados al crear un juego:
```
GameRepository: Agregando juego en BD LOCAL: [Nombre]
GameRepository: ✓ Juego agregado en BD local con ID: 123
GameRepository: Creando juego en microservicio: [Nombre]
GameRepository: ✓ Juego creado en microservicio con ID: 456
GameRepository: ✓ RemoteId actualizado en BD local
```

### Logs esperados al bloquear un usuario:
```
UserRepository: Bloqueando/desbloqueando usuario en microservicio: [Email]
UserRepository: ✓ Usuario bloqueado en microservicio
UserRepository: ✓ Usuario bloqueado en BD local
```

---

**Fecha**: 17 de Noviembre, 2025  
**Versión**: 1.0  
**Estado**: ✅ Verificado y Funcional

