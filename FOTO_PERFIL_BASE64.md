# 📸 SISTEMA DE FOTOS DE PERFIL CON BASE64

## 🎯 Problema Resuelto

Anteriormente, cuando un usuario guardaba una foto de perfil:
- ❌ Solo se guardaba la **URI local** del dispositivo
- ❌ La foto **NO se subía al microservicio**
- ❌ Si se desinstalaba la app, **la foto se perdía**
- ❌ No se podía ver la foto en otros dispositivos

## ✅ Solución Implementada

Ahora las fotos de perfil se convierten a **Base64** y se suben permanentemente:
- ✅ La foto se **convierte a Base64** (formato de texto)
- ✅ Se **sube al microservicio** de autenticación
- ✅ Se **guarda en la base de datos** del microservicio
- ✅ La foto **persiste** aunque se desinstale la app
- ✅ Se puede ver en **cualquier dispositivo** al hacer login

---

## 🔧 Archivos Modificados/Creados

### 1. **NUEVO**: `ImageUtils.kt`
Utilidad para convertir imágenes a Base64 con compresión inteligente.

**Ubicación**: `app/src/main/java/com/example/uinavegacion/utils/ImageUtils.kt`

**Funcionalidades**:
- ✅ Convierte URI de imagen a Base64
- ✅ Comprime la imagen automáticamente (máx. 500KB)
- ✅ Redimensiona si es muy grande (máx. 1024px)
- ✅ Corrige la orientación según metadatos EXIF
- ✅ Formato: `data:image/jpeg;base64,<datos>`

**Métodos principales**:
```kotlin
// Convertir imagen a Base64
fun uriToBase64(context: Context, imageUri: Uri, maxSizeKB: Int = 500): String?

// Convertir Base64 a Bitmap (para mostrar)
fun base64ToBitmap(base64String: String): Bitmap?
```

---

### 2. **MODIFICADO**: `ProfileEditScreen.kt`
Actualizado para convertir imágenes a Base64 antes de guardar.

**Cambios**:

#### Antes:
```kotlin
// Solo guardaba la URI local
profilePhotoUri = photoUri.toString()
```

#### Ahora:
```kotlin
// Convierte a Base64 y guarda
photoSavedMessage = "Procesando imagen..."
val base64Image = ImageUtils.uriToBase64(context, photoUri, maxSizeKB = 500)
profilePhotoUri = base64Image // Guarda el Base64
```

**Flujo completo**:
1. Usuario toma foto o selecciona de galería
2. Muestra mensaje "Procesando imagen..."
3. Convierte la imagen a Base64
4. Guarda el Base64 en BD local
5. Cuando se actualiza el perfil, se sube al microservicio
6. Muestra "✅ Foto tomada y guardada"

---

### 3. **MODIFICADO**: `app/build.gradle.kts`
Agregada dependencia para manejo de orientación de imágenes.

```kotlin
// ExifInterface para manejo de orientación de imágenes
implementation("androidx.exifinterface:exifinterface:1.3.7")
```

---

## 📊 Flujo de Datos

### Guardar Foto de Perfil:

```
[Usuario toma foto/selecciona de galería]
           ↓
[Imagen URI en dispositivo]
           ↓
[ImageUtils.uriToBase64()]
    ├─ Lee la imagen
    ├─ Corrige orientación (EXIF)
    ├─ Redimensiona si es muy grande
    ├─ Comprime a JPEG (85% calidad)
    └─ Convierte a Base64
           ↓
[String Base64: "data:image/jpeg;base64,/9j/4AAQ..."]
           ↓
[Guarda en BD Local]
           ↓
[Al actualizar perfil → Sube al microservicio]
           ↓
[Guarda en BD del microservicio]
           ↓
[✅ Foto permanente y accesible desde cualquier dispositivo]
```

---

## 🎨 Optimizaciones Implementadas

### 1. **Compresión Inteligente**
- Máximo 500KB por imagen
- Calidad JPEG: 85%
- Redimensiona automáticamente si excede 1024px

### 2. **Corrección de Orientación**
- Lee metadatos EXIF
- Rota la imagen correctamente
- Evita fotos "de lado" o "al revés"

### 3. **Manejo de Errores**
- Valida que la imagen se pueda leer
- Muestra mensajes claros al usuario
- Logs detallados para debugging

### 4. **Experiencia de Usuario**
- Muestra "Procesando imagen..." mientras convierte
- Muestra "✅ Foto tomada y guardada" al finalizar
- Maneja errores con mensajes claros

---

## 🔍 Ejemplo de Base64 Generado

