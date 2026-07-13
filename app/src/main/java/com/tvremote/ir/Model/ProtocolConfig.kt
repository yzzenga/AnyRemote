package com.tvremote.ir.Model

/**
 * 红外协议配置（一个品牌可能有多个协议/代码组）
 */
data class ProtocolConfig(
    val id: String,
    val protocol: String,         // 协议类型: NEC, SONY_SIRC, RC5, RC6, SAMSUNG, RAW
    val frequency: Int,           // 载波频率 (Hz)
    val deviceCode: Long,         // 设备码 / 地址码
    val subDeviceCode: Long? = null,  // 子设备码（某些协议需要）
    val keys: Map<String, IRCommand>,  // 按键映射
    val description: String = "",      // 协议描述（用于显示）
    val manufacturer: String = ""      // 制造商信息
)
