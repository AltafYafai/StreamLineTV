package com.streamline.tv

data class AddonRepoResponse(
    val name: String,
    val version: String,
    val scrapers: List<RepoScraper>
)

data class RepoScraper(
    val id: String,
    val name: String,
    val description: String,
    val version: String,
    val logo: String?,
    val author: String?,
    val enabled: Boolean
)
