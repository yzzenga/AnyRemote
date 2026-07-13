package com.tvremote.ir.IR

/**
 * 红外协议定义与原始码生成算法
 */
object IRProtocols {

    /** NEC 协议（38KHz 载波，最常用的协议）
     *  Leader: 9000us ON, 4500us OFF
     *  Bit 0:  560us ON, 560us OFF
     *  Bit 1:  560us ON, 1690us OFF
     *  Repeat: 9000us ON, 2250us OFF
     */
    object NEC {
        val FREQUENCY = 38000
        private val LEADER_ON = 9000
        private val LEADER_OFF = 4500
        private val BIT0_ON = 560
        private val BIT0_OFF = 560
        private val BIT1_OFF = 1690
        private val REPEAT_LEADER = 9000
        private val REPEAT_GAP = 2250
        private val END_GAP = 560  // 结束脉冲

        fun generatePattern(address: Long, command: Long, bits: Int = 32): IntArray {
            val pattern = mutableListOf<Int>()

            // Leader
            pattern.add(LEADER_ON)
            pattern.add(LEADER_OFF)

            // 地址码（16位） + 命令码（16位），通常共32位
            val fullCode = ((address and 0xFFFF) shl 16) or (command and 0xFFFF)
            for (i in bits - 1 downTo 0) {
                val bit = ((fullCode shr i) and 1L).toInt()
                pattern.add(BIT0_ON)
                pattern.add(if (bit == 1) BIT1_OFF else BIT0_OFF)
            }

            // 结束脉冲
            pattern.add(END_GAP)

            return pattern.toIntArray()
        }

        // 标准 NEC：16位地址 + 8位命令 + 8位命令反码
        fun generateStandard(address: Long, command: Long): IntArray {
            val cmdInv = (command xor 0xFF) and 0xFF
            val fullCode = ((address and 0xFFFF) shl 16) or ((command and 0xFF) shl 8) or cmdInv
            val pattern = mutableListOf<Int>()

            pattern.add(LEADER_ON)
            pattern.add(LEADER_OFF)

            for (i in 31 downTo 0) {
                val bit = ((fullCode shr i) and 1L).toInt()
                pattern.add(BIT0_ON)
                pattern.add(if (bit == 1) BIT1_OFF else BIT0_OFF)
            }

            pattern.add(END_GAP)
            return pattern.toIntArray()
        }

        // 扩展 NEC：16位地址 + 16位命令
        fun generateExtend(address: Long, command: Long): IntArray {
            return generatePattern(address, command, 32)
        }

        // 重复码
        fun generateRepeat(): IntArray {
            return intArrayOf(REPEAT_LEADER, REPEAT_GAP)
        }
    }

    /** Sony SIRC 协议（40KHz 载波）
     *  Leader: 2400us ON, 600us OFF
     *  Bit 0: 600us ON, 600us OFF
     *  Bit 1: 600us ON, 1200us OFF
     */
    object SonySIRC {
        val FREQUENCY = 40000
        private val LEADER_ON = 2400
        private val LEADER_OFF = 600
        private val BIT_ON = 600
        private val BIT0_OFF = 600
        private val BIT1_OFF = 1200

        fun generatePattern(deviceCode: Long, commandCode: Long, bits: Int = 12): IntArray {
            val pattern = mutableListOf<Int>()
            pattern.add(LEADER_ON)
            pattern.add(LEADER_OFF)

            // SIRC: 命令码在后面，设备码在前面
            // 12位 = 7位命令 + 5位设备
            // 15位 = 7位命令 + 8位设备
            val cmdBits = if (bits == 12) 7 else if (bits == 15) 7 else 8
            val devBits = bits - cmdBits
            val fullCode = (commandCode and ((1L shl cmdBits) - 1)) or
                    ((deviceCode and ((1L shl devBits) - 1)) shl cmdBits)

            for (i in bits - 1 downTo 0) {
                val bit = ((fullCode shr i) and 1L).toInt()
                pattern.add(BIT_ON)
                pattern.add(if (bit == 1) BIT1_OFF else BIT0_OFF)
            }

            return pattern.toIntArray()
        }
    }

