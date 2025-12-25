package com.cvte.irremote.model.repository

import android.content.Context
import com.cvte.irremote.model.entity.IRConfig
import com.cvte.irremote.model.entity.IRKey
import com.cvte.irremote.model.entity.KeyCategory
import com.cvte.irremote.utils.IRLogger
import com.cvte.irremote.utils.PreferenceManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File

/**
 * 配置数据仓库
 * 
 * 管理IR配置的加载、保存、导入导出
 * 数据源:
 * - assets/default_config.json (默认配置)
 * - SharedPreferences (用户配置存储)
 * - 外部文件 (导入导出)
 */
class ConfigRepository private constructor(private val context: Context) {
    
    companion object {
        private const val TAG = "ConfigRepository"
        private const val PREF_CONFIGS = "ir_configs"
        private const val DEFAULT_CONFIG_FILE = "default_config.json"
        private const val DEFAULT_CONFIG_ID = "cvte_factory"
        
        @Volatile
        private var instance: ConfigRepository? = null
        
        fun getInstance(context: Context): ConfigRepository {
            return instance ?: synchronized(this) {
                instance ?: ConfigRepository(context.applicationContext).also { 
                    instance = it 
                }
            }
        }
    }
    
    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()
    
    private val preferenceManager = PreferenceManager.getInstance(context)
    
    private val prefs = context.getSharedPreferences(PREF_CONFIGS, Context.MODE_PRIVATE)
    
    // 配置缓存
    private val configCache = mutableMapOf<String, IRConfig>()
    
    init {
        // 初始化时加载默认配置
        loadDefaultConfig()
    }
    
    /**
     * 获取当前使用的配置
     */
    fun getCurrentConfig(): IRConfig? {
        val configId = preferenceManager.getCurrentConfigId()
        return if (configId != null) {
            getConfig(configId)
        } else {
            getDefaultConfig()
        }
    }
    
    /**
     * 设置当前使用的配置ID
     */
    fun setCurrentConfigId(configId: String) {
        preferenceManager.setCurrentConfigId(configId)
    }
    
    /**
     * 获取默认配置
     */
    fun getDefaultConfig(): IRConfig? {
        return configCache[DEFAULT_CONFIG_ID] ?: loadDefaultConfig()
    }
    
    /**
     * 获取指定ID的配置
     */
    fun getConfig(configId: String): IRConfig? {
        // 首先检查缓存
        configCache[configId]?.let { return it }
        
        // 从SharedPreferences加载
        val json = prefs.getString(configId, null)
        if (json != null) {
            try {
                val config = gson.fromJson(json, IRConfig::class.java)
                configCache[configId] = config
                return config
            } catch (e: Exception) {
                IRLogger.e(TAG, "Failed to parse config: $configId", e)
            }
        }
        
        return null
    }
    
    /**
     * 获取所有配置列表
     */
    fun getAllConfigs(): List<IRConfig> {
        val configs = mutableListOf<IRConfig>()
        
        // 添加默认配置
        getDefaultConfig()?.let { configs.add(it) }
        
        // 添加用户保存的配置
        prefs.all.forEach { (key, value) ->
            if (key != DEFAULT_CONFIG_ID && value is String) {
                try {
                    val config = gson.fromJson(value, IRConfig::class.java)
                    configs.add(config)
                } catch (e: Exception) {
                    IRLogger.e(TAG, "Failed to parse config: $key", e)
                }
            }
        }
        
        return configs
    }
    
    /**
     * 保存配置
     */
    fun saveConfig(config: IRConfig): Boolean {
        return try {
            val json = gson.toJson(config)
            prefs.edit().putString(config.id, json).apply()
            configCache[config.id] = config
            IRLogger.i(TAG, "Saved config: ${config.name}")
            true
        } catch (e: Exception) {
            IRLogger.e(TAG, "Failed to save config", e)
            false
        }
    }
    
    /**
     * 删除配置
     */
    fun deleteConfig(configId: String): Boolean {
        if (configId == DEFAULT_CONFIG_ID) {
            IRLogger.w(TAG, "Cannot delete default config")
            return false
        }
        
        return try {
            prefs.edit().remove(configId).apply()
            configCache.remove(configId)
            IRLogger.i(TAG, "Deleted config: $configId")
            true
        } catch (e: Exception) {
            IRLogger.e(TAG, "Failed to delete config", e)
            false
        }
    }
    
