package com.example.uinavegacion.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.uinavegacion.viewmodel.CartViewModel
import com.example.uinavegacion.viewmodel.LibraryViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.uinavegacion.navigation.*
import com.example.uinavegacion.ui.utils.*
import com.example.uinavegacion.ui.utils.GameImages
import com.example.uinavegacion.ui.components.AnimatedButton
import com.example.uinavegacion.ui.components.AnimatedOutlinedButton
import com.example.uinavegacion.ui.components.AnimatedIconButton
import com.example.uinavegacion.ui.components.AnimatedTextButton
import com.example.uinavegacion.ui.components.AnimatedFloatingActionButton
import com.example.uinavegacion.data.SessionManager
import com.example.uinavegacion.ui.theme.AppColors
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush

// Colores del tema profesional
private val DarkBlue = AppColors.DarkBlue
private val MediumBlue = AppColors.MediumBlue
private val LightBlue = AppColors.LightBlue
private val AccentBlue = AppColors.AccentBlue
private val BrightBlue = AppColors.BrightBlue
private val Cyan = AppColors.Cyan

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(nav: NavHostController, cartViewModel: CartViewModel = viewModel(), libraryViewModel: LibraryViewModel = viewModel()) {
    val cartItems by cartViewModel.items.collectAsState()
    val totalPrice = cartViewModel.getTotalPrice()
    val totalItems = cartViewModel.getTotalItems()
    val windowInfo = rememberWindowInfo()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val errorMessage by cartViewModel.errorMessage.collectAsState()
    val successMessage by cartViewModel.successMessage.collectAsState()
    
    // Mostrar Snackbar cuando hay error
    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            cartViewModel.clearErrorMessage()
        }
    }
    
    // Mostrar Snackbar cuando hay éxito
    LaunchedEffect(successMessage) {
        successMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            cartViewModel.clearSuccessMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DarkBlue,
        topBar = { 
            TopAppBar(
                title = { 
                    Text(
                        "Mi Carrito ($totalItems)", 
                        fontWeight = FontWeight.Bold,
                        color = androidx.compose.ui.graphics.Color.White
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBlue,
                    titleContentColor = androidx.compose.ui.graphics.Color.White
                )
            ) 
        }
    ) { innerPadding ->
        if (cartItems.isEmpty()) {
            // Carrito vacío - centrado completo en tablets
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                EmptyCartContent(
                    modifier = Modifier
                        .then(
                            if (windowInfo.isTablet) {
                                Modifier.widthIn(max = AdaptiveUtils.getMaxContentWidth(windowInfo))
                            } else {
                                Modifier.fillMaxWidth()
                            }
                        )
                        .padding(AdaptiveUtils.getHorizontalPadding(windowInfo)),
                    onExploreGames = { nav.navigate(Route.Games.build()) },
                    isTablet = windowInfo.isTablet
                )
            }
        } else {
            // Carrito con productos - Diseño adaptativo
            if (AdaptiveUtils.shouldUseTwoPaneLayout(windowInfo)) {
                // Diseño de dos paneles para tablets grandes y desktop
                TwoPaneCartContent(
                    cartItems = cartItems,
                    cartViewModel = cartViewModel,
                    totalPrice = totalPrice,
                    totalItems = totalItems,
                    windowInfo = windowInfo,
                    onNavigateToGames = { nav.navigate(Route.Games.build()) },
                    onCompletePurchase = { 
                        // Obtener datos del usuario actual
                        val userId = SessionManager.getCurrentUserId()
                        val remoteUserId = SessionManager.getCurrentUserRemoteId()
                        
                        if (userId != null) {
                            // Procesar compra con microservicio de órdenes
                            cartViewModel.checkout(
                                context = context,
                                userId = userId,
                                remoteUserId = remoteUserId,
                                metodoPago = "Tarjeta",
                                direccionEnvio = null
                            ) { success, message ->
                            if (success) {
                                // Agregar a biblioteca (ya se hace en checkout)
                                libraryViewModel.addPurchasedGames(cartItems)
                                
                                // Mostrar mensaje de éxito
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message ?: "Compra realizada exitosamente",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                                
                                // Navegar a la biblioteca después de un pequeño delay
                                scope.launch {
                                    delay(500)
                                    libraryViewModel.forceRefresh()
                                    nav.navigate(Route.Library.path)
                                }
                            } else {
                                // Mostrar error si el checkout falló
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message ?: "Error al procesar la compra",
                                        duration = SnackbarDuration.Long
                                    )
                                }
                            }
                        }
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    "Debes iniciar sesión para realizar una compra",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            } else {
                // Diseño de una columna para móviles y tablets pequeños - centrado en tablets
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = if (windowInfo.isTablet) Alignment.Center else Alignment.TopCenter
                ) {
                    SinglePaneCartContent(
                        cartItems = cartItems,
                        cartViewModel = cartViewModel,
                        totalPrice = totalPrice,
                        totalItems = totalItems,
                        windowInfo = windowInfo,
                        onNavigateToGames = { nav.navigate(Route.Games.build()) },
                        onCompletePurchase = { 
                            // Obtener datos del usuario actual
                            val userId = SessionManager.getCurrentUserId()
                            val remoteUserId = SessionManager.getCurrentUserRemoteId()
                            
                            if (userId != null) {
                                // Procesar compra con microservicio de órdenes
                                cartViewModel.checkout(
                                    context = context,
                                    userId = userId,
                                    remoteUserId = remoteUserId,
                                    metodoPago = "Tarjeta",
                                    direccionEnvio = null
                                ) { success, message ->
                                if (success) {
                                    // Agregar a biblioteca (ya se hace en checkout)
                                    libraryViewModel.addPurchasedGames(cartItems)
                                    
                                    // Mostrar mensaje de éxito
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message ?: "Compra realizada exitosamente",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                    
                                    // Navegar a la biblioteca después de un pequeño delay
                                    scope.launch {
                                        delay(500)
                                        libraryViewModel.forceRefresh()
                                        nav.navigate(Route.Library.path)
                                    }
                                } else {
                                    // Mostrar error si el checkout falló
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message ?: "Error al procesar la compra",
                                            duration = SnackbarDuration.Long
                                        )
                                    }
                                }
                            }
                            } else {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        "Debes iniciar sesión para realizar una compra",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .then(
                                if (windowInfo.isTablet) {
                                    Modifier.widthIn(max = AdaptiveUtils.getMaxContentWidth(windowInfo))
                                } else {
                                    Modifier.fillMaxWidth()
                                }
                            )
                            .padding(AdaptiveUtils.getHorizontalPadding(windowInfo))
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyCartContent(
    modifier: Modifier = Modifier,
    onExploreGames: () -> Unit,
    isTablet: Boolean = false
) {
    Card(
        modifier = modifier.shadow(6.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MediumBlue),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(MediumBlue, LightBlue.copy(alpha = 0.8f))
                    )
                )
                .padding(if (isTablet) 48.dp else 32.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.ShoppingCart,
                    contentDescription = null,
                    modifier = Modifier.size(if (isTablet) 80.dp else 64.dp),
                    tint = Cyan
                )
                Spacer(Modifier.height(if (isTablet) 24.dp else 16.dp))
                Text(
                    text = "Tu carrito está vacío",
                    style = if (isTablet) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = androidx.compose.ui.graphics.Color.White
                )
                Spacer(Modifier.height(if (isTablet) 12.dp else 8.dp))
                Text(
                    text = "Agrega algunos juegos para comenzar",
                    style = if (isTablet) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = AccentBlue
                )
                Spacer(Modifier.height(if (isTablet) 24.dp else 16.dp))
                Button(
                    onClick = onExploreGames,
                    modifier = Modifier.then(if (isTablet) Modifier.height(56.dp) else Modifier),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrightBlue,
                        contentColor = androidx.compose.ui.graphics.Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.Explore,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Explorar Juegos",
                        style = if (isTablet) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun TwoPaneCartContent(
    cartItems: List<com.example.uinavegacion.viewmodel.CartItem>,
    cartViewModel: CartViewModel,
    totalPrice: Double,
    totalItems: Int,
    windowInfo: WindowInfo,
    onNavigateToGames: () -> Unit,
    onCompletePurchase: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(AdaptiveUtils.getHorizontalPadding(windowInfo)),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Panel izquierdo: Lista de productos (60% del ancho)
        Card(
            modifier = Modifier
                .weight(0.6f)
                .fillMaxHeight(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Productos en tu carrito",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(AdaptiveUtils.getItemSpacing(windowInfo))
                ) {
                    items(cartItems) { item ->
                        TabletCartItem(
                            item = item,
                            cartViewModel = cartViewModel,
                            windowInfo = windowInfo
                        )
                    }
                }
            }
        }
        
        // Panel derecho: Resumen y acciones (40% del ancho)
        Card(
            modifier = Modifier
                .weight(0.4f)
                .fillMaxHeight(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Resumen del pedido
                CartSummary(
                    totalItems = totalItems,
                    totalPrice = totalPrice,
                    isTablet = true
                )
                
                // Botones de acción
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AnimatedButton(
                        onClick = onCompletePurchase,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Comprar Ahora", style = MaterialTheme.typography.titleMedium)
                    }

                    AnimatedOutlinedButton(
                        onClick = onNavigateToGames,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Seguir Comprando")
                    }

                    AnimatedOutlinedButton(
                        onClick = { cartViewModel.clearCart() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Vaciar Carrito")
                    }
                }
            }
        }
    }
}

@Composable
private fun SinglePaneCartContent(
    cartItems: List<com.example.uinavegacion.viewmodel.CartItem>,
    cartViewModel: CartViewModel,
    totalPrice: Double,
    totalItems: Int,
    windowInfo: WindowInfo,
    onNavigateToGames: () -> Unit,
    onCompletePurchase: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .then(if (windowInfo.isTablet) Modifier.fillMaxHeight(0.9f) else Modifier),
        verticalArrangement = Arrangement.spacedBy(AdaptiveUtils.getItemSpacing(windowInfo))
    ) {
        // Lista de productos - máximo espacio posible
        LazyColumn(
            modifier = Modifier.weight(1f), // Dar todo el espacio disponible a los productos
            verticalArrangement = Arrangement.spacedBy(AdaptiveUtils.getItemSpacing(windowInfo))
        ) {
            items(cartItems) { item ->
                if (windowInfo.isTablet) {
                    TabletCartItem(
                        item = item,
                        cartViewModel = cartViewModel,
                        windowInfo = windowInfo
                    )
                } else {
                    MobileCartItem(
                        item = item,
                        cartViewModel = cartViewModel
                    )
                }
            }
        }

        // Resumen del carrito - compacto y pegado abajo
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            CartSummary(
                totalItems = totalItems,
                totalPrice = totalPrice,
                isTablet = windowInfo.isTablet
            )
        }

        // Botones de acción
        if (windowInfo.isTablet) {
            // Para tablets: botones más grandes y espaciosos
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AnimatedButton(
                    onClick = onCompletePurchase,
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Comprar Ahora", style = MaterialTheme.typography.titleMedium)
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AnimatedOutlinedButton(
                        onClick = onNavigateToGames,
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text("Seguir Comprando")
                    }
                    
                    AnimatedOutlinedButton(
                        onClick = { cartViewModel.clearCart() },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Vaciar Carrito")
                    }
                }
            }
        } else {
            // Para móviles: diseño original compacto
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AnimatedOutlinedButton(
                    onClick = onNavigateToGames,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Seguir Comprando")
                }

                AnimatedButton(
                    onClick = onCompletePurchase,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Comprar Ahora")
                }
            }
            
            AnimatedOutlinedButton(
                onClick = { cartViewModel.clearCart() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Vaciar Carrito")
            }
        }
    }
}

@Composable
private fun TabletCartItem(
    item: com.example.uinavegacion.viewmodel.CartItem,
    cartViewModel: CartViewModel,
    windowInfo: WindowInfo
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen más grande para tablets
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = item.imageUrl.ifEmpty { GameImages.getDefaultImage() },
                    contentDescription = item.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(Modifier.width(20.dp))

            // Información del producto expandida
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Precio unitario:",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    if (item.hasDiscount) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = androidx.compose.ui.graphics.Color(0xFFE74C3C),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "-${item.discount}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = androidx.compose.ui.graphics.Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = "$${String.format("%.2f", item.originalPrice)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                            )
                            Text(
                                text = "$${String.format("%.2f", item.price)}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = androidx.compose.ui.graphics.Color(0xFF27AE60),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Text(
                            text = "$${String.format("%.2f", item.price)}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Subtotal:",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "$${String.format("%.2f", item.price * item.quantity)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.width(20.dp))

            // Controles de cantidad mejorados
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Controles de cantidad con mejor diseño
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(16.dp)
                        )
                        .padding(6.dp)
                ) {
                    // Botón menos
                    IconButton(
                        onClick = { 
                            if (item.quantity > 1) {
                                cartViewModel.updateQuantity(item.id, item.quantity - 1)
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                if (item.quantity > 1) 
                                    MaterialTheme.colorScheme.primaryContainer 
                                else 
                                    MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(12.dp)
                            ),
                        enabled = item.quantity > 1
                    ) {
                        Icon(
                            Icons.Default.Remove,
                            contentDescription = "Disminuir cantidad",
                            tint = if (item.quantity > 1) 
                                MaterialTheme.colorScheme.onPrimaryContainer 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    
                    // Cantidad
                    Card(
                        modifier = Modifier.size(56.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = "${item.quantity}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                    
                    // Botón más
                    IconButton(
                        onClick = { 
                            cartViewModel.updateQuantity(item.id, item.quantity + 1) 
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                MaterialTheme.colorScheme.primaryContainer,
                                RoundedCornerShape(12.dp)
                            ),
                        enabled = item.quantity < item.maxStock
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Aumentar cantidad",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                
                // Botón eliminar más grande
                AnimatedOutlinedButton(
                    onClick = { cartViewModel.removeGame(item.id) },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Eliminar", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun MobileCartItem(
    item: com.example.uinavegacion.viewmodel.CartItem,
    cartViewModel: CartViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Imagen del juego
            Card(
                modifier = Modifier.size(80.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = item.imageUrl.ifEmpty { GameImages.getDefaultImage() },
                        contentDescription = item.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Información del producto
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Nombre del juego
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                // Precio con descuento si aplica
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (item.hasDiscount) {
                        // Badge de descuento
                        Surface(
                            color = androidx.compose.ui.graphics.Color(0xFFFF3B30),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "-${item.discount}%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = androidx.compose.ui.graphics.Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        
                        // Precio original tachado
                        Text(
                            text = "$${String.format("%.2f", item.originalPrice)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                        )
                    }
                }
                
                // Precio actual
                Text(
                    text = "$${String.format("%.2f", item.price)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (item.hasDiscount) 
                        androidx.compose.ui.graphics.Color(0xFF34C759) 
                    else 
                        MaterialTheme.colorScheme.primary
                )
                
                // Controles de cantidad mejorados
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Controles de cantidad con mejor diseño
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(12.dp)
                            )
                            .padding(4.dp)
                    ) {
                        // Botón menos - mejorado
                        IconButton(
                            onClick = { 
                                if (item.quantity > 1) {
                                    cartViewModel.updateQuantity(item.id, item.quantity - 1)
                                }
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    if (item.quantity > 1) 
                                        MaterialTheme.colorScheme.primaryContainer 
                                    else 
                                        MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(8.dp)
                                ),
                            enabled = item.quantity > 1
                        ) {
                            Icon(
                                Icons.Default.Remove,
                                contentDescription = "Disminuir",
                                modifier = Modifier.size(20.dp),
                                tint = if (item.quantity > 1) 
                                    MaterialTheme.colorScheme.onPrimaryContainer 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                        }
                        
                        // Cantidad - mejorada
                        Text(
                            text = "${item.quantity}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        
                        // Botón más - mejorado
                        IconButton(
                            onClick = { 
                                cartViewModel.updateQuantity(item.id, item.quantity + 1) 
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    RoundedCornerShape(8.dp)
                                ),
                            enabled = item.quantity < item.maxStock
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Aumentar",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    
                    // Botón eliminar mejorado
                    IconButton(
                        onClick = { cartViewModel.removeGame(item.id) },
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                MaterialTheme.colorScheme.errorContainer,
                                RoundedCornerShape(10.dp)
                            )
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            modifier = Modifier.size(22.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
                
                // Subtotal
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Subtotal",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$${String.format("%.2f", item.price * item.quantity)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CartSummary(
    totalItems: Int,
    totalPrice: Double,
    isTablet: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (isTablet) 20.dp else 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Encabezado con icono
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.ShoppingCart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Resumen de Compra",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Divider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                thickness = 1.dp
            )
            
            // Cantidad de juegos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total de juegos",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "$totalItems",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
            
            Divider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                thickness = 1.dp
            )
            
            // Total a pagar - Destacado
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Total a Pagar",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                        )
                        Text(
                            text = "Incluye todos los juegos",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        )
                    }
                    Text(
                        text = "$${String.format("%.2f", totalPrice)}",
                        style = if (isTablet) 
                            MaterialTheme.typography.displaySmall 
                        else 
                            MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}