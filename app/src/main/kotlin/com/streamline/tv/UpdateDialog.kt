@file:OptIn(ExperimentalTvMaterial3Api::class)
package com.streamline.tv

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.tv.material3.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Composable
fun UpdateDialog(
    currentVersion: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var latestRelease by remember { mutableStateOf<GithubRelease?>(null) }
    var isChecking by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val service = Retrofit.Builder()
                .baseUrl("https://api.github.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(UpdateService::class.java)
            
            val release = service.getLatestRelease("repos/AltafYafai/StreamLineTV/releases/latest")
            if (release.tag_name != "v$currentVersion") {
                latestRelease = release
            }
        } catch (e: Exception) {
            // Silence error
        } finally {
            isChecking = false
        }
    }

    if (latestRelease != null) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.9f)),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .width(600.dp)
                        .wrapContentHeight(),
                    shape = ClickableSurfaceDefaults.shape(MaterialTheme.shapes.large),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = Color.White,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant // Keep background stable
                    ),
                    onClick = {} // Trap focus
                ) {
                    Column(modifier = Modifier.padding(32.dp)) {
                        Text(
                            text = "A New Version is Available",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "StreamLineTV ${latestRelease!!.tag_name}",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text(
                            text = "Release Notes:",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.Gray
                        )
                        
                        Text(
                            text = latestRelease!!.body,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .heightIn(max = 200.dp)
                                .padding(vertical = 12.dp),
                            color = Color.LightGray
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Explicit focus colors to avoid 'white' look
                            Button(
                                onClick = {
                                    val apkAsset = latestRelease!!.assets.firstOrNull { it.name.endsWith(".apk") }
                                    if (apkAsset != null) {
                                        UpdateDownloader(context).downloadAndInstall(
                                            apkAsset.browser_download_url,
                                            "StreamLineTV_${latestRelease!!.tag_name}.apk"
                                        )
                                    }
                                    onDismiss()
                                },
                                modifier = Modifier.weight(1f),
                                scale = ButtonDefaults.scale(focusedScale = 1.05f),
                                colors = ButtonDefaults.colors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = Color.Black,
                                    focusedContainerColor = Color.White,
                                    focusedContentColor = Color.Black
                                )
                            ) {
                                Text("Update Now")
                            }
                            
                            OutlinedButton(
                                onClick = onDismiss,
                                modifier = Modifier.weight(1f),
                                scale = ButtonDefaults.scale(focusedScale = 1.05f),
                                colors = ButtonDefaults.colors(
                                    containerColor = Color.Transparent,
                                    contentColor = Color.White,
                                    focusedContainerColor = Color.White.copy(alpha = 0.1f),
                                    focusedContentColor = Color.White
                                )
                            ) {
                                Text("Later")
                            }
                        }
                    }
                }
            }
        }
    }
}
