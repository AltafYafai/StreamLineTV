package com.streamline.tv

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.material3.*
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MediaDetailScreen(
    mediaItem: MediaItem,
    addonManager: AddonManager,
    repository: StremioRepository,
    libraryManager: LibraryManager,
    onWatchNow: (MediaItem) -> Unit,
    onBack: () -> Unit
) {
    var fullMediaItem by remember { mutableStateOf(mediaItem) }
    val watchlistIds by libraryManager.watchlistIds.collectAsState(initial = emptySet())
    val isInWatchlist = watchlistIds.contains(mediaItem.id)
    val installedAddons by addonManager.installedAddons.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    LaunchedEffect(mediaItem, installedAddons) {
        if (installedAddons.isNotEmpty()) {
            val detail = repository.fetchMediaDetail(installedAddons.first(), mediaItem.type, mediaItem.id)
            if (detail != null) {
                fullMediaItem = detail
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        AsyncImage(
            model = fullMediaItem.bannerUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.4f
        )
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(48.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = fullMediaItem.title,
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = fullMediaItem.metadata,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = fullMediaItem.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(0.6f)
            )

            // Episode List for Series
            if (fullMediaItem.type == "series" && fullMediaItem.episodes.isNotEmpty()) {
                Text(
                    "Episodes", 
                    style = MaterialTheme.typography.titleLarge, 
                    modifier = Modifier.padding(top = 32.dp, bottom = 16.dp)
                )
                TvLazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(fullMediaItem.episodes.size) { index ->
                        val ep = fullMediaItem.episodes[index]
                        FocusableCard(onClick = { /* In a real app, resolve stream for this episode */ }) {
                            Column(Modifier.padding(16.dp)) {
                                Text("S${ep.season} E${ep.episode}", style = MaterialTheme.typography.labelSmall)
                                Text(ep.title, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = { onWatchNow(fullMediaItem) },
                    modifier = Modifier.width(200.dp)
                ) {
                    Text("Watch Now")
                }

                OutlinedButton(
                    onClick = { 
                        scope.launch {
                            libraryManager.toggleWatchlist(mediaItem.id)
                        }
                    },
                    modifier = Modifier.width(200.dp)
                ) {
                    Text(if (isInWatchlist) "In Watchlist" else "Add to Watchlist")
                }
            }
        }
    }
}
