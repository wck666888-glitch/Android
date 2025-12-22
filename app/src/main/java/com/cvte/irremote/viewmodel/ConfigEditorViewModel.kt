package com.cvte.irremote.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.cvte.irremote.model.entity.IRConfig
import com.cvte.irremote.model.entity.IRKey
import com.cvte.irremote.model.entity.KeyCategory
import com.cvte.irremote.model.repository.ConfigRepository
import com.cvte.irremote.utils.IRLogger
import java.io.File
import java.util.UUID

/**
 * 配置编辑器 ViewModel
 */
class ConfigEditorViewModel(application: Application) : AndroidViewModel(application) {
    
    companion object {
        private const val TAG = "ConfigEditorVM"
    }
    
    private val configRepository = ConfigRepository.getInstance(application)
    
    // 当前编辑的配置
    private val _config = MutableLiveData<IRConfig?>()
    val config: LiveData<IRConfig?> = _config
    
    // 按键列表
    private val _keys = MutableLiveData<List<IRKey>>()
    val keys: LiveData<List<IRKey>> = _keys
    
    // 操作结果
    private val _saveResult = MutableLiveData<Boolean>()
    val saveResult: LiveData<Boolean> = _saveResult
    
    // 消息提示
    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message
    
    // 临时存储编辑中的数据
    private var editingKeys = mutableListOf<IRKey>()
    
    /**
     * 加载配置
     */
    fun loadConfig(configId: String?) {
        val loadedConfig = if (configId != null) {
            configRepository.getConfig(configId)
        } else {
            // 创建新配置
            createNewConfig()
        }
        
        _config.value = loadedConfig
        loadedConfig?.let {
            editingKeys = it.keys.toMutableList()
            _keys.value = editingKeys.toList()
        }
    }
    
    /**
     * 创建新配置
     */
    private fun createNewConfig(): IRConfig {
        return IRConfig(
            id = UUID.randomUUID().toString(),
            name = "新配置",
            protocol = IRConfig.PROTOCOL_NEC,
            header = 0x8890,
            keys = emptyList()
        )
    }
    
    /**
     * 保存配置
     */
    fun saveConfig(name: String, protocol: Int, header: Int) {
        val currentConfig = _config.value ?: return
        
        if (name.isBlank()) {
            _message.value = "配置名称不能为空"
            return
        }
        
        val updatedConfig = currentConfig.copy(
            name = name,
            protocol = protocol,
            header = header,
            keys = editingKeys.toList(),
            updatedAt = System.currentTimeMillis()
        )
        
        val success = configRepository.saveConfig(updatedConfig)
        _saveResult.value = success
        
        if (success) {
            _config.value = updatedConfig
            _message.value = "配置已保存"
            IRLogger.i(TAG, "Config saved: ${updatedConfig.name}")
        } else {
            _message.value = "保存失败"
        }
    }
    
    /**
     * 添加按键
     */
    fun addKey(keyName: String, keyCode: Int, displayName: String, category: KeyCategory = KeyCategory.FUNCTION) {
        if (keyName.isBlank()) {
            _message.value = "按键名称不能为空"
            return
        }
        
        // 检查重复
        if (editingKeys.any { it.keyName == keyName }) {
            _message.value = "按键名称已存在"
            return
        }
        
        val newKey = IRKey(
            keyName = keyName,
            keyCode = keyCode,
            displayName = displayName.ifBlank { keyName },
            category = category
        )
        
        editingKeys.add(newKey)
        _keys.value = editingKeys.toList()
        _message.value = "按键已添加"
    }
    
    /**
     * 更新按键
     */
    fun updateKey(index: Int, keyName: String, keyCode: Int, displayName: String, category: KeyCategory = KeyCategory.FUNCTION) {
        if (index < 0 || index >= editingKeys.size) return
        
        if (keyName.isBlank()) {
            _message.value = "按键名称不能为空"
            return
        }
        
        val updatedKey = IRKey(
            keyName = keyName,
            keyCode = keyCode,
            displayName = displayName.ifBlank { keyName },
            category = category
        )
        
        editingKeys[index] = updatedKey
        _keys.value = editingKeys.toList()
        _message.value = "按键已更新"
    }
    
    /**
     * 删除按键
     */
    fun deleteKey(index: Int) {
        if (index < 0 || index >= editingKeys.size) return
        
        editingKeys.removeAt(index)
        _keys.value = editingKeys.toList()
        _message.value = "按键已删除"
    }
    
    /**
     * 导出配置
     */
    fun exportConfig(): String? {
        val currentConfig = _config.value ?: return null
        return configRepository.exportConfig(currentConfig)
    }
    
    /**
     * 导出配置到文件
     */
    fun exportToFile(file: File): Boolean {
        val currentConfig = _config.value ?: return false
        return configRepository.exportToFile(currentConfig, file)
    }
    
    /**
     * 导入配置
     */
    fun importConfig(json: String): Boolean {
        val imported = configRepository.importConfig(json)
        if (imported != null) {
            _config.value = imported
            editingKeys = imported.keys.toMutableList()
            _keys.value = editingKeys.toList()
            _message.value = "配置已导入"
            return true
        } else {
            _message.value = "导入失败"
            return false
        }
    }
    
    /**
     * 从文件导入配置
     */
    fun importFromFile(file: File): Boolean {
        val imported = configRepository.importFromFile(file)
        if (imported != null) {
            _config.value = imported
            editingKeys = imported.keys.toMutableList()
            _keys.value = editingKeys.toList()
            _message.value = "配置已导入"
            return true
        } else {
            _message.value = "导入失败"
            return false
        }
    }
    
    /**
     * 获取所有配置列表
     */
    fun getAllConfigs(): List<IRConfig> = configRepository.getAllConfigs()
}
