package com.cvte.irremote.network

import com.cvte.irremote.model.entity.IRConfig
import retrofit2.http.GET
import retrofit2.http.Path

interface ConfigApiService {
    @GET("api/configs")
    suspend fun getAllConfigs(): List<ConfigMetadata>

    @GET("api/configs/{id}")
    suspend fun getConfig(@Path("id") id: String): IRConfig
}

data class ConfigMetadata(
    val id: String,
    val name: String,
    val description: String,
    val version: String,
    val updated_at: Long
)