    /**
     * 导出配置到JSON字符串
     */
    fun exportConfig(config: IRConfig): String {
        return gson.toJson(config)
    }
    
    /**
     * 从JSON字符串导入配置
     */
    fun importConfig(json: String): IRConfig? {
        return try {
            val config = gson.fromJson(json, IRConfig::class.java)
            saveConfig(config)
            config
        } catch (e: Exception) {
            IRLogger.e(TAG, "Failed to import config", e)
            null
        }
    }
    
    /**
     * 导出配置到文件
     */
    fun exportToFile(config: IRConfig, file: File): Boolean {
        return try {
            file.writeText(gson.toJson(config))
            IRLogger.i(TAG, "Exported config to: ${file.absolutePath}")
            true
        } catch (e: Exception) {
            IRLogger.e(TAG, "Failed to export config", e)
            false
        }
    }
    
    /**
     * 从文件导入配置
     */
    fun importFromFile(file: File): IRConfig? {
        return try {
            val json = file.readText()
            importConfig(json)
        } catch (e: Exception) {
            IRLogger.e(TAG, "Failed to import from file", e)
            null
        }
    }

    /**
     * 从远程服务器同步配置
     */
    suspend fun syncFromRemote(): Boolean {
        return try {
            val configs = com.cvte.irremote.network.RetrofitClient.apiService.getAllConfigs()
            var successCount = 0
            
            configs.forEach { metadata ->
                try {
                    val config = com.cvte.irremote.network.RetrofitClient.apiService.getConfig(metadata.id)
                    saveConfig(config)
                    successCount++
                } catch (e: Exception) {
                    IRLogger.e(TAG, "Failed to fetch config: ${metadata.id}", e)
                }
            }
            
            IRLogger.i(TAG, "Synced $successCount configs from remote")
            true  // Success if API call completed, even with empty list
        } catch (e: Exception) {
            IRLogger.e(TAG, "Failed to sync from remote", e)
            false
        }
    }

    /**
     * 从远程同步指定配置
     */
    suspend fun syncSpecificConfig(configId: String): IRConfig? {
        return try {
            val config = com.cvte.irremote.network.RetrofitClient.apiService.getConfig(configId)
            saveConfig(config)
            IRLogger.i(TAG, "Synced specific config: $configId")
            config
        } catch (e: Exception) {
            IRLogger.e(TAG, "Failed to sync specific config: $configId", e)
            null
        }
    }
    
    /**
     * 加载默认配置 (CVTE工厂遥控器)
     */
    private fun loadDefaultConfig(): IRConfig? {
        // 尝试从assets加载
        try {
            val assetManager = context.assets
            val inputStream = assetManager.open(DEFAULT_CONFIG_FILE)
            val json = inputStream.bufferedReader().use { it.readText() }
            val config = gson.fromJson(json, IRConfig::class.java)
            configCache[config.id] = config
            return config
        } catch (e: Exception) {
            IRLogger.w(TAG, "Default config file not found, using built-in config")
        }
        
        // 使用内置的默认配置
        val defaultConfig = createBuiltInConfig()
        configCache[DEFAULT_CONFIG_ID] = defaultConfig
        return defaultConfig
    }
    
