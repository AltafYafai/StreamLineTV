@file:OptIn(ExperimentalTvMaterial3Api::class)
package com.streamline.tv

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.tv.foundation.lazy.grid.TvGridCells
import androidx.tv.foundation.lazy.grid.TvLazyVerticalGrid
import androidx.tv.material3.*
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import androidx.compose.material3.Surface as MaterialSurface

interface RepoService {
    @GET("nuvio-providers/refs/heads/main/manifest.json")
    suspend fun getRepo(): AddonRepoResponse
}

@Composable
fun AddonBrowserScreen(
    addonManager: AddonManager,
    onBack: () -> Unit
) {
    var repoData by remember { mutableStateOf<AddonRepoResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val installedAddons by addonManager.installedAddons.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try {
            val service = Retrofit.Builder()
                .baseUrl("https://raw.githubusercontent.com/yoruix/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(RepoService::class.java)
            repoData = service.getRepo()
        } catch (e: Exception) {
            // Silence error
        } finally {
            isLoading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp)) {
        Row(
            modifier = Modifier.padding(vertical = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Addon Repository",
                style = MaterialTheme.typography.displayMedium,
                modifier = Modifier.weight(1f),
                color = Color.White
            )
            Button(onClick = onBack) {
                Text("Back")
            }
        }

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Loading Repository...", style = MaterialTheme.typography.headlineMedium, color = Color.White)
            }
        } else if (repoData != null) {
            TvLazyVerticalGrid(
                columns = TvGridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                items(repoData!!.scrapers.size) { index ->
                    val scraper = repoData!!.scrapers[index]
                    val addonKey = "repo:${scraper.id}"
                    val isInstalled = installedAddons.contains(addonKey)
                    
                    FocusableCard(
                        onClick = {
                            scope.launch {
                                if (isInstalled) {
                                    addonManager.removeAddon(addonKey)
                                } else {
                                    addonManager.addAddon(addonKey)
                                }
                            }
                        }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            AsyncImage(
                                model = scraper.logo,
                                contentDescription = scraper.name,
                                modifier = Modifier.size(64.dp).align(Alignment.CenterHorizontally),
                                contentScale = ContentScale.Fit
                            )
                            Text(
                                text = scraper.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            Text(
                                text = scraper.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.LightGray,
                                maxLines = 2
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            val statusColor = if (isInstalled) Color.Red else MaterialTheme.colorScheme.primary

                            MaterialSurface(
                                shape = androidx.compose.material3.MaterialTheme.shapes.extraSmall,
                                color = statusColor.copy(alpha = 0.2f),
                                contentColor = statusColor
                            ) {
                                Text(
                                    text = if (isInstalled) "UNINSTALL" else "INSTALL",
                                    style = MaterialTheme.typography.labelLarge,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
