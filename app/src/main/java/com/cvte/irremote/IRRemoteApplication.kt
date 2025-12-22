package com.cvte.irremote

import android.app.Application
import com.cvte.irremote.ir.IRManager
import com.cvte.irremote.model.repository.ConfigRepository
import com.cvte.irremote.utils.IRLogger

/**
 * IR Remote 应用类
 * 
 * 负责应用级别的初始化工作
 */
class IRRemoteApplication : Application() {
    
    companion object {
        private const val TAG = "IRRemoteApp"
        
        lateinit var instance: IRRemoteApplication
            private set
    }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        
        // 初始化日志
        IRLogger.isEnabled = true
        IRLogger.i(TAG, "Application started")
        
        // 预初始化配置仓库
        ConfigRepository.getInstance(this)
        
        // 预初始化IR管理器
        val irManager = IRManager.getInstance(this)
        
        // 检查IR支持情况
        if (irManager.isIRSupported()) {
            IRLogger.i(TAG, "IR emitter is supported")
        } else {
            IRLogger.w(TAG, "IR emitter is NOT supported on this device")
        }
        
        // 加载默认配置
        irManager.loadConfig()
    }
}
