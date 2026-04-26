package com.streamline.tv

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun AppNavigation(themeManager: ThemeManager) {
    val context = LocalContext.current
    val addonManager = remember { AddonManager(context) }
    val libraryManager = remember { LibraryManager(context) }
    val repository = remember { StremioRepository() }
    val scope = rememberCoroutineScope()
    
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var detailMediaItem by remember { mutableStateOf<MediaItem?>(null) }
    var playingMediaItem by remember { mutableStateOf<MediaItem?>(null) }
    
    var isAddonBrowserVisible by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    
    val navItems = listOf(
        NavItem("Home", Icons.Default.Home),
        NavItem("Catalog", Icons.Default.List),
        NavItem("Search", Icons.Default.Search),
        NavItem("Library", Icons.Default.Favorite),
        NavItem("Settings", Icons.Default.Settings)
    )

    // Initialize default addons
    LaunchedEffect(Unit) {
        addonManager.initializeDefaultAddons()
    }

    // Handle Back Button
    BackHandler(enabled = playingMediaItem != null || detailMediaItem != null || isAddonBrowserVisible) {
        if (playingMediaItem != null) {
            playingMediaItem = null
        } else if (detailMediaItem != null) {
            detailMediaItem = null
        } else if (isAddonBrowserVisible) {
            isAddonBrowserVisible = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (playingMediaItem != null) {
            PlayerScreen(
                mediaItem = playingMediaItem!!,
                addonManager = addonManager,
                repository = repository,
                libraryManager = libraryManager,
                onBack = { playingMediaItem = null }
            )
        } else if (detailMediaItem != null) {
            MediaDetailScreen(
                mediaItem = detailMediaItem!!,
                addonManager = addonManager,
                repository = repository,
                libraryManager = libraryManager,
                onWatchNow = { playingMediaItem = it },
                onBack = { detailMediaItem = null }
            )
        } else if (isAddonBrowserVisible) {
            AddonBrowserScreen(
                addonManager = addonManager,
                onBack = { isAddonBrowserVisible = false }
            )
        } else {
            NavigationDrawer(
                drawerContent = { _ ->
                    Column(
                        Modifier
                            .fillMaxHeight()
                            .padding(12.dp)
                            .selectableGroup(),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        navItems.forEachIndexed { index, item ->
                            NavigationDrawerItem(
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index },
                                leadingContent = {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.title,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            ) {
                                Text(
                                    text = item.title,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                    }
                }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 24.dp)
                ) {
                    val onMediaClick: (MediaItem) -> Unit = { item ->
                        detailMediaItem = item
                    }
                    
                    when (selectedTabIndex) {
                        0 -> HomeScreen(addonManager, repository, onMediaClick)
                        1 -> CatalogScreen(addonManager, repository, onMediaClick)
                        2 -> SearchScreen(addonManager, repository, libraryManager, onMediaClick)
                        3 -> LibraryScreen(addonManager, repository, libraryManager, onMediaClick)
                        4 -> SettingsScreen(
                            addonManager = addonManager, 
                            themeManager = themeManager,
                            onBrowseAddons = { isAddonBrowserVisible = true },
                            onCheckUpdate = { showUpdateDialog = true }
                        )
                    }
                }
            }
        }

        // Show Update Dialog overlay
        if (showUpdateDialog) {
            UpdateDialog(
                currentVersion = "1.0",
                onDismiss = { showUpdateDialog = false }
            )
        }
    }
}

data class NavItem(val title: String, val icon: ImageVector)
