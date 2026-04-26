package com.streamline.tv

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun UpdateDialog(
    currentVersion: String,
    onDismiss: () -> Unit
) {
    var latestRelease by remember { mutableStateOf<GithubRelease?>(null) }
    var isChecking by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val service = Retrofit.Builder()
                .baseUrl("https://api.github.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(UpdateService::class.java)
            
            // Replace with your actual repo API URL
            val release = service.getLatestRelease("repos/AltafYafai/StreamLineTV/releases/latest")
            if (release.tag_name != "v$currentVersion") {
                latestRelease = release
            }
        } catch (e: Exception) {
            // Handle error
        } finally {
            isChecking = false
        }
    }

    if (latestRelease != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.8f)),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .width(500.dp)
                    .wrapContentHeight(),
                shape = MaterialTheme.shapes.large,
                colors = SurfaceDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "Update Available: ${latestRelease!!.tag_name}",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Changelog:",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Text(
                        text = latestRelease!!.body,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .heightIn(max = 200.dp)
                            .padding(vertical = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = { /* In real app, launch browser or download manager */ },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Download")
                        }
                        
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Later")
                        }
                    }
                }
            }
        }
    }
}
