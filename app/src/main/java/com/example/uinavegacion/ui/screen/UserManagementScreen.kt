package com.example.uinavegacion.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.uinavegacion.data.local.database.AppDatabase
import com.example.uinavegacion.data.local.user.UserEntity
import com.example.uinavegacion.data.repository.UserRepository
import com.example.uinavegacion.ui.viewmodel.UserManagementViewModel
import com.example.uinavegacion.ui.viewmodel.UserManagementViewModelFactory

// 🎨 Colores del tema Admin (azul oscuro profesional)
private val AdminDarkBlue = Color(0xFF0D1B2A)
private val AdminMediumBlue = Color(0xFF1B263B)
private val AdminLightBlue = Color(0xFF415A77)
private val AdminAccentBlue = Color(0xFF778DA9)
private val AdminBrightBlue = Color(0xFF4A90E2)
private val AdminCyan = Color(0xFF00D9FF)
private val AdminGreen = Color(0xFF00E676)
private val AdminRed = Color(0xFFFF5252)

/**
 * Pantalla de gestión de usuarios para administradores
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(navController: NavHostController) {
    // Configurar ViewModel con dependencias
    val context = LocalContext.current.applicationContext
    val db = remember { AppDatabase.getInstance(context) }
    val userRepository = remember { UserRepository(db.userDao()) }
    
    val viewModel: UserManagementViewModel = viewModel(
        factory = UserManagementViewModelFactory(userRepository)
    )
    
    // Observar estados
    val users by viewModel.users.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    
    // Refrescar usuarios cada vez que se abre la pantalla
    LaunchedEffect(Unit) {
        viewModel.onScreenResumed()
    }
    
    // Estado para los diálogos de confirmación
    var showBlockDialog by remember { mutableStateOf(false) }
    var showUnblockDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<UserEntity?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Gestión de Usuarios",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        viewModel.refreshUsers()
                    }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Actualizar",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AdminMediumBlue
                )
            )
        },
        containerColor = AdminDarkBlue
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(AdminDarkBlue, AdminMediumBlue)
                    )
                )
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Estadísticas modernas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    value = "${users.size}",
                    label = "Total",
                    containerColor = AdminBrightBlue,
                    contentColor = Color.White,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    value = "${users.count { !it.isBlocked }}",
                    label = "Activos",
                    containerColor = AdminGreen,
                    contentColor = Color.White,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    value = "${users.count { it.isBlocked }}",
                    label = "Bloqueados",
                    containerColor = AdminRed,
                    contentColor = Color.White,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Lista de usuarios
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Cargando usuarios...")
                        }
                    }
                }
                
                error != null -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                error!!,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { viewModel.refreshUsers() }
                            ) {
                                Text("Reintentar")
                            }
                        }
                    }
                }
                
                users.isEmpty() -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.People,
                                contentDescription = "Sin usuarios",
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "No hay usuarios registrados",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "Los usuarios aparecerán aquí cuando se registren",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(users) { user ->
                            CompactUserItem(
                                user = user,
                                onToggleBlock = { 
                                    selectedUser = user
                                    if (user.isBlocked) {
                                        showUnblockDialog = true
                                    } else {
                                        showBlockDialog = true
                                    }
                                },
                                onDelete = {
                                    selectedUser = user
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }
                }
            }
            
            // Mostrar mensaje de éxito
            successMessage?.let { message ->
                LaunchedEffect(message) {
                    kotlinx.coroutines.delay(3000)
                    viewModel.clearMessages()
                }
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.clearMessages() }) {
                            Text("OK")
                        }
                    }
                ) {
                    Text(message)
                }
            }
        }
        
        // Diálogo de confirmación de BLOQUEO
        if (showBlockDialog && selectedUser != null) {
            AlertDialog(
                onDismissRequest = { 
                    showBlockDialog = false
                    selectedUser = null
                },
                icon = {
                    Icon(
                        Icons.Default.Block,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )
                },
                title = { 
                    Text(
                        "Bloquear Usuario",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                text = {
                    Text(
                        "¿Estás seguro de que deseas bloquear a ${selectedUser!!.name}?\n\n" +
                        "El usuario no podrá acceder a la aplicación hasta que sea desbloqueado por un administrador."
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.toggleUserBlockStatus(
                                selectedUser!!.id,
                                selectedUser!!.isBlocked
                            )
                            showBlockDialog = false
                            selectedUser = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Bloquear")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { 
                        showBlockDialog = false
                        selectedUser = null
                    }) {
                        Text("Cancelar")
                    }
                }
            )
        }
        
        // Diálogo de confirmación de DESBLOQUEO
        if (showUnblockDialog && selectedUser != null) {
            AlertDialog(
                onDismissRequest = { 
                    showUnblockDialog = false
                    selectedUser = null
                },
                icon = {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                },
                title = { 
                    Text(
                        "Desbloquear Usuario",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                text = {
                    Text(
                        "¿Estás seguro de que deseas desbloquear a ${selectedUser!!.name}?\n\n" +
                        "El usuario podrá volver a acceder a la aplicación normalmente."
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.toggleUserBlockStatus(
                                selectedUser!!.id,
                                selectedUser!!.isBlocked
                            )
                            showUnblockDialog = false
                            selectedUser = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Desbloquear")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { 
                        showUnblockDialog = false
                        selectedUser = null
                    }) {
                        Text("Cancelar")
                    }
                }
            )
        }
        
        // Diálogo de confirmación de ELIMINACIÓN
        if (showDeleteDialog && selectedUser != null) {
            AlertDialog(
                onDismissRequest = { 
                    showDeleteDialog = false
                    selectedUser = null
                },
                icon = {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )
                },
                title = { 
                    Text(
                        "Eliminar Usuario",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                text = {
                    Text(
                        "¿Estás seguro de que deseas eliminar a ${selectedUser!!.name}?\n\n" +
                        "⚠️ Esta acción es PERMANENTE y no se puede deshacer. " +
                        "Se eliminarán todos los datos asociados al usuario."
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteUser(
                                selectedUser!!.id,
                                selectedUser!!.name
                            )
                            showDeleteDialog = false
                            selectedUser = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { 
                        showDeleteDialog = false
                        selectedUser = null
                    }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Composable
private fun StatCard(
    value: String,
    label: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
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
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = contentColor
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.95f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun CompactUserItem(
    user: UserEntity,
    onToggleBlock: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (user.isBlocked) 
                AdminRed.copy(alpha = 0.2f)
            else 
                AdminMediumBlue
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = if (user.isBlocked) {
                            listOf(AdminRed.copy(alpha = 0.3f), AdminRed.copy(alpha = 0.2f))
                        } else {
                            listOf(AdminMediumBlue, AdminLightBlue.copy(alpha = 0.8f))
                        }
                    )
                )
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar moderno
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .shadow(3.dp, CircleShape)
                    .clip(CircleShape)
                    .background(
                        if (user.isBlocked) 
                            AdminRed
                        else 
                            AdminBrightBlue,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (user.profilePhotoUri == null) {
                    if (user.isBlocked) {
                        Icon(
                            Icons.Default.Block,
                            contentDescription = "Bloqueado",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    } else {
                        Text(
                            user.name.take(1).uppercase(),
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Información moderna
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        user.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = Color.White,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Surface(
                        color = if (user.isBlocked) 
                            AdminRed 
                        else 
                            AdminGreen,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            if (user.isBlocked) "BLOQUEADO" else "ACTIVO",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Default.Email,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = AdminAccentBlue
                    )
                    Text(
                        user.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AdminAccentBlue,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Botones de acción modernos
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Botón de Bloquear/Desbloquear
                Button(
                    onClick = onToggleBlock,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (user.isBlocked) 
                            AdminGreen
                        else 
                            AdminRed,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(40.dp)
                ) {
                    Icon(
                        if (user.isBlocked) Icons.Default.CheckCircle else Icons.Default.Block,
                        contentDescription = if (user.isBlocked) "Desbloquear" else "Bloquear",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        if (user.isBlocked) "Desbloquear" else "Bloquear",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Botón de Eliminar
                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AdminDarkBlue,
                        contentColor = AdminRed
                    ),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, AdminRed),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Eliminar",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}