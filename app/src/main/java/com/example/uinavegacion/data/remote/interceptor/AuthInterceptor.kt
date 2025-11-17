package com.example.uinavegacion.data.remote.interceptor

import com.example.uinavegacion.data.SessionManager
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor que agrega el token de autenticación a todas las peticiones HTTP
 */
class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Obtener el token del SessionManager
        val token = SessionManager.getToken()
        
        // Si hay token, agregarlo al header
        val request = if (!token.isNullOrBlank()) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }
        
        return chain.proceed(request)
    }
}

