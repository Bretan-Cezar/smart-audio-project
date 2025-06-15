package com.bretancezar.samcontrolapp.service

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import androidx.annotation.RequiresPermission
import com.bretancezar.samcontrolapp.utils.SmartAmbienceMode
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.IOException
import kotlin.concurrent.thread

class SmartAmbienceService(
    private val _applicationContext: Context
) {
    private var _bluetooth: BluetoothManager = _applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private var _targetAddress = "DC:A6:32:70:23:BB"
    private var _channelNo: Int = 1
    private var _socket: BluetoothSocket? = null
    private var _device: BluetoothDevice? = null
    private var deviceSettings: DeviceSettings? = null

    fun isConnected(): Boolean {
        /*
        BluetoothSocket.isConnected() is not reliable

        TODO try this https://stackoverflow.com/questions/43803884/bluetoothsocket-isconnected-issue
             or write a method that sends some test data and catches `IOException: Broken pipe` on send failure
        */
        return _device != null && _socket != null && _socket!!.isConnected && deviceSettings != null
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun init() {
        val device = getDevice()
        _device = device

        val socket = connectDevice()
        _socket = socket

        val currentSettings = fetchCurrentSettings()
        deviceSettings = DeviceSettings(
            SmartAmbienceMode.entries.find { it.internalName == currentSettings.smartAmbienceMode }!!,
            currentSettings.phraseList,
            currentSettings.micInputGainStateEnabled
        )
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun getDevice(): BluetoothDevice {
        val device = _bluetooth.adapter.getRemoteDevice(_targetAddress)

        if (device == null) {
            throw ServiceException(ServiceExceptionCauses.CONNECTION_ERROR, cause = null)
        }

        return device
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun connectDevice(): BluetoothSocket {
        // Must only be called if the device object is retrieved and set beforehand
        val deviceClass = _device!!.javaClass

        val createSocket = deviceClass.getMethod("createRfcommSocket", Int::class.java)

        var socket: BluetoothSocket

        try {
            socket = (createSocket.invoke(_device, _channelNo)) as BluetoothSocket
        }
        catch (ioe: IOException) {
            throw ServiceException(ServiceExceptionCauses.CLIENT_BLUETOOTH_ERROR, cause = ioe)
        }

        try {
            socket.connect()
        }
        catch (bse: Exception) {
            throw ServiceException(ServiceExceptionCauses.CONNECTION_ERROR, cause = bse)
        }

        return socket
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun fetchCurrentSettings(): ModifiableSettingsDTO {
        val fd = _socket?.inputStream

        var data = ByteArray(4096)
        fd?.read(data)

        data = data.copyOfRange(0, data.indexOfFirst { it == 0.toByte() })

        val settings: ModifiableSettingsDTO

        try {
            settings = Json.decodeFromString<ModifiableSettingsDTO>(data.toString(Charsets.UTF_8))
            return settings
        }
        catch (se: SerializationException) {
            throw ServiceException(ServiceExceptionCauses.DEVICE_SETTINGS_FETCH_ERROR, cause = se)
        }
        catch (se: IllegalArgumentException) {
            throw ServiceException(ServiceExceptionCauses.DEVICE_SETTINGS_FETCH_ERROR, cause = se)
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun setPhraseList(phraseList: List<String>) {
        val fd = _socket?.outputStream

        if (!isConnected()) {
            throw ServiceException(ServiceExceptionCauses.CONNECTION_ERROR, cause = null)
        }

        val newSettings = ModifiableSettingsDTO(deviceSettings!!.smartAmbienceMode.internalName, phraseList, deviceSettings!!.micInputGainStateEnabled)

        val data = Json.encodeToString(newSettings).toByteArray(Charsets.UTF_8).copyOf(4096)

        fd?.write(data)

        deviceSettings!!.phraseList = phraseList
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun setMode(smartAmbienceMode: SmartAmbienceMode) {
        val fd = _socket?.outputStream

        if (!isConnected()) {
            throw ServiceException(ServiceExceptionCauses.CONNECTION_ERROR, cause = null)
        }

        val newSettings = ModifiableSettingsDTO(smartAmbienceMode.internalName, deviceSettings!!.phraseList, deviceSettings!!.micInputGainStateEnabled)

        val data = Json.encodeToString(newSettings).toByteArray(Charsets.UTF_8).copyOf(4096)

        fd?.write(data)

        deviceSettings!!.smartAmbienceMode = smartAmbienceMode
    }

    fun getCurrentPhraseList(): List<String> {
        if (!isConnected()) {
            throw ServiceException(ServiceExceptionCauses.UNKNOWN_ERROR, cause = null)
        }

        return deviceSettings!!.phraseList
    }

    fun getCurrentMode(): SmartAmbienceMode {
        if (!isConnected()) {
            throw ServiceException(ServiceExceptionCauses.UNKNOWN_ERROR, cause = null)
        }

        return deviceSettings!!.smartAmbienceMode
    }

    fun getCurrentMicGain(): Float {
        if (!isConnected()) {
            throw ServiceException(ServiceExceptionCauses.UNKNOWN_ERROR, cause = null)
        }

        return deviceSettings!!.micInputGainStateEnabled
    }
}