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
fun CatalogScreen(
    addonManager: AddonManager,
    repository: StremioRepository,
    onMediaClick: (MediaItem) -> Unit
) {
    val installedAddons by addonManager.installedAddons.collectAsState(initial = emptyList())
    var movies by remember { mutableStateOf(SampleData.movies) }

    LaunchedEffect(installedAddons) {
        if (installedAddons.isNotEmpty()) {
            val fetched = repository.fetchCatalog(installedAddons.first(), "movie", "top")
            if (fetched.isNotEmpty()) {
                movies = fetched
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp)) {
        Text(
            text = "Movies & Shows",
            style = MaterialTheme.typography.displayMedium,
            modifier = Modifier.padding(vertical = 24.dp)
        )
        
        TvLazyVerticalGrid(
            columns = TvGridCells.Fixed(6),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 32.dp)
        ) {
            items(movies.size) { index ->
                CatalogItemCard(movie = movies[index], onMediaClick = onMediaClick)
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun CatalogItemCard(movie: MediaItem, onMediaClick: (MediaItem) -> Unit) {
    FocusableCard(
        onClick = { onMediaClick(movie) },
        modifier = Modifier
            .aspectRatio(2f / 3f)
            .fillMaxWidth()
    ) {
        AsyncImage(
            model = movie.posterUrl,
            contentDescription = movie.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}
