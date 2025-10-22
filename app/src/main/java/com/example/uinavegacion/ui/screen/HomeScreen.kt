package com.example.uinavegacion.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(nav: NavHostController, cartViewModel: CartViewModel = viewModel()) {
    val windowInfo = rememberWindowInfo()
    
    val featuredGames = listOf(
        Triple("Super Mario Bros", "$29.99", "Plataformas"),
        Triple("The Legend of Zelda", "$39.99", "Aventura"),
        Triple("Pokémon Red", "$24.99", "RPG"),
        Triple("Minecraft", "$26.99", "Aventura"),
        Triple("Call of Duty", "$59.99", "Acción"),
        Triple("Overwatch 2", "$39.99", "Acción"),
        Triple("FIFA 24", "$69.99", "Deportes"),
        Triple("Assassin's Creed", "$49.99", "Aventura")
    )

    val categories = listOf("Acción", "Aventura", "RPG", "Plataformas", "Deportes", "Estrategia")

    // Usar diseño adaptativo según el tamaño de pantalla
    when (windowInfo.deviceType) {
        DeviceType.PHONE_PORTRAIT, DeviceType.PHONE_LANDSCAPE -> {
            PhoneHomeLayout(
                nav = nav,
                featuredGames = featuredGames,
                categories = categories,
                windowInfo = windowInfo
            )
        }
        DeviceType.TABLET_PORTRAIT, DeviceType.TABLET_LANDSCAPE, DeviceType.DESKTOP -> {
            TabletHomeLayout(
                nav = nav,
                featuredGames = featuredGames,
                categories = categories,
                windowInfo = windowInfo
            )
        }
    }
}

@Composable
private fun PhoneHomeLayout(
    nav: NavHostController,
    featuredGames: List<Triple<String, String, String>>,
    categories: List<String>,
    windowInfo: WindowInfo
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(AdaptiveUtils.getHorizontalPadding(windowInfo)),
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
        SectionHeader(title = "Juegos Destacados", onSeeAll = { nav.navigate(Route.Games.path) })

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(AdaptiveUtils.getItemSpacing(windowInfo)),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(featuredGames) { game ->
                GameCard(
                    game = game,
                    onClick = { nav.navigate(Route.Games.path) },
                    windowInfo = windowInfo
                )
            }
        }
    }
}

@Composable
private fun TabletHomeLayout(
    nav: NavHostController,
    featuredGames: List<Triple<String, String, String>>,
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
                .padding(horizontal = horizontalPadding),
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
            SectionHeader(title = "Juegos Destacados", onSeeAll = { nav.navigate(Route.Games.path) })

            LazyVerticalGrid(
                columns = GridCells.Fixed(AdaptiveUtils.getGridColumns(windowInfo)),
                verticalArrangement = Arrangement.spacedBy(AdaptiveUtils.getItemSpacing(windowInfo)),
                horizontalArrangement = Arrangement.spacedBy(AdaptiveUtils.getItemSpacing(windowInfo)),
                modifier = Modifier.height(600.dp)
            ) {
                items(featuredGames) { game ->
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
private fun SectionHeader(title: String, onSeeAll: () -> Unit) {
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

@Composable
private fun GameCard(
    game: Triple<String, String, String>,
    onClick: () -> Unit,
    windowInfo: WindowInfo
) {
    val (name, price, category) = game
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
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.secondaryContainer
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Precio destacado en la esquina
                Card(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Text(
                        text = price,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                
                // Icono del juego
                Text(
                    text = "🎮",
                    style = MaterialTheme.typography.headlineLarge
                )
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
                    text = name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = category,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
