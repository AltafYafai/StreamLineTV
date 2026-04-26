@file:OptIn(ExperimentalTvMaterial3Api::class)
package com.streamline.tv

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.TextButton
import androidx.compose.material3.Surface as MaterialSurface
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
import androidx.media3.common.PlaybackParameters
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.material3.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

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
    
    // UI State
    var areControlsVisible by remember { mutableStateOf(true) }
    var showSettingsMenu by remember { mutableStateOf(false) }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }
    var resizeMode by remember { mutableIntStateOf(AspectRatioFrameLayout.RESIZE_MODE_FIT) }

    // Auto-hide timer
    LaunchedEffect(areControlsVisible, isPlaying) {
        if (areControlsVisible && isPlaying && !showSettingsMenu) {
            delay(5000)
            areControlsVisible = false
        }
    }

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
        
        val savedState = libraryManager.getPlaybackState(mediaItem.id).first()
        if (savedState != null) {
            val parts = savedState.split("|")
            if (parts.size == 2) {
                val pos = parts[1].toLongOrNull() ?: 0L
                exoPlayer.seekTo(pos)
            }
        }
    }

    // 2. Initialize Player
    LaunchedEffect(videoUrl, subtitles) {
        if (videoUrl.isNotEmpty()) {
            val media3ItemBuilder = Media3Item.Builder().setUri(videoUrl)
            subtitles.forEach { sub ->
                val subConfig = Media3Item.SubtitleConfiguration.Builder(android.net.Uri.parse(sub.url))
                    .setMimeType(MimeTypes.TEXT_VTT)
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
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { areControlsVisible = true }
    ) {
        VideoPlayer(
            exoPlayer = exoPlayer,
            resizeMode = resizeMode,
            modifier = Modifier.fillMaxSize()
        )

        if (videoUrl.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Resolving Stream...", color = Color.White)
            }
        }

        // Playback Controls Overlay
        AnimatedVisibility(
            visible = areControlsVisible,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 }
        ) {
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
                        color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
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
                    IconButton(onClick = { 
                        areControlsVisible = true
                        exoPlayer.seekTo((exoPlayer.currentPosition - 10000).coerceAtLeast(0)) 
                    }) {
                        Icon(Icons.Default.FastRewind, contentDescription = "Rewind", tint = Color.White)
                    }
                    
                    Spacer(modifier = Modifier.width(24.dp))

                    Surface(
                        onClick = { 
                            areControlsVisible = true
                            if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play() 
                        },
                        shape = ClickableSurfaceDefaults.shape(MaterialTheme.shapes.extraLarge),
                        colors = ClickableSurfaceDefaults.colors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.Black),
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = "Play/Pause", modifier = Modifier.size(40.dp))
                        }
                    }

                    Spacer(modifier = Modifier.width(24.dp))

                    IconButton(onClick = { 
                        areControlsVisible = true
                        exoPlayer.seekTo((exoPlayer.currentPosition + 30000).coerceAtMost(duration)) 
                    }) {
                        Icon(Icons.Default.FastForward, contentDescription = "Fast Forward", tint = Color.White)
                    }
                    
                    Spacer(modifier = Modifier.width(48.dp))
                    
                    IconButton(onClick = { showSettingsMenu = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Playback Settings", tint = Color.White)
                    }
                }
            }
        }
        
        // Comprehensive Settings Menu
        if (showSettingsMenu) {
            areControlsVisible = true
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)).clickable { showSettingsMenu = false }, contentAlignment = Alignment.CenterEnd) {
                MaterialSurface(
                    modifier = Modifier.width(350.dp).fillMaxHeight(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant
                ) {
                    TvLazyColumn(Modifier.padding(24.dp)) {
                        item { Text("Playback Settings", style = MaterialTheme.typography.headlineSmall, color = Color.White) }
                        
                        item { SettingHeader("Speed") }
                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(0.5f, 1.0f, 1.5f, 2.0f).forEach { speed ->
                                    PlayerSelectableChip(label = "${speed}x", isSelected = playbackSpeed == speed) {
                                        playbackSpeed = speed
                                        exoPlayer.playbackParameters = PlaybackParameters(speed)
                                    }
                                }
                            }
                        }

                        item { SettingHeader("Resize Mode") }
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                PlayerSelectableItem("Fit", resizeMode == AspectRatioFrameLayout.RESIZE_MODE_FIT) { resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT }
                                PlayerSelectableItem("Zoom", resizeMode == AspectRatioFrameLayout.RESIZE_MODE_ZOOM) { resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM }
                                PlayerSelectableItem("Fill", resizeMode == AspectRatioFrameLayout.RESIZE_MODE_FILL) { resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL }
                            }
                        }

                        if (subtitles.isNotEmpty()) {
                            item { SettingHeader("Subtitles") }
                            items(subtitles.size) { index ->
                                val sub = subtitles[index]
                                PlayerSelectableItem(sub.lang, false) { /* track toggle */ }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
    )
}

@Composable
fun PlayerSelectableChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(MaterialTheme.shapes.small),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.DarkGray,
            contentColor = if (isSelected) Color.Black else Color.White
        ),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Text(label, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
    }
}

@Composable
fun PlayerSelectableItem(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = ClickableSurfaceDefaults.shape(MaterialTheme.shapes.medium),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent,
            contentColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.White
        )
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(label, modifier = Modifier.weight(1f))
            if (isSelected) Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
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
