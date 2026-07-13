package com.tvremote.ir.IR

import com.tvremote.ir.Model.ProtocolConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * 频率扫描与校准引擎
 */
class FrequencyScanner(private val irBlaster: IRBlaster) {

    data class ScanProgress(
        val currentFrequency: Int,
        val progress: Float,        // 0..1
        val message: String
    )

    data class ScanResult(
        val frequency: Int,
        val pattern: IntArray,
        val succeeded: Boolean
    )

    companion object {
        // 标准扫描频率列表（逐步覆盖所有常见频率）
        val STANDARD_FREQUENCIES = listOf(
            // 第一轮：最常用频率
            38000, 40000, 36000,
            // 第二轮：次常用
            56000, 37000, 39000,
            // 第三轮：其他
            41000, 42000, 44000, 45000, 46000, 48000, 50000, 57000, 60000
        )

        // 电源键码（用于测试的标准码值）
        // 这些是多数品牌通用的测试码
        val TEST_COMMANDS = mapOf(
            "NEC" to 0x45L,       // NEC Power
            "SAMSUNG" to 0xE0E040BFL,
            "SONY_SIRC" to 0x15L,
            "RC5" to 0x0CL
        )
    }

    /**
     * 在多个频率上尝试发送电源码
     * 用户观察电视是否有反应
     */
    fun scanFrequencies(
        testCommands: Map<String, Long> = TEST_COMMANDS,
        frequencies: List<Int> = STANDARD_FREQUENCIES
    ): Flow<ScanProgress> = flow {
        val totalSteps = frequencies.size
        frequencies.forEachIndexed { index, freq ->
            emit(ScanProgress(
                currentFrequency = freq,
                progress = (index + 1).toFloat() / totalSteps,
                message = "正在测试 ${freq / 1000}KHz..."
            ))

            // 尝试用不同协议发送电源码
            for ((protocol, cmdCode) in testCommands) {
                val pattern = IRProtocols.generateRawPattern(
                    protocol = protocol,
                    address = 0,
                    command = cmdCode
                )
                try {
                    irBlaster.transmit(freq, pattern)
                } catch (_: Exception) {
                    // 忽略单个频率失败
                }
            }

            delay(800) // 每个频率间隔800ms让用户观察
        }

        emit(ScanProgress(
            currentFrequency = 0,
            progress = 1f,
            message = "扫描完成"
        ))
    }.flowOn(Dispatchers.IO)

    /**
     * 在特定频率上测试某个协议配置
     */
    fun testProtocol(
        protocol: String,
        address: Long,
        command: Long,
        frequency: Int,
        bits: Int = 32
    ) {
        val pattern = IRProtocols.generateRawPattern(protocol, address, command, bits)
        irBlaster.transmit(frequency, pattern)
    }

    /**
     * 全自动扫描：尝试所有频率与内置码组的组合
     */
    fun autoCalibrate(
        codeGroups: List<ProtocolConfig>
    ): Flow<ScanProgress> = flow {
        val total = codeGroups.size
        codeGroups.forEachIndexed { index, config ->
            emit(ScanProgress(
                currentFrequency = config.frequency,
                progress = (index + 1).toFloat() / total,
                message = "测试 ${config.description} (${config.frequency / 1000}KHz)"
            ))

            // 发送电源键
            config.keys["power"]?.let { key ->
                val pattern = IRProtocols.generateRawPattern(
                    protocol = config.protocol,
                    address = config.deviceCode,
                    command = key.code,
                    bits = key.bits
                )
                try {
                    irBlaster.transmit(config.frequency, pattern)
                } catch (_: Exception) { }
            }

            delay(600) // 让用户观察
        }

        emit(ScanProgress(
            currentFrequency = 0,
            progress = 1f,
            message = "校准扫描完成"
        ))
    }.flowOn(Dispatchers.IO)
}
