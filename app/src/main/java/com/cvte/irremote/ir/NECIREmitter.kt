package com.cvte.irremote.ir

import android.content.Context
import android.hardware.ConsumerIrManager
import android.os.Build
import com.cvte.irremote.model.entity.EmitResult
import com.cvte.irremote.model.entity.IRConfig
import com.cvte.irremote.utils.IRLogger

/**
 * NEC协议IR发射器实现
 * 
 * 使用Android ConsumerIrManager API实现NEC协议的红外信号发射
 * 
 * NEC协议时序参数:
 * - 载波频率: 38kHz
 * - Header: 9ms脉冲 + 4.5ms间隔
 * - Bit 0: 560μs脉冲 + 560μs间隔
 * - Bit 1: 560μs脉冲 + 1680μs间隔
 * - Stop bit: 560μs脉冲
 */
class NECIREmitter(context: Context) : IIREmitter {
    
    companion object {
        private const val TAG = "NECIREmitter"
        
        // NEC协议载波频率 (Hz)
        const val CARRIER_FREQUENCY = 38000
        
        // NEC协议时序参数 (微秒)
        private const val HEADER_PULSE = 9000      // 9ms 高电平
        private const val HEADER_SPACE = 4500      // 4.5ms 低电平
        private const val BIT_PULSE = 560          // 560μs 高电平
        private const val BIT_0_SPACE = 560        // Bit 0: 560μs 低电平
        private const val BIT_1_SPACE = 1680       // Bit 1: 1680μs 低电平
        private const val STOP_PULSE = 560         // 结束位
        
        // 重复码时序
        private const val REPEAT_HEADER_PULSE = 9000
        private const val REPEAT_HEADER_SPACE = 2250
    }
    
    private val irManager: ConsumerIrManager? = try {
        context.getSystemService(Context.CONSUMER_IR_SERVICE) as? ConsumerIrManager
    } catch (e: Exception) {
        IRLogger.e(TAG, "Failed to get ConsumerIrManager", e)
        null
    }
    
    override fun isSupported(): Boolean {
        return irManager?.hasIrEmitter() == true
    }
    
