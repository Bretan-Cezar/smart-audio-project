package com.bretancezar.samcontrolapp.navigation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.size
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bretancezar.samcontrolapp.service.SmartAmbienceService
import com.bretancezar.samcontrolapp.ui.screen.DeviceScreen
import com.bretancezar.samcontrolapp.ui.screen.SmartAmbienceScreen
import com.bretancezar.samcontrolapp.ui.screen.SoundScreen
import com.bretancezar.samcontrolapp.utils.Screens
import com.bretancezar.samcontrolapp.viewmodel.NavigationViewModel
import com.bretancezar.samcontrolapp.viewmodel.SmartAmbienceViewModel

@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AppNavigation(
    applicationContext: Context
) {
    val navController = rememberNavController()
    val startDestination = Screens.SMART_AMBIENCE
    val navigationViewModel = viewModel {  NavigationViewModel(startDestination, navController) }
    val smartAmbienceService = SmartAmbienceService(applicationContext)
    val smartAmbienceViewModel = viewModel { SmartAmbienceViewModel(smartAmbienceService) }

    Log.i("BT", smartAmbienceService.checkConnected().toString())

    Scaffold(
        bottomBar = { BottomNavBar(navigationViewModel) },
        topBar = {
            TopAppBar(
                title = { Text("SAM Control App") }
            )
        }
    ) {

        NavHost(
            navController = navController,
            startDestination = startDestination.routeName
        ) {

            composable(
                route = Screens.SMART_AMBIENCE.routeName
            ) {
                SmartAmbienceScreen(smartAmbienceViewModel)
            }

            composable(
                route = Screens.SOUND.routeName
            ) {
                SoundScreen()
            }

            composable(
                route = Screens.DEVICE.routeName
            ) {
                DeviceScreen()
            }
        }

    }
}

@Composable
fun BottomNavBar(viewModel: NavigationViewModel) {

    val screenList: List<Screens> = viewModel.screenList
    val selectedScreen by viewModel.currentScreen.collectAsState()

    NavigationBar(
        windowInsets = NavigationBarDefaults.windowInsets
    ) {
        screenList.forEachIndexed { _, screen ->
            NavigationBarItem(
                icon = {
                    Icon(painter = painterResource(screen.icon), contentDescription = null, modifier = Modifier.size(24.dp), tint = Color.White)
                },
                label = { Text(screen.displayName) },
                selected = selectedScreen == screen,
                onClick = { viewModel.setCurrentScreen(screen) }
            )
        }
    }
}