package com.tvremote.ir.Model

/**
 * 用户保存的遥控器
 */
data class SavedRemote(
    val id: String,                // 唯一标识
    val name: String,              // 用户自定义名称
    val deviceType: DeviceType,    // 设备类型
    val brandId: String,           // 品牌ID
    val brandName: String,         // 品牌名（显示用）
    val protocolId: String,        // 协议配置ID
    val frequency: Int,            // 载波频率
    val createdAt: Long = System.currentTimeMillis()
)