    /** RC5 协议（36KHz 载波，双相曼彻斯特编码）
     *  Bit: 889us 每半位
     */
    object RC5 {
        val FREQUENCY = 36000
        private val HALF_BIT = 889

        fun generatePattern(address: Long, command: Long): IntArray {
            val pattern = mutableListOf<Int>()
            // RC5: 1位起始位(1) + 1位场位 + 1位控制位 + 5位地址 + 6位命令 = 14位
            val fullCode = (1L shl 13) or (1L shl 12) or
                    ((address and 0x1F) shl 6) or (command and 0x3F)

            var lastState = 1  // 起始为高
            for (i in 13 downTo 0) {
                val bit = ((fullCode shr i) and 1L).toInt()
                // 曼彻斯特编码: 1 = 高→低, 0 = 低→高
                if (bit == 1) {
                    pattern.add(if (lastState == 1) HALF_BIT else 0)
                    pattern.add(if (lastState == 1) 0 else HALF_BIT)
                    lastState = 0
                } else {
                    pattern.add(if (lastState == 0) HALF_BIT else 0)
                    pattern.add(if (lastState == 0) 0 else HALF_BIT)
                    lastState = 1
                }
            }

            return pattern.toIntArray()
        }
    }

    /** Samsung 协议（38KHz，类似NEC但时序略有不同） */
    object Samsung {
        val FREQUENCY = 38000
        private val LEADER_ON = 4500
        private val LEADER_OFF = 4500
        private val BIT_ON = 560
        private val BIT0_OFF = 560
        private val BIT1_OFF = 1690
        private val END = 560

        fun generatePattern(address: Long, command: Long): IntArray {
            val pattern = mutableListOf<Int>()
            pattern.add(LEADER_ON)
            pattern.add(LEADER_OFF)

            val fullCode = ((address and 0xFFFF) shl 16) or (command and 0xFFFF)
            for (i in 31 downTo 0) {
                val bit = ((fullCode shr i) and 1L).toInt()
                pattern.add(BIT_ON)
                pattern.add(if (bit == 1) BIT1_OFF else BIT0_OFF)
            }

            pattern.add(END)
            return pattern.toIntArray()
        }
    }

    /** RG 协议（中国品牌常用，类似NEC但地址/命令格式不同） */
    object RG {
        val FREQUENCY = 38000
        // 使用NEC的时序，但码值格式不同
        fun generatePattern(systemCode: Long, keyCode: Long): IntArray {
            return NEC.generatePattern(systemCode, keyCode, 32)
        }
    }

    /**
     * 根据协议名称和参数生成原始红外码流
     */
    fun generateRawPattern(
        protocol: String,
        address: Long,
        command: Long,
        bits: Int = 32
    ): IntArray {
        return when (protocol.uppercase()) {
            "NEC" -> NEC.generatePattern(address, command, bits)
            "NEC_STANDARD" -> NEC.generateStandard(address, command)
            "SONY", "SONY_SIRC" -> SonySIRC.generatePattern(address, command, bits)
            "RC5" -> RC5.generatePattern(address, command)
            "SAMSUNG" -> Samsung.generatePattern(address, command)
            "RG" -> RG.generatePattern(address, command)
            else -> NEC.generatePattern(address, command, bits) // 默认使用NEC
        }
    }

    /**
     * 获取协议对应的默认载波频率
     */
    fun getFrequency(protocol: String): Int {
        return when (protocol.uppercase()) {
            "NEC", "NEC_STANDARD", "SAMSUNG", "RG" -> 38000
            "SONY", "SONY_SIRC" -> 40000
            "RC5", "RC6" -> 36000
            else -> 38000
        }
    }

    /**
     * 常用载波频率列表（用于扫描）
     */
    val COMMON_FREQUENCIES = intArrayOf(
        36000, 37000, 38000, 39000, 40000,
        41000, 42000, 44000, 45000, 46000,
        48000, 50000, 56000, 57000, 60000
    )
}
