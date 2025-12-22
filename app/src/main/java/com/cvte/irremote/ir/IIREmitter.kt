package com.cvte.irremote.ir

import com.cvte.irremote.model.entity.EmitResult

/**
 * IR发射器接口
 * 
 * 定义IR信号发射的标准接口，支持扩展不同的IR协议实现
 */
interface IIREmitter {
    
    /**
     * 检查设备是否支持IR发射功能
     * 
     * @return true 如果设备支持IR发射
     */
    fun isSupported(): Boolean
    
    /**
     * 获取设备支持的IR载波频率范围
     * 
     * @return 支持的频率范围列表 (每对 [min, max])，如果不支持返回空列表
     */
    fun getCarrierFrequencies(): List<IntRange>
    
    /**
     * 发射IR信号
     * 
     * @param protocol 协议类型 (如 NEC = 0x01)
     * @param header 协议头码值
     * @param keyCode 按键码值
     * @return 发射结果
     */
    fun emit(protocol: Int, header: Int, keyCode: Int): EmitResult
    
    /**
     * 使用预编码的pattern发射IR信号
     * 
     * @param frequency 载波频率 (Hz)
     * @param pattern 时序数组 (微秒)
     * @return 发射结果
     */
    fun emitRaw(frequency: Int, pattern: IntArray): EmitResult
    
    /**
     * 停止当前发射 (如果正在发射)
     */
    fun cancel()
}
