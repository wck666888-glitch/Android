package com.cvte.irremote.model.entity

import com.google.gson.annotations.SerializedName

/**
 * IR按键数据模型
 * 
 * 表示遥控器上的单个按键及其对应的IR码值
 * 
 * @property keyName 按键标识符 (如 KEY_POWER, KEY_0)
 * @property keyCode IR码值 (16位整数)
 * @property displayName 显示名称 (如 "电源", "0")
 * @property category 按键分类 (数字/方向/功能等)
 */
data class IRKey(
    @SerializedName("key_name")
    val keyName: String,
    
    @SerializedName("key_code")
    val keyCode: Int,
    
    @SerializedName("display_name")
    val displayName: String,
    
    @SerializedName("category")
    val category: KeyCategory = KeyCategory.FUNCTION
) {
    /**
     * 获取格式化的码值字符串 (16进制)
     */
    fun getFormattedKeyCode(): String = String.format("0x%04X", keyCode)
    
    companion object {
        /**
         * 从码值字符串解析 (支持 "0x0001" 或 "1" 格式)
         */
        fun parseKeyCode(codeStr: String): Int {
            val trimmed = codeStr.trim()
            return when {
                trimmed.startsWith("0x", ignoreCase = true) -> 
                    trimmed.substring(2).toInt(16)
                trimmed.startsWith("0X", ignoreCase = true) -> 
                    trimmed.substring(2).toInt(16)
                else -> trimmed.toInt()
            }
        }
    }
}

/**
 * 按键分类枚举
 */
enum class KeyCategory {
    @SerializedName("number")
    NUMBER,          // 数字键 0-9
    
    @SerializedName("direction")
    DIRECTION,       // 方向键 上下左右+确认
    
    @SerializedName("volume")
    VOLUME,          // 音量控制
    
    @SerializedName("channel")
    CHANNEL,         // 频道控制
    
    @SerializedName("color")
    COLOR,           // 颜色快捷键
    
    @SerializedName("function")
    FUNCTION,        // 功能键 (电源/菜单/返回等)
    
    @SerializedName("factory")
    FACTORY          // 工厂测试键
}
