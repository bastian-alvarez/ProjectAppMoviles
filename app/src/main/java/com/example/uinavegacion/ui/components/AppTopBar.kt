package com.example.uinavegacion.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
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
    var isSearchFocused by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }

    // Animación de entrada
    LaunchedEffect(Unit) {
        visible = true
    }

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

    // Animación de escala para el foco del campo de búsqueda
    val scale by animateFloatAsState(
        targetValue = if (isSearchFocused) 1.02f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "search_scale"
    )

    // Surface personalizada para tener control total del espacio
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(500)) + slideInVertically(
            initialOffsetY = { -it },
            animationSpec = tween(500)
        ),
        exit = fadeOut() + slideOutVertically()
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            color = MaterialTheme.colorScheme.primary,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Botón de menú hamburguesa con animación
                AnimatedVisibility(
                    visible = showHamburger && onOpenDrawer != null,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    IconButton(
                        onClick = onOpenDrawer ?: {},
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Menu,
                            contentDescription = "Menú",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                // Barra de búsqueda con animación
                OutlinedTextField(
                    value = localQuery,
                    onValueChange = { newValue -> 
                        localQuery = newValue
                    },
                    placeholder = {
                        Text(
                            "Buscar juegos",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    leadingIcon = {
                        // Ícono de búsqueda con rotación sutil al escribir
                        val rotation by animateFloatAsState(
                            targetValue = if (localQuery.isNotEmpty()) 360f else 0f,
                            animationSpec = tween(durationMillis = 600),
                            label = "icon_rotation"
                        )
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Buscar",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(22.dp)
                                .graphicsLayer(rotationZ = rotation)
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
                        .height(56.dp)
                        .graphicsLayer(scaleX = scale, scaleY = scale)
                )
            }
        }
    }
}