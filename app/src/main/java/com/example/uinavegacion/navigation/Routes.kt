package com.example.uinavegacion.navigation

/**
 * Sealed class con TODAS las rutas de la app.
 * Cada objeto define su "path" y, cuando corresponde, helpers build(...)
 * para construir rutas con argumentos.
 */
sealed class Route(val path: String) {

    // Core
    data object Splash : Route("splash")
    data object Home : Route("home")

    // Auth
    data object Login : Route("login")
    data object Register : Route("register")
    data object ForgotPassword : Route("forgot_password")
    data object VerifyEmail : Route("verify_email?email={email}") {
        fun build(email: String) = "verify_email?email=$email"
    }

    // Perfil / Cuenta
    data object Profile : Route("profile")
    data object ProfileEdit : Route("profile_edit")
    data object ChangePassword : Route("change_password")

    // Catálogo / Contenido
    data object Games : Route("games")
    data object GameDetail : Route("game/{gameId}") {
        fun build(gameId: String) = "game/$gameId"
    }
    data object Library : Route("library")

    // Tienda
    data object Cart : Route("cart")
    data object Checkout : Route("checkout")

    // Ajustes
    data object Settings : Route("settings")
    data object CredentialsInfo : Route("credentials_info")

    // Administrador
    data object AdminDashboard : Route("admin/dashboard")
    data object AdminGames : Route("admin/games")
    data object AdminAddGame : Route("admin/games/add")
    data object AdminEditGame : Route("admin/games/edit/{gameId}") {
        fun build(gameId: String) = "admin/games/edit/$gameId"
    }
    data object AdminUsers : Route("admin/users")

    // Estados / Errores
    data object NoConnection : Route("no_connection")
    data object Maintenance : Route("maintenance")
    data object NotFound : Route("not_found")
}



/*
* “Strings mágicos” se refiere a cuando pones un texto duro y repetido en varias partes del código,
* Si mañana cambias "home" por "inicio", tendrías que buscar todas las ocurrencias de "home" a mano.
* Eso es frágil y propenso a errores.
La idea es: mejor centralizar esos strings en una sola clase (Route), y usarlos desde ahí.*/