package com.example.uinavegacion.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.uinavegacion.data.local.database.AppDatabase
import com.example.uinavegacion.data.SessionManager
import com.example.uinavegacion.navigation.Route
import kotlinx.coroutines.launch

@Composable
fun AppDrawer(
    currentRoute: String?,          // Ruta actual para saber qué ítem resaltar
    onNavigate: (String) -> Unit,   // Función para navegar a cualquier pantalla
    onLogout: () -> Unit,           // Función para cerrar la sesión
    isAdmin: Boolean = false,
    cartCount: Int = 0,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(modifier = modifier) {
        // 1. Header con información del usuario y foto de perfil
        Column(modifier = Modifier.padding(16.dp)) {
            val context = LocalContext.current
            val db = remember { AppDatabase.getInstance(context) }
            var profilePhotoUri by remember { mutableStateOf<String?>(null) }
            var displayName by remember { mutableStateOf("Usuario Demo") }
            var displayEmail by remember { mutableStateOf("user1@demo.com") }
            
            // Cargar datos del usuario desde SessionManager
            // Se actualiza cuando cambia la ruta (por ejemplo, al volver de editar perfil)
            LaunchedEffect(currentRoute) {
                val currentUserEmail = SessionManager.getCurrentUserEmail()
                if (currentUserEmail != null) {
                    val user = db.userDao().getByEmail(currentUserEmail)
                    if (user != null) {
                        displayName = user.name
                        displayEmail = user.email
                        profilePhotoUri = user.profilePhotoUri
                    }
                } else {
                    // Fallback para usuarios demo
                    val user = db.userDao().getByEmail(if (isAdmin) "admin@steamish.com" else "user1@demo.com")
                    if (user != null) {
                        displayName = user.name
                        displayEmail = user.email
                        profilePhotoUri = user.profilePhotoUri
                    }
                }
            }
            
            Surface(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                if (profilePhotoUri != null) {
                    AsyncImage(
                        model = profilePhotoUri,
                        contentDescription = "Foto de perfil",
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                } else {
                    Icon(
                        Icons.Filled.AccountCircle,
                        contentDescription = "Avatar",
                        modifier = Modifier.padding(12.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (SessionManager.isAdmin()) "Administrador" else displayName, 
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = SessionManager.getCurrentUserEmail() ?: displayEmail, 
                style = MaterialTheme.typography.bodySmall
            )
        }

        HorizontalDivider()

        // 2. Items de navegación funcionales
        Column(modifier = Modifier.padding(12.dp)) {
            if (isAdmin) {
                NavigationDrawerItem(
                    label = { Text("Agregar Productos") },
                    selected = currentRoute == Route.AdminAddGame.path,
                    onClick = { onNavigate(Route.AdminAddGame.path) },
                    icon = { Icon(Icons.Filled.AddBox, contentDescription = "Agregar Productos") },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text("Administrar Usuarios") },
                    selected = currentRoute == Route.AdminGames.path,
                    onClick = { onNavigate(Route.AdminGames.path) },
                    icon = { Icon(Icons.Filled.People, contentDescription = "Administrar Usuarios") },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text("Perfil") },
                    selected = currentRoute == Route.Profile.path,
                    onClick = { onNavigate(Route.Profile.path) },
                    icon = { Icon(Icons.Filled.Person, contentDescription = "Perfil") },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text("Cerrar Sesión") },
                    selected = false, // El logout nunca está "seleccionado"
                    onClick = onLogout, // Llama a la función de logout del NavGraph
                    icon = { Icon(Icons.Default.ExitToApp, contentDescription = "Cerrar Sesión") },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            } else {
                NavigationDrawerItem(
                    label = { Text("Inicio") },
                    selected = currentRoute == Route.Home.path,
                    onClick = { onNavigate(Route.Home.path) },
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Inicio") },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text("Juegos") },
                    selected = currentRoute == Route.Games.path,
                    onClick = { onNavigate(Route.Games.path) },
                    icon = { Icon(Icons.Filled.SportsEsports, contentDescription = "Juegos") },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text("Biblioteca") },
                    selected = currentRoute == Route.Library.path,
                    onClick = { onNavigate(Route.Library.path) },
                    icon = { Icon(Icons.Filled.LocalLibrary, contentDescription = "Biblioteca") },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text("Carrito") },
                    selected = currentRoute == Route.Cart.path,
                    onClick = { onNavigate(Route.Cart.path) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (cartCount > 0) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor = MaterialTheme.colorScheme.onError
                                    ) {
                                        Text(
                                            text = if (cartCount > 99) "99+" else cartCount.toString(),
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Filled.ShoppingCart, contentDescription = "Carrito")
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text("Ajustes") },
                    selected = currentRoute == Route.Settings.path,
                    onClick = { onNavigate(Route.Settings.path) },
                    icon = { Icon(Icons.Filled.Settings, contentDescription = "Ajustes") },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }

        // 3. Espaciador para empujar el logout al final
        Spacer(modifier = Modifier.weight(1f))

        // 4. Footer con acción de logout funcional
        Column(modifier = Modifier.padding(12.dp)) {
            HorizontalDivider()
            NavigationDrawerItem(
                label = { Text("Cerrar Sesión") },
                selected = false, // El logout nunca está "seleccionado"
                onClick = onLogout, // Llama a la función de logout del NavGraph
                icon = { Icon(Icons.Default.ExitToApp, contentDescription = "Cerrar Sesión") },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Versión 1.0.0",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

// Componente animado para NavigationDrawerItem
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AnimatedNavigationDrawerItem(
    label: @Composable () -> Unit,
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    index: Int = 0
) {
    var visible by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay((index * 50).toLong())
        visible = true
    }
    
    // Animación de escala al hacer hover/click
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "drawer_item_scale"
    )
    
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(300)) + 
                slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = tween(300)
                ),
        exit = fadeOut() + slideOutHorizontally()
    ) {
        NavigationDrawerItem(
            label = label,
            selected = selected,
            onClick = {
                isPressed = true
                onClick()
                scope.launch {
                    kotlinx.coroutines.delay(100)
                    isPressed = false
                }
            },
            icon = icon,
            modifier = modifier.graphicsLayer(
                scaleX = scale,
                scaleY = scale
            )
        )
    }
}
