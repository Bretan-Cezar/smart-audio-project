package com.bretancezar.samcontrolapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.bretancezar.samcontrolapp.navigation.AppNavigation
import com.bretancezar.samcontrolapp.ui.theme.SAMControlAppTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SAMControlAppTheme {
                MainApplication()
            }
        }
    }
}

@Composable
fun MainApplication() {

    AppNavigation(LocalContext.current)
}