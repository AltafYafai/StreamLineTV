package com.streamline.tv

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class AddonManager(private val context: Context) {
    private val ADDONS_KEY = stringSetPreferencesKey("installed_addons")
    private val INITIALIZED_KEY = booleanPreferencesKey("addons_initialized")

    val installedAddons: Flow<List<String>> = context.dataStore.data
        .map { preferences ->
            preferences[ADDONS_KEY]?.toList() ?: emptyList()
        }

    suspend fun initializeDefaultAddons() {
        val prefs = context.dataStore.data.first()
        if (prefs[INITIALIZED_KEY] != true) {
            addAddon("https://raw.githubusercontent.com/yoruix/nuvio-providers/refs/heads/main/manifest.json")
            context.dataStore.edit { it[INITIALIZED_KEY] = true }
        }
    }

    suspend fun addAddon(url: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[ADDONS_KEY] ?: emptySet()
            preferences[ADDONS_KEY] = current + url
        }
    }

    suspend fun removeAddon(url: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[ADDONS_KEY] ?: emptySet()
            preferences[ADDONS_KEY] = current - url
        }
    }
}
