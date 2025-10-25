package com.example.uinavegacion.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.uinavegacion.data.local.database.AppDatabase
import com.example.uinavegacion.data.local.juego.JuegoEntity
import com.example.uinavegacion.data.local.categoria.CategoriaEntity
import com.example.uinavegacion.data.local.genero.GeneroEntity
import com.example.uinavegacion.data.repository.GameRepository
import com.example.uinavegacion.ui.viewmodel.GameManagementViewModel
import com.example.uinavegacion.ui.viewmodel.GameManagementViewModelFactory

/**
 * Pantalla de gestión de juegos para administradores
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameManagementScreen(navController: NavHostController) {
    // Configurar ViewModel con dependencias
    val context = LocalContext.current
    val applicationContext = context.applicationContext
    val db = remember { AppDatabase.getInstance(applicationContext) }
    val gameRepository = remember { GameRepository(db.juegoDao()) }
    
    val viewModel: GameManagementViewModel = viewModel(
        factory = GameManagementViewModelFactory(gameRepository)
    )
    
    // Estado para controlar si ya se intentó cargar datos
    var dataInitialized by remember { mutableStateOf(false) }
    
    // Función para forzar recreación de datos
    val forceResetDatabase = suspend {
        try {
            android.util.Log.d("GameManagementScreen", "🔄 FORZANDO RECREACIÓN COMPLETA DE BASE DE DATOS")
            
            // Primero, asegurar que existan categorías y géneros
            try {
                android.util.Log.d("GameManagementScreen", "🏷️ Verificando categorías y géneros...")
                
                // Verificar si categoría ID 1 existe, si no, crear una
                val categoriaExiste = try {
                    db.categoriaDao().getById(1L) != null
                } catch (e: Exception) {
                    false
                }
                
                if (!categoriaExiste) {
                    val categoria = CategoriaEntity(
                        nombre = "Videojuegos",
                        descripcion = "Categoría general para videojuegos"
                    )
                    try {
                        val catId = db.categoriaDao().insert(categoria)
                        android.util.Log.d("GameManagementScreen", "✅ Categoría creada con ID: $catId")
                    } catch (e: Exception) {
                        android.util.Log.e("GameManagementScreen", "❌ Error creando categoría: ${e.message}")
                    }
                } else {
                    android.util.Log.d("GameManagementScreen", "ℹ️ Categoría ya existe")
                }
                
                // Verificar si género ID 1 existe, si no, crear uno
                val generoExiste = try {
                    db.generoDao().getById(1L) != null
                } catch (e: Exception) {
                    false
                }
                
                if (!generoExiste) {
                    val genero = GeneroEntity(
                        nombre = "Acción",
                        descripcion = "Juegos de acción y aventura"
                    )
                    try {
                        val genId = db.generoDao().insert(genero)
                        android.util.Log.d("GameManagementScreen", "✅ Género creado con ID: $genId")
                    } catch (e: Exception) {
                        android.util.Log.e("GameManagementScreen", "❌ Error creando género: ${e.message}")
                    }
                } else {
                    android.util.Log.d("GameManagementScreen", "ℹ️ Género ya existe")
                }
                
            } catch (e: Exception) {
                android.util.Log.e("GameManagementScreen", "❌ Error creando categorías/géneros: ${e.message}")
            }
            
            // Eliminar juegos existentes si los hay
            try {
                val existingGames = db.juegoDao().getAll()
                existingGames.forEach { game ->
                    db.juegoDao().delete(game.id)
                    android.util.Log.d("GameManagementScreen", "🗑️ Eliminado: ${game.nombre}")
                }
            } catch (e: Exception) {
                android.util.Log.w("GameManagementScreen", "No hay juegos previos para eliminar")
            }
            
            android.util.Log.d("GameManagementScreen", "💾 INSERTANDO CATÁLOGO COMPLETO...")
            
            val juegosSeed = listOf(
                JuegoEntity(id = 0, nombre = "Super Mario Bros", precio = 29.99, imagenUrl = null, descripcion = "El clásico juego de plataformas de Nintendo", stock = 15, desarrollador = "Nintendo", fechaLanzamiento = "1985", categoriaId = 1, generoId = 1),
                JuegoEntity(id = 0, nombre = "The Legend of Zelda", precio = 39.99, imagenUrl = null, descripcion = "Épica aventura en el reino de Hyrule", stock = 8, desarrollador = "Nintendo", fechaLanzamiento = "1986", categoriaId = 1, generoId = 1),
                JuegoEntity(id = 0, nombre = "Minecraft", precio = 26.99, imagenUrl = null, descripcion = "Construye y explora mundos infinitos", stock = 25, desarrollador = "Mojang", fechaLanzamiento = "2011", categoriaId = 1, generoId = 1),
                JuegoEntity(id = 0, nombre = "Call of Duty", precio = 59.99, imagenUrl = null, descripcion = "Acción militar intensa", stock = 12, desarrollador = "Activision", fechaLanzamiento = "2019", categoriaId = 1, generoId = 1),
                JuegoEntity(id = 0, nombre = "FIFA 24", precio = 69.99, imagenUrl = null, descripcion = "El mejor simulador de fútbol", stock = 18, desarrollador = "EA Sports", fechaLanzamiento = "2023", categoriaId = 1, generoId = 1),
                JuegoEntity(id = 0, nombre = "Pokémon Red", precio = 24.99, imagenUrl = null, descripcion = "Conviértete en maestro Pokémon", stock = 20, desarrollador = "Game Freak", fechaLanzamiento = "1996", categoriaId = 1, generoId = 1),
                JuegoEntity(id = 0, nombre = "The Witcher 3", precio = 39.99, imagenUrl = null, descripcion = "Aventura épica de Geralt de Rivia", stock = 6, desarrollador = "CD Projekt RED", fechaLanzamiento = "2015", categoriaId = 1, generoId = 1),
                JuegoEntity(id = 0, nombre = "Grand Theft Auto V", precio = 29.99, imagenUrl = null, descripcion = "Mundo abierto criminal", stock = 22, desarrollador = "Rockstar Games", fechaLanzamiento = "2013", categoriaId = 1, generoId = 1)
            )
            
            var insertedCount = 0
            juegosSeed.forEach { juego ->
                try {
                    val id = db.juegoDao().insert(juego)
                    insertedCount++
                    android.util.Log.d("GameManagementScreen", "✅ [$insertedCount] ${juego.nombre} insertado con ID: $id")
                } catch (e: Exception) {
                    android.util.Log.e("GameManagementScreen", "❌ Error insertando ${juego.nombre}: ${e.message}")
                }
            }
            
            val finalCount = db.juegoDao().count()
            android.util.Log.d("GameManagementScreen", "� RESULTADO FINAL: $finalCount juegos en base de datos")
            
            if (finalCount > 0) {
                android.util.Log.d("GameManagementScreen", "🎉 ¡ÉXITO! Base de datos inicializada correctamente")
                dataInitialized = true
            } else {
                android.util.Log.e("GameManagementScreen", "💥 ERROR: No se pudo insertar ningún juego")
            }
            
        } catch (e: Exception) {
            android.util.Log.e("GameManagementScreen", "💥 ERROR CRÍTICO en recreación de BD", e)
        }
    }
    
    // Inicialización automática
    LaunchedEffect(Unit) {
        if (!dataInitialized) {
            forceResetDatabase()
            kotlinx.coroutines.delay(1000) // Más tiempo para asegurar que todo se guarde
            viewModel.refreshGames()
        }
    }
    
    // Observar estados
    val games by viewModel.games.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    
    // Snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Mostrar mensajes
    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Long
            )
            viewModel.clearMessages()
        }
    }
    
    LaunchedEffect(successMessage) {
        successMessage?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            viewModel.clearMessages()
        }
    }
    
    // Estado para diálogo
    var showDialog by remember { mutableStateOf(false) }
    var gameToEdit by remember { mutableStateOf<JuegoEntity?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var gameToDelete by remember { mutableStateOf<JuegoEntity?>(null) }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Gestión de Juegos",
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
                        gameToEdit = null
                        showDialog = true
                    }) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Agregar juego"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    gameToEdit = null
                    showDialog = true
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar juego")
            }
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
                            "${games.size}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            "Total Juegos",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "${games.sumOf { it.stock }}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            "Stock Total",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Lista de juegos
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Cargando juegos...")
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
                                onClick = { 
                                    scope.launch {
                                        forceResetDatabase()
                                        kotlinx.coroutines.delay(1000)
                                        viewModel.refreshGames()
                                    }
                                }
                            ) {
                                Text("Reintentar")
                            }
                        }
                    }
                }
                
                games.isEmpty() -> {
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
                                Icons.Default.Games,
                                contentDescription = "Sin juegos",
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "No hay juegos en el catálogo",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "Agrega tu primer juego para comenzar",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { 
                                    scope.launch {
                                        dataInitialized = false // Reset para forzar inicialización
                                        forceResetDatabase()
                                        kotlinx.coroutines.delay(1000)
                                        viewModel.refreshGames()
                                    }
                                }
                            ) {
                                Text("Cargar Datos de Prueba")
                            }
                        }
                    }
                }
                
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(games) { game ->
                            GameManagementItem(
                                game = game,
                                onEdit = { 
                                    gameToEdit = game
                                    showDialog = true
                                },
                                onDelete = { 
                                    gameToDelete = game
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Diálogo para agregar/editar juego
    if (showDialog) {
        AddEditGameDialog(
            game = gameToEdit,
            onDismiss = { showDialog = false },
            onSave = { nombre, descripcion, precio, stock, imageUrl ->
                if (gameToEdit != null) {
                    viewModel.updateGame(
                        gameToEdit!!.copy(
                            nombre = nombre,
                            descripcion = descripcion,
                            precio = precio,
                            stock = stock,
                            imagenUrl = imageUrl.ifEmpty { null }
                        )
                    )
                } else {
                    viewModel.addGame(nombre, descripcion, precio, stock, imageUrl)
                }
                showDialog = false
            }
        )
    }
    
    // Diálogo de confirmación de eliminación
    if (showDeleteDialog && gameToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar Juego") },
            text = { Text("¿Estás seguro de que deseas eliminar '${gameToDelete!!.nombre}'? Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteGame(gameToDelete!!.id)
                        showDeleteDialog = false
                        gameToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showDeleteDialog = false
                    gameToDelete = null
                }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditGameDialog(
    game: JuegoEntity?,
    onDismiss: () -> Unit,
    onSave: (String, String, Double, Int, String) -> Unit
) {
    var nombre by remember { mutableStateOf(game?.nombre ?: "") }
    var descripcion by remember { mutableStateOf(game?.descripcion ?: "") }
    var precio by remember { mutableStateOf(game?.precio?.toString() ?: "") }
    var stock by remember { mutableStateOf(game?.stock?.toString() ?: "") }
    var imageUrl by remember { mutableStateOf(game?.imagenUrl ?: "") }
    
    // Validaciones en tiempo real
    val isNombreValid = nombre.isNotBlank()
    val isPrecioValid = precio.toDoubleOrNull()?.let { it > 0 } ?: false
    val isStockValid = stock.toIntOrNull()?.let { it >= 0 } ?: false
    val isFormValid = isNombreValid && isPrecioValid && isStockValid
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (game == null) Icons.Default.Add else Icons.Default.Edit,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    if (game == null) "Agregar Juego" else "Editar Juego",
                    style = MaterialTheme.typography.titleLarge
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Nombre del juego
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre del juego *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = !isNombreValid && nombre.isNotEmpty(),
                    supportingText = if (!isNombreValid && nombre.isNotEmpty()) {
                        { Text("El nombre no puede estar vacío", color = MaterialTheme.colorScheme.error) }
                    } else null,
                    leadingIcon = {
                        Icon(Icons.Default.Games, contentDescription = null)
                    }
                )
                
                // Descripción
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    minLines = 2,
                    placeholder = { Text("Descripción del juego...") },
                    leadingIcon = {
                        Icon(Icons.Default.Description, contentDescription = null)
                    }
                )
                
                // Precio
                OutlinedTextField(
                    value = precio,
                    onValueChange = { newValue ->
                        // Solo permitir números y punto decimal
                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                            precio = newValue
                        }
                    },
                    label = { Text("Precio *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    prefix = { Text("$") },
                    isError = !isPrecioValid && precio.isNotEmpty(),
                    supportingText = if (!isPrecioValid && precio.isNotEmpty()) {
                        { Text("El precio debe ser mayor a 0", color = MaterialTheme.colorScheme.error) }
                    } else null,
                    leadingIcon = {
                        Icon(Icons.Default.AttachMoney, contentDescription = null)
                    },
                    placeholder = { Text("0.00") }
                )
                
                // Stock
                OutlinedTextField(
                    value = stock,
                    onValueChange = { newValue ->
                        // Solo permitir números enteros
                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d+$"))) {
                            stock = newValue
                        }
                    },
                    label = { Text("Stock *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = !isStockValid && stock.isNotEmpty(),
                    supportingText = if (!isStockValid && stock.isNotEmpty()) {
                        { Text("El stock debe ser 0 o mayor", color = MaterialTheme.colorScheme.error) }
                    } else null,
                    leadingIcon = {
                        Icon(Icons.Default.Inventory, contentDescription = null)
                    },
                    placeholder = { Text("0") }
                )
                
                // URL de imagen
                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("URL de imagen (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("https://ejemplo.com/imagen.webp") },
                    leadingIcon = {
                        Icon(Icons.Default.Image, contentDescription = null)
                    }
                )
                
                // Indicador de campos obligatorios
                Text(
                    text = "* Campos obligatorios",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val precioDouble = precio.toDoubleOrNull() ?: 0.0
                    val stockInt = stock.toIntOrNull() ?: 0
                    onSave(nombre.trim(), descripcion.trim(), precioDouble, stockInt, imageUrl.trim())
                },
                enabled = isFormValid
            ) {
                Icon(
                    imageVector = if (game == null) Icons.Default.Add else Icons.Default.Save,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(if (game == null) "Agregar" else "Guardar")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun GameManagementItem(
    game: JuegoEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Información del juego
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    game.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    game.descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    Text(
                        "$${game.precio}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        "Stock: ${game.stock}",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (game.stock > 0) 
                            MaterialTheme.colorScheme.onSurfaceVariant 
                        else 
                            MaterialTheme.colorScheme.error
                    )
                }
            }
            
            // Acciones
            Row {
                IconButton(
                    onClick = onEdit,
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Editar"
                    )
                }
                IconButton(
                    onClick = onDelete,
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar"
                    )
                }
            }
        }
    }
}