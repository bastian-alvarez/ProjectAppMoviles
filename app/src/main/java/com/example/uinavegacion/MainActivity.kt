package com.example.uinavegacion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.example.uinavegacion.navigation.AppNavGraph
import androidx.compose.ui.platform.LocalContext
import com.example.uinavegacion.data.local.database.AppDatabase
import android.app.Application
import com.example.uinavegacion.data.local.user.UserDao
import com.example.uinavegacion.data.local.juego.JuegoDao
import com.example.uinavegacion.data.local.ordenCompra.OrdenCompraDao
import com.example.uinavegacion.data.local.detalle.DetalleDao
import com.example.uinavegacion.data.local.admin.AdminDao
import com.example.uinavegacion.data.repository.UserRepository
import com.example.uinavegacion.data.repository.AdminRepository
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
    MaterialTheme { // Provee colores/tipografías Material 3
        Surface(color = MaterialTheme.colorScheme.background) { // Fondo general
            AppNavGraph(navController = navController) // Carga el NavHost + Scaffold + Drawer
        }
    }
}