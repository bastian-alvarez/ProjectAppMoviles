package com.example.uinavegacion.data.remote.licencia

import com.example.uinavegacion.BuildConfig
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Cliente Retrofit sencillo para el microservicio de licencias.
 * Permite reutilizar el mismo estilo de integración usado en otros servicios remotos.
 */
object LicenciaApi {

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

    val service: LicenciaService by lazy {
        Retrofit.Builder()
            .baseUrl(ensureTrailingSlash(BuildConfig.LICENCIA_BASE_URL))
            .client(okHttpClient)
            .addConverterFactory(gsonFactory)
            .build()
            .create(LicenciaService::class.java)
    }

    private fun ensureTrailingSlash(url: String): String =
        if (url.endsWith("/")) url else "$url/"
}


