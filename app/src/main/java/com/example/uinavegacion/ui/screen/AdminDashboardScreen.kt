package com.example.uinavegacion.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.uinavegacion.navigation.Route
import com.example.uinavegacion.data.local.database.AppDatabase
import com.example.uinavegacion.data.repository.AdminStatsRepository
import com.example.uinavegacion.data.repository.GameRepository
import com.example.uinavegacion.ui.viewmodel.AdminDashboardViewModel
import com.example.uinavegacion.ui.viewmodel.AdminDashboardViewModelFactory
import com.example.uinavegacion.ui.viewmodel.GameCatalogViewModel
import com.example.uinavegacion.ui.viewmodel.GameCatalogViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(navController: NavHostController) {
    // Configurar ViewModel con dependencias
    val context = LocalContext.current.applicationContext
    val db = remember { AppDatabase.getInstance(context) }
    val adminStatsRepository = remember { 
        AdminStatsRepository(
            userDao = db.userDao(),
            juegoDao = db.juegoDao(),
            ordenCompraDao = db.ordenCompraDao(),
            adminDao = db.adminDao()
        )
    }
    val gameRepository = remember { GameRepository(db.juegoDao()) }
    
    val viewModel: AdminDashboardViewModel = viewModel(
        factory = AdminDashboardViewModelFactory(adminStatsRepository)
    )
    val gameCatalogViewModel: GameCatalogViewModel = viewModel(
        factory = GameCatalogViewModelFactory(
            gameRepository = gameRepository,
            categoriaDao = db.categoriaDao(),
            generoDao = db.generoDao()
        )
    )
    
    // Observar estados
    val dashboardStats by viewModel.dashboardStats.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val catalogGames by gameCatalogViewModel.games.collectAsState()
    val gamesLoading by gameCatalogViewModel.isLoading.collectAsState()
    val realtimeGamesCount = catalogGames.count { it.activo }
    val combinedGamesLoading = isLoading || gamesLoading
    
    // Mostrar error si existe
    LaunchedEffect(error) {
        error?.let {
            android.util.Log.e("AdminDashboard", "Error: $it")
        }
    }
    
    // Refrescar estadísticas manualmente al entrar por primera vez
    LaunchedEffect(Unit) {
        try {
            viewModel.refreshStats()
        } catch (e: Exception) {
            android.util.Log.e("AdminDashboard", "Error al refrescar estadísticas", e)
        }
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header compacto
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.AdminPanelSettings,
                        contentDescription = "Admin",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Column {
                        Text(
                            text = "Panel Admin",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Gestión de plataforma",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
                IconButton(
                    onClick = { navController.navigate(Route.Home.path) },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.ExitToApp,
                        contentDescription = "Salir",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Estadísticas en grid compacto 2x2
        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Usuarios
                    StatCard(
                        title = "Usuarios",
                        value = if (isLoading) "..." else "${dashboardStats.totalUsers}",
                        icon = Icons.Default.People,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Juegos
                    StatCard(
                        title = "Juegos",
                        value = if (combinedGamesLoading) "..." else "$realtimeGamesCount",
                        icon = Icons.Default.Games,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Órdenes
                    StatCard(
                        title = "Órdenes",
                        value = if (isLoading) "..." else "${dashboardStats.totalOrders}",
                        icon = Icons.Default.ShoppingCart,
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Admins
                    StatCard(
                        title = "Admins",
                        value = if (isLoading) "..." else "${dashboardStats.totalAdmins}",
                        icon = Icons.Default.Security,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Acciones rápidas
        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Acciones Rápidas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                )
                
                // Gestión de juegos
                ActionCard(
                    title = "Gestionar Juegos",
                    subtitle = "Catálogo completo",
                    icon = Icons.Default.Games,
                    onClick = { navController.navigate(Route.AdminGames.path) }
                )
                
                // Gestión de usuarios
                ActionCard(
                    title = "Gestionar Usuarios",
                    subtitle = "Lista de usuarios",
                    icon = Icons.Default.People,
                    onClick = { navController.navigate(Route.AdminUsers.path) }
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = title,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun ActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Ir",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
