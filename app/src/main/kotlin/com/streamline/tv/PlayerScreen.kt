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
    var duration by remember { mutableLongStateOf(0L) }
    
    var areControlsVisible by remember { mutableStateOf(true) }
    var showSettingsMenu by remember { mutableStateOf(false) }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }
    var resizeMode by remember { mutableIntStateOf(AspectRatioFrameLayout.RESIZE_MODE_FIT) }

    LaunchedEffect(areControlsVisible, isPlaying) {
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
                            listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent, Color.Black.copy(alpha = 0.9f))
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
                }

                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(64.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { 
                            areControlsVisible = true
                            exoPlayer.seekTo((exoPlayer.currentPosition - 10000).coerceAtLeast(0)) 
                        },
                        modifier = Modifier.size(64.dp)
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
                        modifier = Modifier.size(100.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, 
                                contentDescription = "Play/Pause", 
                                modifier = Modifier.size(64.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = { 
                            areControlsVisible = true
                            exoPlayer.seekTo((exoPlayer.currentPosition + 30000).coerceAtMost(duration)) 
                        },
                        modifier = Modifier.size(64.dp)
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(formatTime(currentPosition), color = Color.White, style = MaterialTheme.typography.labelMedium)
                        LinearProgressIndicator(
                            progress = if (duration > 0) currentPosition.toFloat() / duration else 0f,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 24.dp)
                                .height(10.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = Color.DarkGray
                        )
                        Text(formatTime(duration), color = Color.White, style = MaterialTheme.typography.labelMedium)
                    }

                    Spacer(Modifier.height(32.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                            PlayerActionItem(Icons.Rounded.Subtitles, "Subtitles") { showSettingsMenu = true }
                            PlayerActionItem(Icons.Rounded.SlowMotionVideo, "Speed (${playbackSpeed}x)") { showSettingsMenu = true }
                            PlayerActionItem(Icons.Rounded.AspectRatio, "Ratio") { showSettingsMenu = true }
                        }
                        
                        if (mediaItem.type == "series") {
                            Button(onClick = { /* Next logic */ }) {
                                Icon(Icons.Rounded.SkipNext, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Next Episode")
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
                    modifier = Modifier.width(400.dp).fillMaxHeight(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant
                ) {
                    TvLazyColumn(Modifier.padding(32.dp)) {
                        item { Text("Player Customization", style = MaterialTheme.typography.headlineMedium, color = Color.White) }
                        
                        item { PlayerSettingHeader("Playback Speed") }
                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                listOf(0.5f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                                    PlayerSelectableChip(label = "${speed}x", isSelected = playbackSpeed == speed) {
                                        playbackSpeed = speed
                                        exoPlayer.playbackParameters = PlaybackParameters(speed)
                                    }
                                }
                            }
                        }

                        item { PlayerSettingHeader("Video Scaling") }
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                PlayerSelectableItem("Original Aspect", resizeMode == AspectRatioFrameLayout.RESIZE_MODE_FIT) { resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT }
                                PlayerSelectableItem("Zoom & Crop", resizeMode == AspectRatioFrameLayout.RESIZE_MODE_ZOOM) { resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM }
                                PlayerSelectableItem("Fill Screen", resizeMode == AspectRatioFrameLayout.RESIZE_MODE_FILL) { resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL }
                            }
                        }

                        if (subtitles.isNotEmpty()) {
                            item { PlayerSettingHeader("Subtitles") }
                            items(subtitles.size) { index ->
                                val sub = subtitles[index]
                                PlayerSelectableItem(sub.lang, false) { /* track toggle */ }
                            }
                        }
                        
                        item { Spacer(Modifier.height(100.dp)) }
                    }
                }
            }
        }
    }
}
