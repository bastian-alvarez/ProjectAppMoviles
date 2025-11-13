package com.example.uinavegacion.data.remote.config

object ApiConfig {
    // URLs base de los microservicios
    const val AUTH_SERVICE_BASE_URL = "http://10.0.2.2:3001/api/" // 10.0.2.2 es localhost en emulador
    const val GAME_CATALOG_SERVICE_BASE_URL = "http://10.0.2.2:3002/api/"
    const val ORDER_SERVICE_BASE_URL = "http://10.0.2.2:3003/api/"
    const val LIBRARY_SERVICE_BASE_URL = "http://10.0.2.2:3004/api/"
    
    // Para dispositivo físico, usar la IP de tu PC:
    // const val AUTH_SERVICE_BASE_URL = "http://192.168.1.X:3001/api/"
}

