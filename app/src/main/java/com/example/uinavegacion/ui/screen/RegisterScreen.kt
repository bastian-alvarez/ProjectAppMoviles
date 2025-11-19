package com.example.uinavegacion.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.foundation.text.KeyboardOptions
import com.example.uinavegacion.data.local.database.AppDatabase
import com.example.uinavegacion.data.repository.UserRepository
import com.example.uinavegacion.data.repository.AdminRepository
import com.example.uinavegacion.ui.viewmodel.AuthViewModel
import com.example.uinavegacion.ui.viewmodel.AuthViewModelFactory
import android.widget.Toast
import com.example.uinavegacion.ui.theme.AppColors
import androidx.compose.ui.draw.shadow

// Función helper para colores de campos de texto
@Composable
private fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = AppColors.TextWhite,
    unfocusedTextColor = AppColors.TextWhite,
    focusedBorderColor = AppColors.BrightBlue,
    unfocusedBorderColor = AppColors.LightBlue,
    focusedLabelColor = AppColors.Cyan,
    unfocusedLabelColor = AppColors.AccentBlue,
    cursorColor = AppColors.Cyan,
    focusedTrailingIconColor = AppColors.AccentBlue,
    unfocusedTrailingIconColor = AppColors.AccentBlue
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(nav: NavHostController) {
    // Configurar ViewModel con dependencias
    val localContext = LocalContext.current
    val appContext = localContext.applicationContext
    val db = remember { AppDatabase.getInstance(appContext) }
    val userRepository = remember { UserRepository(db.userDao()) }
    val adminRepository = remember { AdminRepository(db.adminDao()) }
    
    val viewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(
            application = appContext as android.app.Application,
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
            val email = registerState.email.takeIf { it.isNotBlank() }
            val message = email?.let { "Te has registrado con éxito: $it" } ?: "Te has registrado con éxito"
            Toast.makeText(localContext, message, Toast.LENGTH_LONG).show()
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
                    colors = listOf(AppColors.DarkBlue, AppColors.MediumBlue)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.9f)
                .padding(vertical = 24.dp)
                .shadow(12.dp, RoundedCornerShape(20.dp)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = AppColors.MediumBlue
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo/Título - Más compacto
                Text(
                    text = "GameStore Pro",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Cyan
                )
                Text(
                    text = "Crea tu cuenta nueva",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.AccentBlue,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(20.dp))

                // Campo Nickname
                OutlinedTextField(
                    value = registerState.name,
                    onValueChange = viewModel::onNameChange,
                    label = { Text("Nickname", style = MaterialTheme.typography.bodyMedium) },
                    placeholder = { Text("Ej: Gamer123", style = MaterialTheme.typography.bodySmall) },
                    singleLine = true,
                    isError = registerState.nameError != null,
                    supportingText = registerState.nameError?.let { { Text(it, color = AppColors.Red, style = MaterialTheme.typography.bodySmall) } }
                        ?: { Text("Tu nombre de usuario", style = MaterialTheme.typography.bodySmall, color = AppColors.AccentBlue) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = textFieldColors()
                )
                Spacer(Modifier.height(12.dp))

                // Campo Email
                OutlinedTextField(
                    value = registerState.email,
                    onValueChange = viewModel::onRegisterEmailChange,
                    label = { Text("Correo Electrónico", style = MaterialTheme.typography.bodyMedium) },
                    singleLine = true,
                    isError = registerState.emailError != null,
                    supportingText = registerState.emailError?.let { { Text(it, color = AppColors.Red, style = MaterialTheme.typography.bodySmall) } },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = textFieldColors()
                )
                Spacer(Modifier.height(12.dp))

                // Campo Teléfono (Obligatorio con formato chileno)
                OutlinedTextField(
                    value = registerState.phone,
                    onValueChange = viewModel::onPhoneChange,
                    label = { Text("Teléfono Celular *", style = MaterialTheme.typography.bodyMedium) },
                    placeholder = { Text("+569 7777 7777", style = MaterialTheme.typography.bodySmall) },
                    singleLine = true,
                    isError = registerState.phoneError != null,
                    supportingText = registerState.phoneError?.let { { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) } }
                        ?: { Text("Formato: +569 XXXX XXXX", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = textFieldColors()
                )
                Spacer(Modifier.height(12.dp))

                // Campo Contraseña
                OutlinedTextField(
                    value = registerState.pass,
                    onValueChange = viewModel::onRegisterPassChange,
                    label = { Text("Contraseña", style = MaterialTheme.typography.bodyMedium) },
                    singleLine = true,
                    isError = registerState.passError != null,
                    supportingText = registerState.passError?.let { { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) } },
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
                    shape = RoundedCornerShape(10.dp),
                    colors = textFieldColors()
                )
                
                // Indicador de requisitos de contraseña
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Requisitos de contraseña:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(6.dp))
                        
                        // Requisito: Mínimo 8 caracteres
                        PasswordRequirementItem(
                            text = "Mínimo 8 caracteres",
                            isMet = registerState.pass.length >= 8
                        )
                        
                        // Requisito: Una mayúscula
                        PasswordRequirementItem(
                            text = "Al menos una letra mayúscula (A-Z)",
                            isMet = registerState.pass.any { it.isUpperCase() }
                        )
                        
                        // Requisito: Una minúscula
                        PasswordRequirementItem(
                            text = "Al menos una letra minúscula (a-z)",
                            isMet = registerState.pass.any { it.isLowerCase() }
                        )
                        
                        // Requisito: Un número
                        PasswordRequirementItem(
                            text = "Al menos un número (0-9)",
                            isMet = registerState.pass.any { it.isDigit() }
                        )
                        
                        // Requisito: Un carácter especial
                        PasswordRequirementItem(
                            text = "Al menos un símbolo (!@#$%^&*)",
                            isMet = registerState.pass.any { !it.isLetterOrDigit() }
                        )
                        
                        // Requisito: Sin espacios
                        PasswordRequirementItem(
                            text = "Sin espacios en blanco",
                            isMet = !registerState.pass.contains(' ')
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))

                // Campo Confirmar Contraseña
                OutlinedTextField(
                    value = registerState.confirm,
                    onValueChange = viewModel::onConfirmChange,
                    label = { Text("Confirmar Contraseña", style = MaterialTheme.typography.bodyMedium) },
                    singleLine = true,
                    isError = registerState.confirmError != null,
                    supportingText = registerState.confirmError?.let { { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) } },
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
                    shape = RoundedCornerShape(10.dp),
                    colors = textFieldColors()
                )
                
                // Mostrar error general si existe
                if (registerState.errorMsg != null) {
                    Spacer(Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = registerState.errorMsg ?: "",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                
                Spacer(Modifier.height(20.dp))

                // Botón de registro
                Button(
                    onClick = { viewModel.submitRegister() },
                    enabled = registerState.canSubmit && !registerState.isSubmitting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.BrightBlue,
                        contentColor = AppColors.TextWhite,
                        disabledContainerColor = AppColors.LightBlue.copy(alpha = 0.3f),
                        disabledContentColor = AppColors.AccentBlue
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 2.dp,
                        pressedElevation = 4.dp
                    )
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
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                // Botón de login
                OutlinedButton(
                    onClick = { nav.navigate(Route.Login.path) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AppColors.Cyan
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        2.dp, 
                        AppColors.Cyan
                    )
                ){
                    Text(
                        "Ya tengo una cuenta",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

// Componente para mostrar cada requisito de contraseña
@Composable
private fun PasswordRequirementItem(
    text: String,
    isMet: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isMet) Icons.Default.CheckCircle else Icons.Default.Cancel,
            contentDescription = if (isMet) "Requisito cumplido" else "Requisito no cumplido",
            tint = if (isMet) 
                MaterialTheme.colorScheme.tertiary 
            else 
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = if (isMet) 
                MaterialTheme.colorScheme.onSurfaceVariant 
            else 
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            fontWeight = if (isMet) FontWeight.Medium else FontWeight.Normal
        )
    }
}
