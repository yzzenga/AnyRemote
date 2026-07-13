package com.tvremote.ir.Data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tvremote.ir.Model.Brand
import com.tvremote.ir.Model.DeviceType
import com.tvremote.ir.Model.IRCommand
import com.tvremote.ir.Model.ProtocolConfig

/**
 * IR码库加载器 - 从JSON文件解析品牌和协议数据
 */
class IRCodeLoader(private val context: Context) {

    data class IRCodeDatabase(
        val brands: List<BrandEntry>
    )

    data class BrandEntry(
        val id: String,
        val name: String,
        val nameEn: String,
        val deviceType: String? = null,
        val hot: Boolean = false,
        val protocols: List<ProtocolEntry>
    )

    data class ProtocolEntry(
        val id: String,
        val protocol: String,
        val frequency: Int = 38000,
        val deviceCode: Long = 0,
        val subDeviceCode: Long? = null,
        val description: String = "",
        val keys: Map<String, KeyEntry>
    )

    data class KeyEntry(
        val code: Long,
        val bits: Int = 32,
        val repeat: Int = 3
    )

    private val gson = Gson()
    private var cachedBrands: List<Brand>? = null
    private var cachedProtocols: Map<String, List<ProtocolConfig>>? = null

    /**
     * 加载所有品牌
     */
    fun loadBrands(): List<Brand> {
        cachedBrands?.let { return it }

        val db = parseDatabase()
        val brands = db.brands.map { entry ->
            val defaultProtocol = entry.protocols.firstOrNull()
            Brand(
                id = entry.id,
                name = entry.name,
                nameEn = entry.nameEn,
                deviceType = DeviceType.fromString(entry.deviceType ?: "TV"),
                hot = entry.hot,
                defaultProtocol = defaultProtocol?.protocol ?: "",
                defaultFrequency = defaultProtocol?.frequency ?: 38000
            )
        }

        cachedBrands = brands
        return brands
    }

    /**
     * 按设备类型获取品牌
     */
    fun getBrandsByType(deviceType: DeviceType): List<Brand> {
        return loadBrands().filter { it.deviceType == deviceType }
    }

    /**
     * 获取热门品牌（按设备类型过滤）
     */
    fun getHotBrands(deviceType: DeviceType = DeviceType.TV): List<Brand> {
        return loadBrands().filter { it.hot && it.deviceType == deviceType }
    }

    /**
     * 搜索品牌（按设备类型过滤）
     */
    fun searchBrands(query: String, deviceType: DeviceType? = null): List<Brand> {
        val q = query.lowercase()
        return loadBrands().filter { brand ->
            if (deviceType != null && brand.deviceType != deviceType) false
            else brand.name.lowercase().contains(q) ||
                    brand.nameEn.lowercase().contains(q)
        }
    }

    /**
     * 加载所有协议配置
     */
    fun loadAllProtocols(): Map<String, List<ProtocolConfig>> {
        cachedProtocols?.let { return it }

        val db = parseDatabase()
        val result = mutableMapOf<String, List<ProtocolConfig>>()

        db.brands.forEach { brandEntry ->
            val protocols = brandEntry.protocols.map { protoEntry ->
                val keys = protoEntry.keys.mapValues { (_, keyEntry) ->
                    IRCommand(
                        code = keyEntry.code,
                        bits = keyEntry.bits,
                        repeatCount = keyEntry.repeat
                    )
                }

                ProtocolConfig(
                    id = protoEntry.id,
                    protocol = protoEntry.protocol,
                    frequency = protoEntry.frequency,
                    deviceCode = protoEntry.deviceCode,
                    subDeviceCode = protoEntry.subDeviceCode,
                    description = protoEntry.description,
                    keys = keys
                )
            }

            result[brandEntry.id] = protocols
        }

        cachedProtocols = result
        return result
    }

    /**
     * 获取品牌的协议配置列表
     */
    fun getProtocolsForBrand(brandId: String): List<ProtocolConfig> {
        val allProtocols = loadAllProtocols()
        return allProtocols[brandId] ?: emptyList()
    }

    /**
     * 解析JSON数据库
     */
    private fun parseDatabase(): IRCodeDatabase {
        return try {
            val json = context.resources.openRawResource(
                com.tvremote.ir.R.raw.ir_codes
            ).bufferedReader().use { it.readText() }

            val type = object : TypeToken<IRCodeDatabase>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            IRCodeDatabase(brands = emptyList())
        }
    }
}
