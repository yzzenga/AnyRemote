package com.tvremote.ir.Model

/**
 * 设备类型
 */
enum class DeviceType(val label: String) {
    TV("电视"),
    AC("空调");

    companion object {
        fun fromString(value: String): DeviceType {
            return entries.find { it.name == value } ?: TV
        }
    }
}
