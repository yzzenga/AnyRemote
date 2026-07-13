package com.tvremote.ir.Model

/**
 * 频率/码库扫描结果
 */
data class ScanResult(
    val frequency: Int,                // 匹配到的频率
    val protocolConfig: ProtocolConfig, // 匹配到的协议配置
    val confirmed: Boolean = false     // 用户是否确认有效
)
