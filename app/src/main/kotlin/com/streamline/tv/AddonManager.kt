package com.streamline.tv

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class AddonManager(private val context: Context) {
    private val ADDONS_KEY = stringSetPreferencesKey("installed_addons")

    val installedAddons: Flow<List<String>> = context.dataStore.data
        .map { preferences ->
            preferences[ADDONS_KEY]?.toList() ?: emptyList()
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
