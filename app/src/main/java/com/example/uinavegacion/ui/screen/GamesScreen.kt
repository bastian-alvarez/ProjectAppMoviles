package com.example.uinavegacion.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.uinavegacion.viewmodel.SearchViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.uinavegacion.navigation.*
import com.example.uinavegacion.ui.utils.*

data class Game(
    val id: String, 
    val name: String, 
    val price: Double, 
    val category: String, 
    val stock: Int,
    val description: String = "Descripción del juego",
    val imageUrl: String = "" // URL de la imagen del juego
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamesScreen(nav: NavHostController, searchViewModel: SearchViewModel = viewModel(), cartViewModel: com.example.uinavegacion.viewmodel.CartViewModel = viewModel()) {
    val windowInfo = rememberWindowInfo()
    
    // Lista ampliada de juegos con stock e imágenes
    val allGames = listOf(
        Game("1", "Super Mario Bros", 29.99, "Plataformas", 15, "El clásico juego de plataformas", "https://picsum.photos/seed/mario/400/300.webp"),
        Game("2", "The Legend of Zelda", 39.99, "Aventura", 8, "Épica aventura en Hyrule", "https://picsum.photos/seed/zelda/400/300.webp"),
        Game("3", "Pokémon Red", 24.99, "RPG", 20, "Conviértete en maestro Pokémon", "https://picsum.photos/seed/pokemon/400/300.webp"),
        Game("4", "Sonic the Hedgehog", 19.99, "Plataformas", 12, "Velocidad supersónica", "https://picsum.photos/seed/sonic/400/300.webp"),
        Game("5", "Final Fantasy VII", 49.99, "RPG", 5, "RPG épico de Square Enix", "https://picsum.photos/seed/ff7/400/300.webp"),
        Game("6", "Street Fighter II", 14.99, "Arcade", 10, "El mejor juego de lucha", "https://picsum.photos/seed/streetfighter/400/300.webp"),
        Game("7", "Minecraft", 26.99, "Aventura", 25, "Construye tu mundo", "https://picsum.photos/seed/minecraft/400/300.webp"),
        Game("8", "Call of Duty", 59.99, "Acción", 7, "Acción militar intensa", "https://picsum.photos/seed/cod/400/300.webp"),
        Game("9", "FIFA 24", 69.99, "Deportes", 18, "El mejor fútbol virtual", "https://picsum.photos/seed/fifa/400/300.webp"),
        Game("10", "The Witcher 3", 39.99, "RPG", 6, "Aventura de Geralt de Rivia", "https://picsum.photos/seed/witcher/400/300.webp"),
        Game("11", "Overwatch 2", 39.99, "Acción", 14, "Shooter por equipos", "https://picsum.photos/seed/overwatch/400/300.webp"),
        Game("12", "Cyberpunk 2077", 59.99, "RPG", 9, "Futuro cyberpunk", "https://picsum.photos/seed/cyberpunk/400/300.webp"),
        Game("13", "Red Dead Redemption 2", 49.99, "Aventura", 11, "Western épico", "https://picsum.photos/seed/rdr2/400/300.webp"),
        Game("14", "Among Us", 4.99, "Arcade", 30, "Encuentra al impostor", "https://picsum.photos/seed/amongus/400/300.webp"),
        Game("15", "Valorant", 0.0, "Acción", 100, "Shooter táctico gratis", "https://picsum.photos/seed/valorant/400/300.webp"),
        Game("16", "Assassin's Creed Valhalla", 59.99, "Aventura", 13, "Aventura vikinga", "https://picsum.photos/seed/acvalhalla/400/300.webp")
    )
    val query by searchViewModel.query.collectAsState()
    
    // categorías disponibles
    val categories = listOf("Todos", "Plataformas", "Aventura", "RPG", "Arcade", "Acción", "Deportes")
    var selectedCategory by remember { mutableStateOf("Todos") }

    val games = allGames.filter { game ->
        val matchesCategory = selectedCategory == "Todos" || game.category == selectedCategory
        val matchesQuery = query.isBlank() || game.name.contains(query, ignoreCase = true)
        matchesCategory && matchesQuery
    }

    // Usar diseño adaptativo según el tipo de dispositivo
    when (windowInfo.deviceType) {
        DeviceType.PHONE_PORTRAIT, DeviceType.PHONE_LANDSCAPE -> {
            PhoneGamesLayout(
                games = games,
                categories = categories,
                selectedCategory = selectedCategory,
                onCategorySelect = { selectedCategory = it },
                nav = nav,
                cartViewModel = cartViewModel,
                windowInfo = windowInfo
            )
        }
        DeviceType.TABLET_PORTRAIT, DeviceType.TABLET_LANDSCAPE, DeviceType.DESKTOP -> {
            TabletGamesLayout(
                games = games,
                categories = categories,
                selectedCategory = selectedCategory,
                onCategorySelect = { selectedCategory = it },
                nav = nav,
                cartViewModel = cartViewModel,
                windowInfo = windowInfo
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PhoneGamesLayout(
    games: List<Game>,
    categories: List<String>,
    selectedCategory: String,
    onCategorySelect: (String) -> Unit,
    nav: NavHostController,
    cartViewModel: com.example.uinavegacion.viewmodel.CartViewModel,
    windowInfo: WindowInfo
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(AdaptiveUtils.getHorizontalPadding(windowInfo)),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            GamesHeader(
                categories = categories,
                selectedCategory = selectedCategory,
                onCategorySelect = onCategorySelect,
                windowInfo = windowInfo
            )
        }

        items(games) { game ->
            GameListItem(
                game = game,
                nav = nav,
                cartViewModel = cartViewModel,
                windowInfo = windowInfo
            )
        }

        item {
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Total de juegos: ${games.size}",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TabletGamesLayout(
    games: List<Game>,
    categories: List<String>,
    selectedCategory: String,
    onCategorySelect: (String) -> Unit,
    nav: NavHostController,
    cartViewModel: com.example.uinavegacion.viewmodel.CartViewModel,
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
            GamesHeader(
                categories = categories,
                selectedCategory = selectedCategory,
                onCategorySelect = onCategorySelect,
                windowInfo = windowInfo
            )
            
            // Usar grid para tablets
            LazyVerticalGrid(
                columns = GridCells.Fixed(AdaptiveUtils.getGridColumns(windowInfo)),
                verticalArrangement = Arrangement.spacedBy(AdaptiveUtils.getItemSpacing(windowInfo)),
                horizontalArrangement = Arrangement.spacedBy(AdaptiveUtils.getItemSpacing(windowInfo)),
                modifier = Modifier.fillMaxSize()
            ) {
                items(games) { game ->
                    GameGridItem(
                        game = game,
                        nav = nav,
                        cartViewModel = cartViewModel,
                        windowInfo = windowInfo
                    )
                }
                
                // Spacer item al final
                item {
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun GamesHeader(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelect: (String) -> Unit,
    windowInfo: WindowInfo
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Título principal
        Text(
            text = "Catálogo de Juegos",
            style = if (windowInfo.isTablet) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Descubre nuestra colección de videojuegos",
            style = if (windowInfo.isTablet) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Spacer(Modifier.height(16.dp))

        // Fila de chips de categorías adaptativa
        if (windowInfo.isTablet && windowInfo.isLandscape) {
            // Para tablets en landscape, usar dos filas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                categories.take(4).forEach { cat ->
                    val selected = cat == selectedCategory
                    FilterChip(
                        selected = selected,
                        onClick = { onCategorySelect(cat) },
                        label = { 
                            Text(
                                cat,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            ) 
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                categories.drop(4).forEach { cat ->
                    val selected = cat == selectedCategory
                    FilterChip(
                        selected = selected,
                        onClick = { onCategorySelect(cat) },
                        label = { 
                            Text(
                                cat,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            ) 
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
        } else {
            // Para teléfonos y tablets en portrait, usar scroll horizontal
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 4.dp)
            ) {
                categories.forEach { cat ->
                    val selected = cat == selectedCategory
                    FilterChip(
                        selected = selected,
                        onClick = { onCategorySelect(cat) },
                        label = { 
                            Text(
                                cat,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            ) 
                        },
                        modifier = Modifier.padding(end = 8.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GameListItem(
    game: Game,
    nav: NavHostController,
    cartViewModel: com.example.uinavegacion.viewmodel.CartViewModel,
    windowInfo: WindowInfo
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        onClick = { nav.navigate(Route.GameDetail.build(game.id)) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (windowInfo.isTablet) 20.dp else 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen del juego con AsyncImage
            Box(
                modifier = Modifier
                    .size(if (windowInfo.isTablet) 120.dp else 80.dp)
                    .clip(RoundedCornerShape(if (windowInfo.isTablet) 12.dp else 8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
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
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Precio destacado en esquina
                Card(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Text(
                        text = if (game.price == 0.0) "Gratis" else "$${game.price}",
                        style = if (windowInfo.isTablet) MaterialTheme.typography.bodySmall else MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = if (windowInfo.isTablet) 6.dp else 4.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(Modifier.width(if (windowInfo.isTablet) 20.dp else 12.dp))

            // Información del juego
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = game.name,
                    style = if (windowInfo.isTablet) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = if (windowInfo.isTablet) 2 else 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(if (windowInfo.isTablet) 6.dp else 4.dp))
                
                if (windowInfo.isTablet) {
                    Text(
                        text = game.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(6.dp))
                }
                
                // Categoría en chip
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    modifier = Modifier.wrapContentWidth()
                ) {
                    Text(
                        text = game.category,
                        style = if (windowInfo.isTablet) MaterialTheme.typography.bodySmall else MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = if (windowInfo.isTablet) 8.dp else 6.dp, vertical = if (windowInfo.isTablet) 4.dp else 2.dp)
                    )
                }
                
                if (!windowInfo.isTablet) {
                    Spacer(Modifier.height(4.dp))
                    // Stock compacto para móviles
                    Text(
                        text = "Stock: ${game.stock}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (game.stock > 5) MaterialTheme.colorScheme.primary 
                               else MaterialTheme.colorScheme.error
                    )
                } else {
                    Spacer(Modifier.height(8.dp))
                    // Stock con indicador visual para tablets
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Stock: ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${game.stock}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (game.stock > 5) MaterialTheme.colorScheme.primary 
                                   else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Botón de acción optimizado
            Button(
                onClick = {
                    if (game.stock > 0 && !cartViewModel.isInCart(game.id)) {
                        cartViewModel.addGame(game.id, game.name, game.price)
                    }
                },
                enabled = game.stock > 0 && !cartViewModel.isInCart(game.id),
                modifier = Modifier
                    .height(if (windowInfo.isTablet) 56.dp else 40.dp)
                    .widthIn(min = if (windowInfo.isTablet) 200.dp else 100.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (cartViewModel.isInCart(game.id)) 
                        MaterialTheme.colorScheme.secondary 
                    else MaterialTheme.colorScheme.primary
                ),
                contentPadding = PaddingValues(
                    horizontal = if (windowInfo.isTablet) 16.dp else 8.dp,
                    vertical = if (windowInfo.isTablet) 12.dp else 8.dp
                )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (cartViewModel.isInCart(game.id)) 
                            Icons.Default.Done 
                        else Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(if (windowInfo.isTablet) 20.dp else 18.dp)
                    )
                    if (windowInfo.isTablet) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            if (cartViewModel.isInCart(game.id)) "En Carrito"
                            else if (game.stock > 0) "Agregar"
                            else "Sin Stock",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            if (cartViewModel.isInCart(game.id)) "✓"
                            else "+",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GameGridItem(
    game: Game,
    nav: NavHostController,
    cartViewModel: com.example.uinavegacion.viewmodel.CartViewModel,
    windowInfo: WindowInfo
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.75f), // Proporción 3:4 para las cards
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        onClick = { nav.navigate(Route.GameDetail.build(game.id)) }
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Imagen del juego con AsyncImage
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.6f)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
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
                        modifier = Modifier.size(60.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Precio destacado en esquina
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
                        text = if (game.price == 0.0) "Gratis" else "$${game.price}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            // Información del juego
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.4f)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = game.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = game.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
                
                // Botón de acción compacto con icono
                Button(
                    onClick = {
                        if (game.stock > 0 && !cartViewModel.isInCart(game.id)) {
                            cartViewModel.addGame(game.id, game.name, game.price)
                        }
                    },
                    enabled = game.stock > 0 && !cartViewModel.isInCart(game.id),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (windowInfo.isTablet) 44.dp else 36.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (cartViewModel.isInCart(game.id)) 
                            MaterialTheme.colorScheme.secondary 
                        else MaterialTheme.colorScheme.primary
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = if (cartViewModel.isInCart(game.id)) 
                                Icons.Default.Done 
                            else Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (cartViewModel.isInCart(game.id)) "✓ Añadido"
                            else if (game.stock > 0) "Agregar"
                            else "Agotado",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}