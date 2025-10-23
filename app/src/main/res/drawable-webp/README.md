# 🎮 GameStore WebP Images - README

## 📂 Estructura de Imágenes

Este directorio contiene todas las imágenes WebP optimizadas para GameStore Android:

### 📁 game_covers/
Portadas de juegos en formato 300x400px
- `cyberpunk_2077.webp`
- `the_witcher_3.webp` 
- `minecraft.webp`
- `assassins_creed_valhalla.webp`
- `fortnite.webp`

### 📁 game_banners/
Banners promocionales en formato 800x300px
- `cyberpunk_banner.webp`
- `witcher_banner.webp`
- `minecraft_banner.webp`

### 📁 game_screenshots/
Capturas de pantalla en formato 600x400px
- `cyberpunk_screenshot_1.webp`
- `witcher_screenshot_1.webp`
- `minecraft_screenshot_1.webp`

## ⚙️ Especificaciones

- **Formato**: WebP con calidad 85%
- **Compresión**: Lossy optimizada para mobile
- **Soporte**: Android 4.2+ (API 17+)
- **Cache**: Gestionado por Coil ImageLoader

## 🔧 Uso en Código

```kotlin
// Cargar portada de juego
GameImage(
    imageResource = "cyberpunk_2077",
    contentDescription = "Cyberpunk 2077",
    imageType = GameImageType.COVER
)
```

## 📊 Beneficios

- 🔥 **25-35% menor** tamaño que PNG
- ⚡ **Carga más rápida** en redes lentas  
- 📱 **Menos memoria** utilizada
- 🎯 **Mejor UX** general

Para más detalles, consulta `WEBP_IMAGES_SYSTEM.md`