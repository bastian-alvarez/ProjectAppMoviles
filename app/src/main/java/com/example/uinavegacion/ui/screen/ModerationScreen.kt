package com.example.uinavegacion.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.uinavegacion.data.local.database.AppDatabase
import com.example.uinavegacion.data.local.juego.JuegoEntity
import com.example.uinavegacion.data.local.resena.ResenaEntity
import com.example.uinavegacion.data.local.user.UserEntity
import com.example.uinavegacion.data.repository.ResenaRepository
import com.example.uinavegacion.data.repository.UserRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModerationScreen(
    navController: androidx.navigation.NavHostController? = null
) {
    val context = LocalContext.current.applicationContext
    val db = remember { AppDatabase.getInstance(context) }
    val resenaRepository = remember { ResenaRepository(db.resenaDao()) }
    val userRepository = remember { UserRepository(db.userDao()) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var resenas by remember { mutableStateOf<List<ResenaEntity>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var usersMap by remember { mutableStateOf<Map<Long, UserEntity>>(emptyMap()) }
    var juegosMap by remember { mutableStateOf<Map<Long, JuegoEntity>>(emptyMap()) }
    var filterDeleted by remember { mutableStateOf(false) }
    
    // Cargar reseñas
    LaunchedEffect(filterDeleted) {
        isLoading = true
        try {
            resenas = resenaRepository.getAllResenas(includeDeleted = filterDeleted)
            
            // Cargar usuarios para mostrar nombres
            val userIds = resenas.map { it.userId }.distinct()
            usersMap = userIds.associateWith { userId ->
                userRepository.getUserById(userId) ?: UserEntity(
                    id = userId,
                    name = "Usuario Desconocido",
                    email = "",
                    phone = "",
                    password = ""
                )
            }
            
            // Cargar juegos para mostrar nombres
            val juegoDao = db.juegoDao()
            val juegoIds = resenas.map { it.juegoId }.distinct()
            juegosMap = juegoIds.associateWith { juegoId ->
                juegoDao.getById(juegoId) ?: JuegoEntity(
                    id = juegoId,
                    nombre = "Juego Desconocido",
                    descripcion = "",
                    precio = 0.0,
                    stock = 0,
                    desarrollador = "",
                    fechaLanzamiento = "",
                    categoriaId = 0,
                    generoId = 0,
                    activo = false
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("ModerationScreen", "Error cargando reseñas", e)
        } finally {
            isLoading = false
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Moderación de Reseñas", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    if (navController != null) {
                        IconButton(onClick = { navController.navigate(com.example.uinavegacion.navigation.Route.Games.path) }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                },
                actions = {
                    FilterChip(
                        selected = filterDeleted,
                        onClick = { filterDeleted = !filterDeleted },
                        label = { Text(if (filterDeleted) "Mostrar Eliminadas" else "Ocultar Eliminadas") }
                    )
                    Spacer(Modifier.width(8.dp))
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (resenas.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No hay reseñas para moderar",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(resenas) { resena ->
                    ResenaModerationCard(
                        resena = resena,
                        userName = usersMap[resena.userId]?.name ?: "Usuario Desconocido",
                        juegoName = juegosMap[resena.juegoId]?.nombre ?: "Juego Desconocido",
                        onDelete = {
                            scope.launch {
                                val result = resenaRepository.deleteResena(resena.id)
                                if (result.isSuccess) {
                                    // Recargar reseñas
                                    resenas = resenaRepository.getAllResenas(includeDeleted = filterDeleted)
                                    snackbarHostState.showSnackbar(
                                        "Reseña eliminada",
                                        duration = SnackbarDuration.Short
                                    )
                                } else {
                                    snackbarHostState.showSnackbar(
                                        "Error al eliminar reseña",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        },
                        onRestore = {
                            scope.launch {
                                val result = resenaRepository.restoreResena(resena.id)
                                if (result.isSuccess) {
                                    // Recargar reseñas
                                    resenas = resenaRepository.getAllResenas(includeDeleted = filterDeleted)
                                    snackbarHostState.showSnackbar(
                                        "Reseña restaurada",
                                        duration = SnackbarDuration.Short
                                    )
                                } else {
                                    snackbarHostState.showSnackbar(
                                        "Error al restaurar reseña",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ResenaModerationCard(
    resena: ResenaEntity,
    userName: String,
    juegoName: String,
    onDelete: () -> Unit,
    onRestore: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (resena.isDeleted) 
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header con usuario y fecha
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = userName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = juegoName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (resena.isDeleted) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "ELIMINADA",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            // Calificación con estrellas
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 1..5) {
                    Icon(
                        if (i <= resena.calificacion) Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = null,
                        tint = if (i <= resena.calificacion) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "${resena.calificacion}/5",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(Modifier.height(12.dp))
            
            // Comentario
            Text(
                text = resena.comentario,
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(Modifier.height(16.dp))
            
            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (resena.isDeleted) {
                    Button(
                        onClick = onRestore,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            Icons.Default.Restore,
                            contentDescription = "Restaurar",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Restaurar")
                    }
                } else {
                    Button(
                        onClick = onDelete,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Eliminar")
                    }
                }
            }
        }
    }
}