    override fun getCarrierFrequencies(): List<IntRange> {
        if (irManager == null || !isSupported()) {
            return emptyList()
        }
        
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                irManager.carrierFrequencies?.map { 
                    IntRange(it.minFrequency, it.maxFrequency) 
                } ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            IRLogger.e(TAG, "Failed to get carrier frequencies", e)
            emptyList()
        }
    }
    
    override fun emit(protocol: Int, header: Int, keyCode: Int): EmitResult {
        if (!isSupported()) {
            return EmitResult(
                success = false,
                keyName = "0x${keyCode.toString(16).uppercase()}",
                message = "设备不支持IR发射功能"
            )
        }
        
        // 验证协议
        if (protocol != IRConfig.PROTOCOL_NEC) {
            return EmitResult(
                success = false,
                keyName = "0x${keyCode.toString(16).uppercase()}",
                message = "不支持的协议类型: 0x${protocol.toString(16)}"
            )
        }
        
        return try {
            // 编码NEC信号
            val pattern = encodeNEC(header, keyCode)
            
            IRLogger.d(TAG, "Emitting NEC signal: header=0x${header.toString(16)}, keyCode=0x${keyCode.toString(16)}")
            IRLogger.d(TAG, "Pattern length: ${pattern.size}")
            
            // 发射信号
            irManager?.transmit(CARRIER_FREQUENCY, pattern)
            
            EmitResult(
                success = true,
                keyName = "0x${keyCode.toString(16).uppercase()}",
                message = "信号发送成功"
            )
        } catch (e: Exception) {
            IRLogger.e(TAG, "Failed to emit IR signal", e)
            EmitResult(
                success = false,
                keyName = "0x${keyCode.toString(16).uppercase()}",
                message = "发射失败: ${e.message}"
            )
        }
    }
    
    override fun emitRaw(frequency: Int, pattern: IntArray): EmitResult {
        if (!isSupported()) {
            return EmitResult(
                success = false,
                keyName = "RAW",
                message = "设备不支持IR发射功能"
            )
        }
        
        return try {
            irManager?.transmit(frequency, pattern)
            EmitResult(
                success = true,
                keyName = "RAW",
                message = "信号发送成功"
            )
        } catch (e: Exception) {
            IRLogger.e(TAG, "Failed to emit raw IR signal", e)
            EmitResult(
                success = false,
                keyName = "RAW",
                message = "发射失败: ${e.message}"
            )
        }
    }
    
    override fun cancel() {
        // ConsumerIrManager 不支持取消操作
        // 信号发射是同步的，发送完成后自动结束
    }
    
    /**
     * 编码NEC协议信号
     * 
     * CVTE工厂遥控器数据格式: 
     * [Header 16bit] + [Command 8bit] + [Command Inverse 8bit]
     * 
     * 标准NEC扩展协议，Header为客户码(16位)，Command为8位命令+8位反码
     * 
     * @param header 协议头/客户码 (16位，如0x8890)
     * @param keyCode 按键码值 (8位有效，如0x01)
     * @return IR时序数组
     */
    private fun encodeNEC(header: Int, keyCode: Int): IntArray {
        val pattern = mutableListOf<Int>()
        
        // 提取8位命令码
        val command = keyCode and 0xFF
        // 计算命令反码
        val commandInverse = command.inv() and 0xFF
        
        IRLogger.d(TAG, "NEC Encoding: header=0x${header.toString(16).uppercase()}, " +
                "cmd=0x${command.toString(16).uppercase()}, " +
                "cmdInv=0x${commandInverse.toString(16).uppercase()}")
        
        // 1. 添加起始码 (Leader)
        pattern.add(HEADER_PULSE)
        pattern.add(HEADER_SPACE)
        
        // 2. 编码Header/Address (16位, LSB优先)
        for (i in 0 until 16) {
            val bit = (header shr i) and 0x01
            pattern.add(BIT_PULSE)
            pattern.add(if (bit == 1) BIT_1_SPACE else BIT_0_SPACE)
        }
        
        // 3. 编码Command (8位, LSB优先)
        for (i in 0 until 8) {
            val bit = (command shr i) and 0x01
            pattern.add(BIT_PULSE)
            pattern.add(if (bit == 1) BIT_1_SPACE else BIT_0_SPACE)
        }
        
        // 4. 编码Command Inverse (8位, LSB优先)
        for (i in 0 until 8) {
            val bit = (commandInverse shr i) and 0x01
            pattern.add(BIT_PULSE)
            pattern.add(if (bit == 1) BIT_1_SPACE else BIT_0_SPACE)
        }
        
        // 5. 添加结束位 (Stop bit)
        pattern.add(STOP_PULSE)
        
        IRLogger.d(TAG, "Pattern generated: ${pattern.size} elements")
        
        return pattern.toIntArray()
    }
    
    /**
     * 编码NEC重复码 (用于长按)
     */
    fun encodeRepeatCode(): IntArray {
        return intArrayOf(
            REPEAT_HEADER_PULSE,
            REPEAT_HEADER_SPACE,
            STOP_PULSE
        )
    }
    
    /**
     * 发射重复码 (用于长按场景)
     */
    fun emitRepeat(): EmitResult {
        if (!isSupported()) {
            return EmitResult(
                success = false,
                keyName = "REPEAT",
                message = "设备不支持IR发射功能"
            )
        }
        
        return try {
            val pattern = encodeRepeatCode()
            irManager?.transmit(CARRIER_FREQUENCY, pattern)
            EmitResult(
                success = true,
                keyName = "REPEAT",
                message = "重复码发送成功"
            )
        } catch (e: Exception) {
            IRLogger.e(TAG, "Failed to emit repeat code", e)
            EmitResult(
                success = false,
                keyName = "REPEAT",
                message = "发射失败: ${e.message}"
            )
        }
    }
}
