# 🔗 Conexión de Microservicios con Android

## ✅ Configuración Completada

### 1. Dependencias Agregadas
- ✅ Retrofit 2.11.0
- ✅ Gson Converter 2.11.0
- ✅ OkHttp 4.12.0
- ✅ Logging Interceptor 4.12.0

### 2. Permisos
- ✅ INTERNET ya estaba en AndroidManifest.xml
- ✅ `usesCleartextTraffic="true"` para desarrollo local

### 3. Estructura Creada

```
data/remote/
├── config/
│   ├── ApiConfig.kt          # URLs base de los servicios
│   └── RetrofitClient.kt     # Cliente Retrofit configurado
├── api/
│   ├── AuthApi.kt            # Endpoints de autenticación
│   ├── GameCatalogApi.kt     # Endpoints de catálogo
│   ├── OrderApi.kt           # Endpoints de órdenes
│   └── LibraryApi.kt         # Endpoints de biblioteca
├── dto/
│   ├── AuthResponse.kt       # DTOs de autenticación
│   ├── GameResponse.kt       # DTOs de juegos
│   ├── OrderResponse.kt      # DTOs de órdenes
│   └── LibraryItemResponse.kt # DTOs de biblioteca
└── repository/
    ├── AuthRemoteRepository.kt
    ├── GameCatalogRemoteRepository.kt
    ├── OrderRemoteRepository.kt
    └── LibraryRemoteRepository.kt
```

## 🔧 Configuración de URLs

### Para Emulador Android
```kotlin
// En ApiConfig.kt
const val AUTH_SERVICE_BASE_URL = "http://10.0.2.2:3001/api/"
```
`10.0.2.2` es el alias de `localhost` en el emulador Android.

### Para Dispositivo Físico
1. Encuentra la IP de tu PC:
   - Windows: `ipconfig` en CMD
   - Mac/Linux: `ifconfig` o `ip addr`
   
2. Actualiza `ApiConfig.kt`:
```kotlin
const val AUTH_SERVICE_BASE_URL = "http://192.168.1.X:3001/api/"
```

3. Asegúrate de que el dispositivo y la PC estén en la misma red WiFi.

## 🚀 Uso en ViewModels

### Ejemplo: AuthViewModel usando microservicio

```kotlin
class AuthViewModel : ViewModel() {
    private val authRemoteRepo = AuthRemoteRepository()
    
    fun register(name: String, email: String, phone: String, password: String) {
        viewModelScope.launch {
            val request = RegisterRequest(name, email, phone, password)
            val result = authRemoteRepo.register(request)
            result.onSuccess { response ->
                // Guardar token y usuario
                SessionManager.saveToken(response.token)
                SessionManager.loginUser(response.user)
            }.onFailure { error ->
                // Mostrar error
            }
        }
    }
}
```

### Ejemplo: GameCatalogViewModel usando microservicio

```kotlin
class GameCatalogViewModel : ViewModel() {
    private val gameRemoteRepo = GameCatalogRemoteRepository()
    
    fun loadGames() {
        viewModelScope.launch {
            val result = gameRemoteRepo.getAllGames()
            result.onSuccess { games ->
                _games.value = games
            }.onFailure { error ->
                _error.value = error.message
            }
        }
    }
}
```

## 📝 Próximos Pasos

1. **Actualizar ViewModels existentes** para usar los repositorios remotos
2. **Implementar caché local** (Room) como fallback cuando no hay internet
3. **Manejar errores de conexión** de forma elegante
4. **Agregar autenticación JWT** a las peticiones que lo requieran

## ⚠️ Notas Importantes

- Los microservicios deben estar corriendo antes de usar la app
- Para desarrollo, usa `10.0.2.2` en el emulador
- Para producción, cambia las URLs a las del servidor real
- El logging interceptor mostrará todas las peticiones HTTP en Logcat

## 🔍 Verificar Conexión

1. Inicia los microservicios
2. Abre Logcat en Android Studio
3. Filtra por "OkHttp" para ver las peticiones
4. Deberías ver las peticiones HTTP con sus respuestas

