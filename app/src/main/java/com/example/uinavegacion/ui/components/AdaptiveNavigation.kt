package com.example.uinavegacion.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.uinavegacion.data.local.database.AppDatabase
import com.example.uinavegacion.data.SessionManager
import com.example.uinavegacion.navigation.Route
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Rail de navegación para tablets y pantallas medianas
 */
@Composable
fun AppNavigationRail(
    currentRoute: String?,
    onHome: () -> Unit,
    onGames: () -> Unit,
    onCart: () -> Unit,
    onProfile: () -> Unit,
    cartCount: Int = 0,
    modifier: Modifier = Modifier
) {
    NavigationRail(
        modifier = modifier.fillMaxHeight(),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        // Espaciado superior
        Spacer(Modifier.height(16.dp))
        
        // Logo o título de la app (opcional)
        Box(
            modifier = Modifier
                .size(64.dp)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Gamepad,
                contentDescription = "GameStore Logo",
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(Modifier.height(24.dp))
        
        // Elementos de navegación
        NavigationRailItem(
            icon = {
                Icon(
                    imageVector = if (currentRoute == Route.Home.path) Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = "Inicio"
                )
            },
            label = { Text("Inicio") },
            selected = currentRoute == Route.Home.path,
            onClick = onHome
        )
        
        NavigationRailItem(
            icon = {
                Icon(
                    imageVector = if (currentRoute == Route.Games.path) Icons.Filled.SportsEsports else Icons.Outlined.SportsEsports,
                    contentDescription = "Juegos"
                )
            },
            label = { Text("Juegos") },
            selected = currentRoute == Route.Games.path,
            onClick = onGames
        )
        
        NavigationRailItem(
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
                    Icon(
                        imageVector = if (currentRoute == Route.Cart.path) Icons.Filled.ShoppingCart else Icons.Outlined.ShoppingCart,
                        contentDescription = "Carrito"
                    )
                }
            },
            label = { Text("Carrito") },
            selected = currentRoute == Route.Cart.path,
            onClick = onCart
        )
        
        NavigationRailItem(
            icon = {
                Icon(
                    imageVector = if (currentRoute == Route.Profile.path) Icons.Filled.Person else Icons.Outlined.Person,
                    contentDescription = "Perfil"
                )
            },
            label = { Text("Perfil") },
            selected = currentRoute == Route.Profile.path,
            onClick = onProfile
        )
        
        // Espaciado inferior flexible
        Spacer(Modifier.weight(1f))
    }
}

/**
 * Drawer de navegación permanente para pantallas grandes
 */
