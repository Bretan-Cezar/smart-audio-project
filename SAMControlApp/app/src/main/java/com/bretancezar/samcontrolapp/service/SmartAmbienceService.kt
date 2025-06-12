package com.bretancezar.samcontrolapp.service

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import androidx.annotation.RequiresPermission
import com.bretancezar.samcontrolapp.utils.SmartAmbienceMode
import kotlinx.serialization.json.Json
import java.util.UUID

class SmartAmbienceService(
    private val _applicationContext: Context
) {
    private var _bluetooth: BluetoothManager = _applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private var _targetAddress = "DC:A6:32:70:23:BB"
    private var _uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private var _socket: BluetoothSocket? = null
    private var _device: BluetoothDevice? = null
    private var _channelNo: Int = 1
    private var _phraseList: List<String>
    private var _micInputGainStateEnabled: Float
    private var _smartAmbienceMode: SmartAmbienceMode

    init {
        _device = getDevice()
        connectDevice()
        val currentSettings = getCurrentSettings()

        if (currentSettings == null)
            throw ServiceException()

        _phraseList = currentSettings.phraseList
        _micInputGainStateEnabled = currentSettings.micInputGainStateEnabled
        _smartAmbienceMode = SmartAmbienceMode.entries.find { it.internalName == currentSettings.smartAmbienceMode }!!
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun getDevice(): BluetoothDevice? {
        return _bluetooth.adapter.getRemoteDevice(_targetAddress)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun connectDevice() {
        if (_device == null)
            throw ServiceException()

        val deviceClass = _device?.javaClass

        if (deviceClass == null)
            throw ServiceException()

        val createSocket = deviceClass.getMethod("createRfcommSocket", Int::class.java)

        _socket = (createSocket.invoke(_device, _channelNo)) as BluetoothSocket

        _socket?.connect()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun getCurrentSettings(): ModifiableSettingsDTO? {
        val fd = _socket?.inputStream

        val data: ByteArray? = fd?.readBytes()

        if (data != null) {

            return Json.decodeFromString<ModifiableSettingsDTO>(data.toString())
        }

        return null
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun setPhraseList(phraseList: List<String>) {
        val fd = _socket?.outputStream

        val newSettings = ModifiableSettingsDTO(_smartAmbienceMode.internalName, phraseList, _micInputGainStateEnabled)

        val data = Json.encodeToString(newSettings).toByteArray(Charsets.UTF_8)

        fd?.write(data)

        _phraseList = phraseList
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun setMode(mode: SmartAmbienceMode) {
        val fd = _socket?.outputStream

        val newSettings = ModifiableSettingsDTO(mode.internalName, _phraseList, _micInputGainStateEnabled)

        val data = Json.encodeToString(newSettings).toByteArray(Charsets.UTF_8)

        fd?.write(data)

        _smartAmbienceMode = mode
    }
}