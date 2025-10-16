package com.example.uinavegacion.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
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
fun LibraryScreen(nav: NavHostController) {
    val allGames = listOf(
        GameData("Cyberpunk 2077", "RPG", "2024-01-15", "Instalado", 89.99),
        GameData("The Witcher 3", "RPG", "2024-01-10", "Instalado", 29.99),
        GameData("Minecraft", "Sandbox", "2024-01-20", "Descargando", 19.99),
        GameData("Among Us", "Multijugador", "2024-01-25", "Disponible", 4.99),
        GameData("Valorant", "FPS", "2024-01-12", "Instalado", 0.0),
        GameData("Fortnite", "Battle Royale", "2024-01-18", "Actualizando", 0.0)
    )
    
    var selectedFilter by remember { mutableStateOf("Todos") }
    
    val games = when (selectedFilter) {
        "Instalados" -> allGames.filter { it.status == "Instalado" }
        "Disponibles" -> allGames.filter { it.status == "Disponible" }
        "Descargando" -> allGames.filter { it.status == "Descargando" || it.status == "Actualizando" }
        else -> allGames
    }
    
    Scaffold(
        topBar = { 
            TopAppBar(
                title = { Text("Mi Biblioteca", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    IconButton(onClick = { nav.navigate(Route.Home.path) }) {
                        Icon(
                            Icons.Default.Home,
                            contentDescription = "Inicio",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            ) 
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Estadísticas de la biblioteca
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "${games.size}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            "Total Juegos",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "${games.count { it.status == "Instalado" }}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            "Instalados",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "${games.count { it.status == "Disponible" }}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            "Disponibles",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            if (games.isEmpty()) {
                // Estado vacío
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🎮",
                            style = MaterialTheme.typography.displayLarge
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "Tu biblioteca está vacía",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Compra algunos juegos para comenzar tu colección",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { nav.navigate(Route.Games.path) }
                        ) {
                            Text("Explorar Juegos")
                        }
                    }
                }
            } else {
                // Filtros rápidos
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Filtrar por estado:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                FilterChip(
                                    onClick = { selectedFilter = "Todos" },
                                    label = { Text("Todos") },
                                    selected = selectedFilter == "Todos"
                                )
                            }
                            item {
                                FilterChip(
                                    onClick = { selectedFilter = "Instalados" },
                                    label = { Text("Instalados") },
                                    selected = selectedFilter == "Instalados"
                                )
                            }
                            item {
                                FilterChip(
                                    onClick = { selectedFilter = "Disponibles" },
                                    label = { Text("Disponibles") },
                                    selected = selectedFilter == "Disponibles"
                                )
                            }
                            item {
                                FilterChip(
                                    onClick = { selectedFilter = "Descargando" },
                                    label = { Text("Descargando") },
                                    selected = selectedFilter == "Descargando"
                                )
                            }
                        }
                    }
                }

                // Lista de juegos
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(games) { game ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Imagen del juego
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.secondaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "🎮",
                                        style = MaterialTheme.typography.headlineMedium
                                    )
                                }

                                Spacer(Modifier.width(12.dp))

                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = game.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = game.genre,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = "Agregado: ${game.dateAdded}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Column(
                                    horizontalAlignment = Alignment.End
                                ) {
                                    // Estado del juego
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = when (game.status) {
                                                "Instalado" -> MaterialTheme.colorScheme.primaryContainer
                                                "Descargando" -> MaterialTheme.colorScheme.secondaryContainer
                                                "Actualizando" -> MaterialTheme.colorScheme.tertiaryContainer
                                                else -> MaterialTheme.colorScheme.surfaceVariant
                                            }
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            text = game.status,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium,
                                            color = when (game.status) {
                                                "Instalado" -> MaterialTheme.colorScheme.onPrimaryContainer
                                                "Descargando" -> MaterialTheme.colorScheme.onSecondaryContainer
                                                "Actualizando" -> MaterialTheme.colorScheme.onTertiaryContainer
                                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                                            },
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                    
                                    Spacer(Modifier.height(8.dp))
                                    
                                    // Precio
                                    if (game.price > 0) {
                                        Text(
                                            text = "$${String.format("%.2f", game.price)}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    } else {
                                        Text(
                                            text = "Gratis",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                            
                            // Barra de progreso para descargas/actualizaciones
                            if (game.status == "Descargando" || game.status == "Actualizando") {
                                LinearProgressIndicator(
                                    progress = 0.7f,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

data class GameData(
    val name: String,
    val genre: String,
    val dateAdded: String,
    val status: String,
    val price: Double
)
