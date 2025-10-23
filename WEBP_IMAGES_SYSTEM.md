# 🖼️ Sistema de Imágenes WebP - GameStore

## 📋 Índice
1. [Estructura de Imágenes](#estructura-de-imágenes)
2. [Configuración WebP](#configuración-webp)
3. [Integración con Coil](#integración-con-coil)
4. [Sistema de Recursos](#sistema-de-recursos)
5. [Performance y Optimización](#performance-y-optimización)
6. [Gestión de Assets](#gestión-de-assets)

---

## 📁 Estructura de Imágenes

### Organización de Directorios
```
app/src/main/res/
├── drawable-webp/                    # Imágenes WebP principales
│   ├── game_covers/                 # Portadas de juegos
│   │   ├── cyberpunk_2077.webp
│   │   ├── the_witcher_3.webp
│   │   ├── assassins_creed_valhalla.webp
│   │   ├── minecraft.webp
│   │   └── fortnite.webp
│   ├── game_screenshots/            # Capturas de pantalla
│   │   ├── cyberpunk_screenshot_1.webp
│   │   ├── witcher_screenshot_1.webp
│   │   └── minecraft_screenshot_1.webp
│   ├── game_banners/               # Banners promocionales
│   │   ├── cyberpunk_banner.webp
│   │   ├── witcher_banner.webp
│   │   └── minecraft_banner.webp
│   └── ui_elements/                # Elementos de UI
│       ├── logo_gamestore.webp
│       ├── empty_cart.webp
│       └── placeholder_game.webp
├── drawable-hdpi/                  # Fallbacks PNG alta densidad
├── drawable-xhdpi/                 # Fallbacks PNG extra alta densidad
└── drawable-xxhdpi/                # Fallbacks PNG ultra alta densidad
```

### Convenciones de Nomenclatura
```kotlin
// Formato: [categoria]_[nombre_juego]_[tipo].webp
game_covers/cyberpunk_2077.webp           // Portada principal
game_screenshots/cyberpunk_screenshot_1.webp  // Captura 1
game_banners/cyberpunk_banner.webp        // Banner promocional

// Para juegos con espacios: usar guiones bajos
game_covers/assassins_creed_valhalla.webp
game_covers/grand_theft_auto_v.webp
```

---

## ⚙️ Configuración WebP

### build.gradle.kts (app level)
```kotlin
android {
    compileSdk 34

    defaultConfig {
        applicationId "com.gamestore.app"
        minSdk 21  // WebP soportado desde API 21+
        targetSdk 34
        
        // Configuración para WebP
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildFeatures {
        compose = true
    }
    
    // Configuración de recursos
    androidResources {
        additionalParameters += listOf(
            "--allow-reserved-package-id",
            "--auto-add-overlay"
        )
    }
}

dependencies {
    // Coil para cargar imágenes WebP
    implementation("io.coil-kt:coil-compose:2.5.0")
    implementation("io.coil-kt:coil-gif:2.5.0")
    
    // Opcional: Soporte para WebP animado
    implementation("com.github.bumptech.glide:webp:4.14.2")
}
```

### Configuración de Recursos WebP
```xml
<!-- app/src/main/res/values/webp_config.xml -->
<resources>
    <!-- Configuración de calidad WebP -->
    <integer name="webp_quality">85</integer>
    <integer name="webp_lossless">0</integer>
    
    <!-- Dimensiones estándar para imágenes -->
    <dimen name="game_cover_width">300dp</dimen>
    <dimen name="game_cover_height">400dp</dimen>
    <dimen name="game_banner_width">800dp</dimen>
    <dimen name="game_banner_height">300dp</dimen>
    <dimen name="game_screenshot_width">600dp</dimen>
    <dimen name="game_screenshot_height">400dp</dimen>
</resources>
```

---

## 🎨 Integración con Coil

### Configuración de Coil Application
```kotlin
// Application.kt
class GameStoreApplication : Application(), ImageLoaderFactory {
    
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                // Soporte para WebP
                add(SvgDecoder.Factory())
                add(GifDecoder.Factory())
                
                // Decoder personalizado para WebP
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25) // 25% de la memoria para cache de imágenes
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(50 * 1024 * 1024) // 50MB cache en disco
                    .build()
            }
            // Placeholder para imágenes que fallan
            .placeholder(R.drawable.placeholder_game)
            .error(R.drawable.placeholder_game)
            .build()
    }
}
```

### Composable para Imágenes de Juegos
```kotlin
@Composable
fun GameImage(
    imageResource: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    imageType: GameImageType = GameImageType.COVER
) {
    val context = LocalContext.current
    
    // Determinar el recurso correcto
    val imageRes = remember(imageResource) {
        getGameImageResource(context, imageResource, imageType)
    }
    
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(imageRes)
            .crossfade(true)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .build(),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
        placeholder = painterResource(
            when (imageType) {
                GameImageType.COVER -> R.drawable.placeholder_game_cover
                GameImageType.BANNER -> R.drawable.placeholder_game_banner
                GameImageType.SCREENSHOT -> R.drawable.placeholder_game_screenshot
            }
        ),
        error = painterResource(R.drawable.placeholder_game)
    )
}

enum class GameImageType {
    COVER,
    BANNER, 
    SCREENSHOT
}
```

### Sistema de Recursos Dinámico
```kotlin
object GameImageManager {
    
    // Mapeo de juegos a recursos WebP
    private val gameImageMap = mapOf(
        "cyberpunk_2077" to GameImages(
            cover = R.drawable.cyberpunk_2077,
            banner = R.drawable.cyberpunk_banner,
            screenshots = listOf(
                R.drawable.cyberpunk_screenshot_1,
                R.drawable.cyberpunk_screenshot_2,
                R.drawable.cyberpunk_screenshot_3
            )
        ),
        "the_witcher_3" to GameImages(
            cover = R.drawable.the_witcher_3,
            banner = R.drawable.witcher_banner,
            screenshots = listOf(
                R.drawable.witcher_screenshot_1,
                R.drawable.witcher_screenshot_2
            )
        ),
        "minecraft" to GameImages(
            cover = R.drawable.minecraft,
            banner = R.drawable.minecraft_banner,
            screenshots = listOf(
                R.drawable.minecraft_screenshot_1
            )
        ),
        "assassins_creed_valhalla" to GameImages(
            cover = R.drawable.assassins_creed_valhalla,
            banner = R.drawable.assassins_creed_banner,
            screenshots = emptyList()
        ),
        "fortnite" to GameImages(
            cover = R.drawable.fortnite,
            banner = R.drawable.fortnite_banner,
            screenshots = emptyList()
        )
    )
    
    fun getGameImages(gameId: String): GameImages? {
        return gameImageMap[gameId]
    }
    
    fun getGameCover(gameId: String): Int {
        return gameImageMap[gameId]?.cover ?: R.drawable.placeholder_game
    }
    
    fun getGameBanner(gameId: String): Int {
        return gameImageMap[gameId]?.banner ?: R.drawable.placeholder_game_banner
    }
    
    fun getGameScreenshots(gameId: String): List<Int> {
        return gameImageMap[gameId]?.screenshots ?: emptyList()
    }
}

data class GameImages(
    val cover: Int,
    val banner: Int,
    val screenshots: List<Int>
)
```

---

## 🚀 Performance y Optimización

### Carga Lazy de Imágenes
```kotlin
@Composable
fun GameGridItem(
    game: Game,
    windowInfo: WindowInfo,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.75f) // Proporción 3:4 para portadas
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box {
            // Imagen de portada con carga lazy
            GameImage(
                imageResource = game.id,
                contentDescription = "Portada de ${game.title}",
                modifier = Modifier.fillMaxSize(),
                imageType = GameImageType.COVER
            )
            
            // Overlay con información
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            ),
                            startY = 200f
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(
                        text = game.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Text(
                        text = "$${game.price}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
```

### Preloading de Imágenes Críticas
```kotlin
@Composable
fun PreloadGameImages(games: List<Game>) {
    val imageLoader = LocalContext.current.imageLoader
    
    LaunchedEffect(games) {
        // Precargar las primeras 6 imágenes que se verán
        games.take(6).forEach { game ->
            val request = ImageRequest.Builder(LocalContext.current)
                .data(GameImageManager.getGameCover(game.id))
                .memoryCachePolicy(CachePolicy.ENABLED)
                .build()
            
            imageLoader.enqueue(request)
        }
    }
}
```

---

## 📦 Gestión de Assets

### Script de Conversión WebP
```bash
#!/bin/bash
# convert_to_webp.sh - Script para convertir imágenes a WebP

INPUT_DIR="original_images"
OUTPUT_DIR="app/src/main/res/drawable-webp"

# Crear directorios
mkdir -p "$OUTPUT_DIR/game_covers"
mkdir -p "$OUTPUT_DIR/game_banners"
mkdir -p "$OUTPUT_DIR/game_screenshots"

# Convertir portadas (300x400)
for file in "$INPUT_DIR/covers"/*.{jpg,png,jpeg}; do
    if [ -f "$file" ]; then
        filename=$(basename "$file" | cut -d. -f1)
        cwebp -q 85 -resize 300 400 "$file" -o "$OUTPUT_DIR/game_covers/${filename}.webp"
        echo "Converted cover: $filename.webp"
    fi
done

# Convertir banners (800x300)
for file in "$INPUT_DIR/banners"/*.{jpg,png,jpeg}; do
    if [ -f "$file" ]; then
        filename=$(basename "$file" | cut -d. -f1)
        cwebp -q 85 -resize 800 300 "$file" -o "$OUTPUT_DIR/game_banners/${filename}.webp"
        echo "Converted banner: $filename.webp"
    fi
done

# Convertir screenshots (600x400)
for file in "$INPUT_DIR/screenshots"/*.{jpg,png,jpeg}; do
    if [ -f "$file" ]; then
        filename=$(basename "$file" | cut -d. -f1)
        cwebp -q 85 -resize 600 400 "$file" -o "$OUTPUT_DIR/game_screenshots/${filename}.webp"
        echo "Converted screenshot: $filename.webp"
    fi
done

echo "Conversión completada!"
```

### Validación de Imágenes
```kotlin
object ImageValidator {
    
    fun validateGameImages(): List<String> {
        val missingImages = mutableListOf<String>()
        
        // Lista de juegos que deben tener imágenes
        val expectedGames = listOf(
            "cyberpunk_2077",
            "the_witcher_3", 
            "minecraft",
            "assassins_creed_valhalla",
            "fortnite"
        )
        
        expectedGames.forEach { gameId ->
            // Verificar portada
            if (GameImageManager.getGameCover(gameId) == R.drawable.placeholder_game) {
                missingImages.add("Cover missing for: $gameId")
            }
            
            // Verificar banner
            if (GameImageManager.getGameBanner(gameId) == R.drawable.placeholder_game_banner) {
                missingImages.add("Banner missing for: $gameId")
            }
        }
        
        return missingImages
    }
    
    fun getImageSizeInfo(context: Context, resourceId: Int): ImageInfo? {
        return try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeResource(context.resources, resourceId, options)
            
            ImageInfo(
                width = options.outWidth,
                height = options.outHeight,
                mimeType = options.outMimeType ?: "unknown"
            )
        } catch (e: Exception) {
            null
        }
    }
}

data class ImageInfo(
    val width: Int,
    val height: Int,
    val mimeType: String
)
```

---

## 🎯 Implementación en UI

### Actualización de GameCard
```kotlin
@Composable
fun GameCard(
    game: Game,
    onAddToCart: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column {
            // Imagen de portada WebP
            GameImage(
                imageResource = game.id,
                contentDescription = "Portada de ${game.title}",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                imageType = GameImageType.COVER
            )
            
            // Contenido de la card
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = game.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$${game.price}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Button(
                        onClick = onAddToCart,
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Agregar")
                    }
                }
            }
        }
    }
}
```

### Game Detail Screen con Screenshots
```kotlin
@Composable
fun GameDetailScreen(
    game: Game,
    navController: NavHostController
) {
    LazyColumn {
        // Banner principal
        item {
            GameImage(
                imageResource = game.id,
                contentDescription = "Banner de ${game.title}",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                imageType = GameImageType.BANNER
            )
        }
        
        // Información del juego
        item {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = game.title,
                    style = MaterialTheme.typography.headlineMedium
                )
                
                Text(
                    text = game.description,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
        
        // Screenshots
        item {
            val screenshots = GameImageManager.getGameScreenshots(game.id)
            if (screenshots.isNotEmpty()) {
                Text(
                    text = "Capturas de pantalla",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(screenshots) { screenshotRes ->
                        AsyncImage(
                            model = screenshotRes,
                            contentDescription = "Captura de ${game.title}",
                            modifier = Modifier
                                .width(200.dp)
                                .height(120.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}
```

---

## 📊 Beneficios del Sistema WebP

### 🔥 Performance
- **Tamaño reducido**: 25-35% menor que PNG, 15-25% menor que JPEG
- **Carga más rápida**: Menos tiempo de descarga y procesamiento
- **Menor uso de memoria**: Cache más eficiente
- **Mejor experiencia**: Navegación más fluida

### 📱 Compatibilidad
- **Android 4.2+**: Soporte nativo para WebP
- **Fallbacks**: PNG/JPEG para dispositivos antiguos
- **Progressive loading**: Carga progresiva de imágenes
- **Adaptive quality**: Calidad según conexión

### 🛠️ Desarrollo
- **Gestión centralizada**: GameImageManager unifica el acceso
- **Type safety**: Enums para tipos de imagen
- **Cache inteligente**: Coil optimiza automáticamente
- **Debug tools**: Validación y métricas incluidas

---

## ✅ Checklist de Implementación

### Preparación
- [ ] Instalar herramientas de conversión WebP
- [ ] Crear estructura de directorios
- [ ] Configurar build.gradle con Coil

### Conversión de Assets
- [ ] Convertir portadas a WebP (300x400)
- [ ] Convertir banners a WebP (800x300) 
- [ ] Convertir screenshots a WebP (600x400)
- [ ] Crear placeholders en WebP

### Integración de Código
- [ ] Implementar GameImageManager
- [ ] Configurar Coil en Application
- [ ] Actualizar composables con GameImage
- [ ] Añadir preloading para imágenes críticas

### Testing y Validación
- [ ] Probar en diferentes densidades de pantalla
- [ ] Validar performance vs imágenes anteriores
- [ ] Verificar fallbacks en dispositivos antiguos
- [ ] Testear cache y memoria

---

Este sistema de imágenes WebP optimiza significativamente el rendimiento de GameStore mientras mantiene la calidad visual y proporciona una experiencia de usuario superior.