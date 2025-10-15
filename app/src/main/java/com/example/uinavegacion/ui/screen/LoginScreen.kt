package com.example.uinavegacion.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.uinavegacion.navigation.Route
import com.example.uinavegacion.ui.viewmodel.AuthViewModel
import androidx.compose.foundation.background
import androidx.compose.ui.Alignment
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff

@Composable
fun LoginScreenVm(
    onLoginOkNavigateHome: () -> Unit,
    onGoRegister: () -> Unit,
    navController: NavHostController
){
    val vm: AuthViewModel = viewModel()
    val state by vm.login.collectAsStateWithLifecycle()

    if (state.success) {
        vm.clearLoginResult()
        // Navegar según el tipo de usuario
        if (state.isAdmin) {
            navController.navigate(Route.AdminDashboard.path)
        } else {
            onLoginOkNavigateHome()
        }
    }
    LoginScreen(
        email = state.email,
        pass = state.pass,
        emailError = state.emailError,
        passError = state.passError,
        isSubmitting = state.isSubmitting,
        canSubmit = state.canSubmit,
        errorMsg = state.errorMsg,
        onEmailChange = vm::onLoginEmailChange,
        onPassChange = vm::onLoginPassChange,
        onLogin = vm::submitLogin,
        onGoRegister = onGoRegister
    )

}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoginScreen(
    email: String,
    pass: String,
    emailError: String?,
    passError: String?,
    isSubmitting: Boolean,
    canSubmit: Boolean,
    errorMsg: String?,
    onEmailChange: (String) -> Unit,
    onPassChange: (String) -> Unit,
    onLogin: () -> Unit,
    onGoRegister: () -> Unit
) {
    val bg = MaterialTheme.colorScheme.secondaryContainer // Fondo distinto para contraste
    var showPass by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize() // Ocupa todo
            .background(bg) // Fondo
            .padding(16.dp), // Margen
        contentAlignment = Alignment.Center // Centro
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally // Centrado horizontal
        ) {
            Text(
                text = "Login",
                style = MaterialTheme.typography.headlineSmall // Título
            )
            Spacer(Modifier.height(12.dp)) // Separación
            Text(
                text = "Pantalla de Login (demo). Usa la barra superior, el menú lateral o los botones.",
                textAlign = TextAlign.Center // Alineación centrada
            )
            Spacer(Modifier.height(20.dp)) // Separación

            //agregamos formulario
            //input del email
            OutlinedTextField(
                value = email, // en que variable guarda
                onValueChange = onEmailChange, //que ejecuta cuando cambie su valor
                label = { Text("Correo Electronico") }, //texto a mostrar
                singleLine = true, //input de solo una linea
                isError = emailError != null, //si hay error
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email, //tipo de teclado a mostrar en el telefono
                ),
                modifier = Modifier.fillMaxWidth() //tamaño del input

            )
            if(emailError != null){
                Text(emailError, color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall)
            }
            Spacer(Modifier.height(8.dp)) // Separación


            //input para la contraseña
            OutlinedTextField(
                value = pass,
                onValueChange = onPassChange,
                label = { Text("Contraseña") },
                singleLine = true,
                //transformacion del texto visible/tipo password
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
                isError = passError != null,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                ),

            )
            if(passError != null){
                Text(passError, color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall)
            }
            Spacer(Modifier.height(16.dp)) // Separación

            //agregar los borones del formulario
            Button(
                onClick = onLogin,
                enabled = canSubmit && !isSubmitting,
                modifier = Modifier.fillMaxWidth()
            ) {
                if(isSubmitting){
                 CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                 Spacer(Modifier.width(8.dp)) // Separación
                 Text("Iniciando sesion...")
                }
                else{
                    Text("Entrar")
                }
            }
            if(errorMsg != null){
                Spacer(Modifier.height(8.dp)) // Separación
                Text(errorMsg, color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(12.dp)) // Separación
            OutlinedButton(
                onClick = onGoRegister,
                modifier = Modifier.fillMaxWidth()
            ){
                Text("Crear Cuenta")
            }



        }
    }
}




