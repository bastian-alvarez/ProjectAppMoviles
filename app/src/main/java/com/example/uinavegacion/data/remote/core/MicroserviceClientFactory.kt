package com.example.uinavegacion.data.remote.core

import com.example.uinavegacion.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import java.util.concurrent.TimeUnit

/**
 * Centralizado para crear clientes Retrofit hacia los microservicios.
 */
object MicroserviceClientFactory {

    private val json: Json = RemoteJson.instance

    private const val DEFAULT_TIMEOUT_SECONDS = 30L

    private val loggingInterceptor: Interceptor by lazy {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    private fun baseClient(): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .build()

    private fun retrofit(baseUrl: String): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl.ensureTrailingSlash())
            .client(baseClient())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    fun <T> create(baseUrl: String, service: Class<T>): T = retrofit(baseUrl).create(service)

    private val catalogoRetrofit: Retrofit by lazy { retrofit(BuildConfig.CATALOGO_BASE_URL) }
    private val usuarioRetrofit: Retrofit by lazy { retrofit(BuildConfig.USUARIO_BASE_URL) }
    private val juegosRetrofit: Retrofit by lazy { retrofit(BuildConfig.JUEGOS_BASE_URL) }
    private val licenciaRetrofit: Retrofit by lazy { retrofit(BuildConfig.LICENCIA_BASE_URL) }
    private val ordenRetrofit: Retrofit by lazy { retrofit(BuildConfig.ORDEN_BASE_URL) }
    private val resenaRetrofit: Retrofit by lazy { retrofit(BuildConfig.RESENA_BASE_URL) }

    fun catalogoService(): Retrofit = catalogoRetrofit
    fun usuarioService(): Retrofit = usuarioRetrofit
    fun juegosService(): Retrofit = juegosRetrofit
    fun licenciaService(): Retrofit = licenciaRetrofit
    fun ordenService(): Retrofit = ordenRetrofit
    fun resenaService(): Retrofit = resenaRetrofit

    private fun String.ensureTrailingSlash(): String =
        if (endsWith("/")) this else "$this/"
}

