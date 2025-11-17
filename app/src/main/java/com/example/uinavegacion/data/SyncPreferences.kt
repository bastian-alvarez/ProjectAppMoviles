package com.example.uinavegacion.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Gestiona las preferencias de sincronización de datos
 */
object SyncPreferences {
    private const val PREF_NAME = "sync_preferences"
    private const val KEY_GAMES_SYNCED = "games_synced"
    private const val KEY_SYNC_TIMESTAMP = "sync_timestamp"
    
    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }
    
    /**
     * Verifica si los juegos ya fueron sincronizados
     */
    fun areGamesSynced(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_GAMES_SYNCED, false)
    }
    
    /**
     * Marca los juegos como sincronizados
     */
    fun markGamesSynced(context: Context) {
        getPreferences(context).edit().apply {
            putBoolean(KEY_GAMES_SYNCED, true)
            putLong(KEY_SYNC_TIMESTAMP, System.currentTimeMillis())
            apply()
        }
    }
    
    /**
     * Obtiene la fecha de la última sincronización
     */
    fun getLastSyncTimestamp(context: Context): Long {
        return getPreferences(context).getLong(KEY_SYNC_TIMESTAMP, 0)
    }
    
    /**
     * Resetea el estado de sincronización (útil para testing)
     */
    fun resetSyncState(context: Context) {
        getPreferences(context).edit().clear().apply()
    }
}

