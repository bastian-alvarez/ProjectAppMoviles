package com.example.uinavegacion.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.uinavegacion.navigation.*
import com.example.uinavegacion.session.SessionManager
import com.example.uinavegacion.data.local.database.AppDatabase
import com.example.uinavegacion.data.repository.UserRepository
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(nav: NavHostController) {
    val context = LocalContext.current.applicationContext
    val db = remember { AppDatabase.getInstance(context) }
    val userRepo = remember { UserRepository(db.userDao()) }
    val currentEmail by SessionManager.currentUserEmail.collectAsState(initial = null)
    var displayName by remember { mutableStateOf("Usuario Demo") }
    var displayEmail by remember { mutableStateOf("demo@demo.com") }
    var photoUri by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(currentEmail) {
        currentEmail?.let { email ->
            val u = db.userDao().getByEmail(email)
            if (u != null) {
                displayName = u.name
                displayEmail = u.email
                photoUri = u.profilePhotoUri
            }
        }
    }
    Scaffold(
        topBar = { 
            TopAppBar(
                title = { Text("Mi Perfil", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) 
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Información del usuario
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        if (photoUri != null) {
                            AsyncImage(
                                model = photoUri,
                                contentDescription = "Foto de perfil",
                                modifier = Modifier.matchParentSize().clip(CircleShape)
                            )
                        } else {
                            Text(
                                text = "👤",
                                style = MaterialTheme.typography.headlineLarge
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = displayEmail,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Miembro desde: Enero 2024",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Estadísticas del usuario
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Mis Estadísticas",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("12", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Text("Juegos", style = MaterialTheme.typography.bodySmall)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("5", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Text("Órdenes", style = MaterialTheme.typography.bodySmall)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("3", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Text("Favoritos", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            // Acciones del perfil
            Text(
                text = "Configuración",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = { nav.navigate(Route.ProfileEdit.path) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Editar", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Editar Perfil",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Actualiza tu información personal",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text("→", style = MaterialTheme.typography.titleLarge)
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = { nav.navigate(Route.ChangePassword.path) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🔒", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Cambiar Contraseña",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Actualiza tu contraseña de seguridad",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text("→", style = MaterialTheme.typography.titleLarge)
                }
            }

            // Acciones adicionales
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { nav.navigate(Route.Library.path) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Mi Biblioteca")
                }
                OutlinedButton(
                    onClick = { nav.navigate(Route.Orders.path) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Mis Órdenes")
                }
            }

            // Información de la aplicación
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "📱",
                        style = MaterialTheme.typography.displayMedium
                    )
                    Spacer(Modifier.height(12.dp))
                    
                    Text(
                        text = "GameStore App",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(Modifier.height(8.dp))
                    
                    Text(
                        text = "Versión 1.2.5",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(Modifier.height(12.dp))
                    
                    Divider(modifier = Modifier.fillMaxWidth())
                    
                    Spacer(Modifier.height(12.dp))
                    
                    Text(
                        text = "Desarrollado por:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(Modifier.height(8.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "Desarrollador",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "sea.gomez",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "Desarrollador",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "ab.alvarezb",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    
                    Text(
                        text = "Fecha de compilación: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
