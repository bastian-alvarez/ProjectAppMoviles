package com.example.uinavegacion.data.remote.config

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    fun createAuthService(): retrofit2.Retrofit {
        return Retrofit.Builder()
            .baseUrl(ApiConfig.AUTH_SERVICE_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    fun createGameCatalogService(): retrofit2.Retrofit {
        return Retrofit.Builder()
            .baseUrl(ApiConfig.GAME_CATALOG_SERVICE_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    fun createOrderService(): retrofit2.Retrofit {
        return Retrofit.Builder()
            .baseUrl(ApiConfig.ORDER_SERVICE_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    fun createLibraryService(): retrofit2.Retrofit {
        return Retrofit.Builder()
            .baseUrl(ApiConfig.LIBRARY_SERVICE_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}

