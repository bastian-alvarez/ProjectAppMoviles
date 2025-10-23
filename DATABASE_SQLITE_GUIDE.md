# 🗄️ Guía Completa de SQLite en GameStore Android

## 📋 Índice
1. [Configuración de Room Database](#configuración-de-room-database)
2. [Estructura de Entidades](#estructura-de-entidades)
3. [Data Access Objects (DAOs)](#data-access-objects-daos)
4. [Repositorios](#repositorios)
5. [Migración de Base de Datos](#migración-de-base-de-datos)
6. [Implementación en ViewModels](#implementación-en-viewmodels)

---

## 🔧 Configuración de Room Database

### AppDatabase.kt
```kotlin
@Database(
    entities = [
        UserEntity::class,
        AdminEntity::class,
        JuegoEntity::class,
        GeneroEntity::class,
        // ... más entidades
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    
    // DAOs abstractos
    abstract fun userDao(): UserDao
    abstract fun adminDao(): AdminDao
    abstract fun juegoDao(): JuegoDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gamestore_database"
                )
                .fallbackToDestructiveMigration() // Para desarrollo
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
```

### Características Principales:
- **Singleton Pattern**: Una sola instancia de la base de datos
- **Thread Safety**: Uso de `@Volatile` y `synchronized`
- **Fallback Destructivo**: Para desarrollo, elimina datos en conflictos de versión
- **TypeConverters**: Para tipos de datos complejos

---

## 📊 Estructura de Entidades

### 1. UserEntity - Usuarios del Sistema
```kotlin
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val email: String,
    val phone: String,
    val password: String,
    val profilePhotoUri: String? = null  // ⭐ Campo para foto de perfil
)
```

**Características importantes:**
- **Clave primaria auto-generada**: `id` se incrementa automáticamente
- **Campo profilePhotoUri**: Almacena la URI de la foto como String (no BLOB)
- **Campos obligatorios**: name, email, phone, password
- **Campo opcional**: profilePhotoUri puede ser null

### 2. AdminEntity - Administradores
```kotlin
@Entity(tableName = "admins")
data class AdminEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val username: String,
    val password: String,
    val email: String,
    val fullName: String,
    val createdAt: String,
    val isActive: Boolean = true
)
```

### 3. JuegoEntity - Catálogo de Juegos
```kotlin
@Entity(
    tableName = "juegos",
    foreignKeys = [
        ForeignKey(
            entity = GeneroEntity::class,
            parentColumns = ["id"],
            childColumns = ["generoId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class JuegoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val nombre: String,
    val descripcion: String,
    val precio: Double,
    val generoId: Long,
    val fechaCreacion: String,
    val activo: Boolean = true
)
```

**Relaciones de Base de Datos:**
- **Foreign Key**: Relación con GeneroEntity
- **Cascade Delete**: Si se elimina un género, se eliminan sus juegos
- **Índices implícitos**: Room crea índices para foreign keys automáticamente

---

## 🔍 Data Access Objects (DAOs)

### UserDao - Operaciones de Usuario
```kotlin
@Dao
interface UserDao {
    
    // CREATE
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: UserEntity): Long
    
    // READ
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getByEmail(email: String): UserEntity?
    
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): UserEntity?
    
    @Query("SELECT * FROM users ORDER BY id ASC")
    suspend fun getAllOrderedById(): List<UserEntity>
    
    // UPDATE
    @Query("UPDATE users SET name = :name, email = :email, phone = :phone, password = :password WHERE id = :id")
    suspend fun update(id: Long, name: String, email: String, phone: String, password: String)
    
    // ⭐ UPDATE específico para foto de perfil
    @Query("UPDATE users SET profilePhotoUri = :photoUri WHERE id = :id")
    suspend fun updateProfilePhoto(id: Long, photoUri: String?)
    
    // DELETE
    @Query("DELETE FROM users WHERE id = :id")
    suspend fun delete(id: Long)
    
    // STATISTICS
    @Query("SELECT COUNT(*) FROM users")
    suspend fun count(): Int
}
```

### Patrones de Diseño en DAOs:
1. **Operaciones CRUD completas**: Create, Read, Update, Delete
2. **Queries específicas**: Búsqueda por email, actualización de foto
3. **Funciones suspend**: Para operaciones asíncronas
4. **Estrategias de conflicto**: ABORT para duplicados
5. **Ordenamiento**: ORDER BY para listas consistentes

---

## 🔄 Repositorios

### UserRepository - Lógica de Negocio
```kotlin
class UserRepository(private val userDao: UserDao) {
    
    // Login con validación
    suspend fun login(email: String, password: String): Result<UserEntity> {
        val user = userDao.getByEmail(email)
        return if (user != null && user.password == password) {
            Result.success(user)
        } else {
            Result.failure(Exception("Credenciales inválidas"))
        }
    }
    
    // Registro con validación de duplicados
    suspend fun register(name: String, email: String, phone: String, password: String): Result<Long> {
        val exists = userDao.getByEmail(email) != null
        return if (exists) {
            Result.failure(Exception("El correo ya está registrado"))
        } else {
            val id = userDao.insert(UserEntity(
                name = name,
                email = email,
                phone = phone,
                password = password
            ))
            Result.success(id)
        }
    }
    
    // ⭐ Actualización de foto de perfil
    suspend fun updateProfilePhoto(userId: Long, photoUri: String?): Result<Unit> {
        return try {
            userDao.updateProfilePhoto(userId, photoUri)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### Ventajas del Patrón Repository:
- **Separación de responsabilidades**: DAO maneja datos, Repository maneja lógica
- **Manejo de errores**: Uso de Result<T> para operaciones seguras
- **Validaciones**: Verificación de duplicados, credenciales
- **Abstracción**: La UI no conoce detalles de la base de datos

---

## 🔄 Migración de Base de Datos

### Estrategia Actual - Desarrollo
```kotlin
.fallbackToDestructiveMigration()
```

### Migración para Producción
```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE users ADD COLUMN profilePhotoUri TEXT")
    }
}

Room.databaseBuilder(context, AppDatabase::class.java, "gamestore_database")
    .addMigrations(MIGRATION_1_2)
    .build()
```

---

## 🎯 Implementación en ViewModels

### Uso en AuthViewModel
```kotlin
class AuthViewModel(private val userRepository: UserRepository) : ViewModel() {
    
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = userRepository.login(email, password)
                if (result.isSuccess) {
                    _loginResult.value = result.getOrNull()
                } else {
                    _errorMessage.value = result.exceptionOrNull()?.message
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
}
```

---

## 📱 Integración con la UI

### Ejemplo en ProfileEditScreen
```kotlin
// Actualizar foto de perfil
scope.launch {
    val userByEmail = db.userDao().getByEmail(email)
    if (userByEmail != null) {
        userRepository.updateProfilePhoto(userByEmail.id, profilePhotoUri)
        photoSavedMessage = "Foto guardada exitosamente"
    }
}
```

---

## 🔍 Características Avanzadas

### 1. TypeConverters para Datos Complejos
```kotlin
class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return Gson().toJson(value)
    }
    
    @TypeConverter
    fun toStringList(value: String): List<String> {
        return Gson().fromJson(value, object : TypeToken<List<String>>() {}.type)
    }
}
```

### 2. Queries Complejas con JOIN
```kotlin
@Query("""
    SELECT j.*, g.nombre as generoNombre 
    FROM juegos j 
    INNER JOIN generos g ON j.generoId = g.id 
    WHERE j.activo = 1 
    ORDER BY j.fechaCreacion DESC
""")
suspend fun getActiveGamesWithGenre(): List<GameWithGenre>
```

### 3. Observación de Cambios con Flow
```kotlin
@Query("SELECT * FROM users ORDER BY name ASC")
fun getAllUsersFlow(): Flow<List<UserEntity>>
```

---

## ✅ Mejores Prácticas Implementadas

1. **✅ Singleton Database**: Una instancia para toda la app
2. **✅ Suspend Functions**: Operaciones asíncronas no bloqueantes  
3. **✅ Result Wrapping**: Manejo seguro de errores
4. **✅ Repository Pattern**: Separación de capas
5. **✅ Foreign Keys**: Integridad referencial
6. **✅ Índices automáticos**: Optimización de queries
7. **✅ TypeConverters**: Soporte para tipos complejos
8. **✅ Fallback Migration**: Flexibilidad en desarrollo

---

## 🎯 Conclusión

La implementación de SQLite con Room en GameStore Android proporciona:

- **🔒 Persistencia robusta** de datos de usuarios, juegos y configuraciones
- **📸 Almacenamiento eficiente** de URIs de fotos (no BLOBs pesados)
- **🔄 Operaciones asíncronas** que no bloquean la UI
- **🛡️ Manejo seguro** de errores y validaciones
- **📊 Relaciones normalizadas** entre entidades
- **🚀 Alto rendimiento** con índices optimizados

Esta arquitectura permite un crecimiento escalable de la aplicación manteniendo la integridad y performance de los datos.