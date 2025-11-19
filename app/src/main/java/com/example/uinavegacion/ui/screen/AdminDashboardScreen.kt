package com.example.uinavegacion.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
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
import com.example.uinavegacion.data.SessionManager
import com.example.uinavegacion.ui.viewmodel.AdminDashboardViewModel
import com.example.uinavegacion.ui.viewmodel.AdminDashboardViewModelFactory
import com.example.uinavegacion.ui.viewmodel.GameCatalogViewModel
import com.example.uinavegacion.ui.viewmodel.GameCatalogViewModelFactory

import com.example.uinavegacion.ui.theme.AppColors

// Usar colores centralizados
private val AdminDarkBlue = AppColors.DarkBlue
private val AdminMediumBlue = AppColors.MediumBlue
private val AdminLightBlue = AppColors.LightBlue
private val AdminAccentBlue = AppColors.AccentBlue
private val AdminBrightBlue = AppColors.BrightBlue
private val AdminCyan = AppColors.Cyan

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
            // ordenCompraDao = db.ordenCompraDao(),  // DESHABILITADO
            adminDao = db.adminDao()
        )
    }
    val gameRepository = remember { GameRepository(db.juegoDao()) }
    
    val viewModel: AdminDashboardViewModel = viewModel(
        factory = AdminDashboardViewModelFactory(adminStatsRepository)
    )
    val gameCatalogViewModel: GameCatalogViewModel = viewModel(
        factory = GameCatalogViewModelFactory(
            gameRepository = gameRepository
            // categoriaDao = db.categoriaDao(),  // DESHABILITADO
            // generoDao = db.generoDao()  // DESHABILITADO
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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(AdminDarkBlue, AdminMediumBlue)
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header moderno con gradiente
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = AdminMediumBlue
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(AdminMediumBlue, AdminLightBlue)
                                )
                            )
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(AdminBrightBlue, RoundedCornerShape(14.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.AdminPanelSettings,
                                    contentDescription = "Admin",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Panel Administrativo",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Sistema de Gestión Integral",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = AdminCyan
                                )
                            }
                        }
                        IconButton(
                            onClick = { navController.navigate(Route.Home.path) },
                            modifier = Modifier
                                .size(48.dp)
                                .background(AdminBrightBlue.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        ) {
                            Icon(
                                Icons.Default.ExitToApp,
                                contentDescription = "Salir",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            // Estadísticas en grid moderno 2x2
            item {
                Text(
                    text = "Estadísticas del Sistema",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Usuarios
                        StatCard(
                            title = "Usuarios",
                            value = if (isLoading) "..." else "${dashboardStats.totalUsers}",
                            icon = Icons.Default.People,
                            containerColor = AdminBrightBlue,
                            contentColor = Color.White,
                            modifier = Modifier.weight(1f)
                        )
                        
                        // Juegos
                        StatCard(
                            title = "Juegos",
                            value = if (combinedGamesLoading) "..." else "$realtimeGamesCount",
                            icon = Icons.Default.Games,
                            containerColor = Color(0xFF6A5ACD),
                            contentColor = Color.White,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Órdenes
                        StatCard(
                            title = "Órdenes",
                            value = if (isLoading) "..." else "${dashboardStats.totalOrders}",
                            icon = Icons.Default.ShoppingCart,
                            containerColor = Color(0xFF1E88E5),
                            contentColor = Color.White,
                            modifier = Modifier.weight(1f)
                        )
                        
                        // Admins
                        StatCard(
                            title = "Admins",
                            value = if (isLoading) "..." else "${dashboardStats.totalAdmins}",
                            icon = Icons.Default.Security,
                            containerColor = AdminLightBlue,
                            contentColor = Color.White,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Acciones rápidas
            item {
                Text(
                    text = "Acciones Rápidas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Gestión de juegos
                    ActionCard(
                        title = "Gestionar Juegos",
                        subtitle = "Catálogo completo del sistema",
                        icon = Icons.Default.Games,
                        onClick = { navController.navigate(Route.AdminGames.path) }
                    )
                    
                    // Gestión de usuarios
                    ActionCard(
                        title = "Gestionar Usuarios",
                        subtitle = "Control de usuarios registrados",
                        icon = Icons.Default.People,
                        onClick = { navController.navigate(Route.AdminUsers.path) }
                    )
                    
                    // Moderación de reseñas (solo para moderadores)
                    if (SessionManager.isModerator()) {
                        ActionCard(
                            title = "Moderar Reseñas",
                            subtitle = "Revisar y eliminar contenido",
                            icon = Icons.Default.Edit,
                            onClick = { navController.navigate(Route.Moderation.path) }
                        )
                    }
                
                    // Sincronización de datos con microservicios
                    var showSyncDialog by remember { mutableStateOf(false) }
                    var syncMessage by remember { mutableStateOf("") }
                    var isSyncing by remember { mutableStateOf(false) }
                    
                    // Verificar si ya está sincronizado
                    val alreadySynced = remember { com.example.uinavegacion.data.SyncPreferences.areGamesSynced(context) }
                    
                    ActionCard(
                        title = if (alreadySynced) "Re-sincronizar Datos" else "Sincronizar Datos",
                        subtitle = if (alreadySynced) "Volver a exportar datos al servidor" else "Exportar juegos al microservicio",
                        icon = Icons.Default.Sync,
                        onClick = { showSyncDialog = true }
                    )
                
                if (showSyncDialog) {
                    AlertDialog(
                        onDismissRequest = { if (!isSyncing) showSyncDialog = false },
                        title = { Text(if (alreadySynced) "Re-sincronizar Juegos" else "Sincronizar Juegos") },
                        text = {
                            Column {
                                if (isSyncing) {
                                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Exportando juegos al microservicio...")
                                } else if (syncMessage.isNotEmpty()) {
                                    Text(syncMessage)
                                } else {
                                    if (alreadySynced) {
                                        Text("Los juegos ya fueron sincronizados anteriormente.\n\n¿Deseas volver a exportarlos? Esto puede crear duplicados si no limpiaste la base de datos remota.")
                                    } else {
                                        Text("¿Deseas exportar todos los juegos locales al microservicio de Laragon?\n\nEsto creará los juegos en la base de datos remota.")
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            if (!isSyncing && syncMessage.isEmpty()) {
                                Button(
                                    onClick = {
                                        isSyncing = true
                                        // Resetear el estado de sincronización si ya estaba sincronizado
                                        if (alreadySynced) {
                                            com.example.uinavegacion.data.SyncPreferences.resetSyncState(context)
                                        }
                                        CoroutineScope(Dispatchers.IO).launch {
                                            val result = gameRepository.exportLocalGamesToRemote()
                                            withContext(Dispatchers.Main) {
                                                isSyncing = false
                                                syncMessage = result.getOrElse { "Error: ${it.message}" }
                                                // Marcar como sincronizado después de éxito
                                                if (result.isSuccess) {
                                                    com.example.uinavegacion.data.SyncPreferences.markGamesSynced(context)
                                                }
                                            }
                                        }
                                    }
                                ) {
                                    Text(if (alreadySynced) "Re-exportar" else "Exportar")
                                }
                            }
                        },
                        dismissButton = {
                            if (!isSyncing) {
                                TextButton(onClick = { 
                                    showSyncDialog = false
                                    syncMessage = ""
                                }) {
                                    Text(if (syncMessage.isEmpty()) "Cancelar" else "Cerrar")
                                }
                            }
                        }
                    )
                }
                }
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
        modifier = modifier
            .shadow(6.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            containerColor,
                            containerColor.copy(alpha = 0.85f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(contentColor.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = title,
                        tint = contentColor,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = contentColor
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Medium
                )
            }
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
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(14.dp)),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = AdminMediumBlue
        ),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            AdminMediumBlue,
                            AdminLightBlue.copy(alpha = 0.8f)
                        )
                    )
                )
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(AdminBrightBlue.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = title,
                    tint = AdminCyan,
                    modifier = Modifier.size(26.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = AdminAccentBlue
                )
            }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(AdminBrightBlue.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "Ir",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
