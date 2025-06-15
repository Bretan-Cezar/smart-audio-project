package com.bretancezar.samcontrolapp.service

enum class ServiceExceptionCauses(val displayMsg: String) {
    CLIENT_BLUETOOTH_ERROR("Could not connect to device due to lack of Bluetooth permissions or Bluetooth is turned off."),
    CONNECTION_ERROR("Could not connect to device due to it being offline, out of range or not being paired."),
    DEVICE_SETTINGS_FETCH_ERROR("Internal error: could not retrieve current device settings"),
    UNKNOWN_ERROR("Internal error: invalid application state")
}