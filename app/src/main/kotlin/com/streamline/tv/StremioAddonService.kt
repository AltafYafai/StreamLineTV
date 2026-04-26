package com.streamline.tv

import retrofit2.http.GET
import retrofit2.http.Path

interface StremioAddonService {
    @GET("manifest.json")
    suspend fun getManifest(): AddonManifest

    @GET("catalog/{type}/{id}.json")
    suspend fun getCatalog(
        @Path("type") type: String,
        @Path("id") id: String
    ): CatalogResponse

    @GET("catalog/{type}/{id}/{extra}.json")
    suspend fun getCatalogWithExtra(
        @Path("type") type: String,
        @Path("id") id: String,
        @Path("extra") extra: String
    ): CatalogResponse

    @GET("meta/{type}/{id}.json")
    suspend fun getMeta(
        @Path("type") type: String,
        @Path("id") id: String
    ): MetaResponse

    @GET("stream/{type}/{id}.json")
    suspend fun getStreams(
        @Path("type") type: String,
        @Path("id") id: String
    ): StreamResponse
}
