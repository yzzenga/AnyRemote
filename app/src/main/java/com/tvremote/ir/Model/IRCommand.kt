package com.tvremote.ir.Model

/**
 * 单个红外指令（按键对应的码值）
 */
data class IRCommand(
    val code: Long,            // 命令码
    val bits: Int = 32,        // 位宽
    val label: String = "",    // 按键显示名
    val repeatCount: Int = 2   // 重复发送次数
)
