package com.streamline.tv

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.media3.common.C
import androidx.media3.common.MediaItem as Media3Item
import androidx.media3.common.MimeTypes
import androidx.media3.exoplayer.ExoPlayer
import androidx.tv.material3.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun PlayerScreen(
    mediaItem: MediaItem,
    addonManager: AddonManager,
    repository: StremioRepository,
    libraryManager: LibraryManager,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val installedAddons by addonManager.installedAddons.collectAsState(initial = emptyList())
    
    val exoPlayer = remember { ExoPlayer.Builder(context).build() }
    
    var videoUrl by remember { mutableStateOf(mediaItem.videoUrl) }
    var subtitles by remember { mutableStateOf<List<AddonSubtitle>>(emptyList()) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    var showSubtitleMenu by remember { mutableStateOf(false) }

    // 1. Resolve Stream & Subtitles
    LaunchedEffect(mediaItem, installedAddons) {
        if (videoUrl.isEmpty() && installedAddons.isNotEmpty()) {
            for (addon in installedAddons) {
                val streams = repository.fetchStreams(addon, mediaItem.type, mediaItem.id)
                val bestStream = streams.firstOrNull { it.url != null }
                if (bestStream != null) {
                    videoUrl = bestStream.url!!
                    subtitles = bestStream.subtitles ?: emptyList()
                    break
                }
            }
        }
        
        // Load saved position
        val savedState = libraryManager.getPlaybackState(mediaItem.id).first()
        if (savedState != null) {
            val parts = savedState.split("|")
            if (parts.size == 2) {
                val pos = parts[1].toLongOrNull() ?: 0L
                exoPlayer.seekTo(pos)
            }
        }
    }

    // 2. Initialize Player with Subtitles
    LaunchedEffect(videoUrl, subtitles) {
        if (videoUrl.isNotEmpty()) {
            val media3ItemBuilder = Media3Item.Builder().setUri(videoUrl)
            
            // Add subtitles
            subtitles.forEach { sub ->
                val subConfig = Media3Item.SubtitleConfiguration.Builder(android.net.Uri.parse(sub.url))
                    .setMimeType(MimeTypes.TEXT_VTT) // Standard for Stremio
                    .setLanguage(sub.lang)
                    .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                    .build()
                media3ItemBuilder.setSubtitleConfigurations(listOf(subConfig))
            }
            
            exoPlayer.setMediaItem(media3ItemBuilder.build())
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            // Save position before release
            scope.launch {
                libraryManager.savePlaybackState(mediaItem.id, null, exoPlayer.currentPosition)
            }
            exoPlayer.release()
        }
    }

    // Progress Polling
    LaunchedEffect(exoPlayer) {
        while (true) {
            currentPosition = exoPlayer.currentPosition
            duration = exoPlayer.duration.coerceAtLeast(0L)
            isPlaying = exoPlayer.isPlaying
            delay(1000)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (videoUrl.isNotEmpty()) {
            VideoPlayer(
                exoPlayer = exoPlayer,
                subtitles = subtitles,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Resolving Stream...", color = Color.White)
            }
        }

        // Playback Controls Overlay
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                        startY = 500f
                    )
                )
                .padding(48.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(text = mediaItem.title, style = MaterialTheme.typography.displaySmall, color = Color.White)
            Text(text = mediaItem.metadata, style = MaterialTheme.typography.bodyLarge, color = Color.LightGray, modifier = Modifier.padding(bottom = 24.dp))

            // Seek Bar
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(formatTime(currentPosition), color = Color.White)
                LinearProgressIndicator(
                    progress = if (duration > 0) currentPosition.toFloat() / duration else 0f,
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp).height(8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = Color.DarkGray
                )
                Text(formatTime(duration), color = Color.White)
            }

            // Transport Controls
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { exoPlayer.seekTo((exoPlayer.currentPosition - 10000).coerceAtLeast(0)) }) {
                    Icon(Icons.Default.FastRewind, contentDescription = "Rewind", tint = Color.White)
                }
                
                Spacer(modifier = Modifier.width(24.dp))

                Surface(
                    onClick = { if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play() },
                    shape = ClickableSurfaceDefaults.shape(MaterialTheme.shapes.extraLarge),
                    colors = ClickableSurfaceDefaults.colors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.Black),
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = "Play/Pause", modifier = Modifier.size(40.dp))
                    }
                }

                Spacer(modifier = Modifier.width(24.dp))

                IconButton(onClick = { exoPlayer.seekTo((exoPlayer.currentPosition + 30000).coerceAtMost(duration)) }) {
                    Icon(Icons.Default.FastForward, contentDescription = "Fast Forward", tint = Color.White)
                }
                
                if (subtitles.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(24.dp))
                    IconButton(onClick = { showSubtitleMenu = !showSubtitleMenu }) {
                        Icon(Icons.Default.Subtitles, contentDescription = "Subtitles", tint = if (showSubtitleMenu) MaterialTheme.colorScheme.primary else Color.White)
                    }
                }
            }
        }
        
        // Simple Subtitle Track Selection
        if (showSubtitleMenu) {
            Box(Modifier.fillMaxSize().padding(48.dp), contentAlignment = Alignment.BottomEnd) {
                Surface(
                    modifier = Modifier.width(200.dp),
                    shape = ClickableSurfaceDefaults.shape(MaterialTheme.shapes.medium),
                    colors = ClickableSurfaceDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Subtitles", style = MaterialTheme.typography.labelLarge)
                        subtitles.forEach { sub ->
                            TextButton(onClick = { showSubtitleMenu = false }) {
                                Text(sub.lang)
                            }
                        }
                    }
                }
            }
        }
    }
}

fun formatTime(ms: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(ms)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(ms) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(ms) % 60
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
