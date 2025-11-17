package com.example.uinavegacion

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.uinavegacion.ui.theme.UINavegacionTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.rememberNavController
import com.example.uinavegacion.navigation.AppNavGraph
import com.example.uinavegacion.ui.screen.SyncSplashScreen
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.Box
import com.example.uinavegacion.data.local.database.AppDatabase
import android.app.Application
import com.example.uinavegacion.data.local.user.UserDao
import com.example.uinavegacion.data.local.juego.JuegoDao
import com.example.uinavegacion.data.local.ordenCompra.OrdenCompraDao
import com.example.uinavegacion.data.local.detalle.DetalleDao
import com.example.uinavegacion.data.local.admin.AdminDao
import com.example.uinavegacion.data.repository.UserRepository
import com.example.uinavegacion.data.repository.AdminRepository
import com.example.uinavegacion.data.repository.GameRepository
import com.example.uinavegacion.data.SyncPreferences
import com.example.uinavegacion.ui.viewmodel.AuthViewModel
import com.example.uinavegacion.ui.viewmodel.AuthViewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            AppRoot()
        }
    }
}


/*
* En Compose, Surface es un contenedor visual que viene de Material 3.Crea un bloque
*  que puedes personalizar con color, forma, sombra (elevación).
Sirve para aplicar un fondo (color, borde, elevación, forma) siguiendo las guías de diseño
* de Material.
Piensa en él como una “lona base” sobre la cual vas a pintar tu UI.
* Si cambias el tema a dark mode, colorScheme.background
* cambia automáticamente y el Surface pinta la pantalla con el nuevo color.
* */
@Composable // Indica que esta función dibuja UI
fun AppRoot() { // Raíz de la app para separar responsabilidades
    // Contexto de la app
    val context = LocalContext.current.applicationContext
    // Instancia única de la base de datos
    val db = AppDatabase.getInstance(context)

    // Estado para controlar el splash de sincronización
    var isSyncing by remember { mutableStateOf(false) }
    var syncCompleted by remember { mutableStateOf(false) }
    
    // Verificar si necesita sincronización inicial
    LaunchedEffect(Unit) {
        if (!SyncPreferences.areGamesSynced(context)) {
            isSyncing = true
            kotlinx.coroutines.delay(500) // Pequeño delay para mostrar el splash
            
            try {
                val gameRepository = GameRepository(db.juegoDao())
                val result = gameRepository.exportLocalGamesToRemote()
                
                if (result.isSuccess) {
                    SyncPreferences.markGamesSynced(context)
                    Log.i("AppRoot", "✅ Sincronización inicial completada")
                }
            } catch (e: Exception) {
                Log.e("AppRoot", "Error en sincronización: ${e.message}", e)
            } finally {
                kotlinx.coroutines.delay(1000) // Mostrar el splash un poco más
                isSyncing = false
                syncCompleted = true
            }
        } else {
            syncCompleted = true
        }
    }

    // DAOs
    val userDao: UserDao = db.userDao()
    val adminDao: AdminDao = db.adminDao()
    val gameDao: JuegoDao = db.juegoDao()
    val orderDao: OrdenCompraDao = db.ordenCompraDao()
    val orderDetailDao: DetalleDao = db.detalleDao()

    // Repositorios
    val userRepository = UserRepository(userDao)
    val adminRepository = AdminRepository(adminDao)


    // ViewModel de autenticación con factory (Room)
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(context as Application, userRepository, adminRepository)

    )

    val navController = rememberNavController() // Controlador de navegación
    UINavegacionTheme { // Usa nuestro tema personalizado con colores azules
        Surface(color = MaterialTheme.colorScheme.background) { // Fondo general
            Box {
                // Contenido principal (siempre renderizado pero puede estar oculto)
                if (syncCompleted) {
                    AppNavGraph(navController = navController) // Carga el NavHost + Scaffold + Drawer
                }
                
                // Splash de sincronización (se muestra encima si está activo)
                SyncSplashScreen(isVisible = isSyncing)
            }
        }
    }
}