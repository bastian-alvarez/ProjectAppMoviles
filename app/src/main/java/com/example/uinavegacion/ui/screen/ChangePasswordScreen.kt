package com.example.uinavegacion.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.uinavegacion.data.SessionManager
import com.example.uinavegacion.data.local.database.AppDatabase
import com.example.uinavegacion.ui.theme.AppColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Colores del tema profesional
private val DarkBlue = AppColors.DarkBlue
private val MediumBlue = AppColors.MediumBlue
private val LightBlue = AppColors.LightBlue
private val AccentBlue = AppColors.AccentBlue
private val BrightBlue = AppColors.BrightBlue
private val Cyan = AppColors.Cyan

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(nav: NavHostController) {
    // Estados
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    var showCurrentPassword by remember { mutableStateOf(false) }
    var showNewPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    
    var currentPasswordError by remember { mutableStateOf<String?>(null) }
    var newPasswordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    
    var isLoading by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val userEmail = SessionManager.getCurrentUserEmail()
    
    // Función de actualización
    fun updatePassword() {
        currentPasswordError = null
        newPasswordError = null
        confirmPasswordError = null
        
        var hasError = false
        
        if (currentPassword.isBlank()) {
            currentPasswordError = "Requerido"
            hasError = true
        }
        
        if (newPassword.isBlank()) {
            newPasswordError = "Requerido"
            hasError = true
        } else if (newPassword.length < 8) {
            newPasswordError = "Mínimo 8 caracteres"
            hasError = true
        } else if (newPassword == currentPassword) {
            newPasswordError = "Debe ser diferente"
            hasError = true
        }
        
        if (confirmPassword.isBlank()) {
            confirmPasswordError = "Requerido"
            hasError = true
        } else if (confirmPassword != newPassword) {
            confirmPasswordError = "No coinciden"
            hasError = true
        }
        
        if (hasError || userEmail == null) return
        
        isLoading = true
        errorMessage = null
        successMessage = null
        
        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val user = db.userDao().getByEmail(userEmail)
                    
                    if (user == null) {
                        withContext(Dispatchers.Main) {
                            errorMessage = "Usuario no encontrado"
                            isLoading = false
                        }
                        return@withContext
                    }
                    
                    if (user.password != currentPassword) {
                        withContext(Dispatchers.Main) {
                            currentPasswordError = "Incorrecta"
                            errorMessage = "Contraseña actual incorrecta"
                            isLoading = false
                        }
                        return@withContext
                    }
                    
                    db.userDao().updatePassword(user.id, newPassword)
                    
                    withContext(Dispatchers.Main) {
                        successMessage = "Contraseña actualizada"
                        isLoading = false
                        kotlinx.coroutines.delay(1500)
                        currentPassword = ""
                        newPassword = ""
                        confirmPassword = ""
                        successMessage = null
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    errorMessage = "Error: ${e.message}"
                    isLoading = false
                }
            }
        }
    }
    
    Scaffold(
        containerColor = DarkBlue,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Cambiar Contraseña", 
                        fontWeight = FontWeight.Bold,
                        color = androidx.compose.ui.graphics.Color.White
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBlue,
                    titleContentColor = androidx.compose.ui.graphics.Color.White,
                    navigationIconContentColor = androidx.compose.ui.graphics.Color.White
                ),
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Volver", tint = androidx.compose.ui.graphics.Color.White)
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 500.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                
                // Icono decorativo
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                // Título y descripción
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Actualiza tu contraseña",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Mantén tu cuenta segura",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Campos de contraseña
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    
                    // Contraseña actual
                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = { 
                            currentPassword = it
                            currentPasswordError = null
                            errorMessage = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Contraseña Actual") },
                        placeholder = { Text("Ingresa tu contraseña actual") },
                        visualTransformation = if (showCurrentPassword) 
                            VisualTransformation.None 
                        else 
                            PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showCurrentPassword = !showCurrentPassword }) {
                                Icon(
                                    if (showCurrentPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null
                                )
                            }
                        },
                        isError = currentPasswordError != null,
                        supportingText = currentPasswordError?.let { 
                            { Text(it) }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    // Nueva contraseña
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { 
                            newPassword = it
                            newPasswordError = null
                            errorMessage = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Nueva Contraseña") },
                        placeholder = { Text("Mínimo 8 caracteres") },
                        visualTransformation = if (showNewPassword) 
                            VisualTransformation.None 
                        else 
                            PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showNewPassword = !showNewPassword }) {
                                Icon(
                                    if (showNewPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null
                                )
                            }
                        },
                        isError = newPasswordError != null,
                        supportingText = newPasswordError?.let { 
                            { Text(it) }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    // Confirmar contraseña
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { 
                            confirmPassword = it
                            confirmPasswordError = null
                            errorMessage = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Confirmar Contraseña") },
                        placeholder = { Text("Repite la nueva contraseña") },
                        visualTransformation = if (showConfirmPassword) 
                            VisualTransformation.None 
                        else 
                            PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                                Icon(
                                    if (showConfirmPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null
                                )
                            }
                        },
                        isError = confirmPasswordError != null,
                        supportingText = confirmPasswordError?.let { 
                            { Text(it) }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                
                // Mensaje de éxito
                successMessage?.let { message ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF4CAF50).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = message,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF1B5E20)
                            )
                        }
                    }
                }
                
                // Mensaje de error
                errorMessage?.let { message ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = message,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
                
                // Botones
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Botón principal
                    Button(
                        onClick = { updatePassword() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading && 
                                 currentPassword.isNotBlank() && 
                                 newPassword.isNotBlank() && 
                                 confirmPassword.isNotBlank(),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "Actualizando...",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "Actualizar Contraseña",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    // Botón cancelar
                    TextButton(
                        onClick = { nav.popBackStack() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        enabled = !isLoading
                    ) {
                        Text(
                            "Cancelar",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