    /**
     * 创建内置的CVTE工厂遥控器配置
     */
    private fun createBuiltInConfig(): IRConfig {
        val keys = listOf(
            // 电源键
            IRKey("KEY_POWER", 0x0001, "电源", KeyCategory.FUNCTION),
            
            // 数字键
            IRKey("KEY_0", 0x0010, "0", KeyCategory.NUMBER),
            IRKey("KEY_1", 0x0002, "1", KeyCategory.NUMBER),
            IRKey("KEY_2", 0x0012, "2", KeyCategory.NUMBER),
            IRKey("KEY_3", 0x0006, "3", KeyCategory.NUMBER),
            IRKey("KEY_4", 0x0016, "4", KeyCategory.NUMBER),
            IRKey("KEY_5", 0x0003, "5", KeyCategory.NUMBER),
            IRKey("KEY_6", 0x0013, "6", KeyCategory.NUMBER),
            IRKey("KEY_7", 0x0007, "7", KeyCategory.NUMBER),
            IRKey("KEY_8", 0x0017, "8", KeyCategory.NUMBER),
            IRKey("KEY_9", 0x0000, "9", KeyCategory.NUMBER),
            
            // 方向键
            IRKey("KEY_UP", 0x0056, "▲", KeyCategory.DIRECTION),
            IRKey("KEY_DOWN", 0x0050, "▼", KeyCategory.DIRECTION),
            IRKey("KEY_LEFT", 0x0047, "◄", KeyCategory.DIRECTION),
            IRKey("KEY_RIGHT", 0x004b, "►", KeyCategory.DIRECTION),
            IRKey("KEY_ENTER", 0x0057, "确认", KeyCategory.DIRECTION),
            
            // 音量/频道
            IRKey("KEY_VOLUMEUP", 0x0044, "音量+", KeyCategory.VOLUME),
            IRKey("KEY_VOLUMEDOWN", 0x0045, "音量-", KeyCategory.VOLUME),
            IRKey("KEY_CHANNELUP", 0x0048, "频道+", KeyCategory.CHANNEL),
            IRKey("KEY_CHANNELDOWN", 0x0049, "频道-", KeyCategory.CHANNEL),
            IRKey("KEY_MUTE", 0x0011, "静音", KeyCategory.VOLUME),
            
            // 功能键
            IRKey("KEY_MENU", 0x0046, "菜单", KeyCategory.FUNCTION),
            IRKey("KEY_BACK", 0x004a, "返回", KeyCategory.FUNCTION),
            IRKey("KEY_HOME", 0x004c, "主页", KeyCategory.FUNCTION),
            IRKey("KEY_INFO", 0x0005, "信息", KeyCategory.FUNCTION),
            IRKey("KEY_SUBTITLE", 0x0041, "字幕", KeyCategory.FUNCTION),
            IRKey("KEY_HELP", 0x004d, "帮助", KeyCategory.FUNCTION),
            IRKey("KEY_ZOOM", 0x0051, "缩放", KeyCategory.FUNCTION),
            IRKey("KEY_RECORD", 0x0059, "录制", KeyCategory.FUNCTION),
            IRKey("KEY_MEDIA", 0x0009, "媒体", KeyCategory.FUNCTION),
            IRKey("KEY_DIRECTION", 0x000d, "画面模式", KeyCategory.FUNCTION),
            IRKey("KEY_FN_F1", 0x0055, "MTS", KeyCategory.FUNCTION),
            IRKey("KEY_KP1", 0x0040, "KP1", KeyCategory.FUNCTION),
            
            // 颜色快捷键
            IRKey("KEY_RED", 0x001d, "红", KeyCategory.COLOR),
            IRKey("KEY_GREEN", 0x001e, "绿", KeyCategory.COLOR),
            IRKey("KEY_YELLOW", 0x001f, "黄", KeyCategory.COLOR),
            IRKey("KEY_BLUE", 0x001c, "蓝", KeyCategory.COLOR),
            
            // 工厂测试键
            IRKey("KEY_FN_F", 0x0008, "FAC复位", KeyCategory.FACTORY),
            IRKey("KEY_FN_E", 0x0018, "工厂菜单", KeyCategory.FACTORY),
            IRKey("KEY_F13", 0x000a, "自动调台", KeyCategory.FACTORY),
            IRKey("KEY_PROG3", 0x000b, "老化测试", KeyCategory.FACTORY),
            IRKey("KEY_F14", 0x000e, "版本号", KeyCategory.FACTORY),
            IRKey("KEY_F15", 0x00a2, "清除HDCP", KeyCategory.FACTORY),
            IRKey("KEY_F16", 0x00a3, "清除MAC", KeyCategory.FACTORY),
            IRKey("KEY_F17", 0x00a4, "清除CIPLUS", KeyCategory.FACTORY),
            IRKey("KEY_F1", 0x0004, "F1", KeyCategory.FACTORY),
            IRKey("KEY_F1_ALT", 0x0014, "F1备用", KeyCategory.FACTORY),
            IRKey("KEY_FN_B", 0x000f, "FN_B", KeyCategory.FACTORY),
            IRKey("KEY_TOUCHPAD_TOGGLE", 0x000c, "触控切换", KeyCategory.FACTORY),
            IRKey("KEY_BRL_DOT2", 0x0019, "ADC校准", KeyCategory.FACTORY)
        )
        
        return IRConfig(
            id = DEFAULT_CONFIG_ID,
            name = "CVTE工厂遥控器",
            protocol = IRConfig.PROTOCOL_NEC,
            header = 0x8890,
            keys = keys,
            isDefault = true
        )
    }
}
