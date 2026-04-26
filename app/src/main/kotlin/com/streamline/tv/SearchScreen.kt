package com.streamline.tv

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.tv.foundation.lazy.grid.TvGridCells
import androidx.tv.foundation.lazy.grid.TvLazyVerticalGrid
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.material3.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SearchScreen(
    addonManager: AddonManager,
    repository: StremioRepository,
    libraryManager: LibraryManager,
    onMediaClick: (MediaItem) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("movie") }
    val installedAddons by addonManager.installedAddons.collectAsState(initial = emptyList())
    val searchHistory by libraryManager.searchHistory.collectAsState(initial = emptyList())
    var filteredMovies by remember { mutableStateOf<List<MediaItem>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

    LaunchedEffect(searchQuery, selectedType, installedAddons) {
        if (searchQuery.length < 3) {
            filteredMovies = emptyList()
            return@LaunchedEffect
        }
        
        isSearching = true
        delay(500)
        
        if (installedAddons.isNotEmpty()) {
            val results = mutableListOf<MediaItem>()
            for (addon in installedAddons) {
                results.addAll(repository.searchMedia(addon, searchQuery)) 
            }
            filteredMovies = results.distinctBy { it.id }.filter { it.type == selectedType }
            if (filteredMovies.isNotEmpty()) libraryManager.addSearchQuery(searchQuery)
        } else {
            filteredMovies = SampleData.movies.filter { it.title.contains(searchQuery, ignoreCase = true) && it.type == selectedType }
        }
        isSearching = false
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp)) {
        // Search Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 8.dp),
            placeholder = { Text("Search for movies or shows...", color = Color.Gray) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.DarkGray
            ),
            textStyle = MaterialTheme.typography.bodyLarge
        )

        // Filter Chips
        Row(modifier = Modifier.padding(bottom = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FilterChip(label = "Movies", isSelected = selectedType == "movie") { selectedType = "movie" }
            FilterChip(label = "Series", isSelected = selectedType == "series") { selectedType = "series" }
        }

        // Search History
        if (searchQuery.isEmpty() && searchHistory.isNotEmpty()) {
            Text("Recent Searches", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            TvLazyRow(modifier = Modifier.padding(vertical = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(searchHistory.size) { index ->
                    ActionChip(onClick = { searchQuery = searchHistory[index] }) {
                        Text(searchHistory[index], modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), color = Color.White)
                    }
                }
            }
        }

        if (isSearching) {
            Text("Searching...", color = Color.LightGray, modifier = Modifier.padding(top = 16.dp))
        } else {
            TvLazyVerticalGrid(
                columns = TvGridCells.Fixed(6),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 32.dp)
            ) {
                items(filteredMovies.size) { index ->
                    CatalogItemCard(movie = filteredMovies[index], onMediaClick = onMediaClick)
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun FilterChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(MaterialTheme.shapes.extraLarge),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        )
    ) {
        Text(label, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), color = if (isSelected) Color.Black else Color.White)
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ActionChip(onClick: () -> Unit, content: @Composable () -> Unit) {
    Surface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(MaterialTheme.shapes.extraLarge),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            focusedContainerColor = MaterialTheme.colorScheme.primary,
            focusedContentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        content()
    }
}
