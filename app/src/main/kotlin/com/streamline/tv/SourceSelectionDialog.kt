@file:OptIn(ExperimentalTvMaterial3Api::class)
package com.streamline.tv

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.material3.*
import kotlinx.coroutines.launch
import androidx.compose.material3.Surface as MaterialSurface

@Composable
fun SourceSelectionDialog(
    mediaItem: MediaItem,
    addonManager: AddonManager,
    repository: StremioRepository,
    onSourceSelected: (AddonStream) -> Unit,
    onDismiss: () -> Unit
) {
    val installedAddons by addonManager.installedAddons.collectAsState(initial = emptyList())
    var streams by remember { mutableStateOf<List<AddonStream>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(installedAddons) {
        val allStreams = mutableListOf<AddonStream>()
        for (addon in installedAddons) {
            val fetched = repository.fetchStreams(addon, mediaItem.type, mediaItem.id)
            allStreams.addAll(fetched)
        }
        streams = allStreams
        isLoading = false
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.9f)),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .width(600.dp)
                    .fillMaxHeight(0.8f),
                shape = ClickableSurfaceDefaults.shape(MaterialTheme.shapes.large),
                colors = ClickableSurfaceDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = Color.White
                ),
                onClick = {} // Trap focus
            ) {
                Column(modifier = Modifier.padding(32.dp)) {
                    Text(
                        text = "Choose a Source",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                    Text(
                        text = mediaItem.title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    if (isLoading) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Fetching available links...", style = MaterialTheme.typography.titleMedium, color = Color.White)
                        }
                    } else if (streams.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No links found for this content.", color = Color.Gray)
                        }
                    } else {
                        TvLazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(streams.size) { index ->
                                val stream = streams[index]
                                SourceItem(stream) {
                                    onSourceSelected(stream)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SourceItem(stream: AddonStream, onClick: () -> Unit) {
    FocusableCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stream.title ?: "Unknown Source",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                Text(
                    text = if (stream.url != null) "Direct Link" else if (stream.infoHash != null) "Torrent / Debrid" else "Other",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            val quality = remember(stream.title) {
                when {
                    stream.title?.contains("4k", ignoreCase = true) == true -> "4K"
                    stream.title?.contains("1080", ignoreCase = true) == true -> "1080p"
                    stream.title?.contains("720", ignoreCase = true) == true -> "720p"
                    else -> "SD"
                }
            }
            
            MaterialSurface(
                shape = androidx.compose.material3.MaterialTheme.shapes.extraSmall,
                color = Color.White.copy(alpha = 0.1f)
            ) {
                Text(
                    text = quality,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White
                )
            }
        }
    }
}
