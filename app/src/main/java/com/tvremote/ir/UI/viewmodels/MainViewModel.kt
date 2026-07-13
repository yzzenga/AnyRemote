package com.tvremote.ir.UI.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tvremote.ir.Data.BrandRepository
import com.tvremote.ir.Data.SavedRemoteManager
import com.tvremote.ir.Model.Brand
import com.tvremote.ir.Model.DeviceType
import com.tvremote.ir.Model.SavedRemote
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * 主界面 ViewModel - 管理品牌列表、搜索和已保存遥控器
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BrandRepository(application)
    private val savedRemoteManager = SavedRemoteManager(application)

    // 当前选中的设备类型
    private val _currentDeviceType = MutableStateFlow(DeviceType.TV)
    val currentDeviceType: StateFlow<DeviceType> = _currentDeviceType

    // 品牌列表
    private val _brands = MutableStateFlow<List<Brand>>(emptyList())
    val brands: StateFlow<List<Brand>> = _brands

    private val _hotBrands = MutableStateFlow<List<Brand>>(emptyList())
    val hotBrands: StateFlow<List<Brand>> = _hotBrands

    // 搜索
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _searchResults = MutableStateFlow<List<Brand>>(emptyList())
    val searchResults: StateFlow<List<Brand>> = _searchResults

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching

    // 已保存遥控器
    private val _savedRemotes = MutableStateFlow<List<SavedRemote>>(emptyList())
    val savedRemotes: StateFlow<List<SavedRemote>> = _savedRemotes

    init {
        loadBrands()
        loadSavedRemotes()
    }

    private fun loadBrands() {
        viewModelScope.launch {
            _brands.value = repository.getAllBrands()
            _hotBrands.value = repository.getHotBrands(_currentDeviceType.value)
        }
    }

    /**
     * 切换设备类型
     */
    fun switchDeviceType(type: DeviceType) {
        _currentDeviceType.value = type
        _hotBrands.value = repository.getHotBrands(type)
        if (_searchQuery.value.isNotBlank()) {
            search(_searchQuery.value)
        }
        loadSavedRemotes()
    }

    /**
     * 搜索品牌（按当前设备类型过滤）
     */
    fun search(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _isSearching.value = false
            _searchResults.value = emptyList()
            return
        }
        _isSearching.value = true
        viewModelScope.launch {
            _searchResults.value = repository.searchBrands(query, _currentDeviceType.value)
        }
    }

    fun clearSearch() {
        _searchQuery.value = ""
        _isSearching.value = false
        _searchResults.value = emptyList()
    }

    /**
     * 加载已保存遥控器
     */
    fun loadSavedRemotes() {
        viewModelScope.launch {
            _savedRemotes.value = savedRemoteManager.getByType(_currentDeviceType.value)
        }
    }

    /**
     * 删除已保存遥控器
     */
    fun deleteSavedRemote(id: String) {
        savedRemoteManager.delete(id)
        loadSavedRemotes()
    }
}
