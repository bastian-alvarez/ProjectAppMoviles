package com.example.uinavegacion.ui.screen

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import android.widget.Toast

import com.example.uinavegacion.ui.theme.AppColors

// Usar colores centralizados
private val AdminDarkBlue = AppColors.DarkBlue
private val AdminMediumBlue = AppColors.MediumBlue
private val AdminLightBlue = AppColors.LightBlue
private val AdminAccentBlue = AppColors.AccentBlue
private val AdminBrightBlue = AppColors.BrightBlue
private val AdminCyan = AppColors.Cyan
private val AdminPurple = AppColors.Purple

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
    
    // Función para forzar recreación de datos
    val forceResetDatabase = suspend {
        try {
            android.util.Log.d("GameManagementScreen", "🔄 FORZANDO RECREACIÓN COMPLETA DE BASE DE DATOS")
            
            // Primero, asegurar que existan todas las categorías y géneros necesarios
            try {
                android.util.Log.d("GameManagementScreen", "🏷️ Verificando y creando categorías y géneros...")
                
                // Crear las 5 categorías necesarias
                val categoriasSeed = listOf(
                    CategoriaEntity(nombre = "Acción", descripcion = "Juegos de alta intensidad y combate"),
                    CategoriaEntity(nombre = "Aventura", descripcion = "Exploración y narrativa inmersiva"),
                    CategoriaEntity(nombre = "RPG", descripcion = "Juegos de rol y desarrollo de personajes"),
                    CategoriaEntity(nombre = "Deportes", descripcion = "Simulaciones deportivas"),
                    CategoriaEntity(nombre = "Estrategia", descripcion = "Planificación y táctica")
                )
                
                categoriasSeed.forEachIndexed { index, categoria ->
                    try {
                        val existing = db.categoriaDao().getById((index + 1).toLong())
                        if (existing == null) {
                            val catId = db.categoriaDao().insert(categoria)
                            android.util.Log.d("GameManagementScreen", "✅ Categoría ${categoria.nombre} creada con ID: $catId")
                        } else {
                            android.util.Log.d("GameManagementScreen", "ℹ️ Categoría ${categoria.nombre} ya existe (ID: ${existing.id})")
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("GameManagementScreen", "❌ Error con categoría ${categoria.nombre}: ${e.message}")
                    }
                }
                
                // Crear los 5 géneros necesarios
                val generosSeed = listOf(
                    GeneroEntity(nombre = "Plataformas", descripcion = "Juegos de salto y plataformas"),
                    GeneroEntity(nombre = "Shooter", descripcion = "Juegos de disparos"),
                    GeneroEntity(nombre = "Racing", descripcion = "Carreras y velocidad"),
                    GeneroEntity(nombre = "Puzzle", descripcion = "Rompecabezas y lógica"),
                    GeneroEntity(nombre = "MMORPG", descripcion = "Juegos masivos en línea")
                )
                
                generosSeed.forEachIndexed { index, genero ->
                    try {
                        val existing = db.generoDao().getById((index + 1).toLong())
                        if (existing == null) {
                            val genId = db.generoDao().insert(genero)
                            android.util.Log.d("GameManagementScreen", "✅ Género ${genero.nombre} creado con ID: $genId")
                        } else {
                            android.util.Log.d("GameManagementScreen", "ℹ️ Género ${genero.nombre} ya existe (ID: ${existing.id})")
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("GameManagementScreen", "❌ Error con género ${genero.nombre}: ${e.message}")
                    }
                }
                
                android.util.Log.d("GameManagementScreen", "✅ Categorías y géneros verificados/creados")
                
            } catch (e: Exception) {
                android.util.Log.e("GameManagementScreen", "❌ Error creando categorías/géneros: ${e.message}", e)
            }
            
            // Eliminar juegos existentes si los hay
            try {
                android.util.Log.d("GameManagementScreen", "🧹 Eliminando catálogo previo...")
                db.juegoDao().deleteAll()
            } catch (e: Exception) {
                android.util.Log.e("GameManagementScreen", "❌ Error eliminando juegos previos: ${e.message}")
            }
            
            android.util.Log.d("GameManagementScreen", "💾 INSERTANDO CATÁLOGO COMPLETO DE JUEGOS PARA PC...")
            
            // Obtener IDs reales de categorías por nombre
            val categoriaAccion = db.categoriaDao().getByNombre("Acción")
            val categoriaAventura = db.categoriaDao().getByNombre("Aventura")
            val categoriaRPG = db.categoriaDao().getByNombre("RPG")
            val categoriaDeportes = db.categoriaDao().getByNombre("Deportes")
            val categoriaEstrategia = db.categoriaDao().getByNombre("Estrategia")
            
            val categoriasMap = mapOf(
                1L to (categoriaAccion?.id ?: 1L),
                2L to (categoriaAventura?.id ?: 2L),
                3L to (categoriaRPG?.id ?: 3L),
                4L to (categoriaDeportes?.id ?: 4L),
                5L to (categoriaEstrategia?.id ?: 5L)
            )
            
            android.util.Log.d("GameManagementScreen", "📁 IDs de categorías:")
            categoriasMap.forEach { (expected, real) ->
                val nombre = when(expected) {
                    1L -> "Acción"
                    2L -> "Aventura"
                    3L -> "RPG"
                    4L -> "Deportes"
                    5L -> "Estrategia"
                    else -> "Desconocida"
                }
                android.util.Log.d("GameManagementScreen", "  $nombre: ID=$real")
            }
            
            // Juegos para PC: 2 por cada una de las 5 categorías (10 juegos totales)
            val juegosSeed = listOf(
                // Categoría 1: Acción (2 juegos para PC)
                JuegoEntity(id = 0, nombre = "Doom Eternal", precio = 59.99, imagenUrl = null, descripcion = "Plataforma: PC. El infierno ha invadido la Tierra. Conviértete en el Slayer y destruye demonios con un arsenal devastador. Combate rápido y brutal, gráficos impresionantes y una banda sonora épica. La experiencia definitiva de acción en primera persona para PC.", stock = 12, desarrollador = "id Software", fechaLanzamiento = "2020", categoriaId = 1, generoId = 2, activo = true, descuento = 20),
                JuegoEntity(id = 0, nombre = "Counter-Strike 2 - Prime", precio = 14.99, imagenUrl = null, descripcion = "Plataforma: PC. Licencia Prime de Counter-Strike 2. El shooter táctico más competitivo del mundo con acceso a servidores Prime, matchmaking mejorado, drops de cajas exclusivas y protección contra cheaters. Combate 5v5 por rondas con mecánicas de precisión milimétrica. Gráficos mejorados con Source 2 y servidores de 128-tick.", stock = 50, desarrollador = "Valve", fechaLanzamiento = "2023", categoriaId = 1, generoId = 2, activo = true, descuento = 0),
                // Categoría 2: Aventura (2 juegos para PC)
                JuegoEntity(id = 0, nombre = "The Witcher 3: Wild Hunt", precio = 39.99, imagenUrl = null, descripcion = "Plataforma: PC. Acompaña a Geralt de Rivia en una aventura épica de mundo abierto. Explora un continente masivo, toma decisiones que moldean el destino y lucha contra monstruos en combate dinámico. La experiencia definitiva de RPG de acción para PC con mods y gráficos mejorados.", stock = 15, desarrollador = "CD Projekt RED", fechaLanzamiento = "2015", categoriaId = 2, generoId = 2, activo = true, descuento = 30),
                JuegoEntity(id = 0, nombre = "Cyberpunk 2077", precio = 49.99, imagenUrl = null, descripcion = "Plataforma: PC. Sumérgete en Night City, una metrópolis futurista llena de peligro y oportunidades. Personaliza tu personaje, elige tu estilo de juego y vive una historia cinematográfica con decisiones que importan. Optimizado para PC con ray tracing y gráficos de última generación.", stock = 10, desarrollador = "CD Projekt RED", fechaLanzamiento = "2020", categoriaId = 2, generoId = 2, activo = true, descuento = 25),
                // Categoría 3: RPG (2 juegos para PC)
                JuegoEntity(id = 0, nombre = "Baldur's Gate 3", precio = 59.99, imagenUrl = null, descripcion = "Plataforma: PC. El RPG definitivo con combate por turnos basado en D&D 5ª edición. Explora un mundo rico, forma tu propio grupo de aventureros y toma decisiones que cambian el curso de la historia. Múltiples finales, romances y más de 174 horas de contenido. Experiencia completa para PC.", stock = 8, desarrollador = "Larian Studios", fechaLanzamiento = "2023", categoriaId = 3, generoId = 3, activo = true, descuento = 0),
                JuegoEntity(id = 0, nombre = "Divinity: Original Sin 2", precio = 44.99, imagenUrl = null, descripcion = "Plataforma: PC. Un RPG táctico de mundo abierto con combate estratégico por turnos. Crea tu propio héroe, forma un grupo de hasta 4 personajes y explora Rivellon. Sistema de combate innovador, narrativa profunda y mods de la comunidad. La experiencia RPG definitiva para PC.", stock = 11, desarrollador = "Larian Studios", fechaLanzamiento = "2017", categoriaId = 3, generoId = 3, activo = true, descuento = 15),
                // Categoría 4: Deportes (2 juegos para PC)
                JuegoEntity(id = 0, nombre = "FIFA 26 - Gold Edition", precio = 89.99, imagenUrl = null, descripcion = "Plataforma: PC. Edición Gold de FIFA 26. La experiencia de fútbol más completa y realista. Incluye el juego base, Ultimate Team con 4600 FIFA Points, acceso anticipado de 3 días, y contenido exclusivo. Juega con los mejores equipos y jugadores del mundo, incluyendo la UEFA Champions League y la Liga Femenina. Modo Carrera mejorado y gráficos de última generación.", stock = 18, desarrollador = "EA Sports", fechaLanzamiento = "2025", categoriaId = 4, generoId = 4, activo = true, descuento = 15),
                JuegoEntity(id = 0, nombre = "Football Manager 2024", precio = 54.99, imagenUrl = null, descripcion = "Plataforma: PC. La simulación de gestión futbolística más profunda y realista. Toma el control de cualquier club, gestiona tácticas, transferencias y desarrollo de jugadores. Base de datos con más de 800,000 jugadores y personal real. La experiencia definitiva de gestión para PC.", stock = 14, desarrollador = "Sports Interactive", fechaLanzamiento = "2023", categoriaId = 4, generoId = 4, activo = true, descuento = 10),
                // Categoría 5: Estrategia (2 juegos para PC)
                JuegoEntity(id = 0, nombre = "Total War: Warhammer III", precio = 59.99, imagenUrl = null, descripcion = "Plataforma: PC. Combina la estrategia épica de Total War con el mundo de Warhammer. Comanda ejércitos masivos en batallas tácticas, gestiona imperios y conquista el mundo. Múltiples facciones, campañas épicas y combate espectacular. La experiencia de estrategia definitiva para PC.", stock = 9, desarrollador = "Creative Assembly", fechaLanzamiento = "2022", categoriaId = 5, generoId = 5, activo = true, descuento = 0),
                JuegoEntity(id = 0, nombre = "Crusader Kings III", precio = 49.99, imagenUrl = null, descripcion = "Plataforma: PC. Gestiona tu dinastía medieval a través de generaciones. Toma decisiones políticas, militares y diplomáticas que moldean la historia. Sistema de personajes complejo, intrigas cortesanas y expansión territorial. El juego de estrategia y simulación más profundo para PC.", stock = 7, desarrollador = "Paradox Development Studio", fechaLanzamiento = "2020", categoriaId = 5, generoId = 5, activo = true, descuento = 20)
            )
            
            var insertedCount = 0
            juegosSeed.forEach { juego ->
                try {
                    // Obtener el ID real de la categoría
                    val categoriaIdReal = categoriasMap[juego.categoriaId] ?: juego.categoriaId
                    
                    // Crear juego con el ID real de categoría y activo
                    val juegoConCategoriaCorrecta = juego.copy(
                        categoriaId = categoriaIdReal,
                        activo = true
                    )
                    
                    val id = db.juegoDao().insert(juegoConCategoriaCorrecta)
                    insertedCount++
                    android.util.Log.d("GameManagementScreen", "✅ [$insertedCount] ${juego.nombre} insertado con ID: $id, categoriaId: $categoriaIdReal")
                } catch (e: Exception) {
                    android.util.Log.e("GameManagementScreen", "❌ Error insertando ${juego.nombre}: ${e.message}", e)
                }
            }
            
            val finalCount = db.juegoDao().count()
            android.util.Log.d("GameManagementScreen", "📊 RESULTADO FINAL: $finalCount juegos en base de datos")
            
            if (finalCount > 0) {
                android.util.Log.d("GameManagementScreen", "🎉 ¡ÉXITO! Base de datos inicializada correctamente")
            } else {
                android.util.Log.e("GameManagementScreen", "💥 ERROR: No se pudo insertar ningún juego")
            }
            
        } catch (e: Exception) {
            android.util.Log.e("GameManagementScreen", "💥 ERROR CRÍTICO en recreación de BD", e)
        }
    }
    
    // Inicialización automática
    // Observar estados
    val games by viewModel.games.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    
    // Snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Refrescar datos cada vez que se abre la pantalla
    LaunchedEffect(Unit) {
        viewModel.onScreenResumed()
    }
    
    // Mostrar mensajes
    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Long
            )
            viewModel.clearMessages()
        }
    }
    
    LaunchedEffect(successMessage) {
        successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
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
    var showResetDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Gestión de Juegos",
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
                    // Botón de diagnóstico
                    IconButton(onClick = { showResetDialog = true }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Restablecer catálogo",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = { 
                        gameToEdit = null
                        showDialog = true
                    }) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Agregar juego",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AdminMediumBlue
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    gameToEdit = null
                    showDialog = true
                },
                containerColor = AdminBrightBlue,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar juego")
            }
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
                .padding(16.dp)
        ) {
            // Estadísticas modernas
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(16.dp)),
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
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .background(AdminBrightBlue.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "${games.size}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Total Juegos",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AdminAccentBlue,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .background(AdminPurple.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "${games.sumOf { it.stock }}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Stock Total",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AdminAccentBlue,
                            fontWeight = FontWeight.Medium
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

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Restablecer catálogo") },
            text = { Text("Se eliminarán los juegos actuales y se cargarán los datos de prueba. ¿Deseas continuar?") },
            confirmButton = {
                Button(
                    onClick = {
                        showResetDialog = false
                        scope.launch {
                            forceResetDatabase()
                            kotlinx.coroutines.delay(1000)
                            viewModel.refreshGames()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Restablecer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
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
    val stockInt = stock.toIntOrNull()
    val isPrecioValid = precio.toDoubleOrNull()?.let { it > 0 } ?: false
    val isStockValid = stockInt?.let { if (game == null) it > 0 else it >= 0 } ?: false
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
                    supportingText = when {
                        stock.isEmpty() -> null
                        stockInt == null -> {
                            { Text("Ingresa un número válido", color = MaterialTheme.colorScheme.error) }
                        }
                        game == null && stockInt <= 0 -> {
                            { Text("El stock inicial debe ser mayor a 0", color = MaterialTheme.colorScheme.error) }
                        }
                        stockInt < 0 -> {
                            { Text("El stock no puede ser negativo", color = MaterialTheme.colorScheme.error) }
                        }
                        else -> null
                    },
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
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = AdminMediumBlue
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(AdminMediumBlue, AdminLightBlue.copy(alpha = 0.8f))
                    )
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Información del juego
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        game.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = Color.White
                    )
                    if (!game.activo) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFF5252)
                        ) {
                            Text(
                                "INACTIVO",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    game.descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AdminAccentBlue,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(AdminBrightBlue.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            "$${game.precio}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = AdminCyan
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(
                                if (game.stock > 0) AdminBrightBlue.copy(alpha = 0.3f) else Color(0xFFFF5252).copy(alpha = 0.3f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            "Stock: ${game.stock}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (game.stock > 0) Color.White else Color(0xFFFF5252)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Acciones modernas
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = onEdit,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AdminBrightBlue,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.size(width = 100.dp, height = 38.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Editar",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Editar", style = MaterialTheme.typography.labelSmall)
                }
                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF5252),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.size(width = 100.dp, height = 38.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Eliminar", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}