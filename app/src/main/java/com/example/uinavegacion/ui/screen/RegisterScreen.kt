package com.example.uinavegacion.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.uinavegacion.navigation.Route
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.foundation.text.KeyboardOptions
import com.example.uinavegacion.data.local.database.AppDatabase
import com.example.uinavegacion.data.repository.UserRepository
import com.example.uinavegacion.data.repository.AdminRepository
import com.example.uinavegacion.ui.viewmodel.AuthViewModel
import com.example.uinavegacion.ui.viewmodel.AuthViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(nav: NavHostController) {
    // Configurar ViewModel con dependencias
    val context = LocalContext.current.applicationContext
    val db = remember { AppDatabase.getInstance(context) }
    val userRepository = remember { UserRepository(db.userDao()) }
    val adminRepository = remember { AdminRepository(db.adminDao()) }
    
    val viewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(
            application = context as android.app.Application,
            userRepository = userRepository,
            adminRepository = adminRepository
        )
    )
    
    // Observar estado del registro
    val registerState by viewModel.register.collectAsState()
    
    var showPass by remember { mutableStateOf(false) }
    var showConfirmPass by remember { mutableStateOf(false) }
    
    // Navegación después de registro exitoso
    LaunchedEffect(registerState.success) {
        if (registerState.success) {
            viewModel.clearRegisterResult()
            nav.navigate(Route.Login.path) {
                popUpTo(Route.Register.path) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.background
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo/Título
                Text(
                    text = "GameStore Pro",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Crea tu cuenta nueva",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(32.dp))

                // Campo Nombre
                OutlinedTextField(
                    value = registerState.name,
                    onValueChange = viewModel::onNameChange,
                    label = { Text("Nombre Completo") },
                    singleLine = true,
                    isError = registerState.nameError != null,
                    supportingText = registerState.nameError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(16.dp))

                // Campo Email
                OutlinedTextField(
                    value = registerState.email,
                    onValueChange = viewModel::onRegisterEmailChange,
                    label = { Text("Correo Electrónico") },
                    singleLine = true,
                    isError = registerState.emailError != null,
                    supportingText = registerState.emailError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(16.dp))

                // Campo Teléfono
                OutlinedTextField(
                    value = registerState.phone,
                    onValueChange = viewModel::onPhoneChange,
                    label = { Text("Teléfono") },
                    singleLine = true,
                    isError = registerState.phoneError != null,
                    supportingText = registerState.phoneError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(16.dp))

                // Campo Contraseña
                OutlinedTextField(
                    value = registerState.pass,
                    onValueChange = viewModel::onRegisterPassChange,
                    label = { Text("Contraseña") },
                    singleLine = true,
                    isError = registerState.passError != null,
                    supportingText = registerState.passError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    visualTransformation = if (showPass) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showPass = !showPass }) {
                            Icon(
                                imageVector = if (showPass) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (showPass) "Ocultar contraseña" else "Mostrar contraseña"
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(16.dp))

                // Campo Confirmar Contraseña
                OutlinedTextField(
                    value = registerState.confirm,
                    onValueChange = viewModel::onConfirmChange,
                    label = { Text("Confirmar Contraseña") },
                    singleLine = true,
                    isError = registerState.confirmError != null,
                    supportingText = registerState.confirmError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    visualTransformation = if (showConfirmPass) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showConfirmPass = !showConfirmPass }) {
                            Icon(
                                imageVector = if (showConfirmPass) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (showConfirmPass) "Ocultar contraseña" else "Mostrar contraseña"
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                
                // Mostrar error general si existe
                if (registerState.errorMsg != null) {
                    Spacer(Modifier.height(16.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = registerState.errorMsg ?: "",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                
                Spacer(Modifier.height(24.dp))

                // Botón de registro
                Button(
                    onClick = { viewModel.submitRegister() },
                    enabled = registerState.canSubmit && !registerState.isSubmitting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (registerState.isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(
                            "Crear Cuenta",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                // Botón de login
                OutlinedButton(
                    onClick = { nav.navigate(Route.Login.path) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ){
                    Text(
                        "Ya tengo una cuenta",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
