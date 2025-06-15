package com.bretancezar.samcontrolapp.navigation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.size
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
import com.bretancezar.samcontrolapp.service.ServiceException
import com.bretancezar.samcontrolapp.service.SmartAmbienceService
import com.bretancezar.samcontrolapp.ui.screen.DeviceScreen
import com.bretancezar.samcontrolapp.ui.screen.FirstScreen
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

    val smartAmbienceService = SmartAmbienceService(applicationContext)

    try {
        smartAmbienceService.init()
    }
    catch (_: ServiceException) {}

    val startDestination = if (smartAmbienceService.isConnected()) Screens.SMART_AMBIENCE else Screens.FIRST_SCREEN

    val navigationViewModel = viewModel {  NavigationViewModel(navController, smartAmbienceService) }

    val smartAmbienceViewModel = viewModel { SmartAmbienceViewModel(smartAmbienceService) }

    Scaffold(
        bottomBar = { if (smartAmbienceService.isConnected()) BottomNavBar(navigationViewModel) },
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
          
            composable (
                route = Screens.FIRST_SCREEN.routeName
            ) {
                FirstScreen(navigationViewModel)
            }
            
            composable(
                route = Screens.SMART_AMBIENCE.routeName
            ) {
                SmartAmbienceScreen(smartAmbienceViewModel, navigationViewModel)
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