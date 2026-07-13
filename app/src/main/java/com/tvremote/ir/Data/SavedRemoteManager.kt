package com.tvremote.ir.Data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tvremote.ir.Model.DeviceType
import com.tvremote.ir.Model.SavedRemote
import java.io.File
import java.util.UUID

/**
 * 已保存遥控器管理器 - JSON文件持久化
 */
class SavedRemoteManager(private val context: Context) {

    private val gson = Gson()
    private val file: File get() = File(context.filesDir, "saved_remotes.json")

    /**
     * 获取所有已保存的遥控器
     */
    fun getAll(): List<SavedRemote> {
        return try {
            if (!file.exists()) return emptyList()
            val json = file.readText()
            val type = object : TypeToken<List<SavedRemote>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * 按设备类型获取
     */
    fun getByType(deviceType: DeviceType): List<SavedRemote> {
        return getAll().filter { it.deviceType == deviceType }
    }

    /**
     * 保存一个遥控器
     */
    fun save(name: String, deviceType: DeviceType, brandId: String, brandName: String,
             protocolId: String, frequency: Int): SavedRemote {
        val remotes = getAll().toMutableList()
        val saved = SavedRemote(
            id = UUID.randomUUID().toString().take(8),
            name = name,
            deviceType = deviceType,
            brandId = brandId,
            brandName = brandName,
            protocolId = protocolId,
            frequency = frequency
        )
        remotes.add(saved)
        writeAll(remotes)
        return saved
    }

    /**
     * 删除一个已保存的遥控器
     */
    fun delete(id: String) {
        val remotes = getAll().toMutableList()
        remotes.removeAll { it.id == id }
        writeAll(remotes)
    }

    /**
     * 更新遥控器
     */
    fun update(remote: SavedRemote) {
        val remotes = getAll().toMutableList()
        val index = remotes.indexOfFirst { it.id == remote.id }
        if (index >= 0) {
            remotes[index] = remote
            writeAll(remotes)
        }
    }

    private fun writeAll(remotes: List<SavedRemote>) {
        try {
            file.writeText(gson.toJson(remotes))
        } catch (e: Exception) {
            // 静默失败
        }
    }
}
