package com.cvte.irremote.model.entity

import com.google.gson.annotations.SerializedName

/**
 * IR配置数据模型
 * 
 * 表示一套完整的遥控器配置，包含协议参数和按键映射
 * 
 * @property id 配置唯一标识符
 * @property name 配置名称 (如 "CVTE工厂遥控")
 * @property protocol 协议类型 (0x01 = NEC)
 * @property header 协议头码值 (如 0x8890)
 * @property keys 按键列表
 * @property isDefault 是否为默认配置
 * @property createdAt 创建时间戳
 * @property updatedAt 更新时间戳
 */
data class IRConfig(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("protocol")
    val protocol: Int,
    
    @SerializedName("header")
    val header: Int,
    
    @SerializedName("keys")
    val keys: List<IRKey>,
    
    @SerializedName("is_default")
    val isDefault: Boolean = false,
    
    @SerializedName("created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @SerializedName("updated_at")
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * 获取格式化的协议字符串
     */
    fun getFormattedProtocol(): String = String.format("0x%02X", protocol)
    
    /**
     * 获取格式化的Header字符串
     */
    fun getFormattedHeader(): String = String.format("0x%04X", header)
    
    /**
     * 根据按键名称查找IRKey
     */
    fun findKeyByName(keyName: String): IRKey? = keys.find { it.keyName == keyName }
    
    /**
     * 根据码值查找IRKey
     */
    fun findKeyByCode(keyCode: Int): IRKey? = keys.find { it.keyCode == keyCode }
    
    /**
     * 按分类获取按键列表
     */
    fun getKeysByCategory(category: KeyCategory): List<IRKey> = 
        keys.filter { it.category == category }
    
    companion object {
        // 协议常量
        const val PROTOCOL_NEC = 0x01
        const val PROTOCOL_RC5 = 0x02
        const val PROTOCOL_RC6 = 0x03
        const val PROTOCOL_SONY = 0x04
        
        /**
         * 获取协议名称
         */
        fun getProtocolName(protocol: Int): String = when (protocol) {
            PROTOCOL_NEC -> "NEC"
            PROTOCOL_RC5 -> "RC5"
            PROTOCOL_RC6 -> "RC6"
            PROTOCOL_SONY -> "Sony SIRC"
            else -> "Unknown"
        }
    }
}
