package com.example.primerintentodeaplicacionsolo

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extension para crear DataStore
private val Context.dataStore by preferencesDataStore(name = "gastos_prefs")

class GastosDataStore(private val context: Context) {
    // Key para guardar lista
    private val GASTOS_KEY = stringPreferencesKey("gastos")
    private val gson = Gson()

    // Guardar lista de gastos
    suspend fun guardarGastos(gastos: List<Gasto>) {
        val json = gson.toJson(gastos)
        context.dataStore.edit { preferences ->
            preferences[GASTOS_KEY] = json
        }
    }

    // Leer lista de gastos
    val gastosFlow: Flow<List<Gasto>> = context.dataStore.data.map { preferences ->
        val json = preferences[GASTOS_KEY] ?: "[]"
        val type = object : TypeToken<List<Gasto>>() {}.type
        gson.fromJson(json, type)
    }
}