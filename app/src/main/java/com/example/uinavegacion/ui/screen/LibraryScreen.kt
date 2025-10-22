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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.uinavegacion.viewmodel.LibraryViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.uinavegacion.navigation.*
import com.example.uinavegacion.ui.utils.rememberWindowInfo
import com.example.uinavegacion.ui.utils.AdaptiveUtils
import com.example.uinavegacion.ui.components.AnimatedButton
import com.example.uinavegacion.ui.components.AnimatedOutlinedButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(nav: NavHostController, libraryViewModel: LibraryViewModel = viewModel()) {
    val games by libraryViewModel.games.collectAsState()
    val libraryStats = libraryViewModel.getLibraryStats()
    var selectedFilter by remember { mutableStateOf("Todos") }
    
    val filteredGames = libraryViewModel.getGamesByStatus(selectedFilter)
    
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
        val windowInfo = rememberWindowInfo()

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
                            "${libraryStats.totalGames}",
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
                            "${libraryStats.installedGames}",
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
                            "${libraryStats.availableGames}",
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
                // Estado vacío — centrar en tablets
                if (windowInfo.isTablet) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier
                                .widthIn(max = AdaptiveUtils.getMaxContentWidth(windowInfo))
                                .padding(AdaptiveUtils.getHorizontalPadding(windowInfo)),
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
                                AnimatedButton(
                                    onClick = { nav.navigate(Route.Games.path) }
                                ) {
                                    Text("Explorar Juegos")
                                }
                            }
                        }
                    }
                } else {
                    // Mobile / phone behavior (unchanged)
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
                    items(filteredGames) { game ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Imagen del juego
                                        AnimatedButton(
                                            onClick = { libraryViewModel.installGame(game.id) },
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Download,
                                                contentDescription = "Instalar",
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Text("Instalar", style = MaterialTheme.typography.bodySmall)
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
                                    
                                    Spacer(Modifier.height(8.dp))
                                    
                                    // Botón de acción según el estado
                                    when (game.status) {
                                        "Disponible" -> {
                                            OutlinedButton(
                                                onClick = { libraryViewModel.installGame(game.id) },
                                                modifier = Modifier.height(32.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Download,
                                                    contentDescription = "Instalar",
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(Modifier.width(4.dp))
                                                Text("Instalar", style = MaterialTheme.typography.bodySmall)
                                            }
                                        }
                                        "Instalado" -> {
                                            Button(
                                                onClick = { /* Abrir juego */ },
                                                modifier = Modifier.height(32.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.PlayArrow,
                                                    contentDescription = "Jugar",
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(Modifier.width(4.dp))
                                                Text("Jugar", style = MaterialTheme.typography.bodySmall)
                                            }
                                        }
                                        "Descargando" -> {
                                            AnimatedOutlinedButton(
                                                onClick = { /* Pausar descarga */ },
                                                modifier = Modifier.height(32.dp),
                                                enabled = false
                                            ) {
                                                Icon(
                                                    Icons.Default.Pause,
                                                    contentDescription = "Descargando",
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(Modifier.width(4.dp))
                                                Text("Descargando", style = MaterialTheme.typography.bodySmall)
                                            }
                                        }
                                        "Actualizando" -> {
                                            AnimatedOutlinedButton(
                                                onClick = { /* Pausar actualización */ },
                                                modifier = Modifier.height(32.dp),
                                                enabled = false
                                            ) {
                                                Icon(
                                                    Icons.Default.Update,
                                                    contentDescription = "Actualizando",
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(Modifier.width(4.dp))
                                                Text("Actualizando", style = MaterialTheme.typography.bodySmall)
                                            }
                                        }
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
