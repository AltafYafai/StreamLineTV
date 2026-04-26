package com.streamline.tv

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ThemeManager(private val context: Context) {
    private val THEME_KEY = stringPreferencesKey("app_theme")

    val selectedTheme: Flow<StreamLineColors> = context.dataStore.data
        .map { preferences ->
            when (preferences[THEME_KEY]) {
                "Green" -> ThemePresets.OrganicGreen
                "Blue" -> ThemePresets.OceanBlue
                else -> ThemePresets.DeepPurple
            }
        }

    val selectedThemeName: Flow<String> = context.dataStore.data
        .map { preferences -> preferences[THEME_KEY] ?: "Purple" }

    suspend fun setTheme(name: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = name
        }
    }
}
