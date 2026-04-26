package com.streamline.tv

import com.google.gson.annotations.SerializedName

/**
 * Enhanced Stremio Addon Protocol Models
 */

data class AddonManifest(
    val id: String,
    val name: String,
    val description: String?,
    val version: String,
    val resources: List<Any>, // Can be strings or resource objects
    val types: List<String>,
    val catalogs: List<AddonCatalog>
)

data class AddonCatalog(
    val type: String,
    val id: String,
    val name: String?
)

data class MetaResponse(
    val meta: MetaDetail
)

data class MetaDetail(
    val id: String,
    val type: String,
    val name: String,
    val poster: String?,
    val background: String?,
    val description: String?,
    val releaseInfo: String?,
    val genres: List<String>?,
    val videos: List<EpisodeDetail>?, // For Series
    val runtime: String?
)

data class EpisodeDetail(
    val id: String,
    val title: String,
    val released: String?,
    val season: Int?,
    val episode: Int?,
    val thumbnail: String?
)

data class CatalogResponse(
    val metas: List<MetaPreview>
)

data class MetaPreview(
    val id: String,
    val type: String,
    val name: String,
    val poster: String?,
    val background: String?,
    val description: String?
)

data class StreamResponse(
    val streams: List<AddonStream>
)

data class AddonStream(
    val title: String?,
    val url: String?,
    val infoHash: String?,
    val fileIdx: Int?,
    val behaviorHints: Map<String, Any>?,
    val subtitles: List<AddonSubtitle>?
)

data class AddonSubtitle(
    val id: String?,
    val url: String,
    val lang: String
)
