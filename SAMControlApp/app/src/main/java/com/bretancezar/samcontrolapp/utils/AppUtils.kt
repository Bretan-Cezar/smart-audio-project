package com.bretancezar.samcontrolapp.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ComponentActivity
import androidx.core.content.ContextCompat


fun getRequiredPermissions(): List<String> {

    // TODO: Platform-wise permissions

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        return listOf(
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN
        )
    else return listOf(
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN
    )
}

fun checkPermissions(@SuppressLint("RestrictedApi") activity: ComponentActivity): Boolean {

    for (permission in getRequiredPermissions()) {

        if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
            return false
        }
    }

    return true
}
