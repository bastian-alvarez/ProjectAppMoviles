package com.example.uinavegacion.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.uinavegacion.viewmodel.SearchViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.uinavegacion.navigation.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamesScreen(nav: NavHostController, searchViewModel: SearchViewModel = viewModel(), cartViewModel: com.example.uinavegacion.viewmodel.CartViewModel = viewModel()) {
    // Modelo simple de juego con stock
    data class Game(
        val id: String, 
        val name: String, 
        val price: Double, 
        val category: String, 
        val stock: Int
    )

    // Lista simple de juegos con stock
    val allGames = listOf(
        Game("1", "Super Mario Bros", 29.99, "Plataformas", 15),
        Game("2", "The Legend of Zelda", 39.99, "Aventura", 8),
        Game("3", "Pokémon Red", 24.99, "RPG", 20),
        Game("4", "Sonic the Hedgehog", 19.99, "Plataformas", 12),
        Game("5", "Final Fantasy VII", 49.99, "RPG", 5),
        Game("6", "Street Fighter II", 14.99, "Arcade", 10),
        Game("7", "Minecraft", 26.99, "Aventura", 25),
        Game("8", "Call of Duty", 59.99, "Acción", 7),
        Game("9", "FIFA 24", 69.99, "Deportes", 18),
        Game("10", "The Witcher 3", 39.99, "RPG", 6)
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Título principal
                Text(
                    text = "Catálogo de Juegos",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Descubre nuestra colección de videojuegos",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(Modifier.height(16.dp))

                // Fila de chips de categorías mejorada
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 4.dp)
                ) {
                    categories.forEach { cat ->
                        val selected = cat == selectedCategory
                        FilterChip(
                            selected = selected,
                            onClick = { selectedCategory = cat },
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
                Spacer(Modifier.height(16.dp))
            }
        }

        items(games) { game ->
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
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Imagen del juego mejorada
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(12.dp))
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
                                text = "$${game.price}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        
                        Text(
                            text = "Juego",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(Modifier.width(20.dp))

                    // Información del juego
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = game.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(6.dp))
                        
                        // Categoría en chip
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            ),
                            modifier = Modifier.wrapContentWidth()
                        ) {
                            Text(
                                text = game.category,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        
                        Spacer(Modifier.height(8.dp))
                        
                        // Stock con indicador visual
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

                    // Botón de acción mejorado
                    Button(
                        onClick = {
                            if (game.stock > 0 && !cartViewModel.isInCart(game.id)) {
                                cartViewModel.addGame(game.id, game.name, game.price)
                            }
                        },
                        enabled = game.stock > 0 && !cartViewModel.isInCart(game.id),
                        modifier = Modifier.height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (cartViewModel.isInCart(game.id)) 
                                MaterialTheme.colorScheme.secondary 
                            else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            if (cartViewModel.isInCart(game.id)) "En Carrito"
                            else if (game.stock > 0) "Agregar"
                            else "Sin Stock",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
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
