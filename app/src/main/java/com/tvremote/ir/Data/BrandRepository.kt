package com.tvremote.ir.Data

import android.content.Context
import com.tvremote.ir.Model.Brand
import com.tvremote.ir.Model.DeviceType
import com.tvremote.ir.Model.ProtocolConfig

/**
 * 品牌仓库 - 提供品牌和遥控器配置的访问
 */
class BrandRepository(private val context: Context) {

    private val codeLoader = IRCodeLoader(context)

    /**
     * 获取所有品牌
     */
    fun getAllBrands(): List<Brand> = codeLoader.loadBrands()

    /**
     * 按设备类型获取品牌
     */
    fun getBrandsByType(deviceType: DeviceType): List<Brand> =
        codeLoader.getBrandsByType(deviceType)

    /**
     * 获取热门品牌
     */
    fun getHotBrands(deviceType: DeviceType = DeviceType.TV): List<Brand> =
        codeLoader.getHotBrands(deviceType)

    /**
     * 搜索品牌
     */
    fun searchBrands(query: String, deviceType: DeviceType? = null): List<Brand> =
        codeLoader.searchBrands(query, deviceType)

    /**
     * 获取品牌的协议配置列表
     */
    fun getProtocolsForBrand(brandId: String): List<ProtocolConfig> =
        codeLoader.getProtocolsForBrand(brandId)

    /**
     * 获取全部协议配置（用于校准扫描）
     */
    fun getAllProtocols(): List<ProtocolConfig> {
        return codeLoader.loadAllProtocols().flatMap { it.value }
    }
}
