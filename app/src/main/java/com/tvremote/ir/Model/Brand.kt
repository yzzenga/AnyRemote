package com.tvremote.ir.Model

/**
 * 品牌数据模型
 */
data class Brand(
    val id: String,
    val name: String,              // 中文名
    val nameEn: String,            // 英文名
    val deviceType: DeviceType = DeviceType.TV,  // 设备类型
    val hot: Boolean = false,      // 是否为热门品牌
    val defaultProtocol: String = "",
    val defaultFrequency: Int = 38000
)
