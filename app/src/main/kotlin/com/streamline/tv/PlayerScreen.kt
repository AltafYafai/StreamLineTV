@file:OptIn(UnstableApi::class, ExperimentalTvMaterial3Api::class)
package com.streamline.tv

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.media3.common.C
import androidx.media3.common.MediaItem as Media3Item
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.material3.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@Composable
fun PlayerScreen(
    mediaItem: MediaItem,
    selectedStream: AddonStream,
    addonManager: AddonManager,
    repository: StremioRepository,
    libraryManager: LibraryManager,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val exoPlayer = remember { ExoPlayer.Builder(context).build() }
    
    var videoUrl by remember { mutableStateOf(selectedStream.url ?: "") }
    var subtitles by remember { mutableStateOf(selectedStream.subtitles ?: emptyList()) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableLongStateOf(0L) }
    var bufferedPosition by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    
    var areControlsVisible by remember { mutableStateOf(true) }
    var showSettingsMenu by remember { mutableStateOf(false) }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }
    var resizeMode by remember { mutableIntStateOf(AspectRatioFrameLayout.RESIZE_MODE_FIT) }
    var currentTime by remember { mutableStateOf("") }

    val endTime = remember(currentPosition, duration) {
        val remaining = duration - currentPosition
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MILLISECOND, remaining.toInt())
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(calendar.time)
    }

    LaunchedEffect(areControlsVisible, isPlaying) {
        scope.launch {
            while (true) {
                currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                delay(1000)
            }
        }
        if (areControlsVisible && isPlaying && !showSettingsMenu) {
            delay(5000)
            areControlsVisible = false
        }
    }

    LaunchedEffect(mediaItem) {
        val savedState = libraryManager.getPlaybackState(mediaItem.id).first()
        if (savedState != null) {
            val parts = savedState.split("|")
            if (parts.size == 2) {
                val pos = parts[1].toLongOrNull() ?: 0L
                exoPlayer.seekTo(pos)
            }
        }
    }

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

    LaunchedEffect(exoPlayer) {
        while (true) {
            currentPosition = exoPlayer.currentPosition
            bufferedPosition = exoPlayer.bufferedPosition
            duration = exoPlayer.duration.coerceAtLeast(0L)
            isPlaying = exoPlayer.isPlaying
            delay(500)
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

        AnimatedVisibility(
            visible = areControlsVisible,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent, Color.Black.copy(alpha = 0.9f))
                        )
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp)
                        .align(Alignment.TopStart),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Spacer(Modifier.width(24.dp))
                    Column {
                        Text(
                            text = mediaItem.title, 
                            style = MaterialTheme.typography.headlineSmall, 
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = selectedStream.title ?: mediaItem.metadata, 
                            style = MaterialTheme.typography.bodyMedium, 
                            color = Color.LightGray
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = currentTime,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Light
                    )
                }

                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(80.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { 
                            areControlsVisible = true
                            exoPlayer.seekTo((exoPlayer.currentPosition - 10000).coerceAtLeast(0)) 
                        },
                        modifier = Modifier.size(80.dp)
                    ) {
                        Icon(Icons.Default.Replay10, contentDescription = "-10s", tint = Color.White, modifier = Modifier.fillMaxSize())
                    }

                    Surface(
                        onClick = { 
                            areControlsVisible = true
                            if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play() 
                        },
                        shape = ClickableSurfaceDefaults.shape(MaterialTheme.shapes.extraLarge),
                        colors = ClickableSurfaceDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.Black,
                            focusedContainerColor = Color.White,
                            focusedContentColor = Color.Black
                        ),
                        modifier = Modifier.size(120.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, 
                                contentDescription = "Play/Pause", 
                                modifier = Modifier.size(80.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = { 
                            areControlsVisible = true
                            exoPlayer.seekTo((exoPlayer.currentPosition + 30000).coerceAtMost(duration)) 
                        },
                        modifier = Modifier.size(80.dp)
                    ) {
                        Icon(Icons.Default.Forward30, contentDescription = "+30s", tint = Color.White, modifier = Modifier.fillMaxSize())
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 48.dp, vertical = 48.dp)
                        .align(Alignment.BottomCenter)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(formatTime(currentPosition), color = Color.White, style = MaterialTheme.typography.labelLarge)
                        Text("Ends at $endTime", color = Color.LightGray, style = MaterialTheme.typography.labelLarge)
                        Text(formatTime(duration), color = Color.White, style = MaterialTheme.typography.labelLarge)
                    }
                    
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                        LinearProgressIndicator(
                            progress = if (duration > 0) bufferedPosition.toFloat() / duration else 0f,
                            modifier = Modifier.fillMaxWidth().height(10.dp),
                            color = Color.White.copy(alpha = 0.2f),
                            trackColor = Color.DarkGray.copy(alpha = 0.5f)
                        )
                        LinearProgressIndicator(
                            progress = if (duration > 0) currentPosition.toFloat() / duration else 0f,
                            modifier = Modifier.fillMaxWidth().height(10.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = Color.Transparent
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                            PlayerActionItem(Icons.Rounded.Subtitles, "Subtitles") { showSettingsMenu = true }
                            PlayerActionItem(Icons.Rounded.AudioFile, "Audio") { showSettingsMenu = true }
                            PlayerActionItem(Icons.Rounded.Speed, "Speed") { showSettingsMenu = true }
                        }
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            if (mediaItem.type == "series") {
                                Button(onClick = { /* Next logic */ }) {
                                    Icon(Icons.Rounded.SkipNext, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Next Episode")
                                }
                            }
                            
                            IconButton(onClick = { showSettingsMenu = true }) {
                                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White, modifier = Modifier.size(32.dp))
                            }
                        }
                    }
                }
            }
        }
        
        if (showSettingsMenu) {
            areControlsVisible = true
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)).clickable { showSettingsMenu = false }, contentAlignment = Alignment.CenterEnd) {
                MaterialSurface(
                    modifier = Modifier.width(420.dp).fillMaxHeight(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant
                ) {
                    TvLazyColumn(Modifier.padding(32.dp)) {
                        item { Text("Player Control Center", style = MaterialTheme.typography.headlineMedium, color = Color.White) }
                        
                        item { PlayerSettingHeader("Playback Speed") }
                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                listOf(0.5f, 1.0f, 1.5f, 2.0f).forEach { speed ->
                                    PlayerSelectableChip(label = "${speed}x", isSelected = playbackSpeed == speed) {
                                        playbackSpeed = speed
                                        exoPlayer.playbackParameters = PlaybackParameters(speed)
                                    }
                                }
                            }
                        }

                        item { PlayerSettingHeader("Audio Track") }
                        item { PlayerSelectableItem("English (Primary)", true) { } }

                        item { PlayerSettingHeader("Video Scaling") }
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                PlayerSelectableItem("Original Aspect", resizeMode == AspectRatioFrameLayout.RESIZE_MODE_FIT) { resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT }
                                PlayerSelectableItem("Zoom & Crop", resizeMode == AspectRatioFrameLayout.RESIZE_MODE_ZOOM) { resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM }
                                PlayerSelectableItem("Stretch to Fill", resizeMode == AspectRatioFrameLayout.RESIZE_MODE_FILL) { resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL }
                            }
                        }

                        if (subtitles.isNotEmpty()) {
                            item { PlayerSettingHeader("Subtitles") }
                            items(subtitles.size) { index ->
                                val sub = subtitles[index]
                                PlayerSelectableItem(sub.lang, false) { }
                            }
                        }
                        item { Spacer(Modifier.height(100.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
fun PlayerSettingHeader(title: String) {
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
            contentColor = if (isSelected) Color.Black else Color.White,
            focusedContainerColor = Color.White,
            focusedContentColor = Color.Black
        ),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Text(label, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), style = MaterialTheme.typography.labelLarge)
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
            contentColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.White,
            focusedContainerColor = MaterialTheme.colorScheme.primary,
            focusedContentColor = Color.Black
        )
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
            if (isSelected) Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun PlayerActionItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(MaterialTheme.shapes.medium),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.White.copy(alpha = 0.05f),
            contentColor = Color.White,
            focusedContainerColor = Color.White,
            focusedContentColor = Color.Black
        )
    ) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Text(label, style = MaterialTheme.typography.labelLarge)
        }
    }
}
