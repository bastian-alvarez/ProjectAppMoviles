package com.example.uinavegacion.data.remote.post

import com.example.uinavegacion.BuildConfig
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Cliente Retrofit ligero para el microservicio de administradores.
 */
object AdminApi {

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

    val service: AdminService by lazy {
        Retrofit.Builder()
            .baseUrl(ensureTrailingSlash(BuildConfig.ADMIN_BASE_URL))
            .client(okHttpClient)
            .addConverterFactory(gsonFactory)
            .build()
            .create(AdminService::class.java)
    }

    private fun ensureTrailingSlash(url: String): String =
        if (url.endsWith("/")) url else "$url/"
}


