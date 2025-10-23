package com.example.uinavegacion.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.uinavegacion.data.local.database.AppDatabase
import com.example.uinavegacion.data.repository.AdminRepository
import com.example.uinavegacion.data.repository.UserRepository
import com.example.uinavegacion.data.SessionManager
import com.example.uinavegacion.ui.viewmodel.AuthViewModel
import com.example.uinavegacion.ui.viewmodel.AuthViewModelFactory
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(nav: NavHostController) {
    val context = LocalContext.current.applicationContext
    val db = remember { AppDatabase.getInstance(context) }
    val userRepo = remember { UserRepository(db.userDao()) }
    val adminRepo = remember { AdminRepository(db.adminDao()) }
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(context.applicationContext as android.app.Application, userRepo, adminRepo))
    
    val changePasswordState by authViewModel.changePassword.collectAsState()
    
    var showCurrentPassword by remember { mutableStateOf(false) }
    var showNewPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    
    // Obtener el email del usuario logueado desde SessionManager
    val userEmail = SessionManager.getCurrentUserEmail() ?: "user1@demo.com"
    
    // Efecto para manejar el logout automático después del cambio exitoso
    LaunchedEffect(changePasswordState.success) {
        if (changePasswordState.success && changePasswordState.successMsg?.contains("redirigido") == true) {
            delay(2000)
            // Limpiar estado del cambio de contraseña
            authViewModel.clearChangePasswordResult()
            
            // Redirigir al login
            nav.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        }
    }
    
    Scaffold(
        topBar = { 
            TopAppBar(
                title = { Text("Cambiar Contraseña", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            ) 
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            
            Spacer(Modifier.height(16.dp))
            
            // Información de seguridad mejorada
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Security,
                        contentDescription = "Seguridad",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "Cambia tu contraseña para mantener tu cuenta segura",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Formulario de cambio de contraseña mejorado
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Text(
                        text = "Cambiar Contraseña",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    // Contraseña actual
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Contraseña actual",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        OutlinedTextField(
                            value = changePasswordState.currentPassword,
                            onValueChange = { authViewModel.onCurrentPasswordChange(it) },
                            placeholder = { Text("Ingresa tu contraseña actual") },
                            visualTransformation = if (showCurrentPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showCurrentPassword = !showCurrentPassword }) {
                                    Icon(
                                        if (showCurrentPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (showCurrentPassword) "Ocultar contraseña" else "Mostrar contraseña",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            isError = changePasswordState.currentPasswordError != null,
                            supportingText = changePasswordState.currentPasswordError?.let { { Text(it) } },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // Nueva contraseña
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Nueva contraseña",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        OutlinedTextField(
                            value = changePasswordState.newPassword,
                            onValueChange = { authViewModel.onNewPasswordChange(it) },
                            placeholder = { Text("Ingresa tu nueva contraseña") },
                            visualTransformation = if (showNewPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showNewPassword = !showNewPassword }) {
                                    Icon(
                                        if (showNewPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (showNewPassword) "Ocultar contraseña" else "Mostrar contraseña",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            isError = changePasswordState.newPasswordError != null,
                            supportingText = changePasswordState.newPasswordError?.let { { Text(it) } },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // Confirmar nueva contraseña
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Confirmar nueva contraseña",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        OutlinedTextField(
                            value = changePasswordState.confirmPassword,
                            onValueChange = { authViewModel.onConfirmPasswordChange(it) },
                            placeholder = { Text("Confirma tu nueva contraseña") },
                            visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                                    Icon(
                                        if (showConfirmPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (showConfirmPassword) "Ocultar contraseña" else "Mostrar contraseña",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            isError = changePasswordState.confirmPasswordError != null,
                            supportingText = changePasswordState.confirmPasswordError?.let { { Text(it) } },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // Mensajes de error y éxito mejorados
                    changePasswordState.errorMsg?.let { errorMsg ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Error,
                                    contentDescription = "Error",
                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = errorMsg,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    changePasswordState.successMsg?.let { successMsg ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Éxito",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = successMsg,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Botones de acción mejorados
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { nav.popBackStack() },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Cancelar", fontWeight = FontWeight.Medium)
                        }
                        
                        Button(
                            onClick = {
                                authViewModel.submitChangePassword(userEmail)
                            },
                            modifier = Modifier.weight(1f).height(48.dp),
                            enabled = changePasswordState.canSubmit && !changePasswordState.isSubmitting,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (changePasswordState.isSubmitting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(Modifier.width(8.dp))
                            }
                            Text("Actualizar", fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
