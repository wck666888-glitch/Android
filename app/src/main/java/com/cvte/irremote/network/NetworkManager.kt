package com.cvte.irremote.network

import android.content.Context
import com.cvte.irremote.model.entity.ConfigMetadata
import com.cvte.irremote.model.entity.IRConfig
import com.cvte.irremote.utils.IRLogger
import com.cvte.irremote.utils.PreferenceManager
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * 网络客户端管理器
 * 
 * 封装Retrofit配置和网络请求
 */
class NetworkManager private constructor(context: Context) {
    
    companion object {
        private const val TAG = "NetworkManager"
        private const val TIMEOUT_SECONDS = 30L
        
        @Volatile
        private var instance: NetworkManager? = null
        
        fun getInstance(context: Context): NetworkManager {
            return instance ?: synchronized(this) {
                instance ?: NetworkManager(context.applicationContext).also { 
                    instance = it 
                }
            }
        }
    }
    
    private val preferenceManager = PreferenceManager.getInstance(context)
    
    private val loggingInterceptor = HttpLoggingInterceptor { message ->
        IRLogger.d(TAG, message)
    }.apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .build()
    
    private val gson = GsonBuilder()
        .setLenient()
        .create()
    
    /**
     * 获取配置服务
     */
    fun getConfigService(): RemoteConfigService {
        val baseUrl = preferenceManager.getServerUrl()
        
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        
        return retrofit.create(RemoteConfigService::class.java)
    }
    
    /**
     * 拉取远程配置列表
     */
    suspend fun fetchConfigList(): Result<List<ConfigMetadata>> {
        return try {
            val service = getConfigService()
            val configs = service.getConfigList()
            Result.success(configs)
        } catch (e: Exception) {
            IRLogger.e(TAG, "Failed to fetch config list", e)
            Result.failure(e)
        }
    }
    
    /**
     * 拉取指定配置
     */
    suspend fun fetchConfig(configId: String): Result<IRConfig> {
        return try {
            val service = getConfigService()
            val config = service.getConfig(configId)
            Result.success(config)
        } catch (e: Exception) {
            IRLogger.e(TAG, "Failed to fetch config: $configId", e)
            Result.failure(e)
        }
    }
    
    /**
     * 拉取默认配置
     */
    suspend fun fetchDefaultConfig(): Result<IRConfig> {
        return try {
            val service = getConfigService()
            val config = service.getDefaultConfig()
            Result.success(config)
        } catch (e: Exception) {
            IRLogger.e(TAG, "Failed to fetch default config", e)
            Result.failure(e)
        }
    }
}
