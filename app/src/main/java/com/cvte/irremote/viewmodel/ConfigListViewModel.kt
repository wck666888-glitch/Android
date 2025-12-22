package com.cvte.irremote.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.cvte.irremote.ir.IRManager
import com.cvte.irremote.model.entity.IRConfig
import com.cvte.irremote.model.repository.ConfigRepository
import com.cvte.irremote.network.NetworkManager
import com.cvte.irremote.utils.IRLogger
import com.cvte.irremote.utils.PreferenceManager
import kotlinx.coroutines.launch

/**
 * 配置列表 ViewModel
 */
class ConfigListViewModel(application: Application) : AndroidViewModel(application) {
    
    companion object {
        private const val TAG = "ConfigListVM"
    }
    
    private val configRepository = ConfigRepository.getInstance(application)
    private val preferenceManager = PreferenceManager.getInstance(application)
    private val networkManager = NetworkManager.getInstance(application)
    private val irManager = IRManager.getInstance(application)
    
    // 配置列表
    private val _configs = MutableLiveData<List<IRConfig>>()
    val configs: LiveData<List<IRConfig>> = _configs
    
    // 当前选中的配置ID
    private val _currentConfigId = MutableLiveData<String?>()
    val currentConfigId: LiveData<String?> = _currentConfigId
    
    // 消息提示
    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message
    
    // 加载状态
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    init {
        _currentConfigId.value = preferenceManager.getCurrentConfigId()
    }
    
    /**
     * 加载所有配置
     */
    fun loadConfigs() {
        _configs.value = configRepository.getAllConfigs()
        _currentConfigId.value = preferenceManager.getCurrentConfigId()
    }
    
    /**
     * 选择配置
     */
    fun selectConfig(config: IRConfig) {
        irManager.setCurrentConfig(config)
        _currentConfigId.value = config.id
        _message.value = "已切换到: ${config.name}"
        IRLogger.i(TAG, "Selected config: ${config.name}")
    }
    
    /**
     * 删除配置
     */
    fun deleteConfig(configId: String) {
        val success = configRepository.deleteConfig(configId)
        if (success) {
            loadConfigs()
            _message.value = "配置已删除"
        } else {
            _message.value = "删除失败"
        }
    }
    
    /**
     * 同步远程配置
     */
    fun syncRemoteConfigs() {
        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                val result = networkManager.fetchConfigList()
                
                result.onSuccess { metadataList ->
                    IRLogger.i(TAG, "Fetched ${metadataList.size} configs from server")
                    
                    // 下载每个配置
                    var successCount = 0
                    for (metadata in metadataList) {
                        val configResult = networkManager.fetchConfig(metadata.id)
                        configResult.onSuccess { config ->
                            configRepository.saveConfig(config)
                            successCount++
                        }
                    }
                    
                    loadConfigs()
                    _message.postValue("同步成功: $successCount 个配置")
                }
                
                result.onFailure { error ->
                    IRLogger.e(TAG, "Sync failed", error)
                    _message.postValue("同步失败: ${error.message}")
                }
            } catch (e: Exception) {
                IRLogger.e(TAG, "Sync error", e)
                _message.postValue("同步失败: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
}
