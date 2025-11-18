# 🔍 DEBUG: SINCRONIZACIÓN DE USUARIOS

## 🐛 Problema Reportado

En la pantalla de "Gestión de Usuarios" solo aparecen **2 usuarios**, pero en la base de datos del microservicio hay **5 usuarios**.

---

## ✅ Solución Implementada

He agregado **logs detallados** en el proceso de sincronización para identificar exactamente qué está pasando.

---

## 📊 Logs Agregados

### 1. Al obtener usuarios del microservicio:
```
📋 Obteniendo usuarios del microservicio (admin endpoint)...
✅ Obtenidos 5 usuarios del microservicio
```

### 2. Al sincronizar cada usuario:
```
  ✓ Sincronizado: user1@example.com (remoteId: 1)
  ✓ Sincronizado: user2@example.com (remoteId: 2)
  ✓ Sincronizado: user3@example.com (remoteId: 3)
  ✓ Sincronizado: user4@example.com (remoteId: 4)
  ✓ Sincronizado: user5@example.com (remoteId: 5)
```

### 3. Resumen de sincronización:
```
📊 Sincronización completada: 5 exitosos, 0 errores
📦 Total usuarios en BD local: 5
```

### 4. Si hay errores:
```
  ❌ Error sincronizando user@example.com: [mensaje de error]
```

---

## 🔧 Cómo Verificar

### Paso 1: Reinstalar la app
```bash
./gradlew installDebug
```

### Paso 2: Abrir Logcat
En Android Studio:
1. Ir a **View → Tool Windows → Logcat**
2. Filtrar por tag: `UserRepository`

O desde terminal:
```bash
adb logcat | grep -E "UserRepository|UserManagementVM"
```

### Paso 3: Abrir Gestión de Usuarios
1. Login como administrador
2. Ir a "Gestión de Usuarios"
3. Observar los logs en Logcat

---

## 📋 Logs Esperados (Ejemplo)

```
D/UserManagementVM: 🚀 INIT - Cargando usuarios desde BD
D/UserRepository: 📋 Obteniendo usuarios del microservicio (admin endpoint)...
D/AdminUserRepo: 📋 Obteniendo todos los usuarios (admin)...
I/okhttp.OkHttpClient: --> GET http://10.0.2.2:3001/api/admin/users?page=0&size=200
I/okhttp.OkHttpClient: <-- 200 http://10.0.2.2:3001/api/admin/users (150ms)
D/AdminUserRepo: ✅ Usuarios obtenidos: 5
D/UserRepository: ✅ Obtenidos 5 usuarios del microservicio
D/UserRepository:   ✓ Sincronizado: nagles@gmail.com (remoteId: 1)
D/UserRepository:   ✓ Sincronizado: basti@gmail.com (remoteId: 2)
D/UserRepository:   ✓ Sincronizado: user3@gmail.com (remoteId: 3)
D/UserRepository:   ✓ Sincronizado: user4@gmail.com (remoteId: 4)
D/UserRepository:   ✓ Sincronizado: user5@gmail.com (remoteId: 5)
D/UserRepository: 📊 Sincronización completada: 5 exitosos, 0 errores
D/UserRepository: 📦 Total usuarios en BD local: 5
```

---

## 🔍 Posibles Causas del Problema

### 1. **El microservicio no devuelve todos los usuarios**
**Verificar**:
- Abrir Postman o navegador
- GET `http://localhost:3001/api/admin/users`
- Verificar que devuelva 5 usuarios

**Solución**: Si no devuelve todos, revisar el backend del microservicio.

---

### 2. **Error de autenticación (403 Forbidden)**
**Logs esperados**:
```
I/okhttp.OkHttpClient: <-- 403 http://10.0.2.2:3001/api/admin/users
D/UserRepository: ⚠️ No se pudo obtener usuarios del microservicio: HTTP 403
D/UserRepository: ⚠️ Usando BD local como fallback
D/UserRepository: 📦 Total usuarios en BD local (fallback): 2
```

**Solución**: 
- Verificar que el token JWT esté siendo enviado
- Verificar que el token sea válido
- Hacer logout y login nuevamente

---

### 3. **Error de conexión al microservicio**
**Logs esperados**:
```
I/okhttp.OkHttpClient: <-- HTTP FAILED: java.net.ConnectException: Failed to connect
D/UserRepository: ⚠️ No se pudo obtener usuarios del microservicio: Failed to connect
D/UserRepository: ⚠️ Usando BD local como fallback
D/UserRepository: 📦 Total usuarios en BD local (fallback): 2
```

**Solución**:
- Verificar que el microservicio esté corriendo en `http://localhost:3001`
- Verificar que Laragon esté activo

---

