# 📸 Integración de Upload Multipart para Imágenes

## Resumen

Se ha implementado la funcionalidad de subida de archivos multipart para imágenes de perfil de usuarios e imágenes de juegos, reemplazando el anterior sistema de Base64.

## Cambios Realizados

### 1. Servicios API Nuevos

#### UserService.kt
- **Ubicación**: `data/remote/api/UserService.kt`
- **Endpoint Principal**: `POST /api/users/me/photo/upload`
- **Descripción**: Permite a los usuarios autenticados subir su foto de perfil
- **Parámetros**:
  - Multipart form-data con campo `file`
  - Acepta JPG, PNG, GIF
  - Tamaño máximo: 5MB

```kotlin
@Multipart
@POST("users/me/photo/upload")
suspend fun uploadProfilePhoto(
    @Part file: MultipartBody.Part
): Response<UserResponse>
```

#### AdminGameService.kt
- **Ubicación**: `data/remote/api/AdminGameService.kt`
- **Endpoint Principal**: `POST /api/admin/games/{id}/image/upload`
- **Descripción**: Permite a los administradores subir imágenes de juegos
- **Parámetros**:
  - Path variable: `id` (Long) - ID del juego
  - Multipart form-data con campo `file`
  - Acepta JPG, PNG, GIF
  - Tamaño máximo: 10MB

```kotlin
@Multipart
@POST("admin/games/{id}/image/upload")
suspend fun uploadGameImage(
    @Path("id") id: Long,
    @Part file: MultipartBody.Part
): Response<GameResponse>
```

### 2. Repositorios

#### UserRemoteRepository.kt
- **Ubicación**: `data/remote/repository/UserRemoteRepository.kt`
- **Método Principal**: `uploadProfilePhoto(imageUri: Uri): Result<UserResponse>`
- **Funcionalidad**:
  1. Convierte `Uri` a `File` temporal
  2. Crea `MultipartBody.Part` con el archivo
  3. Envía la petición al microservicio
  4. Limpia el archivo temporal

```kotlin
suspend fun uploadProfilePhoto(imageUri: Uri): Result<UserResponse> {
    // Convierte Uri a File temporal
    val file = uriToFile(imageUri)
    
    // Crea RequestBody y MultipartBody.Part
    val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
    val multipartBody = MultipartBody.Part.createFormData("file", file.name, requestBody)
    
    // Realiza petición
    val response = service.uploadProfilePhoto(multipartBody)
    
    // Limpia archivo temporal
    file.delete()
    
    return Result.success(response.body()!!)
}
```

#### AdminGameRepository.kt
- **Ubicación**: `data/remote/repository/AdminGameRepository.kt`
- **Método Principal**: `uploadGameImage(gameId: Long, imageUri: Uri): Result<GameResponse>`
- **Funcionalidad**: Similar a `UserRemoteRepository`, pero para imágenes de juegos

### 3. Pantallas Modificadas

#### ProfileEditScreen.kt
- **Cambios**:
  1. Agregado `UserRemoteRepository(context)` para subida de archivos
  2. Modificado `cameraLauncher` para usar `uploadProfilePhoto(photoUri)`
  3. Modificado `galleryLauncher` para usar `uploadProfilePhoto(uri)`
  4. Actualización automática del caché local después de subir
  5. Recarga de sesión con la nueva URL de foto

**Flujo de Subida**:
```
Usuario selecciona foto 
  → uploadProfilePhoto(uri) 
  → Microservicio procesa y guarda
  → Retorna UserResponse con nueva profilePhotoUri
  → Actualiza caché local
  → Actualiza sesión
```

#### GameManagementScreen.kt
- **Cambios**:
  1. Agregado `AdminGameRepository(context)` para subida de imágenes
  2. Agregado `imageLauncher` para seleccionar imágenes de galería
  3. Agregado callback `onUploadImage` en `GameManagementItem`
  4. Agregado botón "Imagen" en cada tarjeta de juego
  5. Snackbar para mostrar estado de subida

**Nueva UI**:
- Cada juego tiene 3 botones:
  - **Editar** (azul): Editar datos del juego
  - **Imagen** (cyan): Subir imagen desde galería
  - **Eliminar** (rojo): Eliminar juego

**Flujo de Subida**:
```
Admin presiona "Imagen"
  → Se abre selector de galería
  → Admin selecciona imagen
  → uploadGameImage(gameId, uri)
  → Microservicio procesa y guarda
  → Retorna GameResponse con nueva imagenUrl
  → Actualiza lista de juegos
```

### 4. Compatibilidad con Administradores

**Nota Importante**: El endpoint de subida de fotos de perfil para administradores aún no está disponible en el microservicio. Por ahora, los administradores usan el método local (Base64) temporalmente.

