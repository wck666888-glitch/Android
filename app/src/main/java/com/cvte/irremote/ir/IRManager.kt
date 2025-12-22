package com.cvte.irremote.ir

import android.content.Context
import com.cvte.irremote.model.entity.EmitResult
import com.cvte.irremote.model.entity.IRConfig
import com.cvte.irremote.model.entity.IRKey
import com.cvte.irremote.model.repository.ConfigRepository
import com.cvte.irremote.utils.IRLogger

/**
 * IR管理器 (单例)
 * 
 * 统一管理IR发射功能，封装底层发射器实现
 * 
 * 职责:
 * - 管理当前使用的IR配置
 * - 根据按键名称发射对应的IR信号
 * - 记录发射日志
 * - 提供发射状态回调
 */
class IRManager private constructor(private val context: Context) {
    
    companion object {
        private const val TAG = "IRManager"
        
        @Volatile
        private var instance: IRManager? = null
        
        /**
         * 获取IRManager单例
         */
        fun getInstance(context: Context): IRManager {
            return instance ?: synchronized(this) {
                instance ?: IRManager(context.applicationContext).also { instance = it }
            }
        }
    }
    
    // IR发射器 (默认使用NEC协议)
    private val emitter: IIREmitter = NECIREmitter(context)
    
    // 配置仓库
    private val configRepository: ConfigRepository by lazy {
        ConfigRepository.getInstance(context)
    }
    
    // 当前使用的配置
    private var currentConfig: IRConfig? = null
    
    // 发射回调监听器
    private var emitListener: OnEmitListener? = null
    
    /**
     * 发射回调接口
     */
    interface OnEmitListener {
        fun onEmitStart(keyName: String)
        fun onEmitSuccess(result: EmitResult)
        fun onEmitFailed(result: EmitResult)
    }
    
    /**
     * 设置发射回调监听器
     */
    fun setOnEmitListener(listener: OnEmitListener?) {
        this.emitListener = listener
    }
    
    /**
     * 检查设备是否支持IR发射
     */
    fun isIRSupported(): Boolean = emitter.isSupported()
    
    /**
     * 获取支持的载波频率
     */
    fun getSupportedFrequencies(): List<IntRange> = emitter.getCarrierFrequencies()
    
    /**
     * 加载配置
     */
    fun loadConfig(configId: String? = null): Boolean {
        return try {
            currentConfig = if (configId != null) {
                configRepository.getConfig(configId)
            } else {
                configRepository.getCurrentConfig()
            }
            
            if (currentConfig != null) {
                IRLogger.i(TAG, "Loaded config: ${currentConfig?.name}")
                true
            } else {
                // 尝试加载默认配置
                currentConfig = configRepository.getDefaultConfig()
                currentConfig != null
            }
        } catch (e: Exception) {
            IRLogger.e(TAG, "Failed to load config", e)
            false
        }
    }
    
    /**
     * 获取当前配置
     */
    fun getCurrentConfig(): IRConfig? = currentConfig
    
    /**
     * 设置当前配置
     */
    fun setCurrentConfig(config: IRConfig) {
        currentConfig = config
        configRepository.setCurrentConfigId(config.id)
        IRLogger.i(TAG, "Set current config: ${config.name}")
    }
    
    /**
     * 根据按键名称发射IR信号
     * 
     * @param keyName 按键名称 (如 KEY_POWER)
     * @return 发射结果
     */
    fun emit(keyName: String): EmitResult {
        val config = currentConfig ?: return EmitResult(
            success = false,
            keyName = keyName,
            message = "未加载配置"
        )
        
        // 查找按键
        val key = config.findKeyByName(keyName) ?: return EmitResult(
            success = false,
            keyName = keyName,
            message = "未找到按键: $keyName"
        )
        
        return emitKey(key, config)
    }
    
    /**
     * 根据按键码值发射IR信号
     * 
     * @param keyCode 按键码值
     * @return 发射结果
     */
    fun emitByCode(keyCode: Int): EmitResult {
        val config = currentConfig ?: return EmitResult(
            success = false,
            keyName = "0x${keyCode.toString(16)}",
            message = "未加载配置"
        )
        
        val key = config.findKeyByCode(keyCode) ?: IRKey(
            keyName = "CUSTOM",
            keyCode = keyCode,
            displayName = "0x${keyCode.toString(16)}"
        )
        
        return emitKey(key, config)
    }
    
    /**
     * 发射指定按键的IR信号
     */
    private fun emitKey(key: IRKey, config: IRConfig): EmitResult {
        // 通知开始发射
        emitListener?.onEmitStart(key.keyName)
        
        IRLogger.d(TAG, "Emitting key: ${key.keyName} (${key.getFormattedKeyCode()})")
        
        // 发射信号
        val result = emitter.emit(
            protocol = config.protocol,
            header = config.header,
            keyCode = key.keyCode
        )
        
        // 记录日志
        if (result.success) {
            IRLogger.i(TAG, "Emit success: ${key.displayName}")
            emitListener?.onEmitSuccess(result.copy(keyName = key.displayName))
        } else {
            IRLogger.w(TAG, "Emit failed: ${result.message}")
            emitListener?.onEmitFailed(result.copy(keyName = key.displayName))
        }
        
        return result.copy(keyName = key.displayName)
    }
    
    /**
     * 发射自定义IR信号
     * 
     * @param protocol 协议类型
     * @param header 协议头
     * @param keyCode 按键码值
     */
    fun emitCustom(protocol: Int, header: Int, keyCode: Int): EmitResult {
        emitListener?.onEmitStart("CUSTOM")
        
        val result = emitter.emit(protocol, header, keyCode)
        
        if (result.success) {
            emitListener?.onEmitSuccess(result)
        } else {
            emitListener?.onEmitFailed(result)
        }
        
        return result
    }
    
    /**
     * 发射原始IR信号
     * 
     * @param frequency 载波频率
     * @param pattern 时序模式
     */
    fun emitRaw(frequency: Int, pattern: IntArray): EmitResult {
        return emitter.emitRaw(frequency, pattern)
    }
}
