@file:OptIn(ExperimentalTvMaterial3Api::class)
package com.streamline.tv

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.material3.*
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    addonManager: AddonManager, 
    themeManager: ThemeManager,
    onBrowseAddons: () -> Unit
) {
    var addonUrl by remember { mutableStateOf("") }
    val installedAddons by addonManager.installedAddons.collectAsState(initial = emptyList())
    val currentThemeName by themeManager.selectedThemeName.collectAsState(initial = "Purple")
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp)) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.displayMedium,
            modifier = Modifier.padding(bottom = 24.dp),
            color = Color.White
        )

        TvLazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item { SettingCategoryHeader("Appearance") }
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ThemeButton("Purple", currentThemeName == "Purple") { scope.launch { themeManager.setTheme("Purple") } }
                    ThemeButton("Green", currentThemeName == "Green") { scope.launch { themeManager.setTheme("Green") } }
                    ThemeButton("Blue", currentThemeName == "Blue") { scope.launch { themeManager.setTheme("Blue") } }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            item { SettingCategoryHeader("Addons & Providers") }
            
            item {
                Surface(
                    onClick = onBrowseAddons,
                    modifier = Modifier.fillMaxWidth(),
                    shape = ClickableSurfaceDefaults.shape(MaterialTheme.shapes.medium),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        contentColor = Color.Black,
                        focusedContainerColor = Color.White,
                        focusedContentColor = Color.Black
                    )
                ) {
                    Row(modifier = Modifier.padding(20.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Storage, contentDescription = null)
                        Spacer(Modifier.width(12.dp))
                        Text("Open Addon Browser", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), 
                    horizontalArrangement = Arrangement.spacedBy(12.dp), 
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = addonUrl,
                        onValueChange = { addonUrl = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Enter Addon URL or JSON manifest URL", color = Color.Gray) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.DarkGray
                        )
                    )
                    Button(
                        onClick = { 
                            if (addonUrl.isNotEmpty()) {
                                scope.launch {
                                    addonManager.addAddon(addonUrl)
                                    addonUrl = ""
                                }
                            }
                        },
                        colors = ButtonDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = Color.White,
                            focusedContainerColor = MaterialTheme.colorScheme.primary,
                            focusedContentColor = Color.Black
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Install")
                        Text("Add Manual")
                    }
                }
            }

            items(installedAddons.size) { index ->
                val url = installedAddons[index]
                // Redesigned Addon Row: Premium list look
                Surface(
                    onClick = { /* Detail if needed */ },
                    modifier = Modifier.fillMaxWidth(),
                    shape = ClickableSurfaceDefaults.shape(MaterialTheme.shapes.medium),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (url.startsWith("repo:")) url.removePrefix("repo:") else url,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                maxLines = 1
                            )
                            Text(
                                text = if (url.startsWith("repo:")) "Community Provider" else "Direct Manifest", 
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                            )
                        }

                        // Seperated focusable Delete button
                        Button(
                            onClick = { 
                                scope.launch { addonManager.removeAddon(url) }
                            },
                            colors = ButtonDefaults.colors(
                                containerColor = Color.Red.copy(alpha = 0.15f),
                                contentColor = Color.Red,
                                focusedContainerColor = Color.Red,
                                focusedContentColor = Color.White
                            ),
                            modifier = Modifier.padding(start = 16.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Delete")
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            item { SettingCategoryHeader("General") }
            item { SettingItem("Language", "English") }
            
            item { Spacer(modifier = Modifier.height(16.dp)) }
            
            item { SettingCategoryHeader("Playback") }
            item { SettingToggle("Auto Frame Rate (AFR)", true) }
            item { SettingToggle("Hardware Acceleration", true) }
        }
    }
}
