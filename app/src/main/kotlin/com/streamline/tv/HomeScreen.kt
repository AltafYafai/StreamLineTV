@file:OptIn(ExperimentalTvMaterial3Api::class)
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
import kotlinx.coroutines.delay

data class HomeRow(
    val title: String,
    val items: List<MediaItem>
)

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
            homeRows = if (rows.isNotEmpty()) rows else listOf(HomeRow("Sample Movies", SampleData.movies))
            isLoading = false
        } else {
            homeRows = listOf(HomeRow("Sample Movies", SampleData.movies))
        }
    }

    val carouselMovies = remember(homeRows) {
        homeRows.firstOrNull()?.items?.take(5) ?: SampleData.movies
    }

    TvLazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Live Carousel Section
        if (carouselMovies.isNotEmpty()) {
            item {
                FeaturedCarousel(carouselMovies, onMediaClick)
            }
        }

        if (isLoading && homeRows.isEmpty()) {
            item {
                Box(Modifier.fillParentMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text("Loading Catalogs...", style = MaterialTheme.typography.headlineMedium)
                }
            }
        } else {
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

@Composable
fun FeaturedCarousel(movies: List<MediaItem>, onMediaClick: (MediaItem) -> Unit) {
    Carousel(
        itemCount = movies.size,
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .padding(16.dp),
        carouselIndicator = {
            CarouselDefaults.IndicatorRow(
                itemCount = movies.size,
                activeItemIndex = it,
                modifier = Modifier
                    .align(androidx.compose.ui.Alignment.BottomEnd)
                    .padding(32.dp)
            )
        }
    ) { index ->
        val movie = movies[index]
        
        // Use a Surface for the background to handle focus/click correctly
        Surface(
            onClick = { onMediaClick(movie) },
            scale = ClickableSurfaceDefaults.scale(focusedScale = 1.0f), // No zoom on carousel items
            modifier = Modifier.fillMaxSize(),
            shape = ClickableSurfaceDefaults.shape(MaterialTheme.shapes.large)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
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
                        .padding(48.dp),
                    contentAlignment = androidx.compose.ui.Alignment.BottomStart
                ) {
                    Column {
                        Text(
                            text = movie.title,
                            style = MaterialTheme.typography.displayMedium,
                            color = androidx.compose.ui.graphics.Color.White
                        )
                        Text(
                            text = movie.description,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.fillMaxWidth(0.5f),
                            maxLines = 2,
                            color = androidx.compose.ui.graphics.Color.LightGray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ContentRow(title: String, movies: List<MediaItem>, onMediaClick: (MediaItem) -> Unit) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(start = 32.dp, bottom = 12.dp),
            color = androidx.compose.ui.graphics.Color.White
        )
        TvLazyRow(
            contentPadding = PaddingValues(start = 32.dp, end = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            items(movies.size) { index ->
                ContentCard(movie = movies[index], onMediaClick)
            }
        }
    }
}

@Composable
fun ContentCard(movie: MediaItem, onMediaClick: (MediaItem) -> Unit) {
    FocusableCard(
        onClick = { onMediaClick(movie) },
        modifier = Modifier
            .width(220.dp)
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