### 4. **Error al sincronizar usuarios individuales**
**Logs esperados**:
```
D/UserRepository: ✅ Obtenidos 5 usuarios del microservicio
D/UserRepository:   ✓ Sincronizado: user1@gmail.com (remoteId: 1)
D/UserRepository:   ✓ Sincronizado: user2@gmail.com (remoteId: 2)
D/UserRepository:   ❌ Error sincronizando user3@gmail.com: UNIQUE constraint failed
D/UserRepository:   ❌ Error sincronizando user4@gmail.com: UNIQUE constraint failed
D/UserRepository:   ❌ Error sincronizando user5@gmail.com: UNIQUE constraint failed
D/UserRepository: 📊 Sincronización completada: 2 exitosos, 3 errores
D/UserRepository: 📦 Total usuarios en BD local: 2
```

**Solución**: 
- Limpiar datos de la app: Settings → Apps → UINavegacion → Clear Data
- Reinstalar la app

---

### 5. **BD local desactualizada**
Si el microservicio devuelve 5 usuarios pero solo se muestran 2, puede ser que:
- La BD local tenga usuarios antiguos que no están en el microservicio
- Los usuarios del microservicio no se están sincronizando correctamente

**Solución**:
- Limpiar datos de la app
- Reinstalar

---

## 🛠️ Comandos Útiles

### Ver logs en tiempo real:
```bash
adb logcat | grep -E "UserRepository|AdminUserRepo|UserManagementVM"
```

### Ver solo errores:
```bash
adb logcat | grep -E "❌|ERROR"
```

### Limpiar datos de la app:
```bash
adb shell pm clear com.example.uinavegacion
```

### Reinstalar la app:
```bash
./gradlew installDebug
```

---

## 📝 Checklist de Verificación

- [ ] El microservicio Auth está corriendo en `http://localhost:3001`
- [ ] El endpoint `/api/admin/users` devuelve 5 usuarios en Postman
- [ ] El administrador tiene un token JWT válido
- [ ] Los logs muestran "✅ Obtenidos 5 usuarios del microservicio"
- [ ] Los logs muestran "📊 Sincronización completada: 5 exitosos, 0 errores"
- [ ] Los logs muestran "📦 Total usuarios en BD local: 5"
- [ ] La pantalla muestra 5 usuarios

---

## 🎯 Próximos Pasos

1. **Reinstalar la app**:
   ```bash
   ./gradlew installDebug
   ```

2. **Abrir Logcat**:
   ```bash
   adb logcat | grep -E "UserRepository|AdminUserRepo"
   ```

3. **Abrir Gestión de Usuarios** en la app

4. **Revisar los logs** y compartir el output completo si el problema persiste

---

## 📸 Ejemplo de Logs Exitosos

```
D/UserManagementVM: 🚀 INIT - Cargando usuarios desde BD
D/UserRepository: 📋 Obteniendo usuarios del microservicio (admin endpoint)...
D/AdminUserRepo: 📋 Obteniendo todos los usuarios (admin)...
I/okhttp.OkHttpClient: --> GET http://10.0.2.2:3001/api/admin/users?page=0&size=200
I/okhttp.OkHttpClient: <-- 200 http://10.0.2.2:3001/api/admin/users (89ms, 1234-byte body)
D/AdminUserRepo: ✅ Usuarios obtenidos: 5
D/UserRepository: ✅ Obtenidos 5 usuarios del microservicio
D/UserRepository:   ✓ Sincronizado: nagles@gmail.com (remoteId: 673a9f5a2e8b4c0012345678)
D/UserRepository: Usuario actualizado en BD local: nagles@gmail.com
D/UserRepository:   ✓ Sincronizado: basti@gmail.com (remoteId: 673a9f5a2e8b4c0012345679)
D/UserRepository: Usuario actualizado en BD local: basti@gmail.com
D/UserRepository:   ✓ Sincronizado: user3@gmail.com (remoteId: 673a9f5a2e8b4c001234567a)
D/UserRepository: Usuario creado en BD local: user3@gmail.com
D/UserRepository:   ✓ Sincronizado: user4@gmail.com (remoteId: 673a9f5a2e8b4c001234567b)
D/UserRepository: Usuario creado en BD local: user4@gmail.com
D/UserRepository:   ✓ Sincronizado: user5@gmail.com (remoteId: 673a9f5a2e8b4c001234567c)
D/UserRepository: Usuario creado en BD local: user5@gmail.com
D/UserRepository: 📊 Sincronización completada: 5 exitosos, 0 errores
D/UserRepository: 📦 Total usuarios en BD local: 5
```

---

**Fecha**: 18 de Noviembre de 2025  
**Versión**: 2.3  
**Estado**: ✅ Logs agregados, listo para debugging

