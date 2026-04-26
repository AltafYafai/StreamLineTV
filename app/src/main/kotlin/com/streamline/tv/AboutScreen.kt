@file:OptIn(ExperimentalTvMaterial3Api::class)
package com.streamline.tv

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.material3.*

@Composable
fun AboutScreen(
    onCheckUpdate: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp)) {
        Text(
            text = "About",
            style = MaterialTheme.typography.displayMedium,
            modifier = Modifier.padding(bottom = 24.dp),
            color = Color.White
        )

        TvLazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
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
                            Text(text = "Current Version: 1.0.7", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White)
                    }
                }
            }

            item { SettingCategoryHeader("Application Details") }
            item { SettingItem("Version", "1.0.7") }
            item { SettingItem("License", "GPL v3.0") }
            item { SettingItem("Developer", "Altaf Yafai") }
            item { SettingItem("GitHub", "github.com/AltafYafai/StreamLineTV") }
            
            item { SettingCategoryHeader("Credits") }
            item { SettingItem("UI Framework", "Jetpack Compose for TV") }
            item { SettingItem("Media Engine", "Android Media3 (ExoPlayer)") }
            item { SettingItem("Image Library", "Coil") }
        }
    }
}
