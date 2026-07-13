package com.tvremote.ir.IR

import android.content.Context
import android.hardware.ConsumerIrManager
import com.tvremote.ir.Model.ProtocolConfig

/**
 * 红外发射管理器
 */
class IRBlaster(private val context: Context) {

    private val irManager: ConsumerIrManager? =
        context.getSystemService(Context.CONSUMER_IR_SERVICE) as? ConsumerIrManager

    /**
     * 设备是否支持红外发射
     */
    val isAvailable: Boolean
        get() = irManager?.hasIrEmitter() == true

    /**
     * 发送原始红外码流
     * @param frequency 载波频率 (Hz)
     * @param pattern 脉冲模式（微秒级 ON/OFF 交替）
     */
    fun transmit(frequency: Int, pattern: IntArray) {
        val mgr = irManager
        if (mgr == null || !mgr.hasIrEmitter()) {
            throw IRException("设备不支持红外发射功能")
        }
        try {
            mgr.transmit(frequency, pattern)
        } catch (e: Exception) {
            throw IRException("红外发射失败: ${e.message}", e)
        }
    }

    /**
     * 通过协议配置发送按键
     */
    fun sendKey(config: ProtocolConfig, keyName: String) {
        val keyConfig = config.keys[keyName] ?: return
        val pattern = IRProtocols.generateRawPattern(
            protocol = config.protocol,
            address = config.deviceCode,
            command = keyConfig.code,
            bits = keyConfig.bits
        )
        val frequency = config.frequency

        // 重复发送多次（多数电视机需要）
        val repeatCount = keyConfig.repeatCount
        for (i in 0 until repeatCount) {
            transmit(frequency, pattern)
            if (i < repeatCount - 1) {
                Thread.sleep(40) // 40ms间隔
            }
        }
    }

}

class IRException(message: String, cause: Throwable? = null) : Exception(message, cause)
