package com.example.uinavegacion.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.uinavegacion.viewmodel.CartViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.uinavegacion.navigation.*
import com.example.uinavegacion.ui.utils.GameImages

@OptIn(ExperimentalMaterial3Api::class)
@Composable  
fun GameDetailScreen(nav: NavHostController, gameId: String, cartViewModel: CartViewModel = viewModel()) {
    
    // Modelo del juego con descripción e imagen
    data class Game(
        val id: String, 
        val name: String, 
        val price: Double, 
        val category: String, 
        val stock: Int,
        val description: String = "Descripción del juego",
        val imageUrl: String = "",
        val discount: Int = 0
    ) {
        val discountedPrice: Double
            get() = if (discount > 0) price * (1 - discount / 100.0) else price
        
        val hasDiscount: Boolean
            get() = discount > 0
    }

    // Lista de juegos con imágenes WebP optimizadas (misma que en GamesScreen)
    val games = listOf(
        Game("1",  "Super Mario Bros",            29.99, "Plataformas", 15,  "El clásico juego de plataformas",     "https://tudominio.com/imagenes/super_mario_bros.webp"),
        Game("2",  "The Legend of Zelda",         39.99, "Aventura",    8,   "Épica aventura en Hyrule",            "https://tudominio.com/imagenes/the_legend_of_zelda.webp"),
        Game("3",  "Pokémon Red",                 24.99, "RPG",         20,  "Conviértete en maestro Pokémon",      "https://tudominio.com/imagenes/pokemon_red.webp"),
        Game("4",  "Sonic the Hedgehog",          19.99, "Plataformas", 12,  "Velocidad supersónica",               "https://tudominio.com/imagenes/sonic_the_hedgehog.webp"),
        Game("5",  "Final Fantasy VII",           49.99, "RPG",         5,   "RPG épico de Square Enix",            "https://tudominio.com/imagenes/final_fantasy_vii.webp", discount = 20),
        Game("6",  "Street Fighter II",           14.99, "Arcade",      10,  "El mejor juego de lucha",             "https://tudominio.com/imagenes/street_fighter_ii.webp"),
        Game("7",  "Minecraft",                   26.99, "Aventura",    25,  "Construye tu mundo",                  "https://tudominio.com/imagenes/minecraft.webp", discount = 20),
        Game("8",  "Call of Duty Modern Warfare", 59.99, "Acción",      7,   "Acción militar intensa",              "https://tudominio.com/imagenes/cod_modern_warfare.webp"),
        Game("9",  "FIFA 24",                     69.99, "Deportes",    18,  "El mejor fútbol virtual",             "https://tudominio.com/imagenes/fifa_24.webp"),
        Game("10", "The Witcher 3 Wild Hunt",     39.99, "RPG",         6,   "Aventura de Geralt de Rivia",         "https://tudominio.com/imagenes/witcher_3.webp"),
        Game("11", "Overwatch 2",                 39.99, "Acción",      14,  "Shooter por equipos",                 "https://tudominio.com/imagenes/overwatch_2.webp"),
        Game("12", "Cyberpunk 2077",              59.99, "RPG",         9,   "Futuro cyberpunk",                    "https://tudominio.com/imagenes/cyberpunk_2077.webp"),
        Game("13", "Red Dead Redemption 2",       49.99, "Aventura",    11,  "Western épico",                       "https://tudominio.com/imagenes/red_dead_redemption_2.webp"),
        Game("14", "Among Us",                    4.99,  "Arcade",      30,  "Encuentra al impostor",               "https://tudominio.com/imagenes/among_us.webp"),
        Game("15", "Valorant",                    19.99, "Acción",      100, "Shooter táctico",                     "https://tudominio.com/imagenes/valorant.webp"),
        Game("16", "Assassin's Creed Valhalla",   59.99, "Aventura",    13,  "Aventura vikinga",                    "https://tudominio.com/imagenes/assassins_creed_valhalla.webp"),
        Game("17", "Fortnite",                    0.0,   "Acción",      100, "Battle Royale",                       "https://tudominio.com/imagenes/fortnite.webp"),
        Game("18", "Dark Souls III",              39.99, "RPG",         8,   "Desafío extremo",                     "https://tudominio.com/imagenes/dark_souls_iii.webp", discount = 20),
        Game("19", "Grand Theft Auto V",          29.99, "Acción",      22,  "Mundo abierto épico",                 "https://tudominio.com/imagenes/gta_v.webp"),
        Game("20", "Elden Ring",                  59.99, "RPG",         10,  "Obra maestra de FromSoftware",        "https://tudominio.com/imagenes/elden_ring.webp", discount = 20)
    )

    val game = games.find { it.id == gameId } ?: games[0]
    val isInCart by remember { derivedStateOf { cartViewModel.isInCart(gameId) } }

    Scaffold(
        topBar = { 
            TopAppBar(
                title = { Text(game.name, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) 
        }
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
                Text("🎮", style = MaterialTheme.typography.displayLarge)
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
                        text = "Stock disponible: ${game.stock}",
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
                        Text("${game.stock} unidades")
                    }
                }
            }

            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        if (game.stock > 0) {
                            cartViewModel.addGame(
                                id = game.id,
                                name = game.name,
                                price = game.discountedPrice,
                                imageUrl = GameImages.getDefaultImage(),
                                originalPrice = if (game.hasDiscount) game.price else null,
                                discount = game.discount
                            )
                        }
                    },
                    enabled = game.stock > 0 && !isInCart,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        if (isInCart) "En Carrito" 
                        else if (game.stock > 0) "Agregar al Carrito"
                        else "Sin Stock"
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