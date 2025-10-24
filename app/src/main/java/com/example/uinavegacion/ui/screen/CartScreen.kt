package com.example.uinavegacion.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.uinavegacion.viewmodel.CartViewModel
import com.example.uinavegacion.viewmodel.LibraryViewModel

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.uinavegacion.navigation.*
import com.example.uinavegacion.ui.utils.*
import com.example.uinavegacion.ui.components.AnimatedButton
import com.example.uinavegacion.ui.components.AnimatedOutlinedButton
import com.example.uinavegacion.ui.components.AnimatedIconButton
import com.example.uinavegacion.ui.components.AnimatedTextButton
import com.example.uinavegacion.ui.components.AnimatedFloatingActionButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(nav: NavHostController, cartViewModel: CartViewModel = viewModel(), libraryViewModel: LibraryViewModel = viewModel()) {
    val cartItems by cartViewModel.items.collectAsState()
    val totalPrice = cartViewModel.getTotalPrice()
    val totalItems = cartViewModel.getTotalItems()
    val windowInfo = rememberWindowInfo()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val errorMessage by cartViewModel.errorMessage.collectAsState()
    
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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { 
            TopAppBar(
                title = { Text("Mi Carrito ($totalItems)", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
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
                        // Añadir juegos comprados a la biblioteca
                        libraryViewModel.addPurchasedGames(cartItems)
                        cartViewModel.clearCart()
                        nav.navigate(Route.Home.path)
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
                            // Añadir juegos comprados a la biblioteca
                            libraryViewModel.addPurchasedGames(cartItems)
                            cartViewModel.clearCart()
                            nav.navigate(Route.Home.path)
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
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isTablet) 8.dp else 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(if (isTablet) 48.dp else 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "🛒",
                style = if (isTablet) MaterialTheme.typography.displayLarge else MaterialTheme.typography.displayMedium
            )
            Spacer(Modifier.height(if (isTablet) 24.dp else 16.dp))
            Text(
                text = "Tu carrito está vacío",
                style = if (isTablet) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(if (isTablet) 12.dp else 8.dp))
            Text(
                text = "Agrega algunos juegos para comenzar",
                style = if (isTablet) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(if (isTablet) 24.dp else 16.dp))
            Button(
                onClick = onExploreGames,
                modifier = Modifier.then(if (isTablet) Modifier.height(56.dp) else Modifier)
            ) {
                Text(
                    "Explorar Juegos",
                    style = if (isTablet) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge
                )
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
                Column {
                    Text(
                        text = "Resumen del Pedido",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    CartSummary(
                        totalItems = totalItems,
                        totalPrice = totalPrice,
                        isTablet = true
                    )
                }
                
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
        // Lista de productos
        LazyColumn(
            modifier = Modifier.weight(1f),
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

        // Resumen del carrito
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                CartSummary(
                    totalItems = totalItems,
                    totalPrice = totalPrice,
                    isTablet = windowInfo.isTablet
                )
            }
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
                if (item.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = item.imageUrl,
                        contentDescription = item.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Sin imagen",
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
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
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Precio unitario:",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "$${String.format("%.2f", item.price)}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
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

            // Controles de cantidad más grandes
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Controles de cantidad
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AnimatedIconButton(
                        onClick = { 
                            if (item.quantity > 1) {
                                cartViewModel.updateQuantity(item.id, item.quantity - 1)
                            }
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                MaterialTheme.colorScheme.secondaryContainer,
                                RoundedCornerShape(12.dp)
                            )
                    ) {
                        Icon(
                            Icons.Default.Remove,
                            contentDescription = "Disminuir cantidad",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    Card(
                        modifier = Modifier.size(48.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = "${item.quantity}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    
                    AnimatedIconButton(
                        onClick = { cartViewModel.updateQuantity(item.id, item.quantity + 1) },
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                MaterialTheme.colorScheme.secondaryContainer,
                                RoundedCornerShape(12.dp)
                            )
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Aumentar cantidad",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(24.dp)
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen del producto
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (item.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = item.imageUrl,
                        contentDescription = item.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Sin imagen",
                        modifier = Modifier.size(30.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Cantidad: ${item.quantity}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Precio: $${item.price}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Subtotal: $${String.format("%.2f", item.price * item.quantity)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Botones de control compactos para móvil
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AnimatedIconButton(
                        onClick = { 
                            if (item.quantity > 1) {
                                cartViewModel.updateQuantity(item.id, item.quantity - 1)
                            }
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                MaterialTheme.colorScheme.secondaryContainer,
                                RoundedCornerShape(8.dp)
                            )
                    ) {
                        Icon(
                            Icons.Default.Remove,
                            contentDescription = "Disminuir cantidad",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    Card(
                        modifier = Modifier.size(40.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = "${item.quantity}",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    
                    AnimatedIconButton(
                        onClick = { cartViewModel.updateQuantity(item.id, item.quantity + 1) },
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                MaterialTheme.colorScheme.secondaryContainer,
                                RoundedCornerShape(8.dp)
                            )
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Aumentar cantidad",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                AnimatedOutlinedButton(
                    onClick = { cartViewModel.removeGame(item.id) },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Quitar", style = MaterialTheme.typography.bodySmall)
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
    val textStyle = if (isTablet) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium
    val titleStyle = if (isTablet) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium
    
    Column {
        if (isTablet) {
            Text(
                text = "Resumen del Pedido",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        } else {
            Text(
                text = "Resumen del Pedido",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Items:", style = textStyle)
            Text("$totalItems juegos", style = textStyle)
        }
        Spacer(Modifier.height(if (isTablet) 8.dp else 4.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Envío:", style = textStyle)
            Text("Gratis", style = textStyle, color = MaterialTheme.colorScheme.primary)
        }
        Spacer(Modifier.height(if (isTablet) 12.dp else 8.dp))
        
        HorizontalDivider()
        Spacer(Modifier.height(if (isTablet) 12.dp else 8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Total:",
                style = titleStyle,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "$${String.format("%.2f", totalPrice)}",
                style = titleStyle,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}