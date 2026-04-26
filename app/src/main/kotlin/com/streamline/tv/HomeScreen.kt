package com.streamline.tv

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.material3.*
import coil.compose.AsyncImage

data class HomeRow(
    val title: String,
    val items: List<MediaItem>
)

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun HomeScreen(
    addonManager: AddonManager,
    repository: StremioRepository,
    onMediaClick: (MediaItem) -> Unit
) {
    val installedAddons by addonManager.installedAddons.collectAsState(initial = emptyList())
    var homeRows by remember { mutableStateOf<List<HomeRow>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    
    LaunchedEffect(installedAddons) {
        if (installedAddons.isNotEmpty()) {
            isLoading = true
            val rows = mutableListOf<HomeRow>()
            
            // For each addon, try to fetch its movie catalogs
            for (addonUrl in installedAddons) {
                val manifest = repository.fetchManifest(addonUrl)
                manifest?.catalogs?.filter { it.type == "movie" }?.forEach { catalog ->
                    val fetchedItems = repository.fetchCatalog(addonUrl, catalog.type, catalog.id)
                    if (fetchedItems.isNotEmpty()) {
                        rows.add(HomeRow(
                            title = "${manifest.name} - ${catalog.name ?: catalog.id.replaceFirstChar { it.uppercase() }}",
                            items = fetchedItems
                        ))
                    }
                }
            }
            
            if (rows.isNotEmpty()) {
                homeRows = rows
            } else {
                // Fallback if no data found in addons
                homeRows = listOf(HomeRow("Sample Movies", SampleData.movies))
            }
            isLoading = false
        } else {
            homeRows = listOf(HomeRow("Sample Movies", SampleData.movies))
        }
    }

    val featuredMovie = homeRows.firstOrNull()?.items?.firstOrNull()

    TvLazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Featured Content (Hero Section)
        if (featuredMovie != null) {
            item {
                FeaturedSection(featuredMovie, onMediaClick)
            }
        }

        if (isLoading && homeRows.isEmpty()) {
            item {
                Box(Modifier.fillParentMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text("Loading Catalogs...", style = MaterialTheme.typography.headlineMedium)
                }
            }
        } else {
            // Dynamic Rows from Addons
            items(homeRows.size) { index ->
                val row = homeRows[index]
                ContentRow(
                    title = row.title,
                    movies = row.items,
                    onMediaClick = onMediaClick
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun FeaturedSection(movie: MediaItem, onMediaClick: (MediaItem) -> Unit) {
    FocusableCard(
        onClick = { onMediaClick(movie) },
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .padding(16.dp),
        shape = ClickableSurfaceDefaults.shape(MaterialTheme.shapes.large)
    ) {
        AsyncImage(
            model = movie.bannerUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.6f
        )
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = androidx.compose.ui.Alignment.BottomStart
        ) {
            Column {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.displayMedium
                )
                Text(
                    text = movie.description,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth(0.5f),
                    maxLines = 2
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ContentRow(title: String, movies: List<MediaItem>, onMediaClick: (MediaItem) -> Unit) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(start = 32.dp, bottom = 8.dp)
        )
        TvLazyRow(
            contentPadding = PaddingValues(horizontal = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            items(movies.size) { index ->
                ContentCard(movie = movies[index], onMediaClick)
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ContentCard(movie: MediaItem, onMediaClick: (MediaItem) -> Unit) {
    FocusableCard(
        onClick = { onMediaClick(movie) },
        modifier = Modifier
            .width(200.dp)
            .aspectRatio(16f / 9f)
    ) {
        AsyncImage(
            model = movie.bannerUrl,
            contentDescription = movie.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}
