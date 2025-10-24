package com.example.uinavegacion.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.uinavegacion.viewmodel.CartViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.uinavegacion.navigation.Route
import com.example.uinavegacion.ui.utils.*
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(nav: NavHostController, cartViewModel: CartViewModel = viewModel()) {
    val windowInfo = rememberWindowInfo()
    
    // Juegos en oferta con 20% de descuento
    val gamesOnSale = listOf(
        Game("5",  "Final Fantasy VII",           49.99, "RPG",         5,   "RPG épico de Square Enix",            "https://tudominio.com/imagenes/final_fantasy_vii.webp", discount = 20),
        Game("7",  "Minecraft",                   26.99, "Aventura",    25,  "Construye tu mundo",                  "https://tudominio.com/imagenes/minecraft.webp", discount = 20),
        Game("18", "Dark Souls III",              39.99, "RPG",         8,   "Desafío extremo",                     "https://tudominio.com/imagenes/dark_souls_iii.webp", discount = 20),
        Game("20", "Elden Ring",                  59.99, "RPG",         10,  "Obra maestra de FromSoftware",        "https://tudominio.com/imagenes/elden_ring.webp", discount = 20)
    )

    val categories = listOf("Acción", "Aventura", "RPG", "Plataformas", "Deportes", "Estrategia")

    // Usar diseño adaptativo según el tamaño de pantalla
    when (windowInfo.deviceType) {
        DeviceType.PHONE_PORTRAIT, DeviceType.PHONE_LANDSCAPE -> {
            PhoneHomeLayout(
                nav = nav,
                gamesOnSale = gamesOnSale,
                categories = categories,
                windowInfo = windowInfo
            )
        }
        DeviceType.TABLET_PORTRAIT, DeviceType.TABLET_LANDSCAPE, DeviceType.DESKTOP -> {
            TabletHomeLayout(
                nav = nav,
                gamesOnSale = gamesOnSale,
                categories = categories,
                windowInfo = windowInfo
            )
        }
    }
}

@Composable
private fun PhoneHomeLayout(
    nav: NavHostController,
    gamesOnSale: List<Game>,
    categories: List<String>,
    windowInfo: WindowInfo
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                start = AdaptiveUtils.getHorizontalPadding(windowInfo),
                end = AdaptiveUtils.getHorizontalPadding(windowInfo),
                top = 24.dp,  // Espaciado superior para evitar que choque con la barra de búsqueda
                bottom = 80.dp  // Espaciado inferior para la barra de navegación
            ),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Banner de bienvenida
        WelcomeBanner(windowInfo = windowInfo)

        // Categorías rápidas
        Text(
            text = "Categorías",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(AdaptiveUtils.getItemSpacing(windowInfo))
        ) {
            items(categories) { category ->
                CategoryCard(category = category, onClick = { nav.navigate(Route.Games.path) })
            }
        }

        // Juegos destacados
        SectionHeader(
            title = "🔥 Juegos en Oferta - 20% OFF", 
            onSeeAll = { nav.navigate(Route.Games.path) },
            isMobile = true
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            items(gamesOnSale) { game ->
                MobileGameCard(
                    game = game,
                    onClick = { nav.navigate(Route.Games.path) }
                )
            }
        }
    }
}

@Composable
private fun TabletHomeLayout(
    nav: NavHostController,
    gamesOnSale: List<Game>,
    categories: List<String>,
    windowInfo: WindowInfo
) {
    val maxContentWidth = AdaptiveUtils.getMaxContentWidth(windowInfo)
    val horizontalPadding = AdaptiveUtils.getHorizontalPadding(windowInfo)
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = maxContentWidth)
                .fillMaxSize()
                .padding(
                    start = horizontalPadding,
                    end = horizontalPadding,
                    top = 24.dp,  // Espaciado superior para evitar que choque con la barra de búsqueda
                    bottom = 0.dp
                ),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Banner de bienvenida más grande para tablets
            WelcomeBanner(windowInfo = windowInfo)

            // Layouts en dos columnas para tablets landscape
            if (AdaptiveUtils.shouldUseTwoPaneLayout(windowInfo)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    // Columna izquierda - Categorías
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Categorías",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.height(200.dp)
                        ) {
                            items(categories) { category ->
                                CategoryCard(
                                    category = category,
                                    onClick = { nav.navigate(Route.Games.path) }
                                )
                            }
                        }
                    }
                    
                    // Columna derecha - Información adicional o contenido destacado
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Destacados del Mes",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "🎮 Oferta Especial",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Hasta 50% de descuento en juegos seleccionados",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Button(
                                    onClick = { nav.navigate(Route.Games.path) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Ver Ofertas")
                                }
                            }
                        }
                    }
                }
            } else {
                // Layout vertical para tablet portrait
                Text(
                    text = "Categorías",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.height(160.dp)
                ) {
                    items(categories) { category ->
                        CategoryCard(
                            category = category,
                            onClick = { nav.navigate(Route.Games.path) }
                        )
                    }
                }
            }

            // Juegos destacados en grid para tablets
            SectionHeader(title = "🔥 Juegos en Oferta - 20% OFF", onSeeAll = { nav.navigate(Route.Games.path) })

            LazyVerticalGrid(
                columns = GridCells.Fixed(AdaptiveUtils.getGridColumns(windowInfo)),
                verticalArrangement = Arrangement.spacedBy(AdaptiveUtils.getItemSpacing(windowInfo)),
                horizontalArrangement = Arrangement.spacedBy(AdaptiveUtils.getItemSpacing(windowInfo)),
                modifier = Modifier.height(600.dp)
            ) {
                items(gamesOnSale) { game ->
                    GameCard(
                        game = game,
                        onClick = { nav.navigate(Route.Games.path) },
                        windowInfo = windowInfo
                    )
                }
            }
        }
    }
}

