package com.example.uinavegacion.data.remote.catalogo

import com.example.uinavegacion.BuildConfig
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Cliente Retrofit sencillo para el microservicio de catálogo.
 * Cambia la URL base mediante BuildConfig.CATALOGO_BASE_URL sin más configuraciones.
 */
object CatalogoApi {

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    private val gsonFactory: GsonConverterFactory = GsonConverterFactory.create(
        GsonBuilder()
            .setLenient()
            .create()
    )

    val service: CatalogoService by lazy {
        Retrofit.Builder()
            .baseUrl(ensureTrailingSlash(BuildConfig.CATALOGO_BASE_URL))
            .client(okHttpClient)
            .addConverterFactory(gsonFactory)
            .build()
            .create(CatalogoService::class.java)
    }

    private fun ensureTrailingSlash(url: String): String =
        if (url.endsWith("/")) url else "$url/"
}


