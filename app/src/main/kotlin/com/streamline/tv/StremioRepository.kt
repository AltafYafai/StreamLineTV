package com.streamline.tv

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class StremioRepository {
    private val services = mutableMapOf<String, StremioAddonService>()

    private fun getService(baseUrl: String): StremioAddonService {
        val sanitizedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        return services.getOrPut(sanitizedUrl) {
            Retrofit.Builder()
                .baseUrl(sanitizedUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(StremioAddonService::class.java)
        }
    }

    suspend fun fetchManifest(addonUrl: String): AddonManifest? {
        return try {
            val service = getService(addonUrl.replace("manifest.json", ""))
            service.getManifest()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun fetchCatalog(addonUrl: String, type: String, id: String): List<MediaItem> {
        return try {
            val service = getService(addonUrl.replace("manifest.json", ""))
            val response = service.getCatalog(type, id)
            response.metas.map { meta ->
                MediaItem(
                    id = meta.id,
                    type = meta.type,
                    title = meta.name,
                    description = meta.description ?: "",
                    metadata = meta.type.replaceFirstChar { it.uppercase() },
                    videoUrl = "",
                    posterUrl = meta.poster ?: "",
                    bannerUrl = meta.background ?: meta.poster ?: ""
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun fetchMediaDetail(addonUrl: String, type: String, id: String): MediaItem? {
        return try {
            val service = getService(addonUrl.replace("manifest.json", ""))
            val response = service.getMeta(type, id)
            val meta = response.meta
            MediaItem(
                id = meta.id,
                type = meta.type,
                title = meta.name,
                description = meta.description ?: "",
                metadata = "${meta.releaseInfo ?: ""} • ${meta.runtime ?: ""}",
                videoUrl = "",
                posterUrl = meta.poster ?: "",
                bannerUrl = meta.background ?: meta.poster ?: "",
                episodes = meta.videos?.map { 
                    EpisodeItem(it.id, it.title, it.season ?: 0, it.episode ?: 0, it.thumbnail)
                } ?: emptyList()
            )
        } catch (e: Exception) {
            null
        }
    }

    suspend fun fetchStreams(addonUrl: String, type: String, id: String): List<AddonStream> {
        return try {
            val service = getService(addonUrl.replace("manifest.json", ""))
            val response = service.getStreams(type, id)
            response.streams
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun searchMedia(addonUrl: String, query: String): List<MediaItem> {
        return try {
            val service = getService(addonUrl.replace("manifest.json", ""))
            val response = service.getCatalogWithExtra("movie", "top", "search=$query")
            response.metas.map { meta ->
                MediaItem(
                    id = meta.id,
                    type = meta.type,
                    title = meta.name,
                    description = meta.description ?: "",
                    metadata = meta.type.replaceFirstChar { it.uppercase() },
                    videoUrl = "",
                    posterUrl = meta.poster ?: "",
                    bannerUrl = meta.background ?: meta.poster ?: ""
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
