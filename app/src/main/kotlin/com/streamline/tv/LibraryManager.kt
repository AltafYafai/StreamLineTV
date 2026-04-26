package com.streamline.tv

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LibraryManager(private val context: Context) {
    private val WATCHLIST_KEY = stringSetPreferencesKey("watchlist")
    private val HISTORY_KEY = stringSetPreferencesKey("search_history")
    
    // Playback state: mediaId -> episodeId|position
    private fun playbackKey(mediaId: String) = stringPreferencesKey("playback_$mediaId")

    // Watchlist
    val watchlistIds: Flow<Set<String>> = context.dataStore.data
        .map { it[WATCHLIST_KEY] ?: emptySet() }

    suspend fun toggleWatchlist(id: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[WATCHLIST_KEY] ?: emptySet()
            if (current.contains(id)) {
                preferences[WATCHLIST_KEY] = current - id
            } else {
                preferences[WATCHLIST_KEY] = current + id
            }
        }
    }

    // Search History
    val searchHistory: Flow<List<String>> = context.dataStore.data
        .map { it[HISTORY_KEY]?.toList() ?: emptyList() }

    suspend fun addSearchQuery(query: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[HISTORY_KEY] ?: emptySet()
            val updated = (setOf(query) + current).take(10).toSet()
            preferences[HISTORY_KEY] = updated
        }
    }

    suspend fun clearHistory() {
        context.dataStore.edit { it.remove(HISTORY_KEY) }
    }

    // Playback Tracking
    fun getPlaybackState(mediaId: String): Flow<String?> = context.dataStore.data
        .map { it[playbackKey(mediaId)] }

    suspend fun savePlaybackState(mediaId: String, episodeId: String?, position: Long) {
        context.dataStore.edit { preferences ->
            preferences[playbackKey(mediaId)] = "${episodeId ?: "movie"}|$position"
        }
    }
}
