# 🧪 Guía de Pruebas de Integración

## 📋 Checklist de Pruebas

### ✅ Pre-requisitos
- [ ] Los 4 microservicios están corriendo en Laragon
- [ ] La app está instalada en el emulador/dispositivo
- [ ] Logcat está abierto para ver los logs

---

## 1️⃣ PRUEBA: Auth Service (Login/Registro)

### Pasos:
1. **Abre la app**
2. **Haz clic en "Registrarse"**
3. **Completa el formulario**:
   - Nombre: Test User
   - Email: test@example.com
   - Contraseña: Test123!
4. **Haz clic en "Registrar"**

### ✅ Resultados Esperados:
```
Logcat:
D/AuthRemoteRepository: Registrando usuario: test@example.com
D/AuthRemoteRepository: Usuario registrado exitosamente con ID: abc123
D/UserRepository: Usuario sincronizado en BD local
I/SessionManager: Usuario logueado: Test User

BD Remota (Auth Service):
SELECT * FROM usuarios WHERE email = 'test@example.com';
→ Debe aparecer el nuevo usuario

BD Local (SQLite):
SELECT * FROM users WHERE email = 'test@example.com';
→ Debe aparecer el nuevo usuario con remoteId
```

### ❌ Si falla:
- Verifica que Auth Service esté en http://localhost:3001
- Revisa Logcat para errores de conexión
- Verifica que el email no exista ya

---

## 2️⃣ PRUEBA: Game Catalog Service (Sincronización)

### Pasos:
1. **Desinstala la app** (para forzar primer inicio)
2. **Instala la app nuevamente**
3. **Abre la app**
4. **Observa el splash de "Sincronizando Datos"**

### ✅ Resultados Esperados:
```
Logcat:
D/GameRepository: Iniciando exportación de 10 juegos al microservicio
D/GameRepository: ✓ Juego exportado: Doom Eternal
D/GameRepository: ✓ Juego exportado: Counter-Strike 2 - Prime
...
I/GameRepository: 📤 Exportación completada:
                  ✅ Exitosos: 10
                  ❌ Fallidos: 0

BD Remota (Game Catalog Service):
SELECT COUNT(*) FROM juegos;
→ Debe mostrar 10 juegos

SELECT nombre, stock, precio FROM juegos LIMIT 5;
→ Debe mostrar los juegos con sus datos
```

### ❌ Si falla:
- Verifica que Game Catalog Service esté en http://localhost:3002
- Verifica que el endpoint POST /games esté implementado
- Usa el botón "Re-sincronizar Datos" en Admin Dashboard

---

## 3️⃣ PRUEBA: Order Service (Compra)

### Pasos:
1. **Inicia sesión** con el usuario de prueba
2. **Ve al catálogo de juegos**
3. **Agrega 2-3 juegos al carrito**
4. **Ve al carrito**
5. **Haz clic en "Proceder al Pago"**
6. **Completa la compra**

### ✅ Resultados Esperados:
```
Logcat:
D/CartViewModel: Iniciando checkout con 3 items
D/OrderRemoteRepository: Creando orden en microservicio
D/OrderRemoteRepository: Orden creada exitosamente con ID: 123
D/GameRepository: Actualizando stock del juego: Doom Eternal
D/GameRepository: Stock actualizado remotamente
D/LibraryRepository: Insertando juego en biblioteca LOCAL
D/LibraryRepository: ✓ Juego agregado a biblioteca LOCAL
D/LibraryRepository: Agregando juego a biblioteca REMOTA
D/LibraryRepository: ✓ Juego agregado exitosamente a biblioteca REMOTA

BD Remota (Order Service):
SELECT * FROM ordenes ORDER BY id DESC LIMIT 1;
→ Debe mostrar la orden recién creada

SELECT o.id, o.total, o.estado, u.email 
FROM ordenes o 
JOIN usuarios u ON o.usuario_id = u.id 
ORDER BY o.id DESC LIMIT 1;
→ Debe mostrar la orden con el email del usuario
```

### ❌ Si falla:
- Verifica que Order Service esté en http://localhost:3003
- Verifica que el usuario tenga remoteId
- Verifica que los juegos tengan remoteId

---

## 4️⃣ PRUEBA: Library Service (Biblioteca)

### Pasos:
1. **Después de completar una compra** (Prueba 3)
2. **Ve a "Mi Biblioteca"** desde el menú
3. **Verifica que los juegos comprados aparezcan**

### ✅ Resultados Esperados:
```
Logcat:
D/LibraryRepository: Obteniendo biblioteca del usuario: 1
D/LibraryRepository: Biblioteca local: 3 juegos
D/LibraryRepository: Sincronizando con biblioteca remota

BD Local (SQLite):
SELECT * FROM biblioteca WHERE userId = 1;
→ Debe mostrar los juegos comprados

BD Remota (Library Service):
SELECT b.*, j.nombre, u.email 
FROM biblioteca b
JOIN juegos j ON b.juego_id = j.id
JOIN usuarios u ON b.usuario_id = u.id
WHERE u.email = 'test@example.com';
→ Debe mostrar los mismos juegos
```

### ❌ Si falla:
- Verifica que Library Service esté en http://localhost:3004
- Verifica los logs de LibraryRepository
- Verifica que el endpoint POST /library esté implementado

---

## 5️⃣ PRUEBA: Stock Update (Actualización de Inventario)

### Pasos:
1. **Antes de comprar**, verifica el stock de un juego en la BD remota
2. **Compra ese juego**
3. **Verifica el stock nuevamente**

