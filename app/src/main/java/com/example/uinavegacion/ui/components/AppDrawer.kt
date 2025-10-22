package com.example.uinavegacion.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.uinavegacion.navigation.Route

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
        // 1. Header con información del usuario (se mantiene como lo tenías)
        Column(modifier = Modifier.padding(16.dp)) {
            Surface(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    Icons.Filled.AccountCircle,
                    contentDescription = "Avatar",
                    modifier = Modifier.padding(12.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(if (isAdmin) "Administrador" else "Usuario Demo", style = MaterialTheme.typography.titleMedium)
            Text(if (isAdmin) "admin@steamish.com" else "demo@correo.cl", style = MaterialTheme.typography.bodySmall)
        }

        Divider()

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
                    icon = { Icon(Icons.Filled.ExitToApp, contentDescription = "Cerrar Sesión") },
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
            Divider()
            NavigationDrawerItem(
                label = { Text("Cerrar Sesión") },
                selected = false, // El logout nunca está "seleccionado"
                onClick = onLogout, // Llama a la función de logout del NavGraph
                icon = { Icon(Icons.Filled.ExitToApp, contentDescription = "Cerrar Sesión") },
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
