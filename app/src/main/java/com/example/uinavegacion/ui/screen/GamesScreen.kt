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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
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
import com.example.uinavegacion.ui.utils.GameImages
import com.example.uinavegacion.data.local.database.AppDatabase
import com.example.uinavegacion.data.repository.GameRepository
import com.example.uinavegacion.ui.model.Game
import com.example.uinavegacion.ui.model.toGame
import com.example.uinavegacion.ui.viewmodel.GameCatalogViewModel
import com.example.uinavegacion.ui.viewmodel.GameCatalogViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamesScreen(
    nav: NavHostController, 
    searchViewModel: SearchViewModel = viewModel(), 
    cartViewModel: com.example.uinavegacion.viewmodel.CartViewModel = viewModel(),
    initialCategory: String? = null
) {
    val windowInfo = rememberWindowInfo()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val errorMessage by cartViewModel.errorMessage.collectAsState()
    val successMessage by cartViewModel.successMessage.collectAsState()
    val context = LocalContext.current.applicationContext
    val db = remember { AppDatabase.getInstance(context) }
    val gameRepository = remember { GameRepository(db.juegoDao()) }
    val catalogViewModel: GameCatalogViewModel = viewModel(
        factory = GameCatalogViewModelFactory(
            gameRepository = gameRepository,
            categoriaDao = db.categoriaDao(),
            generoDao = db.generoDao()
        )
    )
    val catalogGames by catalogViewModel.games.collectAsState()
    val isLoadingCatalog by catalogViewModel.isLoading.collectAsState()
    val catalogCategories by catalogViewModel.categories.collectAsState()
    
    // Mostrar Snackbar cuando hay error
    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            cartViewModel.clearErrorMessage()
        }
    }
    
    // Mostrar Snackbar cuando se agrega al carrito
    LaunchedEffect(successMessage) {
        successMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            cartViewModel.clearSuccessMessage()
        }
    }
    
    val allGames = catalogGames.map { it.toGame() }
    val query by searchViewModel.query.collectAsState()
    
    val availableCategories = catalogCategories.ifEmpty { listOf("General") }
    val categories = listOf("Todos") + availableCategories
    var selectedCategory by remember { mutableStateOf(initialCategory ?: "Todos") }

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
                windowInfo = windowInfo,
                snackbarHostState = snackbarHostState,
                isLoading = isLoadingCatalog
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
                windowInfo = windowInfo,
                snackbarHostState = snackbarHostState,
                isLoading = isLoadingCatalog
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
    windowInfo: WindowInfo,
    snackbarHostState: SnackbarHostState,
    isLoading: Boolean
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = AdaptiveUtils.getHorizontalPadding(windowInfo)),
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

            when {
                isLoading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }

                games.isEmpty() -> {
                    item {
                        Text(
                            text = "No hay juegos disponibles",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> {
                    items(games) { game ->
                GameListItem(
                    game = game,
                    nav = nav,
                    cartViewModel = cartViewModel,
                    windowInfo = windowInfo
                )
                    }

                    item {
                        Text(
                            text = "Total de juegos: ${games.size}",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                        )
                    }
                }
            }
        }
        
        // SnackbarHost sin Scaffold
        Box(
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            SnackbarHost(snackbarHostState)
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
    windowInfo: WindowInfo,
    snackbarHostState: SnackbarHostState,
    isLoading: Boolean
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
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                games.isEmpty() -> {
                    Text(
                        text = "No hay juegos disponibles",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                else -> {
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
                    }
                }
            }
        }
        
        // SnackbarHost sin Scaffold
        Box(
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            SnackbarHost(snackbarHostState)
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
    val cartItems by cartViewModel.items.collectAsState()
    val currentQuantity = cartItems.find { it.id == game.id }?.quantity ?: 0
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
                AsyncImage(
                    model = game.imageUrl.ifEmpty { GameImages.getDefaultImage() },
                    contentDescription = game.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Precio destacado en esquina
                Card(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (game.hasDiscount) androidx.compose.ui.graphics.Color(0xFF27AE60) else MaterialTheme.colorScheme.primary
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = if (windowInfo.isTablet) 6.dp else 4.dp, vertical = 2.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        if (game.hasDiscount) {
                            Text(
                                text = "$${String.format("%.2f", game.price)}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Normal,
                                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.7f),
                                textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                            )
                        }
                        Text(
                            text = if (game.price == 0.0) "Gratis" else "$${String.format("%.2f", game.discountedPrice)}",
                            style = if (windowInfo.isTablet) MaterialTheme.typography.bodySmall else MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = androidx.compose.ui.graphics.Color.White
                        )
                    }
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
                    val remainingStock = (game.stock - currentQuantity).coerceAtLeast(0)
                    Text(
                        text = "Stock: $remainingStock",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (remainingStock > 5) MaterialTheme.colorScheme.primary 
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
                        val remainingTabletStock = (game.stock - currentQuantity).coerceAtLeast(0)
                        Text(
                            text = "$remainingTabletStock",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (remainingTabletStock > 5) MaterialTheme.colorScheme.primary 
                                   else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Botón de acción optimizado
            val canAddMore = game.stock > currentQuantity && cartViewModel.getTotalItems() < com.example.uinavegacion.viewmodel.CartViewModel.MAX_LICENSES_PER_PURCHASE
            Button(
                onClick = {
                    if (canAddMore) {
                        cartViewModel.addGame(
                            id = game.id,
                            name = game.name,
                            price = game.discountedPrice,
                            imageUrl = game.imageUrl,
                            originalPrice = if (game.hasDiscount) game.price else null,
                            discount = game.discount,
                            maxStock = game.stock
                        )
                    }
                },
                enabled = canAddMore,
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
                        imageVector = if (canAddMore.not() && currentQuantity > 0) Icons.Default.Done else Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(if (windowInfo.isTablet) 20.dp else 18.dp)
                    )
                    if (windowInfo.isTablet) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            when {
                                game.stock <= currentQuantity -> "Sin stock"
                                currentQuantity > 0 -> "Agregar"
                                else -> "Agregar"
                            },
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            if (game.stock <= currentQuantity) "-" else "+",
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
    val cartItems by cartViewModel.items.collectAsState()
    val currentQuantity = cartItems.find { it.id == game.id }?.quantity ?: 0
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
                AsyncImage(
                    model = game.imageUrl.ifEmpty { GameImages.getDefaultImage() },
                    contentDescription = game.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Precio destacado en esquina
                Card(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (game.hasDiscount) androidx.compose.ui.graphics.Color(0xFF27AE60) else MaterialTheme.colorScheme.primary
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
                                fontWeight = FontWeight.Normal,
                                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.7f),
                                textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                            )
                        }
                        Text(
                            text = if (game.price == 0.0) "Gratis" else "$${String.format("%.2f", game.discountedPrice)}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = androidx.compose.ui.graphics.Color.White
                        )
                    }
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
                val canAddMore = game.stock > currentQuantity && cartViewModel.getTotalItems() < com.example.uinavegacion.viewmodel.CartViewModel.MAX_LICENSES_PER_PURCHASE
                Button(
                    onClick = {
                        if (canAddMore) {
                            cartViewModel.addGame(
                                id = game.id,
                                name = game.name,
                                price = game.discountedPrice,
                                imageUrl = game.imageUrl,
                                originalPrice = if (game.hasDiscount) game.price else null,
                                discount = game.discount,
                                maxStock = game.stock
                            )
                        }
                    },
                    enabled = canAddMore,
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
                            imageVector = if (game.stock <= currentQuantity) Icons.Default.Done else Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = when {
                                game.stock <= currentQuantity -> "Sin stock"
                                currentQuantity > 0 -> "Agregar"
                                else -> "Agregar"
                            },
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