package com.cvte.irremote.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * SharedPreferences 管理器
 * 
 * 封装应用配置的存储和读取
 */
class PreferenceManager private constructor(context: Context) {
    
    companion object {
        private const val PREF_NAME = "ir_remote_prefs"
        
        // 键名常量
        private const val KEY_CURRENT_CONFIG_ID = "current_config_id"
        private const val KEY_SERVER_URL = "server_url"
        private const val KEY_AUTO_SYNC = "auto_sync"
        private const val KEY_VIBRATE_ON_EMIT = "vibrate_on_emit"
        private const val KEY_SHOW_TOAST = "show_toast"
        private const val KEY_FIRST_LAUNCH = "first_launch"
        
        // 默认值
        private const val DEFAULT_SERVER_URL = "http://192.168.1.100:3000"
        
        @Volatile
        private var instance: PreferenceManager? = null
        
        fun getInstance(context: Context): PreferenceManager {
            return instance ?: synchronized(this) {
                instance ?: PreferenceManager(context.applicationContext).also { 
                    instance = it 
                }
            }
        }
    }
    
    private val prefs: SharedPreferences = 
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    
    // ===== 配置相关 =====
    
    /**
     * 获取当前使用的配置ID
     */
    fun getCurrentConfigId(): String? = prefs.getString(KEY_CURRENT_CONFIG_ID, null)
    
    /**
     * 设置当前使用的配置ID
     */
    fun setCurrentConfigId(configId: String) {
        prefs.edit { putString(KEY_CURRENT_CONFIG_ID, configId) }
    }
    
    // ===== 服务器相关 =====
    
    /**
     * 获取服务器URL
     */
    fun getServerUrl(): String = 
        prefs.getString(KEY_SERVER_URL, DEFAULT_SERVER_URL) ?: DEFAULT_SERVER_URL
    
    /**
     * 设置服务器URL
     */
    fun setServerUrl(url: String) {
        prefs.edit { putString(KEY_SERVER_URL, url) }
    }
    
    /**
     * 是否自动同步
     */
    fun isAutoSyncEnabled(): Boolean = prefs.getBoolean(KEY_AUTO_SYNC, false)
    
    /**
     * 设置自动同步开关
     */
    fun setAutoSyncEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_AUTO_SYNC, enabled) }
    }
    
    // ===== 用户偏好 =====
    
    /**
     * 发射时是否震动
     */
    fun isVibrateOnEmit(): Boolean = prefs.getBoolean(KEY_VIBRATE_ON_EMIT, true)
    
    /**
     * 设置发射震动开关
     */
    fun setVibrateOnEmit(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_VIBRATE_ON_EMIT, enabled) }
    }
    
    /**
     * 是否显示Toast提示
     */
    fun isShowToast(): Boolean = prefs.getBoolean(KEY_SHOW_TOAST, true)
    
    /**
     * 设置Toast显示开关
     */
    fun setShowToast(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_SHOW_TOAST, enabled) }
    }
    
    // ===== 首次启动 =====
    
    /**
     * 是否首次启动
     */
    fun isFirstLaunch(): Boolean = prefs.getBoolean(KEY_FIRST_LAUNCH, true)
    
    /**
     * 标记已非首次启动
     */
    fun setFirstLaunchDone() {
        prefs.edit { putBoolean(KEY_FIRST_LAUNCH, false) }
    }
    
    /**
     * 清除所有配置
     */
    fun clear() {
        prefs.edit { clear() }
    }
}
