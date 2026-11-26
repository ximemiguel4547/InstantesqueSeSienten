package com.instantes.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "settings")

class PreferencesManager(private val context: Context) {
    companion object {
        val PRIMERA_VEZ = booleanPreferencesKey("primera_vez")
    }

    val esPrimeraVez: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PRIMERA_VEZ] ?: true
        }

    suspend fun setPrimeraVez(valor: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PRIMERA_VEZ] = valor
        }
    }
}