```
data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAAYEBQYFBAYGBQYHBwYIChAKCgkJChQODwwQFxQYGBcUFhYaHSUfGhsjHBYWICwgIyYnKSopGR8tMC0oMCUoKSj/2wBDAQcHBwoIChMKChMoGhYaKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCj/wAARCADIAMgDASIAAhEBAxEB/8QAHwAAAQUBAQEBAQEAAAAAAAAAAAECAwQFBgcICQoL/8QAtRAAAgEDAwIEAwUFBAQAAAF9AQIDAAQRBRIhMUEGE1FhByJxFDKBkaEII0KxwRVS0fAkM2JyggkKFhcYGRolJicoKSo0NTY3ODk6Q0RFRkdISUpTVFVWV1hZWmNkZWZnaGlqc3R1dnd4eXqDhIWGh4iJipKTlJWWl5iZmqKjpKWmp6ipqrKztLW2t7i5usLDxMXGx8jJytLT1NXW19jZ2uHi4+Tl5ufo6erx8vP09fb3+Pn6/8QAHwEAAwEBAQEBAQEBAQAAAAAAAAECAwQFBgcICQoL/8QAtREAAgECBAQDBAcFBAQAAQJ3AAECAxEEBSExBhJBUQdhcRMiMoEIFEKRobHBCSMzUvAVYnLRChYkNOEl8RcYGRomJygpKjU2Nzg5OkNERUZHSElKU1RVVldYWVpjZGVmZ2hpanN0dXZ3eHl6goOEhYaHiImKkpOUlbaXmJmaoqOkpaanqKmqsrO0tba3uLm6wsPExcbHyMnK0tPU1dbX2Nna4uPk5ebn6Onq8vP09fb3+Pn6/9oADAMBAAIRAxEAPwD5/ooooA...
```

Este string se guarda en:
- **BD Local**: Tabla `users`, campo `profilePhotoUri`
- **BD Microservicio**: Tabla `usuarios`, campo `foto_perfil_url`

---

## 📱 Compatibilidad

### Formatos de Imagen Soportados:
- ✅ JPEG / JPG
- ✅ PNG
- ✅ WebP
- ✅ Cualquier formato que Android pueda decodificar

### Fuentes de Imagen:
- ✅ Cámara del dispositivo
- ✅ Galería de fotos
- ✅ Cualquier URI de imagen

---

## 🚀 Cómo Usar

### Para el Usuario:
1. Ir a "Editar Perfil"
2. Tocar "Cámara" o "Galería"
3. Seleccionar/tomar foto
4. Esperar mensaje "Procesando imagen..."
5. Ver mensaje "✅ Foto tomada y guardada"
6. Tocar "Guardar Cambios" para subir al microservicio

### Para el Desarrollador:
```kotlin
// Convertir cualquier imagen a Base64
val base64 = ImageUtils.uriToBase64(context, imageUri, maxSizeKB = 500)

// Guardar en BD
userRepository.updateProfilePhoto(userId, base64)

// La foto se sube automáticamente al microservicio
```

---

## 🔐 Seguridad y Privacidad

- ✅ Las imágenes se comprimen antes de subir
- ✅ Tamaño máximo controlado (500KB)
- ✅ Solo se suben cuando el usuario lo autoriza
- ✅ Se requiere autenticación JWT para subir
- ✅ Las fotos se almacenan de forma segura en la BD

---

## 📊 Ventajas de Base64

### ✅ Ventajas:
- **Simple**: No requiere servidor de archivos separado
- **Portable**: Funciona en cualquier base de datos
- **Integrado**: Se guarda junto con los datos del usuario
- **Sin dependencias**: No necesita AWS S3, Firebase Storage, etc.

### ⚠️ Consideraciones:
- **Tamaño**: Base64 aumenta el tamaño ~33%
  - Solución: Compresión a 500KB máximo
- **Performance**: Puede ser más lento para imágenes muy grandes
  - Solución: Redimensionamiento automático a 1024px

---

## 🧪 Testing

### Probar la funcionalidad:
1. Tomar foto con cámara → Verificar que se procesa
2. Seleccionar de galería → Verificar que se procesa
3. Foto muy grande → Verificar que se redimensiona
4. Foto rotada → Verificar que se corrige orientación
5. Guardar cambios → Verificar que se sube al microservicio
6. Desinstalar app → Reinstalar → Login → Foto sigue ahí ✅

---

## 📝 Logs para Debugging

```
🖼️ Convirtiendo imagen a Base64: content://...
📐 Tamaño original: 3024x4032
🔄 Imagen rotada según EXIF: orientación=6
📏 Imagen redimensionada a: 768x1024
✅ Imagen convertida a Base64 (450KB)
```

---

## 🎯 Próximas Mejoras (Opcional)

1. **Caché de imágenes**: Guardar en memoria para cargar más rápido
2. **Servidor de archivos**: Migrar a AWS S3 o Firebase Storage si crece mucho
3. **Múltiples tamaños**: Generar thumbnail + imagen completa
4. **Formato WebP**: Mejor compresión que JPEG
5. **Lazy loading**: Cargar imágenes solo cuando son visibles

---

**Fecha de implementación**: 18 de Noviembre de 2025  
**Versión**: 2.2  
**Estado**: ✅ Completado y funcionando

