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
import androidx.compose.ui.text.style.TextOverflow

@OptIn(ExperimentalMaterial3Api::class)
@Composable // Composable reutilizable: barra superior
fun AppTopBar(
    onOpenDrawer: (() -> Unit)? = null, // Abre el drawer (hamburguesa) - nullable para tablets
    onHome: () -> Unit,       // Navega a Home
    onLogin: () -> Unit,      // Navega a Login
    onRegister: () -> Unit,   // Navega a Registro
    currentQuery: String = "",
    onQueryChanged: (String) -> Unit = {},
    showHamburger: Boolean = true // Controla si mostrar el botón hamburguesa
) {
    //lo que hace es crear una variable de estado recordada que le dice a la interfaz
    // si el menú desplegable de 3 puntitos debe estar visible (true) o oculto (false).
    var showMenu by remember { mutableStateOf(false) } // Estado del menú overflow

    CenterAlignedTopAppBar( // Barra alineada al centro
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            MaterialTheme.colorScheme.primary
        ),
        title = {
            var localQuery by remember { mutableStateOf(currentQuery) }

            // Debounce localQuery and propagate after 300ms
            LaunchedEffect(localQuery) {
                // small debounce
                delay(300)
                if (localQuery != currentQuery) {
                    onQueryChanged(localQuery)
                }
            }

            OutlinedTextField(
                value = localQuery,
                onValueChange = { new -> localQuery = new },
                placeholder = { Text("Buscar juegos...", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Buscar", tint = MaterialTheme.colorScheme.onSurface) },
                singleLine = true,
                shape = RoundedCornerShape(50),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.outline
                ),
                trailingIcon = {
                    if (localQuery.isNotBlank()) {
                        IconButton(onClick = {
                            localQuery = ""
                            onQueryChanged("")
                        }) {
                            Icon(imageVector = Icons.Filled.MoreVert, contentDescription = "Limpiar")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(vertical = 6.dp)
            )
        },
        navigationIcon = { // Ícono a la izquierda (hamburguesa)
            if (showHamburger && onOpenDrawer != null) {
                IconButton(onClick = onOpenDrawer) { // Al presionar, abre drawer
                    Icon(imageVector = Icons.Filled.Menu, contentDescription = "Menú") // Ícono
                }
            }
        },
        actions = { /* acciones removidas: no mostrar iconos adicionales */ }
    )
}