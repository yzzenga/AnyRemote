package com.tvremote.ir.Model

/**
 * 遥控器配置（品牌 + 协议组合）
 */
data class RemoteControl(
    val brand: Brand,
    val protocolConfig: ProtocolConfig,
    val isCalibrated: Boolean = false
) {
    val id: String get() = "${brand.id}_${protocolConfig.id}"
    val displayName: String get() = "${brand.name} - ${protocolConfig.description}"
}
