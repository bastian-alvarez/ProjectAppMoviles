package com.example.uinavegacion.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.scale
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.uinavegacion.data.local.database.AppDatabase
import com.example.uinavegacion.data.repository.GameRepository
// import com.example.uinavegacion.data.repository.ResenaRepository
import com.example.uinavegacion.data.SessionManager
// import com.example.uinavegacion.data.local.resena.ResenaEntity
import com.example.uinavegacion.navigation.*
import com.example.uinavegacion.ui.model.toGame
import com.example.uinavegacion.ui.viewmodel.GameCatalogViewModel
import com.example.uinavegacion.ui.viewmodel.GameCatalogViewModelFactory
import com.example.uinavegacion.viewmodel.CartViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable  
fun GameDetailScreen(nav: NavHostController, gameId: String, cartViewModel: CartViewModel = viewModel()) {
    val context = LocalContext.current.applicationContext
    val db = remember { AppDatabase.getInstance(context) }
    val gameRepository = remember { GameRepository(db.juegoDao()) }
    // val resenaRepository = remember { ResenaRepository(db.resenaDao()) }
    val catalogViewModel: GameCatalogViewModel = viewModel(
        factory = GameCatalogViewModelFactory(
            gameRepository = gameRepository
        )
    )
    
    val catalogGames by catalogViewModel.games.collectAsState()
    val isLoadingCatalog by catalogViewModel.isLoading.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val successMessage by cartViewModel.successMessage.collectAsState()
    val scope = rememberCoroutineScope()
    
    // Estado para reseñas (DESHABILITADO - funcionalidad no disponible)
    // var resenas by remember { mutableStateOf<List<ResenaEntity>>(emptyList()) }
    var averageRating by remember { mutableStateOf(0.0) }
    var isLoadingResenas by remember { mutableStateOf(false) }
    
    // Estado para formulario de reseña (DESHABILITADO)
    var selectedRating by remember { mutableStateOf(0) }
    var comentarioText by remember { mutableStateOf("") }
    var showResenaForm by remember { mutableStateOf(false) }
    // var existingResena by remember { mutableStateOf<ResenaEntity?>(null) }
    
    val currentUser = SessionManager.getCurrentUser()
    val currentUserId = SessionManager.getCurrentUserId()
    
    // Mostrar Snackbar cuando se agrega al carrito
    LaunchedEffect(successMessage) {
        successMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            cartViewModel.clearSuccessMessage()
        }
    }
    
    // Buscar el juego desde la BD
    android.util.Log.d("GameDetailScreen", "🔍 Buscando juego con ID: '$gameId'")
    android.util.Log.d("GameDetailScreen", "📊 Estado: isLoading=$isLoadingCatalog, Total juegos=${catalogGames.size}")
    
    // Esperar a que los juegos se carguen
    if (isLoadingCatalog) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }
    
    catalogGames.forEach { game ->
        android.util.Log.d("GameDetailScreen", "  📦 Juego: ID='${game.id}' (${game.id::class.simpleName}), Nombre='${game.nombre}'")
    }
    
    val catalogGame = catalogGames.find { 
        val gameIdStr = it.id.toString()
        val matches = gameIdStr == gameId
        android.util.Log.d("GameDetailScreen", "  🔄 Comparando: '$gameIdStr' == '$gameId' ? $matches")
        matches
    }
    
    // Si no se encuentra el juego, mostrar mensaje de error y volver
    if (catalogGame == null) {
        android.util.Log.e("GameDetailScreen", "❌ Juego no encontrado con ID: '$gameId'")
        LaunchedEffect(Unit) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    "Juego no encontrado",
                    duration = SnackbarDuration.Short
                )
                kotlinx.coroutines.delay(1000)
                nav.popBackStack()
            }
        }
        return
    }
    
    android.util.Log.d("GameDetailScreen", "Juego encontrado: ${catalogGame.nombre}")
    
    val game = catalogGame.toGame()
    val cartItems by cartViewModel.items.collectAsState()
    val currentQuantity = cartItems.find { it.id == game.id }?.quantity ?: 0
    val juegoIdLong = gameId.toLongOrNull() ?: 0L
    
    // Cargar reseñas (DESHABILITADO - funcionalidad no disponible)
    LaunchedEffect(juegoIdLong) {
        isLoadingResenas = true
        try {
            // resenas = resenaRepository.getResenasByJuegoId(juegoIdLong)
            // averageRating = resenaRepository.getAverageRating(juegoIdLong)
            averageRating = 0.0
            
            // Verificar si el usuario ya hizo una reseña
            // currentUserId?.let { userId ->
            //     existingResena = resenaRepository.getResenasByJuegoId(juegoIdLong)
            //         .find { it.userId == userId }
            // }
        } catch (e: Exception) {
            android.util.Log.e("GameDetailScreen", "Error cargando reseñas", e)
        } finally {
            isLoadingResenas = false
        }
    }

    var expandedDescription by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = { 
            TopAppBar(
                title = { 
                    Text(
                        text = game.name,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            ) 
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Imagen del juego - tamaño optimizado
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = game.imageUrl.ifEmpty { "https://via.placeholder.com/400x220?text=🎮" },
                    contentDescription = game.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            }

            // Información principal - Precio y Stock compactos
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Precio destacado
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (game.hasDiscount) {
                                Text(
                                    text = "$${String.format("%.2f", game.price)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                )
                                Text(
                                    text = "$${String.format("%.2f", game.discountedPrice)}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = androidx.compose.ui.graphics.Color(0xFF27AE60),
                                    fontWeight = FontWeight.Bold
                                )
                                Surface(
                                    color = androidx.compose.ui.graphics.Color(0xFFE74C3C),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "-${game.discount}%",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = androidx.compose.ui.graphics.Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            } else {
                                Text(
                                    text = if (game.price == 0.0) "GRATIS" else "$${String.format("%.2f", game.price)}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        // Stock disponible
                        Surface(
                            color = if (game.stock > 0) 
                                androidx.compose.ui.graphics.Color(0xFF27AE60).copy(alpha = 0.15f)
                            else 
                                androidx.compose.ui.graphics.Color(0xFFE74C3C).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (game.stock > 0) "✓ Stock: ${game.stock}" else "✗ Sin stock",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = if (game.stock > 0) 
                                    androidx.compose.ui.graphics.Color(0xFF27AE60)
                                else 
                                    androidx.compose.ui.graphics.Color(0xFFE74C3C),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
            
            // Descripción - Expandible/Colapsable
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Título de descripción con botón expandir/colapsar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedDescription = !expandedDescription },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Descripción",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            imageVector = if (expandedDescription) 
                                Icons.Default.ExpandLess
                            else 
                                Icons.Default.ExpandMore,
                            contentDescription = if (expandedDescription) "Colapsar" else "Expandir",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Spacer(Modifier.height(8.dp))
                    
                    // Descripción del juego - limitada o completa (más pequeña)
                    Text(
                        text = game.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = if (expandedDescription) Int.MAX_VALUE else 3,
                        overflow = if (expandedDescription) TextOverflow.Visible else TextOverflow.Ellipsis,
                        lineHeight = androidx.compose.ui.unit.TextUnit(18f, androidx.compose.ui.unit.TextUnitType.Sp)
                    )
                }
            }

            // Botón de comprar - destacado y mejorado
            val currentQuantity = cartViewModel.getQuantity(game.id)
            val canAddMore = game.stock > currentQuantity
            
            Button(
                onClick = {
                    if (canAddMore) {
                        cartViewModel.addGame(
                            id = game.id,
                            name = game.name,
                            price = game.discountedPrice,
                            imageUrl = game.imageUrl,
                            originalPrice = if (game.hasDiscount) game.price else null,
                            discount = game.discount,
                            maxStock = game.stock
                        )
                    }
                },
                enabled = canAddMore,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (canAddMore) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (canAddMore) 
                        MaterialTheme.colorScheme.onPrimary 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = if (canAddMore) 4.dp else 0.dp
                )
            ) {
                Text(
                    text = when {
                        game.stock <= 0 -> "Este producto está agotado"
                        game.stock <= currentQuantity -> "Sin stock disponible"
                        currentQuantity > 0 -> "Agregar más al carrito"
                        else -> "Agregar al carrito"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Sección de Reseñas - Mejorada
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Título con calificación promedio
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Reseñas",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (averageRating > 0) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Star,
                                    contentDescription = "Calificación",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Text(
                                    text = String.format("%.1f", averageRating),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "(0)", // resenas.size
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    
                    // Formulario de reseña (DESHABILITADO - funcionalidad no disponible)
                    if (false && currentUser != null) { // && existingResena == null
                        if (!showResenaForm) {
                            OutlinedButton(
                                onClick = { showResenaForm = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Escribir Reseña")
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                // Calificación con estrellas
                                Text(
                                    text = "Calificación:",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Medium
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    for (i in 1..5) {
                                        val isSelected = i <= selectedRating
                                        
                                        // Animación de escala
                                        val scale by animateFloatAsState(
                                            targetValue = if (isSelected) 1.2f else 1f,
                                            animationSpec = tween(durationMillis = 200),
                                            label = "star_scale_$i"
                                        )
                                        
                                        IconButton(
                                            onClick = { selectedRating = i },
                                            modifier = Modifier.size(48.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Star,
                                                contentDescription = "$i estrellas",
                                                tint = if (isSelected) 
                                                    androidx.compose.ui.graphics.Color(0xFFFFD700) // Amarillo dorado
                                                else 
                                                    androidx.compose.ui.graphics.Color(0xFF000000), // Negro
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .scale(scale)
                                            )
                                        }
                                    }
                                }
                                
                                // Campo de comentario
                                OutlinedTextField(
                                    value = comentarioText,
                                    onValueChange = { comentarioText = it },
                                    label = { Text("Tu comentario") },
                                    placeholder = { Text("Escribe tu reseña aquí...") },
                                    modifier = Modifier.fillMaxWidth(),
                                    minLines = 3,
                                    maxLines = 5
                                )
                                
                                // Botones
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            showResenaForm = false
                                            selectedRating = 0
                                            comentarioText = ""
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Cancelar")
                                    }
                                    Button(
                                        onClick = {
                                            if (selectedRating > 0 && comentarioText.isNotBlank() && currentUserId != null) {
                                                scope.launch {
                                                    // DESHABILITADO - funcionalidad de reseñas no disponible
                                                    // val result = resenaRepository.addResena(
                                                    //     userId = currentUserId,
                                                    //     juegoId = juegoIdLong,
                                                    //     calificacion = selectedRating,
                                                    //     comentario = comentarioText
                                                    // )
                                                    // if (result.isSuccess) {
                                                    //     // Recargar reseñas
                                                    //     resenas = resenaRepository.getResenasByJuegoId(juegoIdLong)
                                                    //     averageRating = resenaRepository.getAverageRating(juegoIdLong)
                                                    //     existingResena = resenas.find { it.userId == currentUserId }
                                                        showResenaForm = false
                                                        selectedRating = 0
                                                        comentarioText = ""
                                                        snackbarHostState.showSnackbar(
                                                            "Funcionalidad de reseñas no disponible",
                                                            duration = SnackbarDuration.Short
                                                        )
                                                    // } else {
                                                    //     snackbarHostState.showSnackbar(
                                                    //         result.exceptionOrNull()?.message ?: "Error al agregar reseña",
                                                    //         duration = SnackbarDuration.Short
                                                    //     )
                                                    // }
                                                }
                                            } else {
                                                scope.launch {
                                                    snackbarHostState.showSnackbar(
                                                        "Por favor completa la calificación y el comentario",
                                                        duration = SnackbarDuration.Short
                                                    )
                                                }
                                            }
                                        },
                                        enabled = selectedRating > 0 && comentarioText.isNotBlank(),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Publicar")
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                    
                    // Lista de reseñas o mensajes de estado
                    if (isLoadingResenas) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (true) { // resenas.isEmpty()
                        // Mensaje único y mejorado cuando no hay reseñas
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Star,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                    modifier = Modifier.size(36.dp)
                                )
                                Text(
                                    text = "Aún no hay reseñas",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    fontWeight = FontWeight.SemiBold
                                )
                                if (currentUser == null) {
                                    Text(
                                        text = "Inicia sesión para ser el primero en compartir tu opinión",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                } else {
                                    Text(
                                        text = "Sé el primero en compartir tu opinión",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    } // else {
                        // Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        //     resenas.forEach { resena ->
                        //         ResenaCard(resena = resena)
                        //     }
                        // }
                    // }
                }
            }
        }
    }
}

// DESHABILITADO - Funcionalidad de reseñas no disponible
// @Composable
// private fun ResenaCard(resena: ResenaEntity) {
//     Card(
//         modifier = Modifier.fillMaxWidth(),
//         elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
//     ) {
//         Column(modifier = Modifier.padding(12.dp)) {
//             // Estrellas
//             Row(
//                 horizontalArrangement = Arrangement.spacedBy(2.dp)
//             ) {
//                 for (i in 1..5) {
//                     Icon(
//                         if (i <= resena.calificacion) Icons.Filled.Star else Icons.Outlined.Star,
//                         contentDescription = null,
//                         tint = if (i <= resena.calificacion) 
//                             MaterialTheme.colorScheme.primary 
//                         else 
//                             MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
//                         modifier = Modifier.size(16.dp)
//                     )
//                 }
//             }
//             Spacer(Modifier.height(8.dp))
//             Text(
//                 text = resena.comentario,
//                 style = MaterialTheme.typography.bodyMedium
//             )
//         }
//     }
// }