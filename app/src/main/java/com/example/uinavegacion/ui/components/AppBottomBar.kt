package com.example.uinavegacion.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Arrangement

@Composable
fun AppBottomBar(
    currentRoute: String?,
    onHome: () -> Unit,
    onGames: () -> Unit,
    onCart: () -> Unit,
    cartCount: Int = 0
) {
    // Use full width and center the items so icons appear centered on tablets
    NavigationBar(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            NavigationBarItem(
                selected = currentRoute?.contains("home") == true,
                onClick = onHome,
                icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                label = { Text("Home") }
            )

            NavigationBarItem(
                selected = currentRoute?.contains("games") == true,
                onClick = onGames,
                icon = { Icon(Icons.Filled.SportsEsports, contentDescription = "Juegos") },
                label = { Text("Juegos") }
            )

            NavigationBarItem(
                selected = currentRoute?.contains("cart") == true,
                onClick = onCart,
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
                label = { Text("Carrito") }
            )
        }
    }
}
