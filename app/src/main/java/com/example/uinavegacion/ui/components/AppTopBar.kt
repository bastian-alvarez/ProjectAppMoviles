package com.example.uinavegacion.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    onOpenDrawer: (() -> Unit)? = null,
    onHome: () -> Unit,
    onLogin: () -> Unit,
    onRegister: () -> Unit,
    currentQuery: String = "",
    onQueryChanged: (String) -> Unit = {},
    showHamburger: Boolean = true
) {
    var localQuery by remember { mutableStateOf(currentQuery) }

    LaunchedEffect(currentQuery) {
        if (currentQuery != localQuery) {
            localQuery = currentQuery
        }
    }

    LaunchedEffect(localQuery) {
        delay(100)
        if (localQuery != currentQuery) {
            onQueryChanged(localQuery)
        }
    }

    // Surface personalizada para tener control total del espacio
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp), // Altura aumentada significativamente
        color = MaterialTheme.colorScheme.primary,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 12.dp), // Padding aumentado
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Botón de menú hamburguesa
            if (showHamburger && onOpenDrawer != null) {
                IconButton(
                    onClick = onOpenDrawer,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Menu,
                        contentDescription = "Menú",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            // Barra de búsqueda con espacio completo
            OutlinedTextField(
                value = localQuery,
                onValueChange = { newValue -> localQuery = newValue },
                placeholder = {
                    Text(
                        "Buscar juegos",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Buscar",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(28.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.White,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    focusedPlaceholderColor = Color.Gray,
                    unfocusedPlaceholderColor = Color.Gray
                ),
                textStyle = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp) // Altura aumentada significativamente
            )
        }
    }
}