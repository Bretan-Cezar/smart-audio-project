package com.bretancezar.samcontrolapp.service

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresPermission
import com.bretancezar.samcontrolapp.utils.SmartAmbienceMode
import kotlinx.serialization.json.Json
import java.util.UUID

class SmartAmbienceService(
    private val _applicationContext: Context
) {

    private var _bluetooth: BluetoothManager = _applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private var _address = "DC:A6:32:70:23:BB"
    private var _uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun getDevice(): BluetoothDevice? {
        return _bluetooth.getConnectedDevices(BluetoothProfile.GATT).find { it.address == _address }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun checkConnected(): Boolean {
        
        return getDevice() != null
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun sendPhraseList(phraseList: List<String>) {
        val device = getDevice()

        if (device == null)
            throw ServiceException()

        val sock = device.createRfcommSocketToServiceRecord(_uuid)

        sock.connect()

        val fd = sock.outputStream

        fd.write(Json.encodeToString(phraseList).toByteArray(Charsets.UTF_8))

        sock.close()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun getPhraseList(phraseList: List<String>): List<String> {
        val device = getDevice()

        if (device == null)
            throw ServiceException()

        val sock = device.createRfcommSocketToServiceRecord(_uuid)

        sock.connect()

        val fd = sock.inputStream

        val phraseList: List<String> = Json.decodeFromString(fd.readBytes().toString())

        sock.close()

        return phraseList
    }

    fun setMode(smartAmbienceMode: SmartAmbienceMode) {

    }

    fun getMode(): SmartAmbienceMode {
        return SmartAmbienceMode.MODERATE
    }
}