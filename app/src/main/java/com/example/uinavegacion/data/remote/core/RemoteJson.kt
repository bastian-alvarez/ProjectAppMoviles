package com.example.uinavegacion.data.remote.core

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

/**
 * Shared [Json] instance configured for all microservice integrations.
 */
object RemoteJson {
    @OptIn(ExperimentalSerializationApi::class)
    val instance: Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        isLenient = true
        encodeDefaults = true
        coerceInputValues = true
    }
}

