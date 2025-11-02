package com.example.uinavegacion.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.uinavegacion.data.local.database.AppDatabase
import com.example.uinavegacion.data.repository.GameRepository
import com.example.uinavegacion.navigation.*
import com.example.uinavegacion.ui.model.toGame
import com.example.uinavegacion.ui.viewmodel.GameCatalogViewModel
import com.example.uinavegacion.ui.viewmodel.GameCatalogViewModelFactory
import com.example.uinavegacion.viewmodel.CartViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable  
fun GameDetailScreen(nav: NavHostController, gameId: String, cartViewModel: CartViewModel = viewModel()) {
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
    val snackbarHostState = remember { SnackbarHostState() }
    val successMessage by cartViewModel.successMessage.collectAsState()
    
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
    
    // Buscar el juego desde la BD
    val catalogGame = catalogGames.find { it.id.toString() == gameId }
    
    // Si no se encuentra el juego, mostrar mensaje de error y volver
    if (catalogGame == null) {
        LaunchedEffect(Unit) {
            nav.popBackStack()
        }
        return
    }
    
    val game = catalogGame.toGame()
    val cartItems by cartViewModel.items.collectAsState()
    val currentQuantity = cartItems.find { it.id == game.id }?.quantity ?: 0

    Scaffold(
        topBar = { 
            TopAppBar(
                title = { Text(game.name, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) 
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Imagen del juego
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                coil.compose.AsyncImage(
                    model = game.imageUrl.ifEmpty { "https://via.placeholder.com/400x200?text=🎮" },
                    contentDescription = game.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            }

            // Información principal
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = game.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    
                    // Precio con descuento si aplica
                    if (game.hasDiscount) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "$${String.format("%.2f", game.price)}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                fontWeight = FontWeight.Normal,
                                textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                            )
                            Text(
                                text = "$${String.format("%.2f", game.discountedPrice)}",
                                style = MaterialTheme.typography.headlineSmall,
                                color = androidx.compose.ui.graphics.Color(0xFF27AE60),
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                color = androidx.compose.ui.graphics.Color(0xFFE74C3C),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "-${game.discount}%",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = androidx.compose.ui.graphics.Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    } else {
                        Text(
                            text = if (game.price == 0.0) "Gratis" else "$${String.format("%.2f", game.price)}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Stock disponible: ${(game.stock - currentQuantity).coerceAtLeast(0)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (game.stock > 5) MaterialTheme.colorScheme.onSurfaceVariant 
                               else MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = game.description,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            // Detalles del juego
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Detalles del Juego",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Categoría:", fontWeight = FontWeight.Medium)
                        Text(game.category)
                    }
                    Spacer(Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Precio:", fontWeight = FontWeight.Medium)
                        if (game.hasDiscount) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$${String.format("%.2f", game.price)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                )
                                Text(
                                    text = "$${String.format("%.2f", game.discountedPrice)}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = androidx.compose.ui.graphics.Color(0xFF27AE60),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Text("$${String.format("%.2f", game.price)}")
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Stock:", fontWeight = FontWeight.Medium)
                        Text("${(game.stock - currentQuantity).coerceAtLeast(0)} unidades")
                    }
                }
            }

            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val currentQuantity = cartViewModel.getQuantity(game.id)
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
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        when {
                            game.stock <= currentQuantity -> "Sin stock"
                            currentQuantity > 0 -> "Agregar más"
                            else -> "Agregar al Carrito"
                        }
                    )
                }
                
                Button(
                    onClick = { nav.navigate(Route.Cart.path) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Ver Carrito")
                }
            }
        }
    }
}