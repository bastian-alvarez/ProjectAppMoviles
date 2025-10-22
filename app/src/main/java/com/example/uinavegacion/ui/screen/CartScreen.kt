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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.uinavegacion.navigation.*
import com.example.uinavegacion.ui.utils.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(nav: NavHostController, cartViewModel: CartViewModel = viewModel(), libraryViewModel: LibraryViewModel = viewModel()) {
    val cartItems by cartViewModel.items.collectAsState()
    val totalPrice = cartViewModel.getTotalPrice()
    val totalItems = cartViewModel.getTotalItems()
    val windowInfo = rememberWindowInfo()

    Scaffold(
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
            // Carrito vacío - Mismo diseño para todos los dispositivos
            EmptyCartContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(AdaptiveUtils.getHorizontalPadding(windowInfo)),
                onExploreGames = { nav.navigate(Route.Games.path) }
            )
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
                    onNavigateToGames = { nav.navigate(Route.Games.path) },
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
                // Diseño de una columna para móviles y tablets pequeños
                SinglePaneCartContent(
                    cartItems = cartItems,
                    cartViewModel = cartViewModel,
                    totalPrice = totalPrice,
                    totalItems = totalItems,
                    windowInfo = windowInfo,
                    onNavigateToGames = { nav.navigate(Route.Games.path) },
                    onCompletePurchase = { 
                        // Añadir juegos comprados a la biblioteca
                        libraryViewModel.addPurchasedGames(cartItems)
                        cartViewModel.clearCart()
                        nav.navigate(Route.Home.path)
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(AdaptiveUtils.getHorizontalPadding(windowInfo))
                )
            }
        }
    }
}

@Composable
private fun EmptyCartContent(
    modifier: Modifier = Modifier,
    onExploreGames: () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🛒",
                    style = MaterialTheme.typography.displayLarge
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Tu carrito está vacío",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Agrega algunos juegos para comenzar",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onExploreGames
                ) {
                    Text("Explorar Juegos")
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
                    Button(
                        onClick = onCompletePurchase,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Comprar Ahora", style = MaterialTheme.typography.titleMedium)
                    }
                    
                    OutlinedButton(
                        onClick = onNavigateToGames,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Seguir Comprando")
                    }
                    
                    OutlinedButton(
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
        modifier = modifier,
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
                Button(
                    onClick = onCompletePurchase,
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Comprar Ahora", style = MaterialTheme.typography.titleMedium)
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onNavigateToGames,
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text("Seguir Comprando")
                    }
                    
                    OutlinedButton(
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
                OutlinedButton(
                    onClick = onNavigateToGames,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Seguir Comprando")
                }
                
                Button(
                    onClick = onCompletePurchase,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Comprar Ahora")
                }
            }
            
            OutlinedButton(
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
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "🎮", 
                    style = MaterialTheme.typography.headlineMedium
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
                    IconButton(
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
                    
                    IconButton(
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
                OutlinedButton(
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
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text("Juego", style = MaterialTheme.typography.titleLarge)
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
                    IconButton(
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
                    
                    IconButton(
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
                
                OutlinedButton(
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