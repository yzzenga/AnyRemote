package com.tvremote.ir.UI.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tvremote.ir.Data.BrandRepository
import com.tvremote.ir.Data.SavedRemoteManager
import com.tvremote.ir.IR.FrequencyScanner
import com.tvremote.ir.IR.IRBlaster
import com.tvremote.ir.Model.Brand
import com.tvremote.ir.Model.DeviceType
import com.tvremote.ir.Model.ProtocolConfig
import com.tvremote.ir.Model.SavedRemote
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 遥控器 ViewModel - 管理 IR 发射、校准扫描、保存遥控器
 */
class RemoteViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    val irBlaster = IRBlaster(context)
    private val scanner = FrequencyScanner(irBlaster)
    private val repository = BrandRepository(context)
    private val savedRemoteManager = SavedRemoteManager(context)

    // IR 状态
    private val _irAvailable = MutableStateFlow(false)
    val irAvailable: StateFlow<Boolean> = _irAvailable

    private val _irStatusMessage = MutableStateFlow("")
    val irStatusMessage: StateFlow<String> = _irStatusMessage

    // 品牌/协议
    private val _currentBrand = MutableStateFlow<Brand?>(null)
    val currentBrand: StateFlow<Brand?> = _currentBrand

    private val _protocols = MutableStateFlow<List<ProtocolConfig>>(emptyList())
    val protocols: StateFlow<List<ProtocolConfig>> = _protocols

    private val _currentProtocol = MutableStateFlow<ProtocolConfig?>(null)
    val currentProtocol: StateFlow<ProtocolConfig?> = _currentProtocol

    // 校准扫描状态
    private val _scanning = MutableStateFlow(false)
    val scanning: StateFlow<Boolean> = _scanning

    private val _scanProgress = MutableStateFlow(FrequencyScanner.ScanProgress(0, 0f, ""))
    val scanProgress: StateFlow<FrequencyScanner.ScanProgress> = _scanProgress

    private val _calibrated = MutableStateFlow(false)
    val calibrated: StateFlow<Boolean> = _calibrated

    // 频率手动调节
    private val _manualFrequency = MutableStateFlow(38000)
    val manualFrequency: StateFlow<Int> = _manualFrequency

    // 当前设备类型
    private val _currentDeviceType = MutableStateFlow(DeviceType.TV)
    val currentDeviceType: StateFlow<DeviceType> = _currentDeviceType

    init {
        checkIR()
    }

    private fun checkIR() {
        _irAvailable.value = irBlaster.isAvailable
        _irStatusMessage.value = if (irBlaster.isAvailable) {
            "红外发射器就绪"
        } else {
            "此设备不支持红外发射功能"
        }
    }

    /**
     * 加载品牌的协议配置（用于校准/新设备）
     */
    fun loadBrand(brandId: String) {
        viewModelScope.launch {
            val brands = repository.getAllBrands()
            val brand = brands.find { it.id == brandId }
            _currentBrand.value = brand
            _currentDeviceType.value = brand?.deviceType ?: DeviceType.TV
            _protocols.value = repository.getProtocolsForBrand(brandId)
        }
    }

    /**
     * 从已保存的遥控器加载（跳过校准）
     */
    fun loadFromSaved(savedRemote: SavedRemote) {
        viewModelScope.launch {
            val brands = repository.getAllBrands()
            val brand = brands.find { it.id == savedRemote.brandId }
            _currentBrand.value = brand
            _currentDeviceType.value = savedRemote.deviceType

            val protos = repository.getProtocolsForBrand(savedRemote.brandId)
            _protocols.value = protos

            val proto = protos.find { it.id == savedRemote.protocolId }
            if (proto != null) {
                _currentProtocol.value = proto
                _manualFrequency.value = savedRemote.frequency
                _calibrated.value = true  // 已保存即视为已校准
            }
        }
    }

    /**
     * 选择协议
     */
    fun selectProtocol(protocolConfig: ProtocolConfig) {
        _currentProtocol.value = protocolConfig
        _manualFrequency.value = protocolConfig.frequency
    }

    /**
     * 发送按键
     */
    fun sendKey(keyName: String) {
        val protocol = _currentProtocol.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                irBlaster.sendKey(protocol, keyName)
            } catch (e: Exception) {
                _irStatusMessage.value = "发送失败: ${e.message}"
            }
        }
    }

    /**
     * 在指定频率发送电源键（手动测试）
     */
    fun sendPowerAtFrequency(frequency: Int) {
        val protocol = _currentProtocol.value
        if (protocol != null) {
            val keys = protocol.keys
            val powerKey = keys["power"] ?: return
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val pattern = com.tvremote.ir.IR.IRProtocols.generateRawPattern(
                        protocol = protocol.protocol,
                        address = protocol.deviceCode,
                        command = powerKey.code,
                        bits = powerKey.bits
                    )
                    irBlaster.transmit(frequency, pattern)
                } catch (e: Exception) {
                    _irStatusMessage.value = "发送失败: ${e.message}"
                }
            }
        } else {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val pattern = com.tvremote.ir.IR.IRProtocols.generateRawPattern("NEC", 0, 0x45)
                    irBlaster.transmit(frequency, pattern)
                } catch (e: Exception) {
                    _irStatusMessage.value = "发送失败: ${e.message}"
                }
            }
        }
    }

    /**
     * 开始自动校准扫描
     */
    fun startAutoScan() {
        if (_scanning.value) return
        _scanning.value = true
        _calibrated.value = false

        viewModelScope.launch {
            val allProtocols = repository.getAllProtocols()
            scanner.autoCalibrate(allProtocols).collect { progress ->
                _scanProgress.value = progress
            }
            _scanning.value = false
        }
    }

    /**
     * 开始频率扫描
     */
    fun startFrequencyScan() {
        if (_scanning.value) return
        _scanning.value = true
        _calibrated.value = false

        viewModelScope.launch {
            scanner.scanFrequencies().collect { progress ->
                _scanProgress.value = progress
            }
            _scanning.value = false
        }
    }

    /**
     * 频率调节
     */
    fun adjustFrequency(delta: Int) {
        val newFreq = (_manualFrequency.value + delta).coerceIn(30000, 60000)
        _manualFrequency.value = newFreq
    }

    fun setManualFrequency(freq: Int) {
        _manualFrequency.value = freq.coerceIn(30000, 60000)
    }

    /**
     * 循环切换到下一常用频率
     */
    fun cycleFrequency() {
        val commonFrequencies = listOf(36000, 37000, 38000, 40000, 42000, 45000, 48000, 50000, 56000)
        val current = _manualFrequency.value
        val next = commonFrequencies.indexOfFirst { it > current / 1000 * 1000 }
            ?.let { commonFrequencies.getOrNull(it) } ?: commonFrequencies.first()
        _manualFrequency.value = next
    }

    /**
     * 确认校准完成
     */
    fun confirmCalibration() {
        _calibrated.value = true
        val proto = _currentProtocol.value
        if (proto != null) {
            _manualFrequency.value = proto.frequency
        }
    }

    /**
     * 保存当前遥控器
     */
    fun saveCurrentRemote(name: String): SavedRemote? {
        val brand = _currentBrand.value ?: return null
        val proto = _currentProtocol.value ?: return null
        return savedRemoteManager.save(
            name = name,
            deviceType = _currentDeviceType.value,
            brandId = brand.id,
            brandName = brand.name,
            protocolId = proto.id,
            frequency = proto.frequency
        )
    }
}
