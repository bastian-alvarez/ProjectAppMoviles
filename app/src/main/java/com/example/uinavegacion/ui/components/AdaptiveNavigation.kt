package com.example.uinavegacion.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.uinavegacion.navigation.Route

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
            // Header del drawer
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Gamepad,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = if (isAdmin) "Admin Panel" else "GameStore Pro",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
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