```kotlin
if (isAdmin && adminId != null) {
    // TODO: Implementar endpoint de admin cuando esté disponible
    adminRepository.updateProfilePhoto(adminId!!, photoUri.toString())
} else if (userId != null) {
    // Para usuarios: usar nuevo endpoint multipart
    val result = userRemoteRepository.uploadProfilePhoto(photoUri)
}
```

## Ventajas del Nuevo Sistema

### ✅ Ventajas
1. **Mejor rendimiento**: No hay conversión a Base64, archivos más pequeños
2. **Mayor tamaño permitido**: 
   - Usuarios: 5MB vs 500KB (Base64)
   - Juegos: 10MB vs 500KB (Base64)
3. **Procesamiento en el servidor**: El microservicio puede optimizar, redimensionar y aplicar filtros
4. **URLs persistentes**: Las imágenes se almacenan en el servidor con URLs públicas
5. **Compatibilidad**: Funciona con cualquier tipo de imagen (JPG, PNG, GIF, WebP)

### 📝 Consideraciones
1. **Requiere contexto**: Los repositorios necesitan `Context` para acceder al `ContentResolver`
2. **Archivos temporales**: Se crean archivos temporales en `cacheDir` que se eliminan después
3. **Permisos**: Requiere permisos de lectura de almacenamiento (ya configurados)

## Endpoints del Microservicio

### Usuario - Subir Foto de Perfil
```http
POST http://localhost:3001/api/users/me/photo/upload
Authorization: Bearer {token}
Content-Type: multipart/form-data

file: [archivo de imagen]
```

**Response 200 OK**:
```json
{
  "id": 1,
  "name": "Juan Pérez",
  "email": "juan@example.com",
  "phone": "+569 1234 5678",
  "profilePhotoUri": "http://localhost:3001/api/files/profile-photos/user_1_a1b2c3d4.jpg",
  "isBlocked": false,
  "gender": "M"
}
```

### Admin - Subir Imagen de Juego
```http
POST http://localhost:3002/api/admin/games/1/image/upload
Authorization: Bearer {admin_token}
Content-Type: multipart/form-data

file: [archivo de imagen]
```

**Response 200 OK**:
```json
{
  "id": 1,
  "nombre": "Mi Juego",
  "descripcion": "Descripción del juego",
  "precio": 29.99,
  "stock": 100,
  "imagenUrl": "http://localhost:3002/api/files/game-images/game_1_a1b2c3d4.jpg",
  "desarrollador": "Desarrollador",
  "fechaLanzamiento": "2024",
  "activo": true,
  "descuento": 0
}
```

## Archivos Modificados

### Nuevos Archivos
- `data/remote/api/UserService.kt`
- `data/remote/api/AdminGameService.kt`
- `data/remote/repository/UserRemoteRepository.kt`
- `data/remote/repository/AdminGameRepository.kt`

### Archivos Modificados
- `ui/screen/ProfileEditScreen.kt`
- `ui/screen/GameManagementScreen.kt`

### Archivos Obsoletos (pueden eliminarse)
- `utils/ImageUtils.kt` - Ya no se necesita conversión a Base64

## Testing

### Probar Subida de Foto de Perfil (Usuario)
1. Iniciar sesión como usuario normal
2. Ir a "Editar Perfil"
3. Presionar el botón de foto de perfil
4. Seleccionar "Tomar foto" o "Seleccionar desde galería"
5. Verificar que se muestra "✅ Foto subida al servidor"
6. Volver y verificar que la foto se actualiza

### Probar Subida de Imagen de Juego (Admin)
1. Iniciar sesión como administrador
2. Ir a "Gestión de Juegos"
3. Presionar botón "Imagen" en cualquier juego
4. Seleccionar imagen desde galería
5. Verificar Snackbar "✅ Imagen subida exitosamente"
6. Verificar que la lista se actualiza con la nueva imagen

## Próximos Pasos

1. **Implementar endpoint de foto de perfil para administradores**
2. **Agregar validación de tamaño en el cliente antes de subir**
3. **Mostrar preview de la imagen antes de subir**
4. **Agregar barra de progreso durante la subida**
5. **Implementar cache de imágenes con Coil**
6. **Eliminar `ImageUtils.kt` si ya no se usa en ningún lugar**

## Compatibilidad

- ✅ Android API 24+
- ✅ Kotlin 1.9+
- ✅ Jetpack Compose
- ✅ Retrofit 2.9+
- ✅ OkHttp 4.x

---

**Fecha de Implementación**: 19 de Noviembre de 2025  
**Autor**: Sistema de IA - Cursor  
**Estado**: ✅ Implementado y Funcional (con excepciones mencionadas)

