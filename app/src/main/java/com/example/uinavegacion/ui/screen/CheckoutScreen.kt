package com.example.uinavegacion.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.uinavegacion.navigation.Route
import com.example.uinavegacion.viewmodel.CartViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.example.uinavegacion.data.SessionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(nav: NavHostController, cartViewModel: CartViewModel = viewModel()) {
    val items by cartViewModel.items.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var isProcessing by remember { mutableStateOf(false) }
    val error by cartViewModel.errorMessage.collectAsState()
    val success by cartViewModel.successMessage.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            cartViewModel.clearErrorMessage()
        }
    }

    LaunchedEffect(success) {
        success?.let {
            snackbarHostState.showSnackbar(it)
            cartViewModel.clearSuccessMessage()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Checkout") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { inner ->
        Column(Modifier.padding(inner).padding(16.dp)) {
            Text("Resumen de compra")
            Spacer(Modifier.height(12.dp))
            Text("Total de artículos: ${items.sumOf { it.quantity }}")
            Spacer(Modifier.height(4.dp))
            Text("Monto total: $${String.format("%.2f", items.sumOf { it.price * it.quantity })}")
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    val userId = SessionManager.getCurrentUserId()
                    val remoteUserId = SessionManager.getCurrentUserRemoteId()
                    
                    if (userId == null) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Debes iniciar sesión para realizar una compra")
                        }
                        return@Button
                    }
                    
                    isProcessing = true
                    cartViewModel.checkout(
                        context = context,
                        userId = userId,
                        remoteUserId = remoteUserId,
                        metodoPago = "Tarjeta",
                        direccionEnvio = null
                    ) { successResult, message ->
                        isProcessing = false
                        if (message != null) {
                            scope.launch { snackbarHostState.showSnackbar(message) }
                        }
                        if (successResult) {
                            nav.navigate(Route.Home.path) {
                                popUpTo(Route.Cart.path) { inclusive = true }
                            }
                        }
                    }
                },
                enabled = items.isNotEmpty() && !isProcessing,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                }
                Text(if (isProcessing) "Procesando…" else "Confirmar compra")
            }
        }
    }
}
