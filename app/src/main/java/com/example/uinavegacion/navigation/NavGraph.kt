package com.example.uinavegacion.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.uinavegacion.ui.components.*
import com.example.uinavegacion.ui.screen.*
import com.example.uinavegacion.ui.utils.*
import com.example.uinavegacion.viewmodel.CartViewModel
import com.example.uinavegacion.viewmodel.LibraryViewModel
import com.example.uinavegacion.viewmodel.LibraryViewModelFactory
import com.example.uinavegacion.viewmodel.SearchViewModel
import com.example.uinavegacion.data.SessionManager
import com.example.uinavegacion.data.local.database.AppDatabase
import com.example.uinavegacion.data.repository.LibraryRepository
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import android.app.Application
import kotlinx.coroutines.launch

@Composable
fun AppNavGraph(navController: NavHostController) {
    
    // Información del tamaño de ventana para diseño adaptativo
    val windowInfo = rememberWindowInfo()
    
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    // Contexto y base de datos para LibraryViewModel
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val libraryRepository = remember { LibraryRepository(db.libraryDao()) }

    // ViewModel compartidos
    val cartViewModel: CartViewModel = viewModel()
    val libraryViewModel: LibraryViewModel = viewModel(
        factory = LibraryViewModelFactory(
            application = context.applicationContext as Application,
            libraryRepository = libraryRepository
        )
    )
    val searchViewModel: SearchViewModel = viewModel()

    // Obtener la ruta actual para saber qué pantalla está activa
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Determinar si la ruta actual es de administrador
    val isAdminView = currentRoute?.startsWith("admin") == true

    // --- LÓGICA DE NAVEGACIÓN MEJORADA ---
    // Función genérica para navegar desde el menú
    val navigateAndCloseDrawer: (String) -> Unit = { route ->
        scope.launch { drawerState.close() } // Primero cierra el drawer
        navController.navigate(route) {
            launchSingleTop = true // Evita apilar la misma pantalla
            restoreState = true
        }
    }

    // Acción para cerrar sesión
    val onLogout: () -> Unit = {
        scope.launch { drawerState.close() }
        SessionManager.logout() // Limpiar sesión
        navController.navigate(Route.Login.path) {
            // Limpia todo el historial de navegación para que el usuario no pueda volver atrás
            popUpTo(0) { inclusive = true }
        }
    }

    // Verificar si estamos en pantallas de autenticación
    val isAuthScreen = currentRoute in listOf(
        Route.Splash.path, 
        Route.Login.path, 
        Route.Register.path, 
        Route.ForgotPassword.path, 
        Route.VerifyEmail.path
    )
    
    // Configurar navegación según el tipo de dispositivo
    when (windowInfo.navigationType) {
        NavigationType.PERMANENT_NAVIGATION_DRAWER -> {
            // Drawer permanente para tablets grandes y desktop
            if (isAuthScreen) {
                // Sin drawer en pantallas de autenticación
                AdaptiveScaffold(
                    windowInfo = windowInfo,
                    navController = navController,
                    currentRoute = currentRoute,
                    isAdminView = isAdminView,
                    cartViewModel = cartViewModel,
                    libraryViewModel = libraryViewModel,
                    searchViewModel = searchViewModel,
                    showNavigationElements = false
                )
            } else {
                Row(modifier = Modifier.fillMaxSize()) {
                    AppPermanentNavigationDrawer(
                        currentRoute = currentRoute,
                        onNavigate = navigateAndCloseDrawer,
                        onLogout = onLogout,
                        isAdmin = isAdminView,
                        cartCount = cartViewModel.getTotalItems()
                    )
                    
                    AdaptiveScaffold(
                        windowInfo = windowInfo,
                        navController = navController,
                        currentRoute = currentRoute,
                        isAdminView = isAdminView,
                        cartViewModel = cartViewModel,
                        libraryViewModel = libraryViewModel,
                        searchViewModel = searchViewModel,
                        showNavigationElements = false,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        
        NavigationType.NAVIGATION_RAIL -> {
            // Navigation Rail para tablets medianos
            Row(modifier = Modifier.fillMaxSize()) {
                val showNavigationRail = currentRoute in listOf(
                    Route.Home.path, Route.Games.path, Route.Library.path, Route.Cart.path, Route.Profile.path
                ) && !isAdminView && !isAuthScreen
                
                if (showNavigationRail) {
                    AppNavigationRail(
                        currentRoute = currentRoute,
                        onHome = { navController.navigate(Route.Home.path) { launchSingleTop = true } },
                        onGames = { navController.navigate(Route.Games.build()) { launchSingleTop = true } },
                        onCart = { navController.navigate(Route.Cart.path) { launchSingleTop = true } },
                        onProfile = { navController.navigate(Route.Profile.path) { launchSingleTop = true } },
                        cartCount = cartViewModel.getTotalItems()
                    )
                }
                
                AdaptiveScaffold(
                    windowInfo = windowInfo,
                    navController = navController,
                    currentRoute = currentRoute,
                    isAdminView = isAdminView,
                    cartViewModel = cartViewModel,
                    libraryViewModel = libraryViewModel,
                    searchViewModel = searchViewModel,
                    showNavigationElements = true,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        NavigationType.BOTTOM_NAVIGATION -> {
            // Navegación tradicional con drawer modal para teléfonos
            if (isAdminView || isAuthScreen) {
                // Para administradores o pantallas de autenticación: Sin drawer modal
                AdaptiveScaffold(
                    windowInfo = windowInfo,
                    navController = navController,
                    currentRoute = currentRoute,
                    isAdminView = isAdminView,
                    cartViewModel = cartViewModel,
                    libraryViewModel = libraryViewModel,
                    searchViewModel = searchViewModel,
                    showNavigationElements = true,
                    onOpenDrawer = null // No drawer para admin ni auth
                )
            } else {
                // Para usuarios normales: Con drawer modal
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        AppDrawer(
                            currentRoute = currentRoute,
                            onNavigate = navigateAndCloseDrawer,
                            onLogout = onLogout,
                            isAdmin = isAdminView,
                            cartCount = cartViewModel.getTotalItems()
                        )
                    }
                ) {
                    AdaptiveScaffold(
                        windowInfo = windowInfo,
                        navController = navController,
                        currentRoute = currentRoute,
                        isAdminView = isAdminView,
                        cartViewModel = cartViewModel,
                        libraryViewModel = libraryViewModel,
                        searchViewModel = searchViewModel,
                        showNavigationElements = true,
                        onOpenDrawer = { scope.launch { drawerState.open() } }
                    )
                }
            }
        }
    }
}

@Composable
private fun AdaptiveScaffold(
    windowInfo: WindowInfo,
    navController: NavHostController,
    currentRoute: String?,
    isAdminView: Boolean,
    cartViewModel: CartViewModel,
    searchViewModel: SearchViewModel,
    libraryViewModel: com.example.uinavegacion.viewmodel.LibraryViewModel,
    showNavigationElements: Boolean,
    modifier: Modifier = Modifier,
    onOpenDrawer: (() -> Unit)? = null
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            // Ocultamos la TopBar en las pantallas de autenticación y splash
            val showTopBar = currentRoute !in listOf(
                Route.Splash.path, Route.Login.path, Route.Register.path, Route.ForgotPassword.path, Route.VerifyEmail.path
            ) && !isAdminView && showNavigationElements
            
            if (showTopBar) {
                AppTopBar(
                    onOpenDrawer = onOpenDrawer,
                    onHome = { navController.navigate(Route.Home.path) },
                    onLogin = { navController.navigate(Route.Login.path) },
                    onRegister = { navController.navigate(Route.Register.path) },
                    currentQuery = searchViewModel.query.collectAsState().value,
                    onQueryChanged = { query ->
                        searchViewModel.setQuery(query)
                        if (query.isNotBlank()) {
                            navController.navigate(Route.Games.build())
                        }
                    },
                    showHamburger = windowInfo.navigationType == NavigationType.BOTTOM_NAVIGATION
                )
            }
        },
        bottomBar = {
            // Solo mostrar bottom bar en navegación de tipo BOTTOM_NAVIGATION
            val showBottomBar = currentRoute in listOf(
                Route.Home.path, Route.Games.path, Route.Library.path, Route.Cart.path, Route.Profile.path
            ) && windowInfo.navigationType == NavigationType.BOTTOM_NAVIGATION && !isAdminView
            
            if (showBottomBar) {
                AppBottomBar(
                    currentRoute = currentRoute,
                    onHome = { navController.navigate(Route.Home.path) { launchSingleTop = true } },
                    onGames = { navController.navigate(Route.Games.build()) { launchSingleTop = true } },
                    onCart = { navController.navigate(Route.Cart.path) { launchSingleTop = true } },
                    cartCount = cartViewModel.getTotalItems()
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Route.Login.path,
            modifier = Modifier.padding(innerPadding)
        ) {
                // Todas tus rutas se mantienen exactamente igual que antes
                // ===== Core =====
                composable(Route.Splash.path) { SplashScreen(navController) }
                composable(Route.Home.path) { HomeScreen(navController, cartViewModel) }

                // ===== Auth =====
                composable(Route.Login.path) {
                    LoginScreenVm(
                        // Cuando el login es exitoso, navegamos a Home y limpiamos el historial
                        onLoginOkNavigateHome = {
                            navController.navigate(Route.Home.path) {
                                popUpTo(Route.Login.path) { inclusive = true }
                            }
                        },
                        onGoRegister = { navController.navigate(Route.Register.path) },
                        navController = navController
                    )
                }
                composable(Route.Register.path) {
                    RegisterScreen(
                        nav = navController
                    )
                }
                composable(Route.ForgotPassword.path) { ForgotPasswordScreen(navController) }
                composable(
                    route = Route.VerifyEmail.path,
                    arguments = listOf(navArgument("email") {
                        type = NavType.StringType
                        defaultValue = ""
                        nullable = true
                    })
                ) { backStack ->
                    val email = backStack.arguments?.getString("email").orEmpty()
                    VerifyEmailScreen(navController, email)
                }

                // ===== Perfil =====
                composable(Route.Profile.path) { ProfileScreen(navController) }
                composable(Route.ProfileEdit.path) { ProfileEditScreen(navController) }
                composable(Route.ChangePassword.path) { ChangePasswordScreen(navController) }

                // ===== Catálogo =====
                composable(
                    route = Route.Games.path,
                    arguments = listOf(navArgument("category") { 
                        type = NavType.StringType
                        defaultValue = "Todos"
                        nullable = true
                    })
                ) { backStack ->
                    val category = backStack.arguments?.getString("category") ?: "Todos"
                    GamesScreen(navController, searchViewModel, cartViewModel, initialCategory = category)
                }
                composable(
                    route = Route.GameDetail.path,
                    arguments = listOf(navArgument("gameId") { type = NavType.StringType })
                ) { backStack ->
                    val gameId = backStack.arguments?.getString("gameId").orEmpty()
                    GameDetailScreen(navController, gameId, cartViewModel)
                }
                composable(Route.Library.path) { LibraryScreen(navController, libraryViewModel) }

                // ===== Tienda =====
                composable(Route.Cart.path) { CartScreen(navController, cartViewModel, libraryViewModel) }
                composable(Route.Checkout.path) { CheckoutScreen(navController) }

                // ===== Ajustes =====
                composable(Route.Settings.path) { SettingsScreen(navController) }
                composable(Route.CredentialsInfo.path) { CredentialsInfoScreen(navController) }

                // ===== Administrador =====
                composable(Route.AdminDashboard.path) { AdminDashboardScreen(navController) }
                composable(Route.AdminGames.path) { GameManagementScreen(navController) }
                composable(Route.AdminUsers.path) { UserManagementScreen(navController) }

                // ===== Estados / Errores =====
                composable(Route.NoConnection.path) { NoConnectionScreen(navController) }
                composable(Route.Maintenance.path) { MaintenanceScreen(navController) }
                composable(Route.NotFound.path) { NotFoundScreen(navController) }
            }
        }
    }
