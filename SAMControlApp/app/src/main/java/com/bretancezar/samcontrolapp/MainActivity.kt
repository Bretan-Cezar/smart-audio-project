package com.bretancezar.samcontrolapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import com.bretancezar.samcontrolapp.navigation.AppNavigation
import com.bretancezar.samcontrolapp.ui.theme.SAMControlAppTheme
import com.bretancezar.samcontrolapp.utils.checkPermissions
import com.bretancezar.samcontrolapp.utils.getRequiredPermissions
import kotlin.system.exitProcess

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!checkPermissions(this)) {
            ActivityCompat.requestPermissions(this, getRequiredPermissions().toTypedArray(), 0)
        }


        while (!checkPermissions(this)) {}

        try {
            enableEdgeToEdge()

            setContent {
                SAMControlAppTheme {
                    MainApplication()
                }
            }
        }
        catch (se: SecurityException) {
            exitProcess(1)
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun MainApplication() {

    AppNavigation(LocalContext.current)
}