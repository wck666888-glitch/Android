package com.cvte.irremote.model.entity

import com.google.gson.annotations.SerializedName

/**
 * 配置元数据（用于配置列表展示）
 * 
 * @property id 配置ID
 * @property name 配置名称
 * @property description 配置描述
 * @property version 版本号
 * @property updatedAt 更新时间
 */
data class ConfigMetadata(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("description")
    val description: String = "",
    
    @SerializedName("version")
    val version: String = "1.0",
    
    @SerializedName("updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
