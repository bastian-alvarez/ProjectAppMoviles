# 📸 Sistema de Fotos de Perfil - GameStore Android

## 📋 Índice
1. [Arquitectura del Sistema](#arquitectura-del-sistema)
2. [Almacenamiento en SQLite](#almacenamiento-en-sqlite)
3. [Configuración de Permisos](#configuración-de-permisos)
4. [FileProvider para Cámara](#fileprovider-para-cámara)
5. [Implementación en UI](#implementación-en-ui)
6. [Flujo Completo de Usuario](#flujo-completo-de-usuario)

---

## 🏗️ Arquitectura del Sistema

### Diagrama de Flujo
```
Usuario → UI (ProfileEditScreen) → Camera/Gallery → URI → SQLite → Display
```

### Componentes Principales:
1. **UI Layer**: ProfileEditScreen con botones de acción
2. **Permission Layer**: Manejo de permisos Android
3. **FileProvider**: Configuración segura para cámara
4. **Storage Layer**: SQLite con campo `profilePhotoUri`
5. **Image Loading**: Coil para mostrar imágenes

---

## 🗄️ Almacenamiento en SQLite

### ¿Por qué URI en lugar de BLOB?

#### ❌ Alternativa BLOB (No recomendada):
```kotlin
@Entity(tableName = "users")
data class UserEntity(
    val id: Long,
    val profilePhoto: ByteArray?  // BLOB - Pesado y lento
)
```

**Problemas del BLOB:**
- 📊 **Tamaño**: Aumenta dramáticamente el tamaño de la BD
- 🐌 **Performance**: Queries lentas con datos binarios grandes
- 💾 **Memoria**: Carga toda la imagen en RAM
- 🔄 **Sincronización**: Difícil de sincronizar con servidor

#### ✅ Solución URI (Implementada):
```kotlin
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val email: String,
    val phone: String,
    val password: String,
    val profilePhotoUri: String? = null  // 🎯 Solo la ruta como String
)
```

**Ventajas de URI:**
- ⚡ **Ligero**: Solo almacena la ruta (< 500 bytes)
- 🚀 **Rápido**: Queries instantáneas
- 💾 **Eficiente**: Imagen se carga solo cuando se necesita
- 🔄 **Escalable**: Fácil migración a almacenamiento en nube

### Implementación en UserDao
```kotlin
@Dao
interface UserDao {
    
    // Actualización específica para foto de perfil
    @Query("UPDATE users SET profilePhotoUri = :photoUri WHERE id = :id")
    suspend fun updateProfilePhoto(id: Long, photoUri: String?)
    
    // Recuperar usuario con foto
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): UserEntity?
}
```

### Tipos de URI Manejadas:
1. **Content URI**: `content://media/external/images/media/12345`
2. **File URI**: `file:///storage/emulated/0/Pictures/IMG_123.jpg`
3. **FileProvider URI**: `content://com.example.app.provider/images/photo.jpg`

---

## 🔐 Configuración de Permisos

### AndroidManifest.xml
```xml
<!-- Permisos para fotos de perfil -->
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />

<!-- Declarar hardware opcional -->
<uses-feature android:name="android.hardware.camera" android:required="false" />
<uses-feature android:name="android.hardware.camera.autofocus" android:required="false" />
```

### Manejo de Permisos en Runtime
```kotlin
// Launcher para solicitar permisos
val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
) { isGranted ->
    if (isGranted) {
        cameraLauncher.launch(photoUri)
    } else {
        photoSavedMessage = "Permiso de cámara denegado"
    }
}

// Solicitar permiso antes de usar cámara
Button(onClick = { 
    permissionLauncher.launch(android.Manifest.permission.CAMERA)
}) {
    Text("Tomar Foto")
}
```

---

## 📁 FileProvider para Cámara

### Configuración en AndroidManifest.xml
```xml
<!-- FileProvider para manejo seguro de archivos -->
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.provider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_provider_paths" />
</provider>
```

### file_provider_paths.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<paths>
    <external-files-path name="images" path="Pictures" />
    <external-cache-path name="cache" path="." />
</paths>
```

### Creación de Archivo Temporal
```kotlin
// Función para crear archivo temporal seguro
fun createImageFile(): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val imageFileName = "JPEG_${timeStamp}_"
    val storageDir = File(context.getExternalFilesDir(null), "Pictures")
    storageDir.mkdirs()  // Crear directorio si no existe
    return File.createTempFile(imageFileName, ".jpg", storageDir)
}

// Crear URI con FileProvider
val photoFile = createImageFile()
val photoUri = FileProvider.getUriForFile(
    context,
    "${context.packageName}.provider",
    photoFile
)
```

**Beneficios del FileProvider:**
- 🔒 **Seguridad**: No expone rutas del sistema de archivos
- 🛡️ **Permisos granulares**: Solo acceso a directorios específicos
- 📱 **Compatibilidad**: Funciona con Android 7.0+ (API 24+)
- 🔄 **Temporal**: Permisos automáticamente revocados después del uso

---

## 🎨 Implementación en UI

### ProfileEditScreen - Componente Principal
```kotlin
@Composable
fun ProfileEditScreen(nav: NavHostController) {
    var profilePhotoUri by remember { mutableStateOf<String?>(null) }
    var photoSavedMessage by remember { mutableStateOf<String?>(null) }
    
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val userRepository = remember { UserRepository(db.userDao()) }
    val scope = rememberCoroutineScope()
    
    // Crear archivo temporal para cámara
    val photoFile = remember { createImageFile() }
    val photoUri = remember {
        FileProvider.getUriForFile(context, "${context.packageName}.provider", photoFile)
    }
```

### Launchers para Cámara y Galería
```kotlin
// 📷 Launcher para cámara
val cameraLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.TakePicture()
) { success ->
    if (success) {
        profilePhotoUri = photoUri.toString()
        savePhotoToDatabase(profilePhotoUri)
    }
}

// 🖼️ Launcher para galería
val galleryLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
) { uri ->
    if (uri != null) {
        profilePhotoUri = uri.toString()
        savePhotoToDatabase(profilePhotoUri)
    }
}

// 🔐 Launcher para permisos
val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
) { isGranted ->
    if (isGranted) {
        cameraLauncher.launch(photoUri)
    } else {
        photoSavedMessage = "Permiso de cámara denegado"
    }
}
```

### UI del Avatar con Vista Previa
```kotlin
// Avatar circular con previsualización
Box(
    modifier = Modifier
        .size(120.dp)
        .clip(CircleShape)
        .background(MaterialTheme.colorScheme.primaryContainer)
        .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape),
    contentAlignment = Alignment.Center
) {
    if (profilePhotoUri != null) {
        // 🖼️ Mostrar imagen con Coil
        AsyncImage(
            model = profilePhotoUri,
            contentDescription = "Foto de perfil",
            modifier = Modifier.matchParentSize().clip(CircleShape)
        )
    } else {
        // 👤 Icono placeholder
        Icon(
            Icons.Default.Person,
            contentDescription = "Sin foto",
            modifier = Modifier.size(60.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}
```

### Botones de Acción
```kotlin
Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
    // 📷 Botón Cámara
    OutlinedButton(
        onClick = { 
            permissionLauncher.launch(android.Manifest.permission.CAMERA)
        },
        modifier = Modifier.weight(1f)
    ) {
        Icon(Icons.Default.CameraAlt, contentDescription = "Cámara")
        Spacer(Modifier.width(4.dp))
        Text("Cámara")
    }
    
    // 🖼️ Botón Galería
    Button(
        onClick = { galleryLauncher.launch("image/*") },
        modifier = Modifier.weight(1f)
    ) {
        Icon(Icons.Default.PhotoLibrary, contentDescription = "Galería")
        Spacer(Modifier.width(4.dp))
        Text("Galería")
    }
}
```

---

## 💾 Función de Guardado

```kotlin
// Función para guardar foto en base de datos
fun savePhotoToDatabase(photoUri: String?) {
    scope.launch {
        try {
            val userByEmail = db.userDao().getByEmail(currentUserEmail)
            if (userByEmail != null) {
                userRepository.updateProfilePhoto(userByEmail.id, photoUri)
                photoSavedMessage = if (photoUri != null) {
                    "Foto guardada exitosamente"
                } else {
                    "Foto eliminada"
                }
            } else {
                photoSavedMessage = "Error: Usuario no encontrado"
            }
        } catch (e: Exception) {
            photoSavedMessage = "Error al guardar: ${e.message}"
        }
    }
}
```

---

## 🔄 Flujo Completo de Usuario

### 1. Flujo Cámara
```
Usuario toca "Cámara" 
    ↓
Solicitar permiso CAMERA
    ↓
Crear archivo temporal con FileProvider
    ↓
Lanzar Intent de cámara
    ↓
Usuario toma foto
    ↓
Guardar URI en SQLite
    ↓
Mostrar foto en UI con Coil
```

### 2. Flujo Galería
```
Usuario toca "Galería"
    ↓
Lanzar selector de contenido
    ↓
Usuario selecciona imagen
    ↓
Obtener URI de la imagen
    ↓
Guardar URI en SQLite
    ↓
Mostrar foto en UI con Coil
```

### 3. Flujo Eliminación
```
Usuario toca "Eliminar Foto"
    ↓
Confirmar acción
    ↓
Actualizar SQLite con NULL
    ↓
Mostrar placeholder en UI
```

---

## 🔧 Funciones Auxiliares

### Cargar Foto Existente
```kotlin
// Cargar foto actual del usuario al abrir pantalla
LaunchedEffect(Unit) {
    val userByEmail = db.userDao().getByEmail(currentUserEmail)
    userByEmail?.profilePhotoUri?.let { uri ->
        profilePhotoUri = uri
    }
}
```

### Validación de URI
```kotlin
fun isValidImageUri(uri: String?): Boolean {
    if (uri.isNullOrEmpty()) return false
    
    return try {
        val parsedUri = Uri.parse(uri)
        when (parsedUri.scheme) {
            "content", "file" -> true
            else -> false
        }
    } catch (e: Exception) {
        false
    }
}
```

---

## 📊 Ventajas de la Implementación

### ✅ Performance
- **Consultas rápidas**: Solo strings en SQLite
- **Carga lazy**: Imágenes se cargan solo cuando se muestran
- **Cache automático**: Coil maneja cache de imágenes

### ✅ Seguridad
- **FileProvider**: Acceso controlado a archivos
- **Permisos runtime**: Usuario control total
- **Validación URI**: Previene URIs maliciosas

### ✅ Experiencia de Usuario
- **Vista previa inmediata**: Feedback visual instantáneo
- **Múltiples opciones**: Cámara, galería, eliminar
- **Mensajes informativos**: Estado claro de las operaciones

### ✅ Mantenibilidad
- **Separación de responsabilidades**: UI, Repository, Database
- **Manejo de errores**: Try-catch con mensajes descriptivos
- **Código reutilizable**: Patrones estándar de Android

---

## 🚀 Escalabilidad Futura

### Posibles Mejoras:
1. **Compresión de imágenes** antes de guardar
2. **Almacenamiento en nube** (Firebase Storage, AWS S3)
3. **Múltiples fotos** de perfil
4. **Crop/editing** integrado
5. **Sincronización offline/online**

### Migración a Cloud:
```kotlin
// Ejemplo de migración futura
sealed class PhotoStorage {
    data class Local(val uri: String) : PhotoStorage()
    data class Cloud(val url: String, val localCache: String?) : PhotoStorage()
}
```

---

## 🎯 Conclusión

El sistema de fotos de GameStore Android logra:

- 📸 **Captura flexible** desde cámara o galería
- 💾 **Almacenamiento eficiente** con URIs en SQLite
- 🔒 **Seguridad robusta** con FileProvider y permisos
- 🎨 **UI intuitiva** con vista previa y feedback
- ⚡ **Alto rendimiento** sin impactar la base de datos
- 🔄 **Arquitectura escalable** para futuras mejoras

Esta implementación proporciona una base sólida para el manejo de imágenes de perfil, siguiendo las mejores prácticas de Android y ofreciendo una excelente experiencia de usuario.