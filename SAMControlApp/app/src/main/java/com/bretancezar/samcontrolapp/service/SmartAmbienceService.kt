package com.bretancezar.samcontrolapp.service

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import androidx.annotation.RequiresPermission
import com.bretancezar.samcontrolapp.utils.SmartAmbienceMode
import kotlinx.serialization.json.Json

class SmartAmbienceService(
    private val _applicationContext: Context
) {
    private var _bluetooth: BluetoothManager = _applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private var _targetAddress = "DC:A6:32:70:23:BB"
    private var _socket: BluetoothSocket? = null
    private var _device: BluetoothDevice? = null
    private var _channelNo: Int = 1
    private var phraseList: List<String>
    private var micInputGainStateEnabled: Float
    private var smartAmbienceMode: SmartAmbienceMode

    init {
        _device = getDevice()

        connectDevice()

        val currentSettings = fetchCurrentSettings()

        if (currentSettings == null)
            throw ServiceException()

        phraseList = currentSettings.phraseList
        micInputGainStateEnabled = currentSettings.micInputGainStateEnabled
        smartAmbienceMode = SmartAmbienceMode.entries.find { it.internalName == currentSettings.smartAmbienceMode }!!
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

        if (_socket == null) {
            throw ServiceException()
        }

        _socket!!.connect()

        while (!_socket!!.isConnected) {}
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun fetchCurrentSettings(): ModifiableSettingsDTO? {
        val fd = _socket?.inputStream

        var data = ByteArray(4096)
        fd?.read(data)

        // At least the chars {}": should be present
        if (data.toSet().size >= 4) {
            data = data.copyOfRange(0, data.indexOfFirst { it == 0.toByte() })

            return Json.decodeFromString<ModifiableSettingsDTO>(data.toString(Charsets.UTF_8))
        }

        return null
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun setPhraseList(phraseList: List<String>) {
        val fd = _socket?.outputStream

        val newSettings = ModifiableSettingsDTO(smartAmbienceMode.internalName, phraseList, micInputGainStateEnabled)

        val data = Json.encodeToString(newSettings).toByteArray(Charsets.UTF_8).copyOf(4096)

        fd?.write(data)

        this.phraseList = phraseList
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun setMode(smartAmbienceMode: SmartAmbienceMode) {
        val fd = _socket?.outputStream

        val newSettings = ModifiableSettingsDTO(smartAmbienceMode.internalName, phraseList, micInputGainStateEnabled)

        val data = Json.encodeToString(newSettings).toByteArray(Charsets.UTF_8).copyOf(4096)

        fd?.write(data)

        this.smartAmbienceMode = smartAmbienceMode
    }

    fun getCurrentPhraseList(): List<String> {
        return phraseList
    }

    fun getCurrentMode(): SmartAmbienceMode {
        return smartAmbienceMode
    }

    fun getCurrentMicGain(): Float {
        return micInputGainStateEnabled
    }
}