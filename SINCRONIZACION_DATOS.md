# Sincronización de Datos entre Base de Datos Local y Microservicios

## 📋 Descripción

Esta funcionalidad permite exportar todos los juegos de la base de datos local SQLite hacia el microservicio de Game Catalog en Laragon, asegurando que ambas bases de datos tengan la misma información.

## 🎯 Propósito

Cuando inicias la aplicación por primera vez, los juegos están almacenados localmente en SQLite. Para que los microservicios puedan acceder a estos datos, necesitas sincronizarlos con la base de datos remota de Laragon.

## 🚀 Cómo Usar

### Desde la Aplicación Móvil (Recomendado)

1. **Inicia sesión como administrador** en la aplicación
2. **Navega al Panel de Administración** (Admin Dashboard)
3. **Busca la opción "Sincronizar Datos"** en la sección de Acciones Rápidas
4. **Haz clic en "Sincronizar Datos"**
5. **Confirma la exportación** en el diálogo que aparece
6. **Espera a que termine** el proceso (verás un indicador de progreso)
7. **Revisa el resumen** que muestra cuántos juegos se exportaron exitosamente

### Resultado Esperado

Al finalizar, verás un mensaje como:

```
📤 Exportación completada:
✅ Exitosos: 30
❌ Fallidos: 0
```

## 🔧 Detalles Técnicos

### Endpoints Utilizados

- **POST** `http://10.0.2.2:3002/api/games` - Crea un nuevo juego en el microservicio

### Datos Exportados

Para cada juego se envía:
- `nombre`: Nombre del juego
- `descripcion`: Descripción completa
- `precio`: Precio en formato decimal
- `stock`: Cantidad disponible
- `imagenUrl`: URL de la imagen (puede ser null)
- `desarrollador`: Nombre del desarrollador
- `fechaLanzamiento`: Año de lanzamiento
- `categoriaId`: ID de la categoría
- `generoId`: ID del género
- `descuento`: Porcentaje de descuento (0-100)
- `activo`: Estado del juego (true/false)

### Archivos Modificados

1. **GameCatalogApi.kt** - Agregado endpoint `createGame()`
2. **GameCatalogRemoteRepository.kt** - Agregado método `createGame()`
3. **GameRepository.kt** - Agregado método `exportLocalGamesToRemote()`
4. **AdminDashboardScreen.kt** - Agregada UI para sincronización

## ⚠️ Consideraciones Importantes

### Antes de Sincronizar

1. **Asegúrate de que los microservicios estén corriendo**:
   - Auth Service: http://localhost:3001
   - Game Catalog Service: http://localhost:3002
   - Order Service: http://localhost:3003
   - Library Service: http://localhost:3004

2. **Verifica la conexión**: La aplicación debe poder conectarse a `http://10.0.2.2:3002` (emulador) o `http://localhost:3002` (dispositivo físico con proxy)

### Durante la Sincronización

- El proceso puede tardar varios segundos dependiendo de la cantidad de juegos
- No cierres la aplicación mientras se está sincronizando
- Si hay errores, se mostrarán en el resumen final

### Después de Sincronizar

- Los juegos estarán disponibles en la base de datos de Laragon
- Puedes verificar en phpMyAdmin o en tu gestor de base de datos
- Los juegos tendrán IDs diferentes en la base de datos remota
- La aplicación seguirá usando la base de datos local para operaciones offline

## 🔄 Sincronización Bidireccional

Actualmente, la sincronización es **unidireccional** (Local → Remoto). Para sincronización completa:

1. **Local → Remoto**: Usa el botón "Sincronizar Datos" (exporta juegos locales)
2. **Remoto → Local**: Se hace automáticamente cuando:
   - Inicias sesión
   - Navegas al catálogo de juegos
   - Realizas una compra

## 🐛 Solución de Problemas

### Error: "Error al crear juego: HTTP 500"

**Causa**: El microservicio no está corriendo o hay un error en el servidor

**Solución**:
1. Verifica que Game Catalog Service esté corriendo en http://localhost:3002
2. Revisa los logs del microservicio
3. Asegúrate de que la base de datos de Laragon esté activa

### Error: "Error al crear juego: timeout"

**Causa**: No hay conexión con el microservicio

**Solución**:
1. Verifica que estés usando el emulador de Android (10.0.2.2)
2. Si usas dispositivo físico, configura un proxy o usa la IP de tu PC
3. Verifica que no haya firewall bloqueando el puerto 3002

### Error: "Error al crear juego: Duplicate entry"

**Causa**: Los juegos ya existen en la base de datos remota

**Solución**:
1. Limpia la base de datos remota antes de sincronizar
2. O modifica el código para usar `updateGame()` en lugar de `createGame()` si el juego ya existe

## 📊 Logs y Depuración

Los logs de sincronización se pueden ver en Logcat con el tag `GameRepository`:

```
D/GameRepository: Iniciando exportación de 30 juegos al microservicio
D/GameRepository: ✓ Juego exportado: Doom Eternal
D/GameRepository: ✓ Juego exportado: Counter-Strike 2 - Prime
...
I/GameRepository: 📤 Exportación completada:
                  ✅ Exitosos: 30
                  ❌ Fallidos: 0
```

## 🎓 Próximos Pasos

Para mejorar esta funcionalidad, considera:

1. **Sincronización automática** al iniciar la app por primera vez
2. **Detección de duplicados** antes de crear juegos
3. **Actualización incremental** (solo juegos nuevos o modificados)
4. **Sincronización de categorías y géneros** también
5. **Sincronización bidireccional completa** con resolución de conflictos

