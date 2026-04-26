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
    onBrowseAddons: () -> Unit,
    onCheckUpdate: () -> Unit
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
                        contentColor = Color.Black
                    )
                ) {
                    Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Storage, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Browse Addon Repository", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
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
                    Button(onClick = { 
                        if (addonUrl.isNotEmpty()) {
                            scope.launch {
                                addonManager.addAddon(addonUrl)
                                addonUrl = ""
                            }
                        }
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Install")
                        Text("Install")
                    }
                }
            }

            items(installedAddons.size) { index ->
                val url = installedAddons[index]
                Surface(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth(),
                    shape = ClickableSurfaceDefaults.shape(MaterialTheme.shapes.medium),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (url.startsWith("repo:")) url.removePrefix("repo:") else url,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                                maxLines = 1
                            )
                            if (url.startsWith("repo:")) {
                                Text("Nuvio Provider", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        Button(
                            onClick = { 
                                scope.launch {
                                    addonManager.removeAddon(url)
                                }
                            },
                            colors = ButtonDefaults.colors(
                                containerColor = Color.Red.copy(alpha = 0.2f),
                                contentColor = Color.Red,
                                focusedContainerColor = Color.Red,
                                focusedContentColor = Color.White
                            )
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                            Spacer(Modifier.width(4.dp))
                            Text("Remove")
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            item { SettingCategoryHeader("General") }
            item { SettingItem("Language", "English") }
            
            item { Spacer(modifier = Modifier.height(16.dp)) }
            
            item { SettingCategoryHeader("Playback") }
            item { SettingToggle("Auto Frame Rate (AFR)", true) }
            item { SettingToggle("Hardware Acceleration", true) }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            item { SettingCategoryHeader("About") }
            item {
                Surface(
                    onClick = onCheckUpdate,
                    modifier = Modifier.fillMaxWidth(),
                    shape = ClickableSurfaceDefaults.shape(MaterialTheme.shapes.medium),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Check for Updates", style = MaterialTheme.typography.titleMedium, color = Color.White)
                            Text(text = "Current Version: 1.0", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White)
                    }
                }
            }
            item { SettingItem("Version", "1.0.1-beta") }
            item { SettingItem("License", "GPL v3.0") }
        }
    }
}

@Composable
fun ThemeButton(name: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.width(120.dp),
        shape = ClickableSurfaceDefaults.shape(MaterialTheme.shapes.medium),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        )
    ) {
        Box(modifier = Modifier.padding(12.dp), contentAlignment = androidx.compose.ui.Alignment.Center) {
            Text(text = name, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun SettingCategoryHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun SettingItem(title: String, subtitle: String) {
    Surface(
        onClick = { /* Handle click */ },
        modifier = Modifier.fillMaxWidth(),
        shape = ClickableSurfaceDefaults.shape(MaterialTheme.shapes.medium),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = Color.White)
            Text(
                text = subtitle, 
                style = MaterialTheme.typography.bodySmall,
                color = Color.LightGray
            )
        }
    }
}

@Composable
fun SettingToggle(title: String, initialValue: Boolean) {
    var checked by remember { mutableStateOf(initialValue) }
    
    Surface(
        onClick = { checked = !checked },
        modifier = Modifier.fillMaxWidth(),
        shape = ClickableSurfaceDefaults.shape(MaterialTheme.shapes.medium),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = Color.White)
            Text(
                text = if (checked) "ON" else "OFF",
                color = if (checked) MaterialTheme.colorScheme.primary else Color.Gray,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}