@Composable
private fun WelcomeBanner(windowInfo: WindowInfo) {
    val bannerHeight = when (windowInfo.deviceType) {
        DeviceType.PHONE_PORTRAIT, DeviceType.PHONE_LANDSCAPE -> 160.dp
        DeviceType.TABLET_PORTRAIT -> 200.dp
        DeviceType.TABLET_LANDSCAPE, DeviceType.DESKTOP -> 240.dp
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(bannerHeight),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.secondaryContainer
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "¡Bienvenido a GameStore Pro!",
                    style = when (windowInfo.deviceType) {
                        DeviceType.PHONE_PORTRAIT, DeviceType.PHONE_LANDSCAPE -> MaterialTheme.typography.titleLarge
                        DeviceType.TABLET_PORTRAIT -> MaterialTheme.typography.headlineMedium
                        DeviceType.TABLET_LANDSCAPE, DeviceType.DESKTOP -> MaterialTheme.typography.headlineLarge
                    },
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Descubre los mejores videojuegos del mercado",
                    style = when (windowInfo.deviceType) {
                        DeviceType.PHONE_PORTRAIT, DeviceType.PHONE_LANDSCAPE -> MaterialTheme.typography.bodyLarge
                        DeviceType.TABLET_PORTRAIT -> MaterialTheme.typography.titleMedium
                        DeviceType.TABLET_LANDSCAPE, DeviceType.DESKTOP -> MaterialTheme.typography.titleLarge
                    },
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
private fun CategoryCard(category: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Text(
            text = category,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SectionHeader(title: String, onSeeAll: () -> Unit, isMobile: Boolean = false) {
    if (isMobile) {
        // Diseño vertical para móvil
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            TextButton(
                onClick = onSeeAll,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("Ver todos los juegos →")
            }
        }
    } else {
        // Diseño horizontal para tablet
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onSeeAll) {
                Text("Ver todos")
            }
        }
    }
}

@Composable
private fun MobileGameCard(
    game: Game,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(260.dp),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Imagen del juego con badges
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                // Imagen del juego
                if (game.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = game.imageUrl,
                        contentDescription = game.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Sin imagen",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Badge de descuento
                if (game.hasDiscount) {
                    Card(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(6.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFE74C3C)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "-${game.discount}%",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            
            // Información del juego
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Nombre del juego
                Text(
                    text = game.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(Modifier.height(4.dp))
                
                // Categoría
                Text(
                    text = game.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(Modifier.weight(1f))
                
                // Precios
                if (game.hasDiscount) {
                    // Precio original tachado
                    Text(
                        text = "$${String.format("%.2f", game.price)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                    )
                    // Precio con descuento
                    Text(
                        text = "$${String.format("%.2f", game.discountedPrice)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF27AE60)
                    )
                } else {
                    Text(
                        text = if (game.price == 0.0) "Gratis" else "$${String.format("%.2f", game.price)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun GameCard(
    game: Game,
    onClick: () -> Unit,
    windowInfo: WindowInfo
) {
    val cardWidth = when (windowInfo.deviceType) {
        DeviceType.PHONE_PORTRAIT, DeviceType.PHONE_LANDSCAPE -> 200.dp
        else -> 180.dp
    }
    val cardHeight = when (windowInfo.deviceType) {
        DeviceType.PHONE_PORTRAIT, DeviceType.PHONE_LANDSCAPE -> 240.dp
        else -> 220.dp
    }
    
    Card(
        modifier = Modifier
            .width(cardWidth)
            .height(cardHeight),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Imagen del juego con precio destacado
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                // Imagen del juego con AsyncImage
                if (game.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = game.imageUrl,
                        contentDescription = game.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Sin imagen",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Precio con descuento si aplica
                if (game.hasDiscount) {
                    // Badge de descuento
                    Card(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFE74C3C) // Rojo para descuento
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Text(
                            text = "-${game.discount}%",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                
                // Precio (tachado si hay descuento)
                Card(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (game.hasDiscount) Color(0xFF27AE60) else MaterialTheme.colorScheme.primary
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        if (game.hasDiscount) {
                            Text(
                                text = "$${String.format("%.2f", game.price)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.7f),
                                textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                            )
                        }
                        Text(
                            text = if (game.price == 0.0) "Gratis" else "$${String.format("%.2f", game.discountedPrice)}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
            
            // Información del juego
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = game.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = game.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
