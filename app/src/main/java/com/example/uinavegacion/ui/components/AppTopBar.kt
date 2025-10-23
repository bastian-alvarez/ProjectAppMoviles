package com.example.uinavegacion.ui.components

import androidx.compose.material.icons.Icons // Conjunto de íconos Material
import androidx.compose.material.icons.filled.Home // Ícono Home
import androidx.compose.material.icons.filled.AccountCircle // Ícono Login
import androidx.compose.material.icons.filled.Menu // Ícono hamburguesa
import androidx.compose.material.icons.filled.MoreVert // Ícono 3 puntitos (overflow)
import androidx.compose.material.icons.filled.Person // Ícono Registro
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.TextField
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.unit.dp
import androidx.compose.material3.CenterAlignedTopAppBar // TopAppBar centrada
import androidx.compose.material3.DropdownMenu // Menú desplegable
import androidx.compose.material3.DropdownMenuItem // Opción del menú
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon // Para mostrar íconos
import androidx.compose.material3.IconButton // Botones con ícono
import androidx.compose.material3.MaterialTheme // Tema Material
import androidx.compose.material3.Text // Texto
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.* // remember / mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateObserver
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.debounce
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow

@OptIn(ExperimentalMaterial3Api::class)
@Composable // Composable reutilizable: barra superior con búsqueda
fun AppTopBar(
    onOpenDrawer: (() -> Unit)? = null, // Abre el drawer (hamburguesa) - nullable para tablets
    onHome: () -> Unit,       // Navega a Home
    onLogin: () -> Unit,      // Navega a Login
    onRegister: () -> Unit,   // Navega a Registro
    currentQuery: String = "",
    onQueryChanged: (String) -> Unit = {},
    showHamburger: Boolean = true // Controla si mostrar el botón hamburguesa
) {
    var localQuery by remember { mutableStateOf(currentQuery) }

    // Sincronizar el estado local con el estado externo
    LaunchedEffect(currentQuery) {
        if (currentQuery != localQuery) {
            localQuery = currentQuery
        }
    }

    // Debounce para mejor rendimiento
    LaunchedEffect(localQuery) {
        delay(100)
        if (localQuery != currentQuery) {
            onQueryChanged(localQuery)
        }
    }

    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        ),
        title = {
            // Barra de búsqueda mejorada
            OutlinedTextField(
                value = localQuery,
                onValueChange = { newValue -> localQuery = newValue },
                placeholder = { 
                    Text(
                        "Buscar juegos",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyLarge
                    ) 
                },
                leadingIcon = { 
                    Icon(
                        imageVector = Icons.Filled.Search, 
                        contentDescription = "Buscar",
                        tint = MaterialTheme.colorScheme.primary
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        },
        navigationIcon = {
            if (showHamburger && onOpenDrawer != null) {
                IconButton(onClick = onOpenDrawer) {
                    Icon(
                        imageVector = Icons.Filled.Menu, 
                        contentDescription = "Menú",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        },
        actions = { }
    )
}