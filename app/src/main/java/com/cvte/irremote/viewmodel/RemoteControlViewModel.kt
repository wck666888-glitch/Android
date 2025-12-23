package com.cvte.irremote.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.cvte.irremote.ir.IRManager
import com.cvte.irremote.model.entity.EmitResult
import com.cvte.irremote.model.entity.IRConfig
import com.cvte.irremote.model.repository.ConfigRepository
import com.cvte.irremote.utils.IRLogger

/**
 * 遥控器界面 ViewModel
 * 
 * 管理遥控器界面的业务逻辑和状态
 */
class RemoteControlViewModel(application: Application) : AndroidViewModel(application) {
    
    companion object {
        private const val TAG = "RemoteControlVM"
    }
    
    private val irManager = IRManager.getInstance(application)
    private val configRepository = ConfigRepository.getInstance(application)
    
    // 当前配置
    private val _currentConfig = MutableLiveData<IRConfig?>()
    val currentConfig: LiveData<IRConfig?> = _currentConfig
    
    // 发射结果
    private val _emitResult = MutableLiveData<EmitResult>()
    val emitResult: LiveData<EmitResult> = _emitResult
    
    // IR支持状态
    private val _isIRSupported = MutableLiveData<Boolean>()
    val isIRSupported: LiveData<Boolean> = _isIRSupported
    
    // 发射中状态
    private val _isEmitting = MutableLiveData<Boolean>()
    val isEmitting: LiveData<Boolean> = _isEmitting
    
    // 状态消息
    private val _statusMessage = MutableLiveData<String>()
    val statusMessage: LiveData<String> = _statusMessage

    // 同步状态
    private val _isSyncing = MutableLiveData<Boolean>()
    val isSyncing: LiveData<Boolean> = _isSyncing

    /**
     * 同步远程配置
     */
    fun syncConfigs() {
        if (_isSyncing.value == true) return
        
        _isSyncing.value = true
        _statusMessage.value = "正在同步配置..."
        
        viewModelScope.launch {
            val success = configRepository.syncFromRemote()
            
            _isSyncing.value = false
            if (success) {
                _statusMessage.value = "配置同步成功"
                // 刷新当前配置
                loadCurrentConfig()
            } else {
                _statusMessage.value = "配置同步失败，请检查网络"
            }
        }
    }
    
    init {
        // 检查IR支持
        _isIRSupported.value = irManager.isIRSupported()
        
        // 设置发射回调
        irManager.setOnEmitListener(object : IRManager.OnEmitListener {
            override fun onEmitStart(keyName: String) {
                _isEmitting.postValue(true)
            }
            
            override fun onEmitSuccess(result: EmitResult) {
                _isEmitting.postValue(false)
                _emitResult.postValue(result)
            }
            
            override fun onEmitFailed(result: EmitResult) {
                _isEmitting.postValue(false)
                _emitResult.postValue(result)
            }
        })
        
        // 加载配置
        loadConfig()
    }
    
    /**
     * 加载配置
     */
    fun loadConfig(configId: String? = null) {
        val success = irManager.loadConfig(configId)
        if (success) {
            _currentConfig.value = irManager.getCurrentConfig()
            _statusMessage.value = "已加载: ${_currentConfig.value?.name}"
            IRLogger.i(TAG, "Config loaded: ${_currentConfig.value?.name}")
        } else {
            _statusMessage.value = "配置加载失败"
            IRLogger.w(TAG, "Failed to load config")
        }
    }
    
    /**
     * 发射按键信号
     */
    fun emitKey(keyName: String) {
        if (_isIRSupported.value != true) {
            _emitResult.value = EmitResult(
                success = false,
                keyName = keyName,
                message = "设备不支持IR发射功能"
            )
            return
        }
        
        IRLogger.d(TAG, "Emitting key: $keyName")
        val result = irManager.emit(keyName)
        _emitResult.value = result
    }
    
    /**
     * 发射数字键
     */
    fun emitNumber(number: Int) {
        val keyName = "KEY_$number"
        emitKey(keyName)
    }
    
    /**
     * 发射电源键
     */
    fun emitPower() = emitKey("KEY_POWER")
    
    /**
     * 发射静音键
     */
    fun emitMute() = emitKey("KEY_MUTE")
    
    /**
     * 发射方向键
     */
    fun emitUp() = emitKey("KEY_UP")
    fun emitDown() = emitKey("KEY_DOWN")
    fun emitLeft() = emitKey("KEY_LEFT")
    fun emitRight() = emitKey("KEY_RIGHT")
    fun emitOk() = emitKey("KEY_ENTER")
    
    /**
     * 发射音量键
     */
    fun emitVolumeUp() = emitKey("KEY_VOLUMEUP")
    fun emitVolumeDown() = emitKey("KEY_VOLUMEDOWN")
    
    /**
     * 发射频道键
     */
    fun emitChannelUp() = emitKey("KEY_CHANNELUP")
    fun emitChannelDown() = emitKey("KEY_CHANNELDOWN")
    
    /**
     * 发射功能键
     */
    fun emitMenu() = emitKey("KEY_MENU")
    fun emitBack() = emitKey("KEY_BACK")
    fun emitHome() = emitKey("KEY_HOME")
    fun emitInfo() = emitKey("KEY_INFO")
    fun emitSubtitle() = emitKey("KEY_SUBTITLE")
    
    /**
     * 发射颜色快捷键
     */
    fun emitRed() = emitKey("KEY_RED")
    fun emitGreen() = emitKey("KEY_GREEN")
    fun emitYellow() = emitKey("KEY_YELLOW")
    fun emitBlue() = emitKey("KEY_BLUE")
    
    /**
     * 发射媒体/功能键
     */
    fun emitMedia() = emitKey("KEY_MEDIA")
    fun emitPictureMode() = emitKey("KEY_DIRECTION")
    fun emitMts() = emitKey("KEY_FN_F1")
    fun emitKp1() = emitKey("KEY_KP1")
    fun emitHelp() = emitKey("KEY_HELP")
    fun emitZoom() = emitKey("KEY_ZOOM")
    fun emitRecord() = emitKey("KEY_RECORD")
    
    /**
     * 发射工厂测试键
     */
    fun emitFacReset() = emitKey("KEY_FN_F")
    fun emitFacMenu() = emitKey("KEY_FN_E")
    fun emitFacAutoTuning() = emitKey("KEY_F13")
    fun emitFacAging() = emitKey("KEY_PROG3")
    fun emitFacVersion() = emitKey("KEY_F14")
    fun emitFacEraseHdcp() = emitKey("KEY_F15")
    fun emitFacEraseMac() = emitKey("KEY_F16")
    fun emitFacEraseCiplus() = emitKey("KEY_F17")
    fun emitFacF1() = emitKey("KEY_F1")
    fun emitFacFnB() = emitKey("KEY_FN_B")
    fun emitFacTouchpad() = emitKey("KEY_TOUCHPAD_TOGGLE")
    fun emitFacAdc() = emitKey("KEY_BRL_DOT2")
    
    /**
     * 获取所有配置列表
     */
    fun getAllConfigs(): List<IRConfig> = configRepository.getAllConfigs()
    
    /**
     * 切换配置
     */
    fun switchConfig(config: IRConfig) {
        irManager.setCurrentConfig(config)
        _currentConfig.value = config
        _statusMessage.value = "已切换到: ${config.name}"
    }
    
    override fun onCleared() {
        super.onCleared()
        irManager.setOnEmitListener(null)
    }
}
