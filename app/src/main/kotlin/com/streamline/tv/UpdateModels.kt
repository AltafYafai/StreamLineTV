package com.streamline.tv

import retrofit2.http.GET
import retrofit2.http.Url

interface UpdateService {
    @GET
    suspend fun getLatestRelease(@Url url: String): GithubRelease
}

data class GithubRelease(
    val tag_name: String,
    val name: String,
    val body: String, // Changelog
    val assets: List<GithubAsset>
)

data class GithubAsset(
    val name: String,
    val browser_download_url: String
)