### ✅ Resultados Esperados:
```
Antes de la compra:
SELECT nombre, stock FROM juegos WHERE nombre = 'Doom Eternal';
→ stock = 12

Después de la compra:
SELECT nombre, stock FROM juegos WHERE nombre = 'Doom Eternal';
→ stock = 11

Logcat:
D/GameRepository: Stock anterior: 12, nuevo stock: 11
D/GameRepository: ✓ Stock actualizado en microservicio
```

---

## 6️⃣ PRUEBA: Re-sincronización Manual

### Pasos:
1. **Inicia sesión como administrador**
2. **Ve a "Admin Dashboard"**
3. **Haz clic en "Re-sincronizar Datos"**
4. **Confirma la acción**

### ✅ Resultados Esperados:
```
Logcat:
D/GameRepository: Iniciando exportación de 10 juegos al microservicio
I/GameRepository: 📤 Exportación completada:
                  ✅ Exitosos: 10
                  ❌ Fallidos: 0

Diálogo en la app:
"📤 Exportación completada:
✅ Exitosos: 10
❌ Fallidos: 0"
```

---

## 7️⃣ PRUEBA: Manejo de Errores (Offline)

### Pasos:
1. **Detén todos los microservicios**
2. **Intenta hacer login**
3. **Intenta comprar un juego**

### ✅ Resultados Esperados:
```
Logcat:
W/AuthRemoteRepository: Error en login remoto: Connection refused
D/UserRepository: Intentando login con BD local
D/UserRepository: ✓ Login exitoso con BD local

W/OrderRemoteRepository: Error al crear orden: Connection refused
E/CartViewModel: No se pudo crear la orden en el microservicio

Mensaje en la app:
"Error al procesar la compra. Verifica tu conexión."
```

---

## 📊 TABLA DE RESULTADOS

| Prueba | Microservicio | Estado | Notas |
|--------|--------------|--------|-------|
| 1. Login/Registro | Auth (3001) | ⬜ | |
| 2. Sincronización | Game Catalog (3002) | ⬜ | |
| 3. Compra | Order (3003) | ⬜ | |
| 4. Biblioteca | Library (3004) | ⬜ | |
| 5. Stock Update | Game Catalog (3002) | ⬜ | |
| 6. Re-sincronización | Game Catalog (3002) | ⬜ | |
| 7. Manejo de Errores | Todos | ⬜ | |

**Leyenda**: ⬜ Pendiente | ✅ Exitoso | ❌ Fallido

---

## 🔍 COMANDOS ÚTILES PARA VERIFICAR BD REMOTA

### Auth Service (MySQL/PostgreSQL)
```sql
-- Ver todos los usuarios
SELECT id, nombre, email, created_at FROM usuarios ORDER BY id DESC LIMIT 10;

-- Ver usuario específico
SELECT * FROM usuarios WHERE email = 'test@example.com';

-- Contar usuarios
SELECT COUNT(*) as total_usuarios FROM usuarios;
```

### Game Catalog Service
```sql
-- Ver todos los juegos
SELECT id, nombre, stock, precio FROM juegos ORDER BY id;

-- Ver juegos con stock bajo
SELECT nombre, stock FROM juegos WHERE stock < 5;

-- Ver juegos más caros
SELECT nombre, precio FROM juegos ORDER BY precio DESC LIMIT 5;
```

### Order Service
```sql
-- Ver últimas órdenes
SELECT o.id, o.total, o.estado, u.email, o.created_at 
FROM ordenes o 
JOIN usuarios u ON o.usuario_id = u.id 
ORDER BY o.created_at DESC LIMIT 10;

-- Ver total de ventas
SELECT SUM(total) as total_ventas, COUNT(*) as total_ordenes FROM ordenes;

-- Ver órdenes de un usuario
SELECT * FROM ordenes WHERE usuario_id = 'abc123';
```

### Library Service
```sql
-- Ver biblioteca de un usuario
SELECT b.*, j.nombre, j.precio 
FROM biblioteca b 
JOIN juegos j ON b.juego_id = j.id 
WHERE b.usuario_id = 'abc123';

-- Ver juegos más populares
SELECT j.nombre, COUNT(*) as veces_comprado 
FROM biblioteca b 
JOIN juegos j ON b.juego_id = j.id 
GROUP BY j.nombre 
ORDER BY veces_comprado DESC;

-- Ver usuarios con más juegos
SELECT u.email, COUNT(*) as total_juegos 
FROM biblioteca b 
JOIN usuarios u ON b.usuario_id = u.id 
GROUP BY u.email 
ORDER BY total_juegos DESC;
```

---

## 🎯 CRITERIOS DE ÉXITO

La integración se considera exitosa si:

✅ **Todas las pruebas pasan** (7/7)
✅ **Los datos se sincronizan** entre local y remoto
✅ **Los logs son claros** y sin errores críticos
✅ **El manejo de errores funciona** (modo offline)
✅ **Las BDs remotas** contienen los datos correctos

---

## 🐛 PROBLEMAS COMUNES Y SOLUCIONES

### Problema: "remoteId is null"
**Solución**: El usuario/juego no se sincronizó. Vuelve a hacer login o re-sincroniza el catálogo.

### Problema: "Foreign key constraint fails"
**Solución**: El usuario remoto no existe. Asegúrate de que el login fue exitoso.

### Problema: "Duplicate entry"
**Solución**: El juego ya existe en la BD remota. Limpia la tabla o usa UPDATE en lugar de INSERT.

### Problema: "Connection timeout"
**Solución**: Verifica que el microservicio esté corriendo y que la URL sea correcta (10.0.2.2 para emulador).

---

## 📞 CONTACTO Y SOPORTE

Si encuentras problemas:
1. Revisa los logs en Logcat
2. Verifica que los 4 microservicios estén corriendo
3. Verifica las URLs en `ApiConfig.kt`
4. Revisa este documento de verificación

**¡Buena suerte con las pruebas!** 🚀

