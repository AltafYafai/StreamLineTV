package com.streamline.tv

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.tv.foundation.lazy.grid.TvGridCells
import androidx.tv.foundation.lazy.grid.TvLazyVerticalGrid
import androidx.tv.material3.*
import coil.compose.AsyncImage

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun LibraryScreen(
    addonManager: AddonManager,
    repository: StremioRepository,
    libraryManager: LibraryManager,
    onMediaClick: (MediaItem) -> Unit
) {
    val watchlistIds by libraryManager.watchlistIds.collectAsState(initial = emptySet())
    val installedAddons by addonManager.installedAddons.collectAsState(initial = emptyList())
    var watchlistItems by remember { mutableStateOf<List<MediaItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(watchlistIds, installedAddons) {
        if (watchlistIds.isNotEmpty() && installedAddons.isNotEmpty()) {
            isLoading = true
            val items = mutableListOf<MediaItem>()
            for (id in watchlistIds) {
                val detail = repository.fetchMediaDetail(installedAddons.first(), "movie", id)
                    ?: repository.fetchMediaDetail(installedAddons.first(), "series", id)
                if (detail != null) {
                    items.add(detail)
                }
            }
            watchlistItems = items
            isLoading = false
        } else if (watchlistIds.isEmpty()) {
            watchlistItems = emptyList()
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp)) {
        Text(
            text = "Your Library",
            style = MaterialTheme.typography.displayMedium,
            modifier = Modifier.padding(vertical = 24.dp)
        )

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("Loading Watchlist...")
            }
        } else if (watchlistItems.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("Your watchlist is empty.", color = androidx.compose.ui.graphics.Color.Gray)
            }
        } else {
            TvLazyVerticalGrid(
                columns = TvGridCells.Fixed(6),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 32.dp)
            ) {
                items(watchlistItems.size) { index ->
                    CatalogItemCard(movie = watchlistItems[index], onMediaClick = onMediaClick)
                }
            }
        }
    }
}
