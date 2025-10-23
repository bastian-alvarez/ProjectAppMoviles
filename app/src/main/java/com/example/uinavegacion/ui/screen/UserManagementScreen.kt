package com.example.uinavegacion.ui.screen

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
import java.text.SimpleDateFormat
import java.util.*

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
    
    // Estado para el diálogo de confirmación
    var showBlockDialog by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<UserEntity?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Gestión de Usuarios",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        viewModel.refreshUsers()
                    }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Actualizar"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Estadísticas rápidas
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "${users.size}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            "Total Usuarios",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "${users.count { it.profilePhotoUri != null }}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            "Con Foto de Perfil",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
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
                            UserManagementItem(
                                user = user,
                                onBlock = { 
                                    selectedUser = user
                                    showBlockDialog = true
                                },
                                onViewDetails = { 
                                    // TODO: Ver detalles del usuario
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
        
        // Diálogo de confirmación de bloqueo
        if (showBlockDialog && selectedUser != null) {
            AlertDialog(
                onDismissRequest = { showBlockDialog = false },
                title = { 
                    Text(
                        if (selectedUser!!.isBlocked) "Desbloquear Usuario" 
                        else "Bloquear Usuario"
                    ) 
                },
                text = {
                    Text(
                        if (selectedUser!!.isBlocked) {
                            "¿Estás seguro de que deseas desbloquear a ${selectedUser!!.name}? El usuario podrá volver a acceder a la aplicación."
                        } else {
                            "¿Estás seguro de que deseas bloquear a ${selectedUser!!.name}? El usuario no podrá acceder a la aplicación hasta que sea desbloqueado."
                        }
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
                            containerColor = if (selectedUser!!.isBlocked) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(if (selectedUser!!.isBlocked) "Desbloquear" else "Bloquear")
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
    }
}

@Composable
private fun UserManagementItem(
    user: UserEntity,
    onBlock: () -> Unit,
    onViewDetails: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (user.isBlocked) 
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar del usuario
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .then(
                        if (user.profilePhotoUri != null) {
                            Modifier // TODO: Cargar imagen del usuario
                        } else {
                            Modifier.background(
                                if (user.isBlocked) 
                                    MaterialTheme.colorScheme.error
                                else 
                                    MaterialTheme.colorScheme.primary,
                                CircleShape
                            )
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (user.profilePhotoUri == null) {
                    if (user.isBlocked) {
                        Icon(
                            Icons.Default.Block,
                            contentDescription = "Bloqueado",
                            tint = MaterialTheme.colorScheme.onError
                        )
                    } else {
                        Text(
                            user.name.take(1).uppercase(),
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Información del usuario
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        user.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (user.isBlocked) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.error,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "BLOQUEADO",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onError,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Text(
                    user.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (user.phone.isNotEmpty()) {
                    Text(
                        "Tel: ${user.phone}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Acciones
            Row {
                IconButton(
                    onClick = onViewDetails,
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        Icons.Default.Visibility,
                        contentDescription = "Ver detalles"
                    )
                }
                IconButton(
                    onClick = onBlock,
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = if (user.isBlocked) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        if (user.isBlocked) Icons.Default.CheckCircle else Icons.Default.Block,
                        contentDescription = if (user.isBlocked) "Desbloquear usuario" else "Bloquear usuario"
                    )
                }
            }
        }
    }
}