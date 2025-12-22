package com.cvte.irremote.model.entity

/**
 * IR发射结果
 * 
 * @property success 是否成功
 * @property keyName 发射的按键名称
 * @property message 结果消息
 * @property timestamp 发射时间戳
 */
data class EmitResult(
    val success: Boolean,
    val keyName: String,
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