@Composable
fun AppPermanentNavigationDrawer(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
    isAdmin: Boolean = false,
    cartCount: Int = 0,
    modifier: Modifier = Modifier
) {
    PermanentDrawerSheet(
        modifier = modifier.width(280.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerContentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header del drawer con información del usuario
            val context = LocalContext.current
            val db = remember { AppDatabase.getInstance(context) }
            
            // Observar cambios en SessionManager para actualizar automáticamente
            val currentUser by SessionManager.currentUser.collectAsStateWithLifecycle()
            val currentAdmin by SessionManager.currentAdmin.collectAsStateWithLifecycle()
            
            // Obtener datos actualizados del usuario o admin
            val displayName = remember(currentUser, currentAdmin) {
                currentUser?.name ?: currentAdmin?.name ?: "Usuario"
            }
            
            val displayEmail = remember(currentUser, currentAdmin) {
                currentUser?.email ?: currentAdmin?.email ?: ""
            }
            
            val profilePhotoUri = remember(currentUser, currentAdmin) {
                currentUser?.profilePhotoUri ?: currentAdmin?.profilePhotoUri
            }
            
            // Recargar datos desde BD cuando cambia la ruta o cuando se actualiza el SessionManager
            LaunchedEffect(currentRoute, currentUser, currentAdmin) {
                val email = SessionManager.getCurrentUserEmail()
                if (email != null) {
                    if (SessionManager.isAdmin()) {
                        val admin = db.adminDao().getByEmail(email)
                        if (admin != null) {
                            SessionManager.loginAdmin(admin)
                        }
                    } else {
                        val user = db.userDao().getByEmail(email)
                        if (user != null) {
                            SessionManager.loginUser(user)
                        }
                    }
                }
            }
            
            // Información del usuario en el header
            Column(
                modifier = Modifier.padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Foto de perfil
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
                Spacer(Modifier.height(8.dp))
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = displayEmail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            
            Spacer(Modifier.height(16.dp))
            
            // Items de navegación
            if (!isAdmin) {
                DrawerNavigationItem(
                    icon = Icons.Default.Home,
                    label = "Inicio",
                    isSelected = currentRoute == Route.Home.path,
                    onClick = { onNavigate(Route.Home.path) }
                )
                
                DrawerNavigationItem(
                    icon = Icons.Default.SportsEsports,
                    label = "Juegos",
                    isSelected = currentRoute == Route.Games.path,
                    onClick = { onNavigate(Route.Games.path) }
                )
                
                DrawerNavigationItem(
                    icon = Icons.Default.ShoppingCart,
                    label = "Carrito",
                    isSelected = currentRoute == Route.Cart.path,
                    onClick = { onNavigate(Route.Cart.path) },
                    badge = if (cartCount > 0) {
                        {
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
                    } else null
                )
                
                DrawerNavigationItem(
                    icon = Icons.Default.LibraryBooks,
                    label = "Mi Biblioteca",
                    isSelected = currentRoute == Route.Library.path,
                    onClick = { onNavigate(Route.Library.path) }
                )
                
                DrawerNavigationItem(
                    icon = Icons.Default.Person,
                    label = "Perfil",
                    isSelected = currentRoute == Route.Profile.path,
                    onClick = { onNavigate(Route.Profile.path) }
                )
                
                DrawerNavigationItem(
                    icon = Icons.Default.Settings,
                    label = "Configuración",
                    isSelected = currentRoute == Route.Settings.path,
                    onClick = { onNavigate(Route.Settings.path) }
                )
            } else {
                // Items de administrador
                DrawerNavigationItem(
                    icon = Icons.Default.Dashboard,
                    label = "Dashboard",
                    isSelected = currentRoute == Route.AdminDashboard.path,
                    onClick = { onNavigate(Route.AdminDashboard.path) }
                )
                
                DrawerNavigationItem(
                    icon = Icons.Default.SportsEsports,
                    label = "Gestionar Juegos",
                    isSelected = currentRoute == Route.AdminGames.path,
                    onClick = { onNavigate(Route.AdminGames.path) }
                )
                
                DrawerNavigationItem(
                    icon = Icons.Default.People,
                    label = "Gestionar Usuarios",
                    isSelected = currentRoute == Route.AdminUsers.path,
                    onClick = { onNavigate(Route.AdminUsers.path) }
                )
            }
            
            Spacer(Modifier.weight(1f))
            
            // Botón de logout en la parte inferior
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            
            Spacer(Modifier.height(16.dp))
            
            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Logout, contentDescription = null) },
                label = { Text("Cerrar Sesión") },
                selected = false,
                onClick = onLogout,
                colors = NavigationDrawerItemDefaults.colors(
                    unselectedIconColor = MaterialTheme.colorScheme.error,
                    unselectedTextColor = MaterialTheme.colorScheme.error
                )
            )
        }
    }
}

/**
 * Item individual para el drawer permanente
 */
@Composable
private fun DrawerNavigationItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    badge: (@Composable BoxScope.() -> Unit)? = null
) {
    NavigationDrawerItem(
        icon = {
            if (badge != null) {
                BadgedBox(badge = badge) {
                    Icon(icon, contentDescription = null)
                }
            } else {
                Icon(icon, contentDescription = null)
            }
        },
        label = { Text(label) },
        selected = isSelected,
        onClick = onClick,
        modifier = Modifier.padding(vertical = 2.dp)
    )
}