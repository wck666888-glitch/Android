package com.cvte.irremote.network

import com.cvte.irremote.model.entity.ConfigMetadata
import com.cvte.irremote.model.entity.IRConfig
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * 远程配置服务 API 接口
 * 
 * 用于从服务器拉取IR配置
 */
interface RemoteConfigService {
    
    /**
     * 获取配置列表
     */
    @GET("/api/configs")
    suspend fun getConfigList(): List<ConfigMetadata>
    
    /**
     * 获取指定配置详情
     */
    @GET("/api/configs/{id}")
    suspend fun getConfig(@Path("id") id: String): IRConfig
    
    /**
     * 获取默认配置
     */
    @GET("/api/configs/default")
    suspend fun getDefaultConfig(): IRConfig
}